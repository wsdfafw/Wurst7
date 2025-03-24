/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.util;

import java.util.stream.Stream;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.wurstclient.Feature;
import net.wurstclient.WurstClient;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.settings.Setting;

public enum CmdUtils
{
	;
	
	private static final MinecraftClient MC = WurstClient.MC;
	
	public static Feature findFeature(String name) throws CmdError
	{
		Stream<Feature> stream =
			WurstClient.INSTANCE.getNavigator().getList().stream();
		stream = stream.filter(f -> name.equalsIgnoreCase(f.getName()));
		Feature feature = stream.findFirst().orElse(null);
		
		if(feature == null)
			throw new CmdError("一个功能名为 \"" + name + "\" 无法被找到.");
		
		return feature;
	}
	
	public static Setting findSetting(Feature feature, String name)
		throws CmdError
	{
		name = name.replace("_", " ").toLowerCase();
		Setting setting = feature.getSettings().get(name);
		
		if(setting == null)
			throw new CmdError(
				"一个设置名为 \"" + name + "\" 无法被找到在" + feature.getName() + ".");
		
		return setting;
	}
	
	public static Item parseItem(String nameOrId) throws CmdSyntaxError
	{
		Item item = ItemUtils.getItemFromNameOrID(nameOrId);
		
		if(item == null)
			throw new CmdSyntaxError(
				"\"" + nameOrId + "\" is not a valid item.");
		
		return item;
	}
	
	public static void giveItem(ItemStack stack) throws CmdError
	{
		PlayerInventory inventory = MC.player.getInventory();
		int slot = inventory.getEmptySlot();
		if(slot < 0)
			throw new CmdError("Cannot give item. Your inventory is full.");
		
		inventory.setStack(slot, stack);
		CreativeInventoryActionC2SPacket packet =
			new CreativeInventoryActionC2SPacket(
				InventoryUtils.toNetworkSlot(slot), stack);
		MC.player.networkHandler.sendPacket(packet);
	}
}
