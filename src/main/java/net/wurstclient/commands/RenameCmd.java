/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;

public final class RenameCmd extends Command
{
	public RenameCmd()
	{
		super("rename", "重命名你手中的物品.", ".rename <new_name>",
			"Use $ for colors, use $$ for $.", "Example:", ".rename $cRed Name",
			"(changes the item's name to \u00a7cRed Name\u00a7r)");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(!MC.player.getAbilities().creativeMode)
			throw new CmdError("仅创造模式");
		
		if(args.length == 0)
			throw new CmdSyntaxError();
		
		String message = args[0];
		for(int i = 1; i < args.length; i++)
			message += " " + args[i];
		
		message = message.replace("$", "\u00a7").replace("\u00a7\u00a7", "$");
		ItemStack stack = MC.player.getInventory().getSelectedStack();
		
		if(stack == null)
			throw new CmdError("你手上没有物品.");
		
		stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(message));
		ChatUtils.message("Renamed item to \"\u00a7o" + message + "\u00a7r\".");
	}
}
