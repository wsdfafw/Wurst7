/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.util.hit.HitResult;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.LeftClickListener;
import net.wurstclient.hack.Hack;

@SearchTags({"mace dmg", "MaceDamage", "mace damage"})
public final class MaceDmgHack extends Hack implements LeftClickListener
{
	public MaceDmgHack()
	{
		super("MaceDMG");
		setCategory(Category.COMBAT);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(LeftClickListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(LeftClickListener.class, this);
	}
	
	@Override
	public void onLeftClick(LeftClickEvent event)
	{
		if(MC.crosshairTarget == null
			|| MC.crosshairTarget.getType() != HitResult.Type.ENTITY)
			return;
		
		if(!MC.player.getMainHandStack().isOf(Items.MACE))
			return;
			
		// See ServerPlayNetworkHandler.onPlayerMove()
		// for why it's using these numbers.
		// Also, let me know if you find a way to bypass that check in 1.21.
		for(int i = 0; i < 4; i++)
			sendFakeY(0);
		sendFakeY(Math.sqrt(500));
		sendFakeY(0);
	}
	
	private void sendFakeY(double offset)
	{
		ClientPlayNetworkHandler netHandler = MC.player.networkHandler;
		double posX = MC.player.getX();
		double posY = MC.player.getY();
		double posZ = MC.player.getZ();
		
		netHandler.sendPacket(
			new PositionAndOnGround(posX, posY + offset, posZ, false));
	}
}
