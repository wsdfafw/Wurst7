/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;
import net.wurstclient.DontBlock;
import net.wurstclient.Feature;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.settings.BlockSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.CmdUtils;
import net.wurstclient.util.MathUtils;

@DontBlock
public final class SetBlockCmd extends Command
{
	public SetBlockCmd()
	{
		super("setblock",
			"更改功能的块设置. 允许您通过键绑定更改这些设置.",
			".setblock <feature> <setting> <block>",
			".setblock <feature> <setting> reset",
			"例子: .setblock Nuker ID dirt");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length != 3)
			throw new CmdSyntaxError();
		
		Feature feature = CmdUtils.findFeature(args[0]);
		Setting setting = CmdUtils.findSetting(feature, args[1]);
		BlockSetting blockSetting = getAsBlockSetting(feature, setting);
		setBlock(blockSetting, args[2]);
	}
	
	private BlockSetting getAsBlockSetting(Feature feature, Setting setting)
		throws CmdError
	{
		if(!(setting instanceof BlockSetting))
			throw new CmdError(feature.getName() + " " + setting.getName()
				+ " 不是块设置.");
		
		return (BlockSetting)setting;
	}
	
	private void setBlock(BlockSetting setting, String value)
		throws CmdSyntaxError
	{
		if(value.toLowerCase().equals("重启"))
		{
			setting.resetToDefault();
			return;
		}
		
		Block block = getBlockFromNameOrID(value);
		if(block == null)
			throw new CmdSyntaxError("\"" + value + "\" 不是有效块.");
		
		setting.setBlock(block);
	}
	
	private Block getBlockFromNameOrID(String nameOrId)
	{
		if(MathUtils.isInteger(nameOrId))
		{
			BlockState state = Block.STATE_IDS.get(Integer.parseInt(nameOrId));
			if(state == null)
				return null;
			
			return state.getBlock();
		}
		
		try
		{
			return Registry.BLOCK.getOrEmpty(new Identifier(nameOrId))
				.orElse(null);
			
		}catch(InvalidIdentifierException e)
		{
			return null;
		}
	}
}
