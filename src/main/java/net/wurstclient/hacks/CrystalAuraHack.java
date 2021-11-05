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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IClientPlayerInteractionManager;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.FakePlayerEntity;
import net.wurstclient.util.RotationUtils;
import net.wurstclient.util.RotationUtils.Rotation;

@SearchTags({"crystal aura"})
public final class CrystalAuraHack extends Hack implements UpdateListener
{
	private final SliderSetting range = new SliderSetting("范围", "决定放置水晶的范围并\n引爆水晶.", 6.0, 1.0, 6.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final CheckboxSetting autoPlace = new CheckboxSetting("自动放置水晶", "当开启时, 水晶功能 将会自动\n放置当有效的实体靠近时候.\n当关闭时候, 水晶将会只会\n引爆请手动放置水晶.", true);
    private final EnumSetting<FaceBlocks> faceBlocks = new EnumSetting("面朝水晶", "无论怎么样 水晶功能 必须面朝\n正确的方向放置并\n左键引爆重生锚.\n\n虽然会慢下来,但有效\n避开反作弊.", (Enum[])FaceBlocks.values(), (Enum)FaceBlocks.OFF);
    private final CheckboxSetting checkLOS = new CheckboxSetting("检查视野", "确保你不会因为无法触碰到水晶\n方块,放下,左键\n引爆末影水晶.\n\n虽然会慢下来,但有效\n避开反作弊.", false);
    private final EnumSetting<TakeItemsFrom> takeItemsFrom = new EnumSetting("从哪拿物品", "应该从哪拿末影水晶.", (Enum[])TakeItemsFrom.values(), (Enum)TakeItemsFrom.INVENTORY);
    private final CheckboxSetting filterPlayers = new CheckboxSetting("排除玩家", "不会以其他玩家作为目标\n当自动放置开启时.\n\n他们仍会受到伤害\n当他们足够靠近有效\n目标或一个存在的末影水晶.", false);
    private final CheckboxSetting filterMonsters = new CheckboxSetting("排除怪物", "不会以,僵尸,苦力怕等为目标.\n当自动放置开启时.\n\n他们仍会受到伤害\n当他们足够靠近有效\n目标或一个存在的末影水晶.", true);
    private final CheckboxSetting filterAnimals = new CheckboxSetting("排除动物", "不会以猪,牛,鸡等为目标.\n当自动放置开启时.\n\n他们仍会受到伤害\n当他们足够靠近有效\n目标或一个存在的末影水晶.", true);
    private final CheckboxSetting filterTraders = new CheckboxSetting("排除商人", "不会以村民,流浪商人,诸如此类为目标.\n当自动放置开启时.\n\n他们仍会受到伤害\n当他们足够靠近有效\n目标或一个存在的末影水晶.", true);
    private final CheckboxSetting filterGolems = new CheckboxSetting("排除傀儡们", "不会以铁傀儡,\n雪傀儡 和 潜影盒为目标.\n当自动放置开启时.\n\n他们仍会受到伤害\n当他们足够靠近有效\n目标或一个存在的末影水晶.", true);
    private final CheckboxSetting filterInvisible = new CheckboxSetting("排除隐身", "不会以隐形的实体为目标\n当自动放置开启时.\n\n他们仍会受到伤害\n当他们足够靠近有效\n目标或一个存在的末影水晶.", false);
    private final CheckboxSetting filterNamed = new CheckboxSetting("排除被命名", "不会以已经被命名的实体为目标\n当自动放置开启时.\n\n他们仍会受到伤害\n当他们足够靠近有效\n目标或一个存在的末影水晶.", false);
    private final CheckboxSetting filterStands = new CheckboxSetting("排除盔甲架", "不会以盔甲架为目标.\n当自动放置开启时.\n\n他们仍会受到伤害\n当他们足够靠近有效\n目标或一个存在的末影水晶.", true);

	public CrystalAuraHack()
	{
		super("水晶光环");
		
		setCategory(Category.COMBAT);
		addSetting(range);
		addSetting(autoPlace);
		addSetting(faceBlocks);
		addSetting(checkLOS);
		addSetting(takeItemsFrom);
		
		addSetting(filterPlayers);
		addSetting(filterMonsters);
		addSetting(filterAnimals);
		addSetting(filterTraders);
		addSetting(filterGolems);
		addSetting(filterInvisible);
		addSetting(filterNamed);
		addSetting(filterStands);
	}
	
	@Override
	public void onEnable()
	{
		// disable other killauras
		WURST.getHax().clickAuraHack.setEnabled(false);
		WURST.getHax().fightBotHack.setEnabled(false);
		WURST.getHax().killauraHack.setEnabled(false);
		WURST.getHax().killauraLegitHack.setEnabled(false);
		WURST.getHax().multiAuraHack.setEnabled(false);
		WURST.getHax().protectHack.setEnabled(false);
		WURST.getHax().triggerBotHack.setEnabled(false);
		WURST.getHax().tpAuraHack.setEnabled(false);
		
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
		ArrayList<Entity> crystals = getNearbyCrystals();
		
		if(!crystals.isEmpty())
		{
			detonate(crystals);
			return;
		}
		
		if(!autoPlace.isChecked()
			|| !hasItem(item -> item == Items.END_CRYSTAL))
			return;
		
		ArrayList<Entity> targets = getNearbyTargets();
		placeCrystalsNear(targets);
	}
	
	private ArrayList<BlockPos> placeCrystalsNear(ArrayList<Entity> targets)
	{
		ArrayList<BlockPos> newCrystals = new ArrayList<>();
		
		boolean shouldSwing = false;
		for(Entity target : targets)
		{
			ArrayList<BlockPos> freeBlocks = getFreeBlocksNear(target);
			
			for(BlockPos pos : freeBlocks)
				if(placeCrystal(pos))
				{
					shouldSwing = true;
					newCrystals.add(pos);
					
					// TODO optional speed limit(?)
					break;
				}
		}
		
		if(shouldSwing)
			MC.player.swingHand(Hand.MAIN_HAND);
		
		return newCrystals;
	}
	
	private void detonate(ArrayList<Entity> crystals)
	{
		for(Entity e : crystals)
		{
			faceBlocks.getSelected().face(e.getBoundingBox().getCenter());
			MC.interactionManager.attackEntity(MC.player, e);
		}
		
		if(!crystals.isEmpty())
			MC.player.swingHand(Hand.MAIN_HAND);
	}
	
	private boolean selectItem(Predicate<Item> item)
	{
		PlayerInventory inventory = MC.player.getInventory();
		IClientPlayerInteractionManager im = IMC.getInteractionManager();
		int maxInvSlot = takeItemsFrom.getSelected().maxInvSlot;
		
		for(int slot = 0; slot < maxInvSlot; slot++)
		{
			ItemStack stack = inventory.getStack(slot);
			if(!item.test(stack.getItem()))
				continue;
			
			if(slot < 9)
				inventory.selectedSlot = slot;
			else if(inventory.getEmptySlot() < 9)
				im.windowClick_QUICK_MOVE(slot);
			else if(inventory.getEmptySlot() != -1)
			{
				im.windowClick_QUICK_MOVE(inventory.selectedSlot + 36);
				im.windowClick_QUICK_MOVE(slot);
			}else
			{
				im.windowClick_PICKUP(inventory.selectedSlot + 36);
				im.windowClick_PICKUP(slot);
				im.windowClick_PICKUP(inventory.selectedSlot + 36);
			}
			
			return true;
		}
		
		return false;
	}
	
	private boolean hasItem(Predicate<Item> item)
	{
		PlayerInventory inventory = MC.player.getInventory();
		int maxInvSlot = takeItemsFrom.getSelected().maxInvSlot;
		
		for(int slot = 0; slot < maxInvSlot; slot++)
		{
			ItemStack stack = inventory.getStack(slot);
			if(!item.test(stack.getItem()))
				continue;
			
			return true;
		}
		
		return false;
	}
	
	private boolean placeCrystal(BlockPos pos)
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		double rangeSq = Math.pow(range.getValue(), 2);
		Vec3d posVec = Vec3d.ofCenter(pos);
		double distanceSqPosVec = eyesPos.squaredDistanceTo(posVec);
		
		for(Direction side : Direction.values())
		{
			BlockPos neighbor = pos.offset(side);
			
			// check if neighbor can be right clicked
			if(!isClickableNeighbor(neighbor))
				continue;
			
			Vec3d dirVec = Vec3d.of(side.getVector());
			Vec3d hitVec = posVec.add(dirVec.multiply(0.5));
			
			// check if hitVec is within range
			if(eyesPos.squaredDistanceTo(hitVec) > rangeSq)
				continue;
			
			// check if side is visible (facing away from player)
			if(distanceSqPosVec > eyesPos.squaredDistanceTo(posVec.add(dirVec)))
				continue;
			
			if(checkLOS.isChecked() && MC.world
				.raycast(new RaycastContext(eyesPos, hitVec,
					RaycastContext.ShapeType.COLLIDER,
					RaycastContext.FluidHandling.NONE, MC.player))
				.getType() != HitResult.Type.MISS)
				continue;
			
			if(!selectItem(item -> item == Items.END_CRYSTAL))
				return false;
			
			faceBlocks.getSelected().face(hitVec);
			
			// place block
			IMC.getInteractionManager().rightClickBlock(neighbor,
				side.getOpposite(), hitVec);
			
			return true;
		}
		
		return false;
	}
	
