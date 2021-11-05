/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.LeftClickListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.DontSaveState;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.BlockListSetting;
import net.wurstclient.settings.BlockSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags({"speed nuker", "FastNuker", "fast nuker"})
@DontSaveState
public final class SpeedNukerHack extends Hack
	implements LeftClickListener, UpdateListener
{
	private final SliderSetting range = new SliderSetting("范围", 5.0, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final EnumSetting<Mode> mode = new EnumSetting("模式", "§l普通§r 模式很简单的破坏\n你周边的东西.\n§lID§r 模式只破坏所选的方块\n类型. 左键方块选择其方块.\n§l多个ID§r 模式只破坏那些你选择\n在你 多个ID 列表.\n§l平坦§r 模式只会挖你水平上的方块,\n但不会往下挖.\n§l粉碎§r 模式只会破坏那些\n能够瞬间破坏的方块 (例.如. 高大的草).", (Enum[])Mode.values(), (Enum)Mode.NORMAL);
    private final BlockSetting id = new BlockSetting("ID", "在ID模式,将会破坏指定ID的方块类型.\nair = 不会破坏任何东西", "minecraft:air", true);
    private final CheckboxSetting lockId = new CheckboxSetting("锁ID", "保护且不会导致因点击其他方块\n而改变挖掘的方块,同时也不会因重启而重置.", false);
	
	private final BlockListSetting multiIdList = new BlockListSetting(
		"MultiID List", "The types of blocks to break in MultiID mode.",
		"minecraft:ancient_debris", "minecraft:bone_block", "minecraft:clay",
		"minecraft:coal_ore", "minecraft:diamond_ore", "minecraft:emerald_ore",
		"minecraft:glowstone", "minecraft:gold_ore", "minecraft:iron_ore",
		"minecraft:lapis_ore", "minecraft:nether_gold_ore",
		"minecraft:nether_quartz_ore", "minecraft:redstone_ore");
	
	public SpeedNukerHack()
	{
		super("矿井+");
		
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
		return mode.getSelected().renderName.apply(this);
	}
	
	@Override
	public void onEnable()
	{
		// disable other nukers
		WURST.getHax().autoMineHack.setEnabled(false);
		WURST.getHax().excavatorHack.setEnabled(false);
		WURST.getHax().nukerHack.setEnabled(false);
		WURST.getHax().nukerLegitHack.setEnabled(false);
		WURST.getHax().tunnellerHack.setEnabled(false);
		
		// add listeners
		EVENTS.add(LeftClickListener.class, this);
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		// remove listeners
		EVENTS.remove(LeftClickListener.class, this);
		EVENTS.remove(UpdateListener.class, this);
		
		// resets
		if(!lockId.isChecked())
			id.setBlock(Blocks.AIR);
	}
	
	@Override
	public void onUpdate()
	{
		// abort if using IDNuker without an ID being set
		if(mode.getSelected() == Mode.ID && id.getBlock() == Blocks.AIR)
			return;
		
		// get valid blocks
		Iterable<BlockPos> validBlocks = getValidBlocks(range.getValue(),
			pos -> mode.getSelected().validator.test(this, pos));
		
		Iterator<BlockPos> autoToolIterator = validBlocks.iterator();
		if(autoToolIterator.hasNext())
			WURST.getHax().autoToolHack.equipIfEnabled(autoToolIterator.next());
		
		// break all blocks
		BlockBreaker.breakBlocksWithPacketSpam(validBlocks);
	}
	
	private ArrayList<BlockPos> getValidBlocks(double range,
		Predicate<BlockPos> validator)
	{
		Vec3d eyesVec = RotationUtils.getEyesPos().subtract(0.5, 0.5, 0.5);
		double rangeSq = Math.pow(range + 0.5, 2);
		int rangeI = (int)Math.ceil(range);
		
		BlockPos center = new BlockPos(RotationUtils.getEyesPos());
		BlockPos min = center.add(-rangeI, -rangeI, -rangeI);
		BlockPos max = center.add(rangeI, rangeI, rangeI);
		
		return BlockUtils.getAllInBox(min, max).stream()
			.filter(pos -> eyesVec.squaredDistanceTo(Vec3d.of(pos)) <= rangeSq)
			.filter(BlockUtils::canBeClicked).filter(validator)
			.sorted(Comparator.comparingDouble(
				pos -> eyesVec.squaredDistanceTo(Vec3d.of(pos))))
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
	@Override
	public void onLeftClick(LeftClickEvent event)
	{
		// check mode
		if(mode.getSelected() != Mode.ID)
			return;
		
		if(lockId.isChecked())
			return;
		
		// check hitResult
		if(MC.crosshairTarget == null
			|| !(MC.crosshairTarget instanceof BlockHitResult))
			return;
		
		// check pos
		BlockPos pos = ((BlockHitResult)MC.crosshairTarget).getBlockPos();
		if(pos == null
			|| BlockUtils.getState(pos).getMaterial() == Material.AIR)
			return;
		
		// set id
		id.setBlockName(BlockUtils.getName(pos));
	}
	
	private enum Mode
	{
		NORMAL("普通", n -> "急速挖块", (n, pos) -> true),
		
		ID("ID",
			n -> "ID急速挖块 ["
				+ n.id.getBlockName().replace("minecraft:", "") + "]",
			(n, pos) -> BlockUtils.getName(pos).equals(n.id.getBlockName())),
		
		MULTI_ID("多ID",
			n -> "多ID挖块 [" + n.multiIdList.getBlockNames().size()
				+ (n.multiIdList.getBlockNames().size() == 1 ? " ID]"
					: " IDs]"),
			(n, p) -> n.multiIdList.getBlockNames()
				.contains(BlockUtils.getName(p))),
		
		FLAT("平坦", n -> "平坦型急速挖块",
			(n, pos) -> pos.getY() >= MC.player.getY()),
		
		SMASH("粉碎", n -> "粉碎型急速挖块",
			(n, pos) -> BlockUtils.getHardness(pos) >= 1);
		
		private final String name;
		private final Function<SpeedNukerHack, String> renderName;
		private final BiPredicate<SpeedNukerHack, BlockPos> validator;
		
		private Mode(String name, Function<SpeedNukerHack, String> renderName,
			BiPredicate<SpeedNukerHack, BlockPos> validator)
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
	}
}
