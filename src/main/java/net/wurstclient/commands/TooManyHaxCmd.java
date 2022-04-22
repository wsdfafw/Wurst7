/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.wurstclient.DontBlock;
import net.wurstclient.Feature;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.TooManyHaxHack;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.MathUtils;
import net.wurstclient.util.json.JsonException;

@DontBlock
public final class TooManyHaxCmd extends Command
{
	public TooManyHaxCmd()
	{
		super("toomanyhax",
			"当TooManyHax被启用时，\n一些功能会被暂时性地禁用,\n你可以在此处设置将要禁用的功能", ".toomanyhax block <功能名>\n禁用某功能", ".toomanyhax unblock <功能名>\n取消禁用某功能", ".toomanyhax block-all\n禁用所有功能", ".toomanyhax unblock-all\n取消禁用所有功能", ".toomanyhax list [<页数>]\n显示禁用列表", ".toomanyhax load-profile <预设名>\n加载预设", ".toomanyhax save-profile <预设名>\n设置预设", ".toomanyhax list-profiles [<页数>]\n预设列表", "预设储存路径'.minecraft/wurst/toomanyhax'.");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length < 1)
			throw new CmdSyntaxError();
		
		switch(args[0].toLowerCase())
		{
			case "block":
			block(args);
			break;
			
			case "unblock":
			unblock(args);
			break;
			
			case "block-all":
			blockAll();
			break;
			
			case "unblock-all":
			unblockAll();
			break;
			
			case "list":
			list(args);
			break;
			
			case "load-profile":
			loadProfile(args);
			break;
			
			case "save-profile":
			saveProfile(args);
			break;
			
			case "list-profiles":
			listProfiles(args);
			break;
			
			default:
			throw new CmdSyntaxError();
		}
	}
	
	private void block(String[] args) throws CmdException
	{
		if(args.length != 2)
			throw new CmdSyntaxError();
		
		String name = args[1];
		Feature feature = parseFeature(name);
		String typeAndName = getType(feature) + " '" + name + "'";
		
		if(!feature.isSafeToBlock())
			throw new CmdError("那个 " + typeAndName + " 要被屏蔽并不安全.");
		
		TooManyHaxHack tooManyHax = WURST.getHax().tooManyHaxHack;
		if(tooManyHax.isBlocked(feature))
		{
			ChatUtils.error("那个 " + typeAndName + " 已经被屏蔽了.");
			
			if(!tooManyHax.isEnabled())
				ChatUtils.message("开启 TooManyHax 来看看效果怎么样.");
			
			return;
		}
		
		tooManyHax.setBlocked(feature, true);
		ChatUtils.message("增加 " + typeAndName + " 到 TooManyHax 列表中.");
	}
	
	private void unblock(String[] args) throws CmdException
	{
		if(args.length != 2)
			throw new CmdSyntaxError();
		
		String name = args[1];
		Feature feature = parseFeature(name);
		String typeAndName = getType(feature) + " '" + name + "'";
		
		TooManyHaxHack tooManyHax = WURST.getHax().tooManyHaxHack;
		if(!tooManyHax.isBlocked(feature))
			throw new CmdError("那个 " + typeAndName + " 并未被屏蔽.");
		
		tooManyHax.setBlocked(feature, false);
		ChatUtils.message("移除 " + typeAndName + " 从 TooManyHax 列表中.");
	}
	
	private void blockAll()
	{
		WURST.getHax().tooManyHaxHack.blockAll();
		ChatUtils.message("所有功能都被屏蔽了.");
		ChatUtils
			.message("*注意: 一些特殊的功能作用无法被屏蔽因为");
		ChatUtils.message("他们需要驱动Wurst工作.");
	}
	
	private void unblockAll()
	{
		WURST.getHax().tooManyHaxHack.unblockAll();
		ChatUtils.message("所有功能被解除屏蔽了.");
	}
	
	private Feature parseFeature(String name) throws CmdSyntaxError
	{
		Feature feature = WURST.getFeatureByName(name);
		if(feature == null)
			throw new CmdSyntaxError(
				"一个功能名为 '" + name + "' 无法被找到");
		
		return feature;
	}
	
	private String getType(Feature feature)
	{
		if(feature instanceof Hack)
			return "hack";
		
		if(feature instanceof Command)
			return "command";
		
		if(feature instanceof OtherFeature)
			return "feature";
		
		throw new IllegalStateException();
	}
	
	private void list(String[] args) throws CmdException
	{
		if(args.length > 2)
			throw new CmdSyntaxError();
		
		TooManyHaxHack tooManyHax = WURST.getHax().tooManyHaxHack;
		List<Feature> blocked = tooManyHax.getBlockedFeatures();
		int page = parsePage(args);
		int pages = (int)Math.ceil(blocked.size() / 8.0);
		pages = Math.max(pages, 1);
		
		if(page > pages || page < 1)
			throw new CmdSyntaxError("无效页码: " + page);
		
		String total = "合计: " + blocked.size() + " 功能被屏蔽";
		total += blocked.size() != 1 ? "s" : "";
		ChatUtils.message(total);
		
		int start = (page - 1) * 8;
		int end = Math.min(page * 8, blocked.size());
		
		ChatUtils.message("TooManyHax 列表 (页码 " + page + "/" + pages + ")");
		for(int i = start; i < end; i++)
			ChatUtils.message(blocked.get(i).getName());
	}
	
	private int parsePage(String[] args) throws CmdSyntaxError
	{
		if(args.length < 2)
			return 1;
		
		if(!MathUtils.isInteger(args[1]))
			throw new CmdSyntaxError("不是一个数字: " + args[1]);
		
		return Integer.parseInt(args[1]);
	}
	
	private void loadProfile(String[] args) throws CmdException
	{
		if(args.length != 2)
			throw new CmdSyntaxError();
		
		String name = parseFileName(args[1]);
		
		try
		{
			WURST.getHax().tooManyHaxHack.loadProfile(name);
			ChatUtils.message("TooManyHax 档案载入: " + name);
			
		}catch(NoSuchFileException e)
		{
			throw new CmdError("档案 '" + name + "' 并不存在.");
			
		}catch(JsonException e)
		{
			e.printStackTrace();
			throw new CmdError(
				"档案 '" + name + "' 是损坏的: " + e.getMessage());
			
		}catch(IOException e)
		{
			e.printStackTrace();
			throw new CmdError("无法载入档案: " + e.getMessage());
		}
	}
	
	private void saveProfile(String[] args) throws CmdException
	{
		if(args.length != 2)
			throw new CmdSyntaxError();
		
		String name = parseFileName(args[1]);
		
		try
		{
			WURST.getHax().tooManyHaxHack.saveProfile(name);
			ChatUtils.message("TooManyHax 档案保存: " + name);
			
		}catch(IOException | JsonException e)
		{
			e.printStackTrace();
			throw new CmdError("无法保存档案: " + e.getMessage());
		}
	}
	
	private String parseFileName(String input)
	{
		String fileName = input;
		if(!fileName.endsWith(".json"))
			fileName += ".json";
		
		return fileName;
	}
	
	private void listProfiles(String[] args) throws CmdException
	{
		if(args.length > 2)
			throw new CmdSyntaxError();
		
		ArrayList<Path> files = WURST.getKeybinds().listProfiles();
		int page = parsePage(args);
		int pages = (int)Math.ceil(files.size() / 8.0);
		pages = Math.max(pages, 1);
		
		if(page > pages || page < 1)
			throw new CmdSyntaxError("无效页码: " + page);
		
		String total = "合计: " + files.size() + " 档案";
		total += files.size() != 1 ? "s" : "";
		ChatUtils.message(total);
		
		int start = (page - 1) * 8;
		int end = Math.min(page * 8, files.size());
		
		ChatUtils.message(
			"TooManyHax 档案列表 (页码 " + page + "/" + pages + ")");
		for(int i = start; i < end; i++)
			ChatUtils.message(files.get(i).getFileName().toString());
	}
}
