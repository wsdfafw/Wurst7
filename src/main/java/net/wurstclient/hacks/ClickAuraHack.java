/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.Comparator;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.LeftClickListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.FakePlayerEntity;
import net.wurstclient.util.RotationUtils;
import net.wurstclient.util.RotationUtils.Rotation;

@SearchTags({"click aura", "ClickAimbot", "click aimbot"})
public final class ClickAuraHack extends Hack
	implements UpdateListener, LeftClickListener
{
	private final SliderSetting range = new SliderSetting("范围", 5.0, 1.0, 10.0, 0.05, SliderSetting.ValueDisplay.DECIMAL);
    private final EnumSetting<Priority> priority = new EnumSetting("优先级", "决定哪个实体会优先攻击.\n§l距离§r - 攻击最近的实体.\n§l角度§r - 攻击实体所需要的\n最后头位置所可以砍的角度.\n§l生命§r - 攻击血量最少的实体.", (Enum[])Priority.values(), (Enum)Priority.ANGLE);
    public final SliderSetting fov = new SliderSetting("视场", 360, 30, 360, 10, ValueDisplay.DEGREES);
	private final CheckboxSetting filterPlayers = new CheckboxSetting("排除玩家", "不会攻击其他玩家.", false);
    private final CheckboxSetting filterSleeping = new CheckboxSetting("排除正在睡觉", "不会攻击正在睡觉的玩家.", false);
    private final SliderSetting filterFlying = new SliderSetting("排除飞行中", "不会攻击在飞行中玩家或\n远离地板一定距离的玩家.", 0.0, 0.0, 2.0, 0.05, v -> v == 0.0 ? "关" : SliderSetting.ValueDisplay.DECIMAL.getValueString(v));
    private final CheckboxSetting filterMonsters = new CheckboxSetting("排除怪物", "不会攻击僵尸,苦力怕,诸如此类.", false);
    private final CheckboxSetting filterPigmen = new CheckboxSetting("排除猪人", "不会攻击僵尸猪人.", false);
    private final CheckboxSetting filterEndermen = new CheckboxSetting("排除末影人", "不会攻击末影人.", false);
    private final CheckboxSetting filterAnimals = new CheckboxSetting("排除动物", "不会攻击牛,猪,诸如此类.", false);
    private final CheckboxSetting filterBabies = new CheckboxSetting("排除婴儿", "不会攻击小猪仔,\n小村民, 诸如此类.", false);
    private final CheckboxSetting filterPets = new CheckboxSetting("排除宠物", "不会攻击以驯服的狼,\n已驯服的马, 诸如此类.", false);
    private final CheckboxSetting filterTraders = new CheckboxSetting("排除商人", "不会攻击村民 , 流浪商人, 诸如此类.", false);
    private final CheckboxSetting filterGolems = new CheckboxSetting("排除傀儡们", "不会攻击铁傀儡,\n雪傀儡 和 潜影盒.", false);
    private final CheckboxSetting filterInvisible = new CheckboxSetting("排除隐身", "不会攻击隐形的实体.", false);
    private final CheckboxSetting filterNamed = new CheckboxSetting("排除被命名", "不会攻击已经被命名的实体.", false);
    private final CheckboxSetting filterStands = new CheckboxSetting("排除盔甲架", "不会攻击盔甲架.", false);
    private final CheckboxSetting filterCrystals = new CheckboxSetting("排除末影水晶", "不会攻击末影水晶.", false);
	
	public ClickAuraHack()
	{
		super("点击光环");
		
		setCategory(Category.COMBAT);
		addSetting(range);
		addSetting(priority);
		addSetting(fov);
		addSetting(filterPlayers);
		addSetting(filterSleeping);
		addSetting(filterFlying);
		addSetting(filterMonsters);
		addSetting(filterPigmen);
		addSetting(filterEndermen);
		addSetting(filterAnimals);
		addSetting(filterBabies);
		addSetting(filterPets);
		addSetting(filterTraders);
		addSetting(filterGolems);
		addSetting(filterInvisible);
		addSetting(filterNamed);
		addSetting(filterStands);
		addSetting(filterCrystals);
	}
	
	@Override
	public void onEnable()
	{
		// disable other killauras
		WURST.getHax().crystalAuraHack.setEnabled(false);
		WURST.getHax().fightBotHack.setEnabled(false);
		WURST.getHax().killauraLegitHack.setEnabled(false);
		WURST.getHax().killauraHack.setEnabled(false);
		WURST.getHax().multiAuraHack.setEnabled(false);
		WURST.getHax().protectHack.setEnabled(false);
		WURST.getHax().triggerBotHack.setEnabled(false);
		WURST.getHax().tpAuraHack.setEnabled(false);
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(LeftClickListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(LeftClickListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(!MC.options.keyAttack.isPressed())
			return;
		
		if(MC.player.getAttackCooldownProgress(0) < 1)
			return;
		
		attack();
	}
	
	@Override
	public void onLeftClick(LeftClickEvent event)
	{
		attack();
	}
	
	private void attack()
	{
		// set entity
		ClientPlayerEntity player = MC.player;
		ClientWorld world = MC.world;
		
		if(player.getAttackCooldownProgress(0) < 1)
			return;
		
		double rangeSq = Math.pow(range.getValue(), 2);
		Stream<Entity> stream =
			StreamSupport.stream(MC.world.getEntities().spliterator(), true)
				.filter(e -> !e.isRemoved())
				.filter(e -> e instanceof LivingEntity
					&& ((LivingEntity)e).getHealth() > 0
					|| e instanceof EndCrystalEntity)
				.filter(e -> player.squaredDistanceTo(e) <= rangeSq)
				.filter(e -> e != player)
				.filter(e -> !(e instanceof FakePlayerEntity))
				.filter(e -> !WURST.getFriends().contains(e.getEntityName()));
		
		if(fov.getValue() < 360.0)
			stream = stream.filter(e -> RotationUtils.getAngleToLookVec(
				e.getBoundingBox().getCenter()) <= fov.getValue() / 2.0);
		
		if(filterPlayers.isChecked())
			stream = stream.filter(e -> !(e instanceof PlayerEntity));
		
		if(filterSleeping.isChecked())
			stream = stream.filter(e -> !(e instanceof PlayerEntity
				&& ((PlayerEntity)e).isSleeping()));
		
		if(filterFlying.getValue() > 0)
			stream = stream.filter(e -> {
				
				if(!(e instanceof PlayerEntity))
					return true;
				
				Box box = e.getBoundingBox();
				box = box.union(box.offset(0, -filterFlying.getValue(), 0));
				return !world.isSpaceEmpty(box);
			});
		
		if(filterMonsters.isChecked())
			stream = stream.filter(e -> !(e instanceof Monster));
		
		if(filterPigmen.isChecked())
			stream = stream.filter(e -> !(e instanceof ZombifiedPiglinEntity));
		
		if(filterEndermen.isChecked())
			stream = stream.filter(e -> !(e instanceof EndermanEntity));
		
		if(filterAnimals.isChecked())
			stream = stream.filter(
				e -> !(e instanceof AnimalEntity || e instanceof AmbientEntity
					|| e instanceof WaterCreatureEntity));
		
		if(filterBabies.isChecked())
			stream = stream.filter(e -> !(e instanceof PassiveEntity
				&& ((PassiveEntity)e).isBaby()));
		
		if(filterPets.isChecked())
			stream = stream
				.filter(e -> !(e instanceof TameableEntity
					&& ((TameableEntity)e).isTamed()))
				.filter(e -> !(e instanceof HorseBaseEntity
					&& ((HorseBaseEntity)e).isTame()));
		
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
		
		if(filterCrystals.isChecked())
			stream = stream.filter(e -> !(e instanceof EndCrystalEntity));
		
		Entity target =
			stream.min(priority.getSelected().comparator).orElse(null);
		if(target == null)
			return;
		
		WURST.getHax().autoSwordHack.setSlot();
		
		// face entity
		Rotation rotation = RotationUtils
			.getNeededRotations(target.getBoundingBox().getCenter());
		PlayerMoveC2SPacket.LookAndOnGround packet =
			new PlayerMoveC2SPacket.LookAndOnGround(rotation.getYaw(),
				rotation.getPitch(), MC.player.isOnGround());
		MC.player.networkHandler.sendPacket(packet);
		
		// attack entity
		WURST.getHax().criticalsHack.doCritical();
		MC.interactionManager.attackEntity(player, target);
		player.swingHand(Hand.MAIN_HAND);
	}
	
	private enum Priority
	{
		DISTANCE("Distance", e -> MC.player.squaredDistanceTo(e)),
		
		ANGLE("Angle",
			e -> RotationUtils
				.getAngleToLookVec(e.getBoundingBox().getCenter())),
		
		HEALTH("Health", e -> e instanceof LivingEntity
			? ((LivingEntity)e).getHealth() : Integer.MAX_VALUE);
		
		private final String name;
		private final Comparator<Entity> comparator;
		
		private Priority(String name, ToDoubleFunction<Entity> keyExtractor)
		{
			this.name = name;
			comparator = Comparator.comparingDouble(keyExtractor);
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
