/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import java.util.Comparator;
import java.util.stream.StreamSupport;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.FakePlayerEntity;
import net.wurstclient.util.MathUtils;

public final class TpCmd extends Command
{
	private final CheckboxSetting disableFreecam =
		new CheckboxSetting("Disable Freecam",
			"Disables Freecam just before teleporting.\n\n"
				+ "This allows you to teleport your actual character to your"
				+ " Freecam position by typing \".tp ~ ~ ~\" while Freecam is"
				+ " enabled.",
			true);
	
	public TpCmd()
	{
		super("tp", "将你传送至10个方块以外", ".tp <x> <y> <z>", ".tp <entity>");
		addSetting(disableFreecam);
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		BlockPos pos = argsToPos(args);
		
		if(disableFreecam.isChecked() && WURST.getHax().freecamHack.isEnabled())
			WURST.getHax().freecamHack.setEnabled(false);
		
		MC.player.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
	}
	
	private BlockPos argsToPos(String... args) throws CmdException
	{
		switch(args.length)
		{
			default:
			throw new CmdSyntaxError("无效的坐标");
			
			case 1:
			return argsToEntityPos(args[0]);
			
			case 3:
			return argsToXyzPos(args);
		}
	}
	
	private BlockPos argsToEntityPos(String name) throws CmdError
	{
		LivingEntity entity = StreamSupport
			.stream(MC.world.getEntities().spliterator(), true)
			.filter(LivingEntity.class::isInstance).map(e -> (LivingEntity)e)
			.filter(e -> !e.isRemoved() && e.getHealth() > 0)
			.filter(e -> e != MC.player)
			.filter(e -> !(e instanceof FakePlayerEntity))
			.filter(e -> name.equalsIgnoreCase(e.getDisplayName().getString()))
			.min(
				Comparator.comparingDouble(e -> MC.player.squaredDistanceTo(e)))
			.orElse(null);
		
		if(entity == null)
			throw new CmdError("实体 \"" + name + "\" 无法被找到.");
		
		return BlockPos.ofFloored(entity.getPos());
	}
	
	private BlockPos argsToXyzPos(String... xyz) throws CmdSyntaxError
	{
		BlockPos playerPos = BlockPos.ofFloored(MC.player.getPos());
		int[] player = {playerPos.getX(), playerPos.getY(), playerPos.getZ()};
		int[] pos = new int[3];
		
		for(int i = 0; i < 3; i++)
			if(MathUtils.isInteger(xyz[i]))
				pos[i] = Integer.parseInt(xyz[i]);
			else if(xyz[i].equals("~"))
				pos[i] = player[i];
			else if(xyz[i].startsWith("~")
				&& MathUtils.isInteger(xyz[i].substring(1)))
				pos[i] = player[i] + Integer.parseInt(xyz[i].substring(1));
			else
				throw new CmdSyntaxError("无效坐标");
			
		return new BlockPos(pos[0], pos[1], pos[2]);
	}
}
