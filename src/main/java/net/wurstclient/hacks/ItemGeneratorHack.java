/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.Optional;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.ChatUtils;

@SearchTags({"item generator", "drop infinite"})
public final class ItemGeneratorHack extends Hack implements UpdateListener
{
	private final SliderSetting speed =
		new SliderSetting("速度", "§4§l警告:§r 更高的速度意味着会无响应或\n生成大量的垃圾文件并使你的游戏崩溃!",
			1.0, 1.0, 36.0, 1.0, SliderSetting.ValueDisplay.INTEGER);
	private final SliderSetting stackSize =
		new SliderSetting("物品堆栈", "生成一个物品要有多少要堆在一起.\n似乎不会影响性能.", 1.0, 1.0, 64.0,
			1.0, SliderSetting.ValueDisplay.INTEGER);
	
	private final Random random = Random.createLocal();
	
	public ItemGeneratorHack()
	{
		super("造垃圾");
		
		setCategory(Category.ITEMS);
		addSetting(speed);
		addSetting(stackSize);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		
		if(!MC.player.getAbilities().creativeMode)
		{
			ChatUtils.error("仅限创造模式");
			setEnabled(false);
		}
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		int stacks = speed.getValueI();
		for(int i = 9; i < 9 + stacks; i++)
		{
			// Not sure if it's possible to get an empty optional here,
			// but if so it will just retry.
			Optional<RegistryEntry.Reference<Item>> optional = Optional.empty();
			while(optional.isEmpty())
				optional = Registries.ITEM.getRandom(random);
			
			Item item = optional.get().value();
			ItemStack stack = new ItemStack(item, stackSize.getValueI());
			
			CreativeInventoryActionC2SPacket packet =
				new CreativeInventoryActionC2SPacket(i, stack);
			
			MC.player.networkHandler.sendPacket(packet);
		}
		
		for(int i = 9; i < 9 + stacks; i++)
			IMC.getInteractionManager().windowClick_THROW(i);
	}
}
