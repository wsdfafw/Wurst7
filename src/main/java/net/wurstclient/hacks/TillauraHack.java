/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.HoeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PostMotionListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.settings.SwingHandSetting.SwingHand;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockBreaker.BlockBreakingParams;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.InteractionSimulator;
import net.wurstclient.util.RotationUtils;

@SearchTags({"till aura", "HoeAura", "hoe aura", "FarmlandAura",
	"farmland aura", "farm land aura", "AutoTill", "auto till", "AutoHoe",
	"auto hoe"})
public final class TillauraHack extends Hack implements PostMotionListener
{
	private final SliderSetting range = new SliderSetting("范围", "农田转化器所转化的范围.",
		5.0, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
	private final CheckboxSetting multiTill =
		new CheckboxSetting("多倍转农田", "一下子将多个地转化为农田.\n但无法绕过反作弊.", false);
	private final CheckboxSetting checkLOS = new CheckboxSetting("检查视野",
		"防止农田转化触及到无法触及的方块.\n在有反作弊服务器中表现很好,\n在原生态无插件服务器中无需.", true);
	
	private final List<Block> tillableBlocks = List.of(Blocks.GRASS_BLOCK,
		Blocks.DIRT_PATH, Blocks.DIRT, Blocks.COARSE_DIRT);
	
	public TillauraHack()
	{
		super("种地光环");
		
		setCategory(Category.BLOCKS);
		addSetting(range);
		addSetting(multiTill);
		addSetting(checkLOS);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(PostMotionListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(PostMotionListener.class, this);
	}
	
	@Override
	public void onPostMotion()
	{
		// wait for right click timer
		if(MC.itemUseCooldown > 0)
			return;
		
		// don't till while breaking or riding
		if(MC.interactionManager.isBreakingBlock() || MC.player.isRiding())
			return;
		
		// check held item
		if(!MC.player.isHolding(stack -> stack.getItem() instanceof HoeItem))
			return;
		
		// get valid blocks
		ArrayList<BlockPos> validBlocks = getValidBlocks();
		
		if(multiTill.isChecked())
		{
			boolean shouldSwing = false;
			
			// till all valid blocks
			for(BlockPos pos : validBlocks)
				if(rightClickBlockSimple(pos))
					shouldSwing = true;
				
			// swing arm
			if(shouldSwing)
				MC.player.swingHand(Hand.MAIN_HAND);
		}else
			// till next valid block
			for(BlockPos pos : validBlocks)
				if(rightClickBlockLegit(pos))
					break;
	}
	
	private ArrayList<BlockPos> getValidBlocks()
	{
		Vec3d eyesVec = RotationUtils.getEyesPos();
		BlockPos eyesBlock = BlockPos.ofFloored(eyesVec);
		double rangeSq = range.getValueSq();
		int blockRange = range.getValueCeil();
		
		return BlockUtils.getAllInBoxStream(eyesBlock, blockRange)
			.filter(pos -> pos.getSquaredDistance(eyesVec) <= rangeSq)
			.filter(this::isCorrectBlock)
			.sorted(Comparator
				.comparingDouble(pos -> pos.getSquaredDistance(eyesVec)))
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
	private boolean isCorrectBlock(BlockPos pos)
	{
		if(!tillableBlocks.contains(BlockUtils.getBlock(pos)))
			return false;
		
		if(!BlockUtils.getState(pos.up()).isAir())
			return false;
		
		return true;
	}
	
	private boolean rightClickBlockLegit(BlockPos pos)
	{
		// if this block is unreachable, try the next one
		BlockBreakingParams params = BlockBreaker.getBlockBreakingParams(pos);
		if(params == null || params.distanceSq() > range.getValueSq())
			return false;
		if(checkLOS.isChecked() && !params.lineOfSight())
			return false;
		
		// face and right click the block
		MC.itemUseCooldown = 4;
		WURST.getRotationFaker().faceVectorPacket(params.hitVec());
		InteractionSimulator.rightClickBlock(params.toHitResult());
		return true;
	}
	
	private boolean rightClickBlockSimple(BlockPos pos)
	{
		// if this block is unreachable, try the next one
		BlockBreakingParams params = BlockBreaker.getBlockBreakingParams(pos);
		if(params == null || params.distanceSq() > range.getValueSq())
			return false;
		if(checkLOS.isChecked() && !params.lineOfSight())
			return false;
		
		// right click the block
		InteractionSimulator.rightClickBlock(params.toHitResult(),
			SwingHand.OFF);
		return true;
	}
}
