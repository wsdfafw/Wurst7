/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
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
		new SliderSetting("防踢间隔",
			"反踢应该防止你被踢的频率.\n大多数服务器会在80秒后踢你.",
			30, 5, 80, 1, ValueDisplay.INTEGER.withSuffix(" ticks"));
	
	private final SliderSetting antiKickDistance = new SliderSetting(
		"防踢距离",
		"反踢应该让你跌倒多远.\n"
			+ "大多数服务器至少需要 0.032m 才能阻止您被踢.",
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
		
		player.abilities.flying = false;
		player.flyingSpeed = horizontalSpeed.getValueF();
		
		player.setVelocity(0, 0, 0);
		Vec3d velocity = player.getVelocity();
		
		if(MC.options.keyJump.isPressed())
			player.setVelocity(velocity.x, verticalSpeed.getValue(),
				velocity.z);
		
		if(MC.options.keySneak.isPressed())
		{
			if(slowSneaking.isChecked())
				player.flyingSpeed =
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
			case 0:
			if(MC.options.keySneak.isPressed())
				tickCounter = 2;
			else
				MC.player.setVelocity(velocity.x, -antiKickDistance.getValue(),
					velocity.z);
			break;
			
			case 1:
			MC.player.setVelocity(velocity.x, antiKickDistance.getValue(),
				velocity.z);
			break;
		}
		
		tickCounter++;
	}
	
	@Override
	public void onIsPlayerInWater(IsPlayerInWaterEvent event)
	{
		event.setInWater(false);
	}
}
