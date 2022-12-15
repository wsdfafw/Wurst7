/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.IsPlayerInWaterListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"FlyHack", "fly hack", "flying"})
public final class FlightHack extends Hack
	implements UpdateListener, IsPlayerInWaterListener
{
	public final SliderSetting horizontalSpeed = new SliderSetting(
		"水平速度", 1, 0.05, 10, 0.05, ValueDisplay.DECIMAL);
	
	public final SliderSetting verticalSpeed = new SliderSetting(
		"升速",
		"\u00a7c\u00a7l警告:\u00a7r 设置太高会导致坠落伤害，即使没有坠落。",
		1, 0.05, 5, 0.05, ValueDisplay.DECIMAL);
	
	private final CheckboxSetting slowSneaking = new CheckboxSetting(
		"缓慢潜行",
		"当你潜行时，降低你的水平速度，以防止你出故障。",
		true);
	
	private final CheckboxSetting antiKick = new CheckboxSetting("反踢",
		"让你时不时地摔一跤，以防被踢.",
		false);
	
	private final SliderSetting antiKickInterval =
		new SliderSetting("Anti-Kick Interval",
			"How often Anti-Kick should prevent you from getting kicked.\n"
				+ "Most servers will kick you after 80 ticks.",
			30, 5, 80, 1, ValueDisplay.INTEGER.withSuffix(" ticks"));
	
	private final SliderSetting antiKickDistance = new SliderSetting(
		"Anti-Kick Distance",
		"How far Anti-Kick should make you fall.\n"
			+ "Most servers require at least 0.032m to stop you from getting kicked.",
		0.07, 0.01, 0.2, 0.001, ValueDisplay.DECIMAL.withSuffix("m"));
	
	private int tickCounter = 0;
	
	public FlightHack()
	{
		super("飞行");
		setCategory(Category.MOVEMENT);
		addSetting(horizontalSpeed);
		addSetting(verticalSpeed);
		addSetting(slowSneaking);
		addSetting(antiKick);
		addSetting(antiKickInterval);
		addSetting(antiKickDistance);
	}
	
	@Override
	public void onEnable()
	{
		tickCounter = 0;
		
		WURST.getHax().creativeFlightHack.setEnabled(false);
		WURST.getHax().jetpackHack.setEnabled(false);
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(IsPlayerInWaterListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(IsPlayerInWaterListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		ClientPlayerEntity player = MC.player;
		
		player.getAbilities().flying = false;
		player.airStrafingSpeed = horizontalSpeed.getValueF();
		
		player.setVelocity(0, 0, 0);
		Vec3d velocity = player.getVelocity();
		
		if(MC.options.jumpKey.isPressed())
			player.setVelocity(velocity.x, verticalSpeed.getValue(),
				velocity.z);
		
		if(MC.options.sneakKey.isPressed())
		{
			if(slowSneaking.isChecked())
				player.airStrafingSpeed =
					Math.min(horizontalSpeed.getValueF(), 0.85F);
			
			player.setVelocity(velocity.x, -verticalSpeed.getValue(),
				velocity.z);
		}
		
		if(antiKick.isChecked())
			doAntiKick(velocity);
	}
	
	private void doAntiKick(Vec3d velocity)
	{
		if(tickCounter > antiKickInterval.getValueI() + 1)
			tickCounter = 0;
		
		switch(tickCounter)
		{
			case 0 ->
			{
				if(MC.options.sneakKey.isPressed())
					tickCounter = 2;
				else
					MC.player.setVelocity(velocity.x,
						-antiKickDistance.getValue(), velocity.z);
			}
			
			case 1 -> MC.player.setVelocity(velocity.x,
				antiKickDistance.getValue(), velocity.z);
		}
		
		tickCounter++;
	}
	
	@Override
	public void onIsPlayerInWater(IsPlayerInWaterEvent event)
	{
		event.setInWater(false);
	}
}