	private ArrayList<Entity> getNearbyCrystals()
	{
		ClientPlayerEntity player = MC.player;
		double rangeSq = Math.pow(range.getValue(), 2);
		
		Comparator<Entity> furthestFromPlayer = Comparator
			.<Entity> comparingDouble(e -> MC.player.squaredDistanceTo(e))
			.reversed();
		
		return StreamSupport.stream(MC.world.getEntities().spliterator(), true)
			.filter(e -> e instanceof EndCrystalEntity)
			.filter(e -> !e.isRemoved())
			.filter(e -> player.squaredDistanceTo(e) <= rangeSq)
			.sorted(furthestFromPlayer)
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
	private ArrayList<Entity> getNearbyTargets()
	{
		double rangeSq = Math.pow(range.getValue(), 2);
		
		Comparator<Entity> furthestFromPlayer = Comparator
			.<Entity> comparingDouble(e -> MC.player.squaredDistanceTo(e))
			.reversed();
		
		Stream<Entity> stream =
			StreamSupport.stream(MC.world.getEntities().spliterator(), false)
				.filter(e -> !e.isRemoved())
				.filter(e -> e instanceof LivingEntity
					&& ((LivingEntity)e).getHealth() > 0)
				.filter(e -> e != MC.player)
				.filter(e -> !(e instanceof FakePlayerEntity))
				.filter(e -> !WURST.getFriends().contains(e.getEntityName()))
				.filter(e -> MC.player.squaredDistanceTo(e) <= rangeSq);
		
		if(filterPlayers.isChecked())
			stream = stream.filter(e -> !(e instanceof PlayerEntity));
		
		if(filterMonsters.isChecked())
			stream = stream.filter(e -> !(e instanceof Monster));
		
		if(filterAnimals.isChecked())
			stream = stream.filter(
				e -> !(e instanceof AnimalEntity || e instanceof AmbientEntity
					|| e instanceof WaterCreatureEntity));
		
		if(filterTraders.isChecked())
			stream = stream.filter(e -> !(e instanceof MerchantEntity));
		
		if(filterGolems.isChecked())
			stream = stream.filter(e -> !(e instanceof GolemEntity));
		
		if(filterInvisible.isChecked())
			stream = stream.filter(e -> !e.isInvisible());
		
		if(filterNamed.isChecked())
			stream = stream.filter(e -> !e.hasCustomName());
		
		if(filterStands.isChecked())
			stream = stream.filter(e -> !(e instanceof ArmorStandEntity));
		
		return stream.sorted(furthestFromPlayer)
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
	private ArrayList<BlockPos> getFreeBlocksNear(Entity target)
	{
		Vec3d eyesVec = RotationUtils.getEyesPos().subtract(0.5, 0.5, 0.5);
		double rangeD = range.getValue();
		double rangeSq = Math.pow(rangeD + 0.5, 2);
		int rangeI = 2;
		
		BlockPos center = target.getBlockPos();
		BlockPos min = center.add(-rangeI, -rangeI, -rangeI);
		BlockPos max = center.add(rangeI, rangeI, rangeI);
		Box targetBB = target.getBoundingBox();
		
		Vec3d targetEyesVec =
			target.getPos().add(0, target.getEyeHeight(target.getPose()), 0);
		
		Comparator<BlockPos> closestToTarget =
			Comparator.<BlockPos> comparingDouble(
				pos -> targetEyesVec.squaredDistanceTo(Vec3d.ofCenter(pos)));
		
		return BlockUtils.getAllInBoxStream(min, max)
			.filter(pos -> eyesVec.squaredDistanceTo(Vec3d.of(pos)) <= rangeSq)
			.filter(this::isReplaceable).filter(this::hasCrystalBase)
			.filter(pos -> !targetBB.intersects(new Box(pos)))
			.sorted(closestToTarget)
			.collect(Collectors.toCollection(ArrayList::new));
	}
	
	private boolean isReplaceable(BlockPos pos)
	{
		return BlockUtils.getState(pos).getMaterial().isReplaceable();
	}
	
	private boolean hasCrystalBase(BlockPos pos)
	{
		Block block = BlockUtils.getBlock(pos.down());
		return block == Blocks.BEDROCK || block == Blocks.OBSIDIAN;
	}
	
	private boolean isClickableNeighbor(BlockPos pos)
	{
		return BlockUtils.canBeClicked(pos)
			&& !BlockUtils.getState(pos).getMaterial().isReplaceable();
	}
	
	private enum FaceBlocks
	{
		OFF("Off", v -> {}),
		
		SERVER("Server-side",
			v -> WURST.getRotationFaker().faceVectorPacket(v)),
		
		CLIENT("Client-side",
			v -> WURST.getRotationFaker().faceVectorClient(v)),
		
		SPAM("Packet spam", v -> {
			Rotation rotation = RotationUtils.getNeededRotations(v);
			PlayerMoveC2SPacket.LookAndOnGround packet =
				new PlayerMoveC2SPacket.LookAndOnGround(rotation.getYaw(),
					rotation.getPitch(), MC.player.isOnGround());
			MC.player.networkHandler.sendPacket(packet);
		});
		
		private String name;
		private Consumer<Vec3d> face;
		
		private FaceBlocks(String name, Consumer<Vec3d> face)
		{
			this.name = name;
			this.face = face;
		}
		
		public void face(Vec3d v)
		{
			face.accept(v);
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
	
	private enum TakeItemsFrom
	{
		HOTBAR("快捷栏", 9),
		
		INVENTORY("背包", 36);
		
		private final String name;
		private final int maxInvSlot;
		
		private TakeItemsFrom(String name, int maxInvSlot)
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
