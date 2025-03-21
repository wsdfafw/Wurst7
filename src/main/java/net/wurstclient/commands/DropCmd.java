/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.events.UpdateListener;

public final class DropCmd extends Command implements UpdateListener
{
	private int slowModeTimer;
	private int slowModeSlotCounter;
	
	public DropCmd()
	{
		super("drop", "把你的所有物品丢到地上.", ".drop", "低速模式: .drop slow",
			"使用常规的 .drop 可能会被踢出服务器,", "使用低速模式.");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length > 1)
			throw new CmdSyntaxError();
		
		if(args.length < 1)
		{
			dropAllItems();
			return;
		}
		
		if(!args[0].equalsIgnoreCase("slow"))
			throw new CmdSyntaxError();
		
		slowModeTimer = 5;
		slowModeSlotCounter = 9;
		EVENTS.add(UpdateListener.class, this);
	}
	
	private void dropAllItems()
	{
		for(int i = 9; i < 45; i++)
			IMC.getInteractionManager().windowClick_THROW(i);
	}
	
	@Override
	public void onUpdate()
	{
		slowModeTimer--;
		if(slowModeTimer > 0)
			return;
		
		skipEmptySlots();
		IMC.getInteractionManager().windowClick_THROW(slowModeSlotCounter);
		
		slowModeSlotCounter++;
		slowModeTimer = 5;
		
		if(slowModeSlotCounter >= 45)
			EVENTS.remove(UpdateListener.class, this);
	}
	
	private void skipEmptySlots()
	{
		while(slowModeSlotCounter < 45)
		{
			int adjustedSlot = slowModeSlotCounter;
			if(adjustedSlot >= 36)
				adjustedSlot -= 36;
			
			if(!MC.player.getInventory().getStack(adjustedSlot).isEmpty())
				break;
			
			slowModeSlotCounter++;
		}
	}
}
