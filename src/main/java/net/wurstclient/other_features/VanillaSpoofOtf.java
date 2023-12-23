/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.other_features;

import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.events.ConnectionPacketOutputListener;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.CheckboxSetting;

@DontBlock
@SearchTags({"vanilla spoof", "AntiFabric", "anti fabric", "LibHatesMods",
	"HackedServer"})
public final class VanillaSpoofOtf extends OtherFeature
	implements ConnectionPacketOutputListener
{
	private final CheckboxSetting spoof = new CheckboxSetting("原版伪装", false);
	
	public VanillaSpoofOtf()
	{
		super("原版伪装", "绕过反-Fabric插件假装自己是原版客户端.");
		addSetting(spoof);
		
		EVENTS.add(ConnectionPacketOutputListener.class, this);
	}
	
	@Override
	public void onSentConnectionPacket(ConnectionPacketOutputEvent event)
	{
		if(!spoof.isChecked())
			return;
		
		if(!(event.getPacket() instanceof CustomPayloadC2SPacket packet))
			return;
		
		Identifier channel = packet.payload().id();
		
		if(channel.getNamespace().equals("minecraft")
			&& channel.getPath().equals("register"))
			event.cancel();
			
		// Apparently the Minecraft client no longer sends its brand to the
		// server as of 23w31a
		
		// if(packet.getChannel().getNamespace().equals("minecraft")
		// && packet.getChannel().getPath().equals("brand"))
		// event.setPacket(new CustomPayloadC2SPacket(
		// CustomPayloadC2SPacket.BRAND,
		// new PacketByteBuf(Unpooled.buffer()).writeString("vanilla")));
		
		if(channel.getNamespace().equals("fabric"))
			event.cancel();
	}
	
	@Override
	public boolean isEnabled()
	{
		return spoof.isChecked();
	}
	
	@Override
	public String getPrimaryAction()
	{
		return isEnabled() ? "禁用" : "启用";
	}
	
	@Override
	public void doPrimaryAction()
	{
		spoof.setChecked(!spoof.isChecked());
	}
}
