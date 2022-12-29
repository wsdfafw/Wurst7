/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;

@SearchTags({"boat fly", "BoatFlight", "boat flight", "EntitySpeed",
	"entity speed"})
public final class BoatFlyHack extends Hack implements UpdateListener
{
	private final CheckboxSetting changeForwardSpeed = new CheckboxSetting(
		"改变前进速度",
		"允许§e更改前速§r，禁用平滑加速.",
		false);
	
	private final SliderSetting forwardSpeed = new SliderSetting(
		"前进速度", 1, 0.05, 5, 0.05, SliderSetting.ValueDisplay.DECIMAL);
	
	private final SliderSetting upwardSpeed = new SliderSetting("上升速度",
		0.3, 0, 5, 0.05, SliderSetting.ValueDisplay.DECIMAL);
	
	public BoatFlyHack()
	{
		super("飞船");
		setCategory(Category.MOVEMENT);
		addSetting(changeForwardSpeed);
		addSetting(forwardSpeed);
		addSetting(upwardSpeed);
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
		// check if riding
		if(!MC.player.hasVehicle())
			return;
		
		Entity vehicle = MC.player.getVehicle();
		Vec3d velocity = vehicle.getVelocity();
		
		// default motion
		double motionX = velocity.x;
		double motionY = 0;
		double motionZ = velocity.z;
		
		// up/down
		if(MC.options.keyJump.isPressed())
			motionY = upwardSpeed.getValue();
		else if(MC.options.keySprint.isPressed())
			motionY = velocity.y;
		
		// forward
		if(MC.options.keyForward.isPressed() && changeForwardSpeed.isChecked())
		{
			double speed = forwardSpeed.getValue();
			float yawRad = (float)Math.toRadians(vehicle.getYaw(1));
			
			motionX = MathHelper.sin(-yawRad) * speed;
			motionZ = MathHelper.cos(yawRad) * speed;
		}
		
		// apply motion
		vehicle.setVelocity(motionX, motionY, motionZ);
	}
}
