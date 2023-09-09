/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilder.BuiltBuffer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.ChunkAreaSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.BlockVertexCompiler;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.ChunkSearcher;
import net.wurstclient.util.ChunkUtils;
import net.wurstclient.util.MinPriorityThreadFactory;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags({"cave finder"})
public final class CaveFinderHack extends Hack
	implements UpdateListener, PacketInputListener, RenderListener
{
	private final ChunkAreaSetting area = new ChunkAreaSetting("区域", 
		"将会以玩家附近的区域寻找.\n越高的数值需要越好的电脑.");
	
	private final SliderSetting limit = new SliderSetting("限制", 
		"最大方块所显示的限制.\n越高数值需要越好的电脑.",
		5, 3, 6, 1, ValueDisplay.LOGARITHMIC);
	
	private final ColorSetting color = new ColorSetting("颜色", 
		"洞穴将会被高亮以\n这种颜色.", Color.RED);
	
	private final SliderSetting opacity = new SliderSetting("不透明度", 
		"高亮需要被设置透明度的数值.\n" + "0 = breathing animation", 0,
		0, 1, 0.01, ValueDisplay.PERCENTAGE.withLabel(0, "breathing"));
	
	private int prevLimit;
	private boolean notify;
	
	private final HashMap<Chunk, ChunkSearcher> searchers = new HashMap<>();
	private final Set<ChunkPos> chunksToUpdate =
		Collections.synchronizedSet(new HashSet<>());
	private ExecutorService pool1;
	
	private ForkJoinPool pool2;
	private ForkJoinTask<HashSet<BlockPos>> getMatchingBlocksTask;
	private ForkJoinTask<ArrayList<int[]>> compileVerticesTask;
	
	private VertexBuffer vertexBuffer;
	private boolean bufferUpToDate;
	
	public CaveFinderHack()
	{
		super("洞穴寻找器");
		setCategory(Category.RENDER);
		addSetting(area);
		addSetting(limit);
		addSetting(color);
		addSetting(opacity);
	}
	
	@Override
	public void onEnable()
	{
		prevLimit = limit.getValueI();
		notify = true;
		
		pool1 = MinPriorityThreadFactory.newFixedThreadPool();
		pool2 = new ForkJoinPool();
		
		bufferUpToDate = false;
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(PacketInputListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(PacketInputListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		
		stopPool2Tasks();
		pool1.shutdownNow();
		pool2.shutdownNow();
		
		if(vertexBuffer != null)
			vertexBuffer.close();
		
		chunksToUpdate.clear();
	}
	
	@Override
	public void onReceivedPacket(PacketInputEvent event)
	{
		ChunkPos chunkPos = ChunkUtils.getAffectedChunk(event.getPacket());
		
		if(chunkPos != null)
			chunksToUpdate.add(chunkPos);
	}
	
	@Override
	public void onUpdate()
	{
		Block blockToFind = Blocks.CAVE_AIR;
		BlockPos eyesPos = BlockPos.ofFloored(RotationUtils.getEyesPos());
		int dimensionId = MC.world.getRegistryKey().toString().hashCode();
		
		addSearchersInRange(blockToFind, dimensionId);
		removeSearchersOutOfRange();
		replaceSearchersWithDifferences(blockToFind, dimensionId);
		replaceSearchersWithChunkUpdate(blockToFind, dimensionId);
		
		if(!areAllChunkSearchersDone())
			return;
		
		checkIfLimitChanged();
		
		if(getMatchingBlocksTask == null)
			startGetMatchingBlocksTask(eyesPos);
		
		if(!getMatchingBlocksTask.isDone())
			return;
		
		if(compileVerticesTask == null)
			startCompileVerticesTask();
		
		if(!compileVerticesTask.isDone())
			return;
		
		if(!bufferUpToDate)
			setBufferFromTask();
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		matrixStack.push();
		RenderUtils.applyRegionalRenderOffset(matrixStack);
		
		// generate rainbow color
		float x = System.currentTimeMillis() % 2000 / 1000F;
		float alpha = 0.25F + 0.25F * MathHelper.sin(x * (float)Math.PI);
		
		if(opacity.getValue() > 0)
			alpha = opacity.getValueF();
		
		float[] colorF = color.getColorF();
		RenderSystem.setShaderColor(colorF[0], colorF[1], colorF[2], alpha);
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		
		if(vertexBuffer != null)
		{
			Matrix4f viewMatrix = matrixStack.peek().getPositionMatrix();
			Matrix4f projMatrix = RenderSystem.getProjectionMatrix();
			ShaderProgram shader = RenderSystem.getShader();
			vertexBuffer.bind();
			vertexBuffer.draw(viewMatrix, projMatrix, shader);
			VertexBuffer.unbind();
		}
		
		matrixStack.pop();
		
		// GL resets
		RenderSystem.setShaderColor(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	private void addSearchersInRange(Block block, int dimensionId)
	{
		for(Chunk chunk : area.getSelected().getChunksInRange())
		{
			if(searchers.containsKey(chunk))
				continue;
			
			addSearcher(chunk, block, dimensionId);
		}
	}
	
	private void removeSearchersOutOfRange()
	{
		for(ChunkSearcher searcher : new ArrayList<>(searchers.values()))
		{
			ChunkPos searcherPos = searcher.getChunk().getPos();
			if(area.getSelected().isInRange(searcherPos))
				continue;
			
			removeSearcher(searcher);
		}
	}
	
	private void replaceSearchersWithDifferences(Block currentBlock,
		int dimensionId)
	{
		for(ChunkSearcher oldSearcher : new ArrayList<>(searchers.values()))
		{
			if(currentBlock.equals(oldSearcher.getBlock())
				&& dimensionId == oldSearcher.getDimensionId())
				continue;
			
			removeSearcher(oldSearcher);
			addSearcher(oldSearcher.getChunk(), currentBlock, dimensionId);
		}
	}
	
	private void replaceSearchersWithChunkUpdate(Block currentBlock,
		int dimensionId)
	{
		// get the chunks to update and remove them from the set
		ChunkPos[] chunks;
		synchronized(chunksToUpdate)
		{
			chunks = chunksToUpdate.toArray(ChunkPos[]::new);
			chunksToUpdate.clear();
		}
		
		// update the chunks separately so the synchronization
		// doesn't have to wait for that
		for(ChunkPos chunkPos : chunks)
		{
			Chunk chunk = MC.world.getChunk(chunkPos.x, chunkPos.z);
			
			ChunkSearcher oldSearcher = searchers.get(chunk);
			if(oldSearcher == null)
				continue;
			
			removeSearcher(oldSearcher);
			addSearcher(chunk, currentBlock, dimensionId);
		}
	}
	
	private void addSearcher(Chunk chunk, Block block, int dimensionId)
	{
		stopPool2Tasks();
		
		ChunkSearcher searcher = new ChunkSearcher(chunk, block, dimensionId);
		searchers.put(chunk, searcher);
		searcher.startSearching(pool1);
	}
	
	private void removeSearcher(ChunkSearcher searcher)
	{
		stopPool2Tasks();
		
		searchers.remove(searcher.getChunk());
		searcher.cancelSearching();
	}
	
	private void stopPool2Tasks()
	{
		if(getMatchingBlocksTask != null)
		{
			getMatchingBlocksTask.cancel(true);
			getMatchingBlocksTask = null;
		}
		
		if(compileVerticesTask != null)
		{
			compileVerticesTask.cancel(true);
			compileVerticesTask = null;
		}
		
		bufferUpToDate = false;
	}
	
	private boolean areAllChunkSearchersDone()
	{
		for(ChunkSearcher searcher : searchers.values())
			if(searcher.getStatus() != ChunkSearcher.Status.DONE)
				return false;
			
		return true;
	}
	
	private void checkIfLimitChanged()
	{
		if(limit.getValueI() != prevLimit)
		{
			stopPool2Tasks();
			notify = true;
			prevLimit = limit.getValueI();
		}
	}
	
	private void startGetMatchingBlocksTask(BlockPos eyesPos)
	{
		int maxBlocks = (int)Math.pow(10, limit.getValueI());
		
		Callable<HashSet<BlockPos>> task = () -> searchers.values()
			.parallelStream()
			.flatMap(searcher -> searcher.getMatchingBlocks().stream())
			.sorted(Comparator
				.comparingInt(pos -> eyesPos.getManhattanDistance(pos)))
			.limit(maxBlocks).collect(Collectors.toCollection(HashSet::new));
		
		getMatchingBlocksTask = pool2.submit(task);
	}
	
	private HashSet<BlockPos> getMatchingBlocksFromTask()
	{
		HashSet<BlockPos> matchingBlocks = getMatchingBlocksTask.join();
		int maxBlocks = (int)Math.pow(10, limit.getValueI());
		
		if(matchingBlocks.size() < maxBlocks)
			notify = true;
		else if(notify)
		{
			ChatUtils.warning("CaveFinder found \u00a7lA LOT\u00a7r of blocks!"
				+ " To prevent lag, it will only show the closest \u00a76"
				+ limit.getValueString() + "\u00a7r results.");
			notify = false;
		}
		
		return matchingBlocks;
	}
	
	private void startCompileVerticesTask()
	{
		HashSet<BlockPos> matchingBlocks = getMatchingBlocksFromTask();
		
		BlockPos camPos = RenderUtils.getCameraBlockPos();
		int regionX = (camPos.getX() >> 9) * 512;
		int regionZ = (camPos.getZ() >> 9) * 512;
		
		Callable<ArrayList<int[]>> task =
			() -> BlockVertexCompiler.compile(matchingBlocks, regionX, regionZ);
		
		compileVerticesTask = pool2.submit(task);
	}
	
	private void setBufferFromTask()
	{
		Tessellator tessellator = RenderSystem.renderThreadTesselator();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS,
			VertexFormats.POSITION);
		
		for(int[] vertex : compileVerticesTask.join())
			bufferBuilder.vertex(vertex[0], vertex[1], vertex[2]).next();
		
		BuiltBuffer buffer = bufferBuilder.end();
		
		if(vertexBuffer != null)
			vertexBuffer.close();
		
		vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		vertexBuffer.bind();
		vertexBuffer.upload(buffer);
		VertexBuffer.unbind();
		
		bufferUpToDate = true;
	}
}
