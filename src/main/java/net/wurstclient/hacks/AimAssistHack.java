/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.Comparator;
import java.util.stream.Stream;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.settings.filters.*;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.Rotation;
import net.wurstclient.util.RotationUtils;

public final class AimAssistHack extends Hack
	implements UpdateListener, RenderListener
{
	private final SliderSetting range =
		new SliderSetting("范围", 4.5, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	private final SliderSetting rotationSpeed = new SliderSetting("抬前轮速度", 600,
		10, 3600, 10, ValueDisplay.DEGREES.withSuffix("/s"));
	
	private final SliderSetting fov = new SliderSetting("FOV",
		"视野 - 一个实体在被忽略之前离你的十字准线有多远.\n" + "360\u00b0 = 瞄准你周围的实体.", 120, 30, 360,
		10, ValueDisplay.DEGREES);
	
	private final CheckboxSetting checkLOS = new CheckboxSetting(
		"Check line of sight", "Won't aim at entities behind blocks.", true);
	
	private final EntityFilterList entityFilters =
		new EntityFilterList(FilterPlayersSetting.genericCombat(false),
			FilterSleepingSetting.genericCombat(false),
			FilterFlyingSetting.genericCombat(0),
			FilterHostileSetting.genericCombat(false),
			FilterNeutralSetting
				.genericCombat(AttackDetectingEntityFilter.Mode.OFF),
			FilterPassiveSetting.genericCombat(true),
			FilterPassiveWaterSetting.genericCombat(true),
			FilterBabiesSetting.genericCombat(true),
			FilterBatsSetting.genericCombat(true),
			FilterSlimesSetting.genericCombat(true),
			FilterPetsSetting.genericCombat(true),
			FilterVillagersSetting.genericCombat(true),
			FilterZombieVillagersSetting.genericCombat(true),
			FilterGolemsSetting.genericCombat(false),
			FilterPiglinsSetting
				.genericCombat(AttackDetectingEntityFilter.Mode.OFF),
			FilterZombiePiglinsSetting
				.genericCombat(AttackDetectingEntityFilter.Mode.OFF),
			FilterEndermenSetting
				.genericCombat(AttackDetectingEntityFilter.Mode.OFF),
			FilterShulkersSetting.genericCombat(false),
			FilterInvisibleSetting.genericCombat(true),
			FilterNamedSetting.genericCombat(false),
			FilterShulkerBulletSetting.genericCombat(false),
			FilterArmorStandsSetting.genericCombat(true),
			FilterCrystalsSetting.genericCombat(true));
	
	private Entity target;
	private float nextYaw;
	private float nextPitch;
	
	public AimAssistHack()
	{
		super("瞄准辅助");
		setCategory(Category.COMBAT);
		
		addSetting(range);
		addSetting(rotationSpeed);
		addSetting(fov);
		addSetting(checkLOS);
		
		entityFilters.forEach(this::addSetting);
	}
	
	@Override
	protected void onEnable()
	{
		// disable incompatible hacks
		WURST.getHax().autoFishHack.setEnabled(false);
		WURST.getHax().clickAuraHack.setEnabled(false);
		WURST.getHax().crystalAuraHack.setEnabled(false);
		WURST.getHax().fightBotHack.setEnabled(false);
		WURST.getHax().killauraHack.setEnabled(false);
		WURST.getHax().killauraLegitHack.setEnabled(false);
		WURST.getHax().multiAuraHack.setEnabled(false);
		WURST.getHax().protectHack.setEnabled(false);
		WURST.getHax().tpAuraHack.setEnabled(false);
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		target = null;
	}
	
	@Override
	public void onUpdate()
	{
		// don't aim when a container/inventory screen is open
		if(MC.currentScreen instanceof HandledScreen)
			return;
		
		Stream<Entity> stream = EntityUtils.getAttackableEntities();
		double rangeSq = Math.pow(range.getValue(), 2);
		stream = stream.filter(e -> MC.player.squaredDistanceTo(e) <= rangeSq);
		
		if(fov.getValue() < 360.0)
			stream = stream.filter(e -> RotationUtils.getAngleToLookVec(
				e.getBoundingBox().getCenter()) <= fov.getValue() / 2.0);
		
		stream = entityFilters.applyTo(stream);
		
		target = stream
			.min(Comparator.comparingDouble(e -> RotationUtils
				.getAngleToLookVec(e.getBoundingBox().getCenter())))
			.orElse(null);
		if(target == null)
			return;
		
		Vec3d hitVec = target.getBoundingBox().getCenter();
		if(checkLOS.isChecked() && !BlockUtils.hasLineOfSight(hitVec))
		{
			target = null;
			return;
		}
		
		WURST.getHax().autoSwordHack.setSlot(target);
		faceEntityClient(target);
	}
	
	private boolean faceEntityClient(Entity entity)
	{
		// get needed rotation
		Box box = entity.getBoundingBox();
		Rotation needed = RotationUtils.getNeededRotations(box.getCenter());
		
		// turn towards center of boundingBox
		Rotation next = RotationUtils.slowlyTurnTowards(needed,
			rotationSpeed.getValueI() / 20F);
		nextYaw = next.yaw();
		nextPitch = next.pitch();
		
		// check if facing center
		if(RotationUtils.isAlreadyFacing(needed))
			return true;
		
		// if not facing center, check if facing anything in boundingBox
		return RotationUtils.isFacingBox(box, range.getValue());
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		if(target == null)
			return;
			
		// Not actually rendering anything, just using this method to rotate
		// more smoothly.
		float oldYaw = MC.player.prevYaw;
		float oldPitch = MC.player.prevPitch;
		MC.player.setYaw(MathHelper.lerp(partialTicks, oldYaw, nextYaw));
		MC.player.setPitch(MathHelper.lerp(partialTicks, oldPitch, nextPitch));
	}
}
