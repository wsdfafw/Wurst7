/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import net.minecraft.client.network.ServerInfo;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.LastServerRememberer;

public final class SvCmd extends Command
{
	public SvCmd()
	{
		super("sv", "显示当前连接的服务器的版本.", ".sv");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length != 0)
			throw new CmdSyntaxError();
		
		ChatUtils.message("服务器版本: " + getVersion());
	}
	
	private String getVersion() throws CmdError
	{
		if(MC.isIntegratedServerRunning())
			throw new CmdError("无法在单人服务器中检查版本.");
		
		ServerInfo lastServer = LastServerRememberer.getLastServer();
		if(lastServer == null)
			throw new IllegalStateException(
				"LastServerRememberer功能 并不记得最后一个服务器是什么!");
		
		return lastServer.version.getString();
	}
	
	@Override
	public String getPrimaryAction()
	{
		return "获得服务器版本";
	}
	
	@Override
	public void doPrimaryAction()
	{
		WURST.getCmdProcessor().process("sv");
	}
}
