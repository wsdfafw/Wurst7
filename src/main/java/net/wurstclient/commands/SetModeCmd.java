/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import net.wurstclient.DontBlock;
import net.wurstclient.Feature;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.CmdUtils;

@DontBlock
public final class SetModeCmd extends Command
{
	public SetModeCmd()
	{
		super("setmode",
			"改变 mode 设置的功能. 可以让你\n切换 modes 通过键位.", ".setmode <功能> <设置> <模式>", ".setmode <功能> <设置> (prev(上一个)|next(下一个))");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length != 3)
			throw new CmdSyntaxError();
		
		Feature feature = CmdUtils.findFeature(args[0]);
		Setting setting = CmdUtils.findSetting(feature, args[1]);
		EnumSetting<?> enumSetting = getAsEnumSetting(feature, setting);
		setMode(feature, enumSetting, args[2]);
	}
	
	private EnumSetting<?> getAsEnumSetting(Feature feature, Setting setting)
		throws CmdError
	{
		if(!(setting instanceof EnumSetting<?>))
			throw new CmdError(feature.getName() + " " + setting.getName()
				+ " 不是一个有效的mode设置.");
		
		return (EnumSetting<?>)setting;
	}
	
	private void setMode(Feature feature, EnumSetting<?> setting, String mode)
		throws CmdError
	{
		mode = mode.replace("_", " ").toLowerCase();
		
		switch(mode)
		{
			case "prev":
			setting.selectPrev();
			break;
			
			case "next":
			setting.selectNext();
			break;
			
			default:
			boolean successful = setting.setSelected(mode);
			if(!successful)
				throw new CmdError(
					"一个mode名为 '" + mode + "' 在 " + feature.getName() + " " + setting.getName() + " 无法被寻找到.");
			break;
		}
	}
}
