/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.wurstclient.Category;
import net.wurstclient.events.LeftClickListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.BlockListSetting;
import net.wurstclient.settings.BlockSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

public final class NukerHack extends Hack
	implements UpdateListener, LeftClickListener, RenderListener
{
	private final SliderSetting range = new SliderSetting("范围", 5.0, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final EnumSetting<Mode> mode = new EnumSetting("Mode", "§l普通§r 模式很简单的破坏\n你周边的东西.\n§lID§r 模式只破坏所选的方块\n类型. 左键方块选择其方块.\n§l多个ID§r 模式只破坏那些你选择\n在你 多个ID 列表.\n§l平坦§r 模式只会挖你水平上的方块,\n但不会往下挖.\n§l粉碎§r 模式只会破坏那些\n能够瞬间破坏的方块 (例.如. 高大的草).", (Enum[])Mode.values(), (Enum)Mode.NORMAL);
    private final BlockSetting id = new BlockSetting("ID", "在ID模式,将会破坏指定ID的方块类型.\nair = 不会破坏任何东西", "minecraft:air", true);
    private final CheckboxSetting lockId = new CheckboxSetting("锁ID", "保护且不会导致因点击其他方块\n而改变挖掘的方块,同时也不会因重启而重置.", false);
	
	private final BlockListSetting multiIdList = new BlockListSetting(
		"MultiID List", "The types of blocks to break in MultiID mode.",
		"minecraft:ancient_debris", "minecraft:bone_block", "minecraft:clay",
		"minecraft:coal_ore", "minecraft:diamond_ore", "minecraft:emerald_ore",
		"minecraft:glowstone", "minecraft:gold_ore", "minecraft:iron_ore",
		"minecraft:lapis_ore", "minecraft:nether_gold_ore",
		"minecraft:nether_quartz_ore", "minecraft:redstone_ore");
	
	private final ArrayDeque<Set<BlockPos>> prevBlocks = new ArrayDeque<>();
	private BlockPos currentBlock;
	private float progress;
	private float prevProgress;
	
	public NukerHack()
	{
		super("矿井");
		setCategory(Category.BLOCKS);
		addSetting(range);
		addSetting(mode);
		addSetting(id);
		addSetting(lockId);
		addSetting(multiIdList);
	}
	
	@Override
	public String getRenderName()
	{
		return mode.getSelected().getRenderName(this);
	}
	
	@Override
	protected void onEnable()
	{
		WURST.getHax().autoMineHack.setEnabled(false);
		WURST.getHax().excavatorHack.setEnabled(false);
		WURST.getHax().nukerLegitHack.setEnabled(false);
		WURST.getHax().speedNukerHack.setEnabled(false);
		WURST.getHax().tunnellerHack.setEnabled(false);
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(LeftClickListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(LeftClickListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		
		if(currentBlock != null)
		{
			IMC.getInteractionManager().setBreakingBlock(true);
			MC.interactionManager.cancelBlockBreaking();
			currentBlock = null;
		}
		
		prevBlocks.clear();
		
		if(!lockId.isChecked())
			id.setBlock(Blocks.AIR);
	}
	
	@Override
	public void onUpdate()
	{
		// abort if using IDNuker without an ID being set
		if(mode.getSelected() == Mode.ID && id.getBlock() == Blocks.AIR)
			return;
		
		ClientPlayerEntity player = MC.player;
		
		currentBlock = null;
		Vec3d eyesPos = RotationUtils.getEyesPos().subtract(0.5, 0.5, 0.5);
		BlockPos eyesBlock = new BlockPos(RotationUtils.getEyesPos());
		double rangeSq = Math.pow(range.getValue(), 2);
		int blockRange = (int)Math.ceil(range.getValue());
		
		Vec3i rangeVec = new Vec3i(blockRange, blockRange, blockRange);
		BlockPos min = eyesBlock.subtract(rangeVec);
		BlockPos max = eyesBlock.add(rangeVec);
		
		ArrayList<BlockPos> blocks = BlockUtils.getAllInBox(min, max);
		Stream<BlockPos> stream = blocks.parallelStream();
		
		List<BlockPos> blocks2 = stream
			.filter(pos -> eyesPos.squaredDistanceTo(Vec3d.of(pos)) <= rangeSq)
			.filter(BlockUtils::canBeClicked)
			.filter(mode.getSelected().getValidator(this))
			.sorted(Comparator.comparingDouble(
				pos -> eyesPos.squaredDistanceTo(Vec3d.of(pos))))
			.collect(Collectors.toList());
		
		if(player.getAbilities().creativeMode)
		{
			Stream<BlockPos> stream2 = blocks2.parallelStream();
			for(Set<BlockPos> set : prevBlocks)
				stream2 = stream2.filter(pos -> !set.contains(pos));
			List<BlockPos> blocks3 = stream2.collect(Collectors.toList());
			
			prevBlocks.addLast(new HashSet<>(blocks3));
			while(prevBlocks.size() > 5)
				prevBlocks.removeFirst();
			
			if(!blocks3.isEmpty())
				currentBlock = blocks3.get(0);
			
			MC.interactionManager.cancelBlockBreaking();
			progress = 1;
			prevProgress = 1;
			BlockBreaker.breakBlocksWithPacketSpam(blocks3);
			return;
		}
		
		for(BlockPos pos : blocks2)
			if(BlockBreaker.breakOneBlock(pos))
			{
				currentBlock = pos;
				break;
			}
		
		if(currentBlock == null)
			MC.interactionManager.cancelBlockBreaking();
		
		if(currentBlock != null && BlockUtils.getHardness(currentBlock) < 1)
		{
			prevProgress = progress;
			progress = IMC.getInteractionManager().getCurrentBreakingProgress();
			
			if(progress < prevProgress)
				prevProgress = progress;
			
		}else
		{
			progress = 1;
			prevProgress = 1;
		}
	}
	
	@Override
	public void onLeftClick(LeftClickEvent event)
	{
		if(mode.getSelected() != Mode.ID)
			return;
		
		if(lockId.isChecked())
			return;
		
		if(MC.crosshairTarget == null
			|| MC.crosshairTarget.getType() != HitResult.Type.BLOCK)
			return;
		
		BlockHitResult blockHitResult = (BlockHitResult)MC.crosshairTarget;
		BlockPos pos = new BlockPos(blockHitResult.getBlockPos());
		id.setBlockName(BlockUtils.getName(pos));
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		if(currentBlock == null)
			return;
		
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		matrixStack.push();
		RenderUtils.applyRegionalRenderOffset(matrixStack);
		
		BlockPos camPos = RenderUtils.getCameraBlockPos();
		int regionX = (camPos.getX() >> 9) * 512;
		int regionZ = (camPos.getZ() >> 9) * 512;
		
		Box box = new Box(BlockPos.ORIGIN);
		float p = prevProgress + (progress - prevProgress) * partialTicks;
		float red = p * 2F;
		float green = 2 - red;
		
		matrixStack.translate(currentBlock.getX() - regionX,
			currentBlock.getY(), currentBlock.getZ() - regionZ);
		if(p < 1)
		{
			matrixStack.translate(0.5, 0.5, 0.5);
			matrixStack.scale(p, p, p);
			matrixStack.translate(-0.5, -0.5, -0.5);
		}
		
		RenderSystem.setShader(GameRenderer::getPositionShader);
		
		RenderSystem.setShaderColor(red, green, 0, 0.25F);
		RenderUtils.drawSolidBox(box, matrixStack);
		
		RenderSystem.setShaderColor(red, green, 0, 0.5F);
		RenderUtils.drawOutlinedBox(box, matrixStack);
		
		matrixStack.pop();
		
		// GL resets
		RenderSystem.setShaderColor(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		
	}
	
	private enum Mode
	{
		NORMAL("普通", NukerHack::getName, (n, p) -> true),
		
		ID("ID", n -> "ID挖块 [" + n.id.getBlockName().replace("minecraft:", "")
				+ "]",
			(n, p) -> BlockUtils.getName(p).equals(n.id.getBlockName())),
		
		MULTI_ID("多ID", n -> "多ID挖块 [" + n.multiIdList.getBlockNames().size()
				+ (n.multiIdList.getBlockNames().size() == 1 ? " ID]"
					: " IDs]"),
			(n, p) -> n.multiIdList.getBlockNames()
				.contains(BlockUtils.getName(p))),
		
		FLAT("平坦", n -> "平坦合法版挖块",
			(n, p) -> p.getY() >= MC.player.getPos().getY()),
		
		SMASH("粉碎", n -> "粉碎合法版挖块",
			(n, p) -> BlockUtils.getHardness(p) >= 1);
		
		private final String name;
		private final Function<NukerHack, String> renderName;
		private final BiPredicate<NukerHack, BlockPos> validator;
		
		private Mode(String name, Function<NukerHack, String> renderName,
			BiPredicate<NukerHack, BlockPos> validator)
		{
			this.name = name;
			this.renderName = renderName;
			this.validator = validator;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		
		public String getRenderName(NukerHack n)
		{
			return renderName.apply(n);
		}
		
		public Predicate<BlockPos> getValidator(NukerHack n)
		{
			return p -> validator.test(n, p);
		}
	}
}
