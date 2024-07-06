/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.InventoryUtils;

@SearchTags({"auto leave", "AutoDisconnect", "auto disconnect", "AutoQuit",
	"auto quit"})
public final class AutoLeaveHack extends Hack implements UpdateListener
{
	private final SliderSetting health =
		new SliderSetting("生命值", "自动离开服务器当你的\n生命低于这个数值的时候.", 4, 0.5, 9.5, 0.5,
			ValueDisplay.DECIMAL.withSuffix(" 生命值"));
	
	public final EnumSetting<Mode> mode = new EnumSetting<>("Mode",
		"§l退出§r 模式就和离开服务器一样.\n绕过反作弊检测但无战斗记录.\n\n§l字符§r 模式则发送一些特殊的符号到聊天栏\n导致服务器会将你踢出.\n绕过反作弊和一些版本的战斗记录.\n\n§lTP§r 模式将传送你到一个无效的区域,\n导致服务器将你踢出.\n绕过战斗记录, 但不绕反作弊.\n\n§l自伤§r 模式发送一个攻击包到\n其他玩家但你即是目标也是攻击者\n这会导致将你踢出.\n绕过战斗日志和反作弊",
		Mode.values(), Mode.QUIT);
	
	private final CheckboxSetting disableAutoReconnect = new CheckboxSetting(
		"Disable AutoReconnect", "Automatically turns off AutoReconnect when"
			+ " AutoLeave makes you leave the server.",
		true);
	
	private final SliderSetting totems = new SliderSetting("Totems",
		"Won't leave the server until the number of totems you have reaches"
			+ " this value or falls below it.\n\n"
			+ "11 = always able to leave",
		11, 0, 11, 1, ValueDisplay.INTEGER.withSuffix(" totems")
			.withLabel(1, "1 totem").withLabel(11, "ignore"));
	
	public AutoLeaveHack()
	{
		super("自动离开");
		setCategory(Category.COMBAT);
		addSetting(health);
		addSetting(mode);
		addSetting(disableAutoReconnect);
		addSetting(totems);
	}
	
	@Override
	public String getRenderName()
	{
		if(MC.player.getAbilities().creativeMode)
			return getName() + " (paused)";
		
		return getName() + " [" + mode.getSelected() + "]";
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// check gamemode
		if(MC.player.getAbilities().creativeMode)
			return;
		
		// check health
		float currentHealth = MC.player.getHealth();
		if(currentHealth <= 0F || currentHealth > health.getValueF() * 2F)
			return;
		
		// check totems
		if(totems.getValueI() < 11 && InventoryUtils
			.count(Items.TOTEM_OF_UNDYING, 40, true) > totems.getValueI())
			return;
		
		// leave server
		mode.getSelected().leave.run();
		
		// disable
		setEnabled(false);
		
		if(disableAutoReconnect.isChecked())
			WURST.getHax().autoReconnectHack.setEnabled(false);
	}
	
	public static enum Mode
	{
		QUIT("Quit", () -> MC.world.disconnect()),
		
		CHARS("Chars", () -> MC.getNetworkHandler().sendChatMessage("\u00a7")),
		
		SELFHURT("SelfHurt",
			() -> MC.getNetworkHandler()
				.sendPacket(PlayerInteractEntityC2SPacket.attack(MC.player,
					MC.player.isSneaking())));
		
		private final String name;
		private final Runnable leave;
		
		private Mode(String name, Runnable leave)
		{
			this.name = name;
			this.leave = leave;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
