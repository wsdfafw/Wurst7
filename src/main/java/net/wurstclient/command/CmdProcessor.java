/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.command;

import java.util.Arrays;

import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.wurstclient.WurstClient;
import net.wurstclient.events.ChatOutputListener;
import net.wurstclient.hacks.TooManyHaxHack;
import net.wurstclient.util.ChatUtils;

public final class CmdProcessor implements ChatOutputListener
{
	private final CmdList cmds;
	
	public CmdProcessor(CmdList cmds)
	{
		this.cmds = cmds;
	}
	
	@Override
	public void onSentMessage(ChatOutputEvent event)
	{
		if(!WurstClient.INSTANCE.isEnabled())
			return;
		
		String message = event.getOriginalMessage().trim();
		if(!message.startsWith("."))
			return;
		
		event.cancel();
		process(message.substring(1));
	}
	
	public void process(String input)
	{
		try
		{
			Command cmd = parseCmd(input);
			
			TooManyHaxHack tooManyHax =
				WurstClient.INSTANCE.getHax().tooManyHaxHack;
			if(tooManyHax.isEnabled() && tooManyHax.isBlocked(cmd))
			{
				ChatUtils.error(cmd.getName() + " 已经被 TooManyHax 功能所屏蔽.");
				return;
			}
			
			runCmd(cmd, input);
			
		}catch(CmdNotFoundException e)
		{
			e.printToChat();
		}
	}
	
	private Command parseCmd(String input) throws CmdNotFoundException
	{
		String cmdName = input.split(" ")[0];
		Command cmd = cmds.getCmdByName(cmdName);
		
		if(cmd == null)
			throw new CmdNotFoundException(input);
		
		return cmd;
	}
	
	private void runCmd(Command cmd, String input)
	{
		String[] args = input.split(" ");
		args = Arrays.copyOfRange(args, 1, args.length);
		
		try
		{
			cmd.call(args);
			
		}catch(CmdException e)
		{
			e.printToChat(cmd);
			
		}catch(Throwable e)
		{
			CrashReport report = CrashReport.create(e, "运行 Wurst 命令");
			CrashReportSection section = report.addElement("影响的命令");
			section.add("命令输入", () -> input);
			throw new CrashException(report);
		}
	}
	
	private static class CmdNotFoundException extends Exception
	{
		private final String input;
		
		public CmdNotFoundException(String input)
		{
			this.input = input;
		}
		
		public void printToChat()
		{
			String cmdName = input.split(" ")[0];
			ChatUtils.error("未知的命令: ." + cmdName);
			
			StringBuilder helpMsg = new StringBuilder();
			
			if(input.startsWith("/"))
			{
				helpMsg.append("使用格式 \".say " + input + "\"");
				helpMsg.append(" 作为一条聊天发出.");
				
			}else
			{
				helpMsg.append("输入 \".help\" 获得一堆命令或者 ");
				helpMsg.append("\".say ." + input + "\"");
				helpMsg.append(" 作为一条聊天发出.");
			}
			
			ChatUtils.message(helpMsg.toString());
		}
	}
}
