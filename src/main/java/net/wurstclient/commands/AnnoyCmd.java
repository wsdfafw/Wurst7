/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.network.ClientPlayerEntity;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.events.ChatInputListener;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.ChatUtils;

public final class AnnoyCmd extends Command implements ChatInputListener
{
	private final CheckboxSetting rcMode = new CheckboxSetting("RC mode",
		"远程控制模式。重新启用一个允许.annoy运行Wurst命令的错误。出于安全原因，不推荐使用，但在我们有一个适当的远程控制功能之前，这至少比没有好。",
		false);
	
	private boolean enabled;
	private String target;
	
	public AnnoyCmd()
	{
		super("annoy", "通过重复玩家说的每一句话来惹恼他们.",
			".annoy <player>", "Turn off: .annoy");
		addSetting(rcMode);
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length > 0)
		{
			if(enabled)
				disable();
			
			enable(args);
			
		}else
		{
			if(!enabled)
				throw new CmdError(".annoy 恼人功能已经关闭了.");
			
			disable();
		}
	}
	
	private void enable(String[] args) throws CmdException
	{
		if(args.length < 1)
			throw new CmdSyntaxError();
		
		target = String.join(" ", args);
		ChatUtils.message("现在正在惹恼 " + target + ".");
		
		ClientPlayerEntity player = MC.player;
		if(player != null && target.equals(player.getName().getString()))
			ChatUtils.warning("我不认为你能够惹恼你自己!");
		
		EVENTS.add(ChatInputListener.class, this);
		enabled = true;
	}
	
	private void disable() throws CmdException
	{
		EVENTS.remove(ChatInputListener.class, this);
		
		if(target != null)
		{
			ChatUtils.message("不再惹恼 " + target + ".");
			target = null;
		}
		
		enabled = false;
	}
	
	@Override
	public void onReceivedMessage(ChatInputEvent event)
	{
		String message = event.getComponent().getString();
		if(message.startsWith(ChatUtils.WURST_PREFIX))
			return;
		
		String prefix1 = target + ">";
		if(message.contains("<" + prefix1) || message.contains(prefix1))
		{
			repeat(message, prefix1);
			return;
		}
		
		String prefix2 = target + ":";
		if(message.contains("] " + prefix2) || message.contains("]" + prefix2))
			repeat(message, prefix2);
	}
	
	private void repeat(String message, String prefix)
	{
		int beginIndex = message.indexOf(prefix) + prefix.length();
		String repeated = message.substring(beginIndex).trim();
		repeated = StringUtils.normalizeSpace(repeated);
		
		if(rcMode.isChecked() && repeated.startsWith("."))
			WURST.getCmdProcessor().process(repeated.substring(1));
		else
			MC.getNetworkHandler().sendChatMessage(repeated);
	}
}
