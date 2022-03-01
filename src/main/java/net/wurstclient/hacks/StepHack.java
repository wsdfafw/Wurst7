/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.stream.Collectors;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.wurstclient.Category;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

public final class StepHack extends Hack implements UpdateListener
{
	private final EnumSetting<Mode> mode = new EnumSetting("模式", "§l简单§r 模式 可以一下子走上X格高\n的方块 (开启滑块高度).\n§l合法§r 模式可以绕过反作弊.", (Enum[])Mode.values(), (Enum)Mode.LEGIT);
    private final SliderSetting height = new SliderSetting("高度", "只在 §l简单§r 模式有作用.", 1.0, 1.0, 10.0, 1.0, SliderSetting.ValueDisplay.INTEGER);
	
	public StepHack()
	{
		super("Step");
		setCategory(Category.MOVEMENT);
		addSetting(mode);
		addSetting(height);
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
		MC.player.stepHeight = 0.5F;
	}
	
	@Override
	public void onUpdate()
	{
		if(mode.getSelected() == Mode.SIMPLE)
		{
			// simple mode
			MC.player.stepHeight = height.getValueF();
			return;
		}
		
		// legit mode
		ClientPlayerEntity player = MC.player;
		player.stepHeight = 0.5F;
		
		if(!player.horizontalCollision)
			return;
		
		if(!player.isOnGround() || player.isClimbing()
			|| player.isTouchingWater() || player.isInLava())
			return;
		
		if(player.input.movementForward == 0
			&& player.input.movementSideways == 0)
			return;
		
		if(player.input.jumping)
			return;
		
		Box box = player.getBoundingBox().offset(0, 0.05, 0).expand(0.05);
		
		if(!MC.world.isSpaceEmpty(player, box.offset(0, 1, 0)))
			return;
		
		double stepHeight = -1;
		
		ArrayList<Box> blockCollisions = MC.world
			.getBlockCollisions(player, box).map(VoxelShape::getBoundingBox)
			.collect(Collectors.toCollection(ArrayList::new));
		
		for(Box bb : blockCollisions)
			if(bb.maxY > stepHeight)
				stepHeight = bb.maxY;
			
		stepHeight = stepHeight - player.getY();
		
		if(stepHeight < 0 || stepHeight > 1)
			return;
		
		ClientPlayNetworkHandler netHandler = player.networkHandler;
		
		netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
			player.getX(), player.getY() + 0.42 * stepHeight, player.getZ(),
			player.isOnGround()));
		
		netHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
			player.getX(), player.getY() + 0.753 * stepHeight, player.getZ(),
			player.isOnGround()));
		
		player.setPosition(player.getX(), player.getY() + 1 * stepHeight,
			player.getZ());
	}
	
	public boolean isAutoJumpAllowed()
	{
		return !isEnabled() && !WURST.getCmds().goToCmd.isActive();
	}
	
	private enum Mode
	{
		SIMPLE("简单"),
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
}
