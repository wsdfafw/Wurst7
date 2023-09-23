/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.block.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags({"bonemeal aura", "bone meal aura", "AutoBone", "auto bone"})
public final class BonemealAuraHack extends Hack implements UpdateListener
{
	private final SliderSetting range =
		new SliderSetting("距离", 4.25, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	private final EnumSetting<Mode> mode = new EnumSetting<>("模式",
		"§l快速§r 模式可以同时使用骨粉\n多次对多个方块.\n§l合法§r 模式可以绕过反作弊",
		Mode.values(), Mode.FAST);
	
	private final EnumSetting<AutomationLevel> automationLevel =
		new EnumSetting<>("自动的模式",
			"自动骨粉应该要取决于哪种模式.\n§l右键§r 模式自动右键你手上的骨粉\n看个人喜好用.\n§l快捷栏§r 模式在你的快捷栏上选择骨粉并\n用在农作物上.\n§l背包§r 在你的背包里找到骨粉,\n并将其移动到快捷栏上使用他",
			AutomationLevel.values(), AutomationLevel.RIGHT_CLICK);
	
	private final CheckboxSetting saplings =
		new CheckboxSetting("树苗", true);
	private final CheckboxSetting crops = new CheckboxSetting("农作物",
		"小麦、胡萝卜、土豆和甜菜根.", true);
	private final CheckboxSetting stems =
		new CheckboxSetting("有茎类", "Pumpkins and melons.", true);
	private final CheckboxSetting cocoa = new CheckboxSetting("可可豆", true);
	private final CheckboxSetting other = new CheckboxSetting("其他", false);
	
	public BonemealAuraHack()
	{
		super("撒骨粉机器");
		
		setCategory(Category.BLOCKS);
		addSetting(range);
		addSetting(mode);
		addSetting(automationLevel);
		
		addSetting(saplings);
		addSetting(crops);
		addSetting(stems);
		addSetting(cocoa);
		addSetting(other);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// wait for right click timer
		if(IMC.getItemUseCooldown() > 0)
			return;
		
		// get valid blocks
		ArrayList<BlockPos> validBlocks =
			getValidBlocks(range.getValue(), this::isCorrectBlock);
		
		if(validBlocks.isEmpty())
			return;
		
		// wait for AutoFarm
		if(WURST.getHax().autoFarmHack.isBusy())
			return;
		
		// check held item
		ItemStack stack = MC.player.getInventory().getMainHandStack();
		if(stack.isEmpty() || stack.getItem() != Items.BONE_MEAL)
		{
			selectBonemeal();
			return;
		}
		
		if(mode.getSelected() == Mode.LEGIT)
		{
			// legit mode
			
			// use bone meal on next valid block
			for(BlockPos pos : validBlocks)
				if(rightClickBlockLegit(pos))
					break;
				
		}else
		{
			// fast mode
			
			boolean shouldSwing = false;
			
			// use bone meal on all valid blocks
			for(BlockPos pos : validBlocks)
				if(rightClickBlockSimple(pos))
					shouldSwing = true;
				
			// swing arm
			if(shouldSwing)
				MC.player.swingHand(Hand.MAIN_HAND);
		}
	}
	
	private void selectBonemeal()
	{
		ClientPlayerEntity player = MC.player;
		int maxInvSlot = automationLevel.getSelected().maxInvSlot;
		
		for(int slot = 0; slot < maxInvSlot; slot++)
		{
			if(slot == player.getInventory().selectedSlot)
				continue;
			
			ItemStack stack = player.getInventory().getStack(slot);
			if(stack.isEmpty() || stack.getItem() != Items.BONE_MEAL)
				continue;
			
			if(slot < 9)
				player.getInventory().selectedSlot = slot;
			else if(player.getInventory().getEmptySlot() < 9)
				IMC.getInteractionManager().windowClick_QUICK_MOVE(slot);
			else if(player.getInventory().getEmptySlot() != -1)
			{
				IMC.getInteractionManager().windowClick_QUICK_MOVE(
					player.getInventory().selectedSlot + 36);
				IMC.getInteractionManager().windowClick_QUICK_MOVE(slot);
			}else
			{
				IMC.getInteractionManager().windowClick_PICKUP(
					player.getInventory().selectedSlot + 36);
				IMC.getInteractionManager().windowClick_PICKUP(slot);
				IMC.getInteractionManager().windowClick_PICKUP(
					player.getInventory().selectedSlot + 36);
			}
			
			return;
		}
	}
	
	private ArrayList<BlockPos> getValidBlocks(double range,
		Predicate<BlockPos> validator)
	{
		Vec3d eyesVec = RotationUtils.getEyesPos().subtract(0.5, 0.5, 0.5);
		double rangeSq = Math.pow(range + 0.5, 2);
		int rangeI = (int)Math.ceil(range);
		
		BlockPos center = BlockPos.ofFloored(RotationUtils.getEyesPos());
		BlockPos min = center.add(-rangeI, -rangeI, -rangeI);
		BlockPos max = center.add(rangeI, rangeI, rangeI);
		
		Comparator<BlockPos> c = Comparator.<BlockPos> comparingDouble(
			pos -> eyesVec.squaredDistanceTo(Vec3d.of(pos))).reversed();
		
		return BlockUtils.getAllInBox(min, max).stream()
			.filter(pos -> eyesVec.squaredDistanceTo(Vec3d.of(pos)) <= rangeSq)
			.filter(validator).sorted(c)
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
	private boolean isCorrectBlock(BlockPos pos)
	{
		Block block = BlockUtils.getBlock(pos);
		BlockState state = BlockUtils.getState(pos);
		ClientWorld world = MC.world;
		
		if(!(block instanceof Fertilizable) || block instanceof GrassBlock
			|| !((Fertilizable)block).canGrow(world, MC.world.random, pos,
				state))
			return false;
		
		if(block instanceof SaplingBlock
			&& ((SaplingBlock)block).isFertilizable(world, pos, state))
			return saplings.isChecked();
		if(block instanceof CropBlock
			&& ((CropBlock)block).isFertilizable(world, pos, state))
			return crops.isChecked();
		if(block instanceof StemBlock
			&& ((StemBlock)block).isFertilizable(world, pos, state))
			return stems.isChecked();
		if(block instanceof CocoaBlock
			&& ((CocoaBlock)block).isFertilizable(world, pos, state))
			return cocoa.isChecked();
		return other.isChecked();
	}
	
	private boolean rightClickBlockLegit(BlockPos pos)
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		Vec3d posVec = Vec3d.ofCenter(pos);
		double distanceSqPosVec = eyesPos.squaredDistanceTo(posVec);
		
		for(Direction side : Direction.values())
		{
			Vec3d hitVec = posVec.add(Vec3d.of(side.getVector()).multiply(0.5));
			double distanceSqHitVec = eyesPos.squaredDistanceTo(hitVec);
			
			// check if hitVec is within range (4.25 blocks)
			if(distanceSqHitVec > 18.0625)
				continue;
			
			// check if side is facing towards player
			if(distanceSqHitVec >= distanceSqPosVec)
				continue;
			
			// check line of sight
			if(MC.world
				.raycast(new RaycastContext(eyesPos, hitVec,
					RaycastContext.ShapeType.COLLIDER,
					RaycastContext.FluidHandling.NONE, MC.player))
				.getType() != HitResult.Type.MISS)
				continue;
			
			// face block
			WURST.getRotationFaker().faceVectorPacket(hitVec);
			
			// place block
			IMC.getInteractionManager().rightClickBlock(pos, side, hitVec);
			MC.player.swingHand(Hand.MAIN_HAND);
			IMC.setItemUseCooldown(4);
			
			return true;
		}
		
		return false;
	}
	
	private boolean rightClickBlockSimple(BlockPos pos)
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		Vec3d posVec = Vec3d.ofCenter(pos);
		double distanceSqPosVec = eyesPos.squaredDistanceTo(posVec);
		
		for(Direction side : Direction.values())
		{
			Vec3d hitVec = posVec.add(Vec3d.of(side.getVector()).multiply(0.5));
			double distanceSqHitVec = eyesPos.squaredDistanceTo(hitVec);
			
			// check if hitVec is within range (6 blocks)
			if(distanceSqHitVec > 36)
				continue;
			
			// check if side is facing towards player
			if(distanceSqHitVec >= distanceSqPosVec)
				continue;
			
			// place block
			IMC.getInteractionManager().rightClickBlock(pos, side, hitVec);
			
			return true;
		}
		
		return false;
	}
	
	private enum Mode
	{
		FAST("快速"),
		
		LEGIT("合法");
		
		private final String name;
		
		private Mode(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
	
	private enum AutomationLevel
	{
		RIGHT_CLICK("右键", 0),
		
		HOTBAR("快捷栏", 9),
		
		INVENTORY("物品栏", 36);
		
		private final String name;
		private final int maxInvSlot;
		
		private AutomationLevel(String name, int maxInvSlot)
		{
			this.name = name;
			this.maxInvSlot = maxInvSlot;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
