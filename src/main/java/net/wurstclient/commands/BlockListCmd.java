/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import java.util.List;

import net.minecraft.block.Block;
import net.wurstclient.DontBlock;
import net.wurstclient.Feature;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.settings.BlockListSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.CmdUtils;
import net.wurstclient.util.MathUtils;

@DontBlock
public final class BlockListCmd extends Command
{
	public BlockListCmd()
	{
		super("blocklist", "更改功能的阻止列表设置. 允许您通过键绑定更改这些设置.",
			".blocklist <feature> <setting> add <block>",
			".blocklist <feature> <setting> remove <block>",
			".blocklist <feature> <setting> list [<page>]",
			".blocklist <feature> <setting> reset",
			"例子: .blocklist Nuker MultiID_List add gravel");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length < 3 || args.length > 4)
			throw new CmdSyntaxError();
		
		Feature feature = CmdUtils.findFeature(args[0]);
		Setting abstractSetting = CmdUtils.findSetting(feature, args[1]);
		BlockListSetting setting =
			getAsBlockListSetting(feature, abstractSetting);
		
		switch(args[2].toLowerCase())
		{
			case "add":
			add(feature, setting, args);
			break;
			
			case "remove":
			remove(feature, setting, args);
			break;
			
			case "list":
			list(feature, setting, args);
			break;
			
			case "reset":
			setting.resetToDefaults();
			break;
			
			default:
			throw new CmdSyntaxError();
		}
	}
	
	private void add(Feature feature, BlockListSetting setting, String[] args)
		throws CmdException
	{
		if(args.length != 4)
			throw new CmdSyntaxError();
		
		String inputBlockName = args[3];
		Block block = BlockUtils.getBlockFromNameOrID(inputBlockName);
		if(block == null)
			throw new CmdSyntaxError("\"" + inputBlockName + "\" 不是一个有效的块.");
		
		String blockName = BlockUtils.getName(block);
		if(setting.contains(blockName))
			throw new CmdError(feature.getName() + " " + setting.getName()
				+ " 已经包含 " + blockName);
		
		setting.add(block);
	}
	
	private void remove(Feature feature, BlockListSetting setting,
		String[] args) throws CmdException
	{
		if(args.length != 4)
			throw new CmdSyntaxError();
		
		String inputBlockName = args[3];
		Block block = BlockUtils.getBlockFromNameOrID(inputBlockName);
		if(block == null)
			throw new CmdSyntaxError("\"" + inputBlockName + "\" 不是一个有效的块.");
		
		String blockName = BlockUtils.getName(block);
		int index = setting.indexOf(blockName);
		if(index < 0)
			throw new CmdError(feature.getName() + " " + setting.getName()
				+ " 不含 " + blockName);
		
		setting.remove(index);
	}
	
	private void list(Feature feature, BlockListSetting setting, String[] args)
		throws CmdException
	{
		if(args.length > 4)
			throw new CmdSyntaxError();
		
		List<String> blocks = setting.getBlockNames();
		int page = parsePage(args);
		int pages = (int)Math.ceil(blocks.size() / 8.0);
		pages = Math.max(pages, 1);
		
		if(page > pages || page < 1)
			throw new CmdSyntaxError("无效页数: " + page);
		
		String total = "总计：" + blocks.size() + " 块";
		total += blocks.size() != 1 ? "s" : "";
		ChatUtils.message(total);
		
		int start = (page - 1) * 8;
		int end = Math.min(page * 8, blocks.size());
		
		ChatUtils.message(feature.getName() + " " + setting.getName() + " (页 "
			+ page + "/" + pages + ")");
		for(int i = start; i < end; i++)
			ChatUtils.message(blocks.get(i).toString());
	}
	
	private int parsePage(String[] args) throws CmdSyntaxError
	{
		if(args.length < 4)
			return 1;
		
		if(!MathUtils.isInteger(args[3]))
			throw new CmdSyntaxError("不是数字: " + args[3]);
		
		return Integer.parseInt(args[3]);
	}
	
	private BlockListSetting getAsBlockListSetting(Feature feature,
		Setting setting) throws CmdError
	{
		if(!(setting instanceof BlockListSetting))
			throw new CmdError(feature.getName() + " " + setting.getName()
				+ " 不是 BlockList 设置.");
		
		return (BlockListSetting)setting;
	}
}
