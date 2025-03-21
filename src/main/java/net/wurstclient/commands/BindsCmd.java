/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
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
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.util.InputUtil;
import net.wurstclient.DontBlock;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.keybinds.Keybind;
import net.wurstclient.keybinds.KeybindList;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.MathUtils;
import net.wurstclient.util.json.JsonException;

@DontBlock
public final class BindsCmd extends Command
{
	public BindsCmd()
	{
		super("binds", "可以让你管理绑定的键位在聊天栏处", ".binds add <按键名> <功能名>(增加功能快捷键)",
			".binds add <按键名> <指令>(增加指令快捷键)",
			".binds remove <按键>(删除与某个键相关的快捷键)", ".binds list [<页数>](现用的快捷键的键位)",
			".binds load-profile <预设名>(加载现有的某一个预设)",
			".binds save-profile <预设名>(将现用的快捷键设置另存为预设)",
			".binds list-profiles [<页数>](查看现有的预设)",
			".binds remove-all(删除所有快捷键)", ".binds reset(恢复默认快捷键)",
			"需要设置多个[功能/指令]时,\n用 ';'符号分隔,",
			"预设保存路径'.minecraft/wurst/keybinds'.");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length < 1)
			throw new CmdSyntaxError();
		
		switch(args[0].toLowerCase())
		{
			case "add":
			add(args);
			break;
			
			case "remove":
			remove(args);
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
			
			case "remove-all":
			removeAll();
			break;
			
			case "reset":
			reset();
			break;
			
			default:
			throw new CmdSyntaxError();
		}
	}
	
	private void add(String[] args) throws CmdException
	{
		if(args.length < 3)
			throw new CmdSyntaxError();
		
		String displayKey = args[1];
		String key = parseKey(displayKey);
		String[] cmdArgs = Arrays.copyOfRange(args, 2, args.length);
		String commands = String.join(" ", cmdArgs);
		
		WURST.getKeybinds().add(key, commands);
		ChatUtils.message("绑定设置为: " + displayKey + " -> " + commands);
	}
	
	private void remove(String[] args) throws CmdException
	{
		if(args.length != 2)
			throw new CmdSyntaxError();
		
		String displayKey = args[1];
		String key = parseKey(displayKey);
		
		String commands = WURST.getKeybinds().getCommands(key);
		if(commands == null)
			throw new CmdError("没有什么可以移除的.");
		
		WURST.getKeybinds().remove(key);
		ChatUtils.message("按键移除: " + displayKey + " -> " + commands);
	}
	
	private String parseKey(String displayKey) throws CmdSyntaxError
	{
		String key = displayKey.toLowerCase();
		
		String prefix = "key.keyboard.";
		if(!key.startsWith(prefix))
			key = prefix + key;
		
		try
		{
			InputUtil.fromTranslationKey(key);
			return key;
			
		}catch(IllegalArgumentException e)
		{
			throw new CmdSyntaxError("未知的按键: " + displayKey);
		}
	}
	
	private void list(String[] args) throws CmdException
	{
		if(args.length > 2)
			throw new CmdSyntaxError();
		
		List<Keybind> binds = WURST.getKeybinds().getAllKeybinds();
		int page = parsePage(args);
		int pages = (int)Math.ceil(binds.size() / 8.0);
		pages = Math.max(pages, 1);
		
		if(page > pages || page < 1)
			throw new CmdSyntaxError("无效的页码: " + page);
		
		String total = "合计: " + binds.size() + " 绑定键位";
		total += binds.size() != 1 ? "s" : "";
		ChatUtils.message(total);
		
		int start = (page - 1) * 8;
		int end = Math.min(page * 8, binds.size());
		
		ChatUtils.message("键位绑定列表 (页数 " + page + "/" + pages + ")");
		for(int i = start; i < end; i++)
			ChatUtils.message(binds.get(i).toString());
	}
	
	private int parsePage(String[] args) throws CmdSyntaxError
	{
		if(args.length < 2)
			return 1;
		
		if(!MathUtils.isInteger(args[1]))
			throw new CmdSyntaxError("不是一个数字: " + args[1]);
		
		return Integer.parseInt(args[1]);
	}
	
	private void removeAll()
	{
		WURST.getKeybinds().removeAll();
		ChatUtils.message("所有键位绑定已经移除.");
	}
	
	private void reset()
	{
		WURST.getKeybinds().setKeybinds(KeybindList.DEFAULT_KEYBINDS);
		ChatUtils.message("所有键位绑定已重设为默认值.");
	}
	
	private void loadProfile(String[] args) throws CmdException
	{
		if(args.length != 2)
			throw new CmdSyntaxError();
		
		String name = parseFileName(args[1]);
		
		try
		{
			WURST.getKeybinds().loadProfile(name);
			ChatUtils.message("键位绑定载入: " + name);
			
		}catch(NoSuchFileException e)
		{
			throw new CmdError("档案 '" + name + "' 不存在.");
			
		}catch(JsonException e)
		{
			e.printStackTrace();
			throw new CmdError("档案 '" + name + "' 是不正确的: " + e.getMessage());
			
		}catch(IOException e)
		{
			e.printStackTrace();
			throw new CmdError("无法载入: " + e.getMessage());
		}
	}
	
	private void saveProfile(String[] args) throws CmdException
	{
		if(args.length != 2)
			throw new CmdSyntaxError();
		
		String name = parseFileName(args[1]);
		
		try
		{
			WURST.getKeybinds().saveProfile(name);
			ChatUtils.message("按键保存: " + name);
			
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
			throw new CmdSyntaxError("无效的页码: " + page);
		
		String total = "合计: " + files.size() + " 档案";
		total += files.size() != 1 ? "s" : "";
		ChatUtils.message(total);
		
		int start = (page - 1) * 8;
		int end = Math.min(page * 8, files.size());
		
		ChatUtils.message("键位档案列表 (页数 " + page + "/" + pages + ")");
		for(int i = start; i < end; i++)
			ChatUtils.message(files.get(i).getFileName().toString());
	}
}
