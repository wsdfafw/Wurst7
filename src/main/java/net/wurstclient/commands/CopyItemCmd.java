/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.CmdUtils;

public final class CopyItemCmd extends Command
{
	public CopyItemCmd()
	{
		super("copyitem", "复制其他人持有或穿着的物品\n仅创造模式", ".copyitem <玩家> <物品槽>",
			"有效插槽: hand(手), head(头部), chest(胸部), legs(腿部), feet(足部)");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length != 2)
			throw new CmdSyntaxError();
		
		if(!MC.player.getAbilities().creativeMode)
			throw new CmdError("仅限创造模式");
		
		AbstractClientPlayerEntity player = getPlayer(args[0]);
		ItemStack item = getItem(player, args[1]);
		CmdUtils.giveItem(item);
		
		ChatUtils.message("复制成功");
	}
	
	private AbstractClientPlayerEntity getPlayer(String name) throws CmdError
	{
		for(AbstractClientPlayerEntity player : MC.world.getPlayers())
		{
			if(!player.getName().getString().equalsIgnoreCase(name))
				continue;
			
			return player;
		}
		
		throw new CmdError("Player \"" + name + "\" could not be found.");
	}
	
	private ItemStack getItem(AbstractClientPlayerEntity player, String slot)
		throws CmdSyntaxError
	{
		switch(slot.toLowerCase())
		{
			case "hand":
			return player.getInventory().getMainHandStack();
			
			case "head":
			return player.getInventory().getArmorStack(3);
			
			case "chest":
			return player.getInventory().getArmorStack(2);
			
			case "legs":
			return player.getInventory().getArmorStack(1);
			
			case "feet":
			return player.getInventory().getArmorStack(0);
			
			default:
			throw new CmdSyntaxError();
		}
	}
}
