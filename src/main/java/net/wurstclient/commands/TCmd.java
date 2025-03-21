/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import net.wurstclient.DontBlock;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.TooManyHaxHack;
import net.wurstclient.util.ChatUtils;

@DontBlock
public final class TCmd extends Command
{
	public TCmd()
	{
		super("t", "切换一个作弊功能.", ".t <作弊功能> [on(开)|off(关)]", "例子:",
			"切换 Nuker: .t Nuker", "关闭 Nuker: .t Nuker off");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length < 1 || args.length > 2)
			throw new CmdSyntaxError();
		
		Hack hack = WURST.getHax().getHackByName(args[0]);
		if(hack == null)
			throw new CmdError("未知作弊功能: " + args[0]);
		
		if(args.length == 1)
			setEnabled(hack, !hack.isEnabled());
		else
			switch(args[1].toLowerCase())
			{
				case "on":
				setEnabled(hack, true);
				break;
				
				case "off":
				setEnabled(hack, false);
				break;
				
				default:
				throw new CmdSyntaxError();
			}
	}
	
	private void setEnabled(Hack hack, boolean enabled)
	{
		TooManyHaxHack tooManyHax = WURST.getHax().tooManyHaxHack;
		if(!hack.isEnabled() && tooManyHax.isEnabled()
			&& tooManyHax.isBlocked(hack))
		{
			ChatUtils.error(hack.getName() + "被 TooManyHax 所屏蔽.");
			return;
		}
		
		hack.setEnabled(enabled);
	}
}
