/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.awt.Color;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.render.BufferBuilder.BuiltBuffer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.wurstclient.Category;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.newchunks.NewChunksChunkRenderer;
import net.wurstclient.hacks.newchunks.NewChunksReasonsRenderer;
import net.wurstclient.hacks.newchunks.NewChunksRenderer;
import net.wurstclient.hacks.newchunks.NewChunksShowSetting;
import net.wurstclient.hacks.newchunks.NewChunksStyleSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RenderUtils;

public final class NewChunksHack extends Hack
	implements UpdateListener, RenderListener
{
	private final NewChunksStyleSetting style = new NewChunksStyleSetting();
	
	private final NewChunksShowSetting show = new NewChunksShowSetting();
	
	private final CheckboxSetting showReasons = new CheckboxSetting(
		"显示原因",
		"突出显示导致每个区块被标记为新/旧的块.",
		false);
	
	private final CheckboxSetting showCounter =
		new CheckboxSetting("展示柜台",
			"显示目前为止找到的新/旧块的数量.", false);
	
	private final SliderSetting altitude =
		new SliderSetting("高度", 0, -64, 320, 1, ValueDisplay.INTEGER);
	
	private final SliderSetting drawDistance =
		new SliderSetting("绘制距离", 32, 8, 64, 1, ValueDisplay.INTEGER);
	
	private final SliderSetting opacity = new SliderSetting("不透明", 0.75,
		0.1, 1, 0.01, ValueDisplay.PERCENTAGE);
	
	private final ColorSetting newChunksColor =
		new ColorSetting("新块颜色", Color.RED);
	
	private final ColorSetting oldChunksColor =
		new ColorSetting("旧块颜色", Color.BLUE);
	
	private final CheckboxSetting logChunks = new CheckboxSetting("日志块",
		"找到新/旧区块时写入日志文件.", false);
	
	private final Set<ChunkPos> newChunks =
		Collections.synchronizedSet(new HashSet<>());
	private final Set<ChunkPos> oldChunks =
		Collections.synchronizedSet(new HashSet<>());
	private final Set<ChunkPos> dontCheckAgain =
		Collections.synchronizedSet(new HashSet<>());
	
	private final Set<BlockPos> newChunkReasons =
		Collections.synchronizedSet(new HashSet<>());
	private final Set<BlockPos> oldChunkReasons =
		Collections.synchronizedSet(new HashSet<>());
	
	private final NewChunksRenderer renderer = new NewChunksRenderer(altitude,
		opacity, newChunksColor, oldChunksColor);
	private final NewChunksReasonsRenderer reasonsRenderer =
		new NewChunksReasonsRenderer(drawDistance);
	
	private ChunkPos lastRegion;
	
	public NewChunksHack()
	{
		super("新组块");
		setCategory(Category.RENDER);
		addSetting(style);
		addSetting(show);
		addSetting(showReasons);
		addSetting(showCounter);
		addSetting(altitude);
		addSetting(drawDistance);
		addSetting(opacity);
		addSetting(newChunksColor);
		addSetting(oldChunksColor);
		addSetting(logChunks);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
		oldChunks.clear();
		newChunks.clear();
		dontCheckAgain.clear();
		oldChunkReasons.clear();
		newChunkReasons.clear();
		lastRegion = null;
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		renderer.closeBuffers();
	}
	
	@Override
	public String getRenderName()
	{
		if(!showCounter.isChecked())
			return getName();
		
		return String.format("%s [%d/%d]", getName(), newChunks.size(),
			oldChunks.size());
	}
	
	@Override
	public void onUpdate()
	{
		renderer.closeBuffers();
		
		NewChunksChunkRenderer chunkRenderer =
			style.getSelected().getChunkRenderer();
		
		if(show.getSelected().includesNew())
		{
			BuiltBuffer newChunksBuffer =
				chunkRenderer.buildBuffer(newChunks, drawDistance.getValueI());
			renderer.updateBuffer(0, newChunksBuffer);
			
			if(showReasons.isChecked())
			{
				BuiltBuffer newReasonsBuffer =
					reasonsRenderer.buildBuffer(newChunkReasons);
				renderer.updateBuffer(1, newReasonsBuffer);
			}
		}
		
		if(show.getSelected().includesOld())
		{
			BuiltBuffer oldChunksBuffer =
				chunkRenderer.buildBuffer(oldChunks, drawDistance.getValueI());
			renderer.updateBuffer(2, oldChunksBuffer);
			
			if(showReasons.isChecked())
			{
				BuiltBuffer oldReasonsBuffer =
					reasonsRenderer.buildBuffer(oldChunkReasons);
				renderer.updateBuffer(3, oldReasonsBuffer);
			}
		}
	}
	
	public void afterLoadChunk(int x, int z)
	{
		if(!isEnabled())
			return;
		
		WorldChunk chunk = MC.world.getChunk(x, z);
		new Thread(() -> checkLoadedChunk(chunk), "NewChunks " + chunk.getPos())
			.start();
	}
	
	private void checkLoadedChunk(WorldChunk chunk)
	{
		ChunkPos chunkPos = chunk.getPos();
		if(newChunks.contains(chunkPos) || oldChunks.contains(chunkPos)
			|| dontCheckAgain.contains(chunkPos))
			return;
		
		int minX = chunkPos.getStartX();
		int minY = chunk.getBottomY();
		int minZ = chunkPos.getStartZ();
		int maxX = chunkPos.getEndX();
		int maxY = chunk.getHighestNonEmptySectionYOffset() + 16;
		int maxZ = chunkPos.getEndZ();
		
		for(int x = minX; x <= maxX; x++)
			for(int y = minY; y <= maxY; y++)
				for(int z = minZ; z <= maxZ; z++)
				{
					BlockPos pos = new BlockPos(x, y, z);
					FluidState fluidState = chunk.getFluidState(pos);
					
					if(fluidState.isEmpty() || fluidState.isStill())
						continue;
						
					// Liquid always generates still, the flowing happens later
					// through block updates. Therefore any chunk that contains
					// flowing liquids from the start should be an old chunk.
					oldChunks.add(chunkPos);
					oldChunkReasons.add(pos);
					if(logChunks.isChecked())
						System.out.println("旧块在 " + chunkPos);
					return;
				}
				
		// If the whole loop ran through without finding anything, make sure it
		// never runs again on that chunk, as that would be a huge waste of CPU
		// time.
		dontCheckAgain.add(chunkPos);
	}
	
	public void afterUpdateBlock(BlockPos pos)
	{
		if(!isEnabled())
			return;
		
		// Liquid starts flowing -> probably a new chunk
		FluidState fluidState = BlockUtils.getState(pos).getFluidState();
		if(fluidState.isEmpty() || fluidState.isStill())
			return;
		
		ChunkPos chunkPos = new ChunkPos(pos);
		if(newChunks.contains(chunkPos) || oldChunks.contains(chunkPos))
			return;
		
		newChunks.add(chunkPos);
		newChunkReasons.add(pos);
		if(logChunks.isChecked())
			System.out.println("新块位于 " + chunkPos);
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		BlockPos camPos = RenderUtils.getCameraBlockPos();
		int regionX = (camPos.getX() >> 9) * 512;
		int regionZ = (camPos.getZ() >> 9) * 512;
		ChunkPos region = new ChunkPos(regionX, regionZ);
		if(!region.equals(lastRegion))
		{
			onUpdate();
			lastRegion = region;
		}
		
		renderer.render(matrixStack, partialTicks);
	}
}
