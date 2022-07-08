/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"auto leave", "AutoDisconnect", "auto disconnect", "AutoQuit",
	"auto quit"})
public final class AutoLeaveHack extends Hack implements UpdateListener
{
	private final SliderSetting health = new SliderSetting("生命值",
		"自动离开服务器当你的\n生命低于这个数值的时候.",
		4, 0.5, 9.5, 0.5, ValueDisplay.DECIMAL.withSuffix(" 生命值"));
	
	public final EnumSetting<Mode> mode = new EnumSetting<>("Mode",
		"§l退出§r 模式就和离开服务器一样.\n绕过反作弊检测但无战斗记录.\n\n§l字符§r 模式则发送一些特殊的符号到聊天栏\n导致服务器会将你踢出.\n绕过反作弊和一些版本的战斗记录.\n\n§lTP§r 模式将传送你到一个无效的区域,\n导致服务器将你踢出.\n绕过战斗记录, 但不绕反作弊.\n\n§l自伤§r 模式发送一个攻击包到\n其他玩家但你即是目标也是攻击者\n这会导致将你踢出.\n绕过战斗日志和反作弊",
		Mode.values(), Mode.QUIT);
	
	public AutoLeaveHack()
	{
		super("自动离开");
		
		setCategory(Category.COMBAT);
		addSetting(health);
		addSetting(mode);
	}
	
	@Override
	public String getRenderName()
	{
		return getName() + " [" + mode.getSelected() + "]";
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// check gamemode
		if(MC.player.getAbilities().creativeMode)
			return;
		
		// check for other players
		if(MC.isInSingleplayer()
			&& MC.player.networkHandler.getPlayerList().size() == 1)
			return;
		
		// check health
		if(MC.player.getHealth() > health.getValueF() * 2F)
			return;
		
		// leave server
		switch(mode.getSelected())
		{
			case QUIT:
			MC.world.disconnect();
			break;
			
			case CHARS:
			MC.player.sendChatMessage("\u00a7");
			break;
			
			case TELEPORT:
			MC.player.networkHandler
				.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(3.1e7,
					100, 3.1e7, false));
			break;
			
			case SELFHURT:
			MC.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket
				.attack(MC.player, MC.player.isSneaking()));
			break;
		}
		
		// disable
		setEnabled(false);
	}
	
	public static enum Mode
	{
		QUIT("Quit"),
		
		CHARS("Chars"),
		
		TELEPORT("TP"),
		
		SELFHURT("SelfHurt");
		
		private final String name;
		
		private Mode(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
