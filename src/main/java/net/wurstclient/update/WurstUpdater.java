/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.update;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.wurstclient.WurstClient;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonArray;
import net.wurstclient.util.json.WsonObject;

public final class WurstUpdater implements UpdateListener
{
	private Thread thread;
	private boolean outdated;
	private Text component;
	
	@Override
	public void onUpdate()
	{
		if(thread == null)
		{
			thread = new Thread(this::checkForUpdates, "Wurst更新器");
			thread.start();
			return;
		}
		
		if(thread.isAlive())
			return;
		
		if(component != null)
			ChatUtils.component(component);
		
		WurstClient.INSTANCE.getEventManager().remove(UpdateListener.class,
			this);
	}
	
	public void checkForUpdates()
	{
		Version currentVersion = new Version(WurstClient.VERSION);
		Version latestVersion = null;
		
		try
		{
			WsonArray wson = JsonUtils.parseURLToArray(
				"https://api.github.com/repos/Wurst-Imperium/Wurst-MCX2/releases");
			
			for(WsonObject release : wson.getAllObjects())
			{
				if(!currentVersion.isPreRelease()
					&& release.getBoolean("prerelease"))
					continue;
				
				if(!containsCompatibleAsset(release.getArray("assets")))
					continue;
				
				String tagName = release.getString("tag_name");
				latestVersion = new Version(tagName.substring(1));
				break;
			}
			
			if(latestVersion == null)
				throw new NullPointerException("最新版本并不存在!");
			
			System.out.println("[更新] 当前版本: " + currentVersion);
			System.out.println("[更新] 最新版本: " + latestVersion);
			outdated = currentVersion.shouldUpdateTo(latestVersion);
			
		}catch(Exception e)
		{
			System.err.println("[更新程序] 发生错误!");
			e.printStackTrace();
		}
		
		if(latestVersion == null || latestVersion.isInvalid())
		{
			String text = "An error occurred while checking for updates."
				+ " Click \u00a7nhere\u00a7r to check manually.";
			String url =
				"https://www.wurstclient.net/download/?utm_source=Wurst+Client&utm_medium=WurstUpdater+chat+message&utm_content=An+error+occurred+while+checking+for+updates.";
			showLink(text, url);
			return;
		}
		
		if(!outdated)
			return;
		
		String textPart1 = "Wurst " + latestVersion + " MC"
			+ WurstClient.MC_VERSION + " is now available.";
		String text =
			textPart1 + " Click \u00a7nhere\u00a7r to download the update.";
		String url =
			"https://www.wurstclient.net/download/?utm_source=Wurst+Client&utm_medium=WurstUpdater+chat+message&utm_content="
				+ tryEncode(textPart1);
		showLink(text, url);
	}
	
	private void showLink(String text, String url)
	{
		component = new LiteralText(text);
		
		ClickEvent event = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
		component.getStyle().withClickEvent(event);
	}
	
	private boolean containsCompatibleAsset(WsonArray wsonArray)
		throws JsonException
	{
		String compatibleSuffix = "MC" + WurstClient.MC_VERSION + ".jar";
		
		for(WsonObject asset : wsonArray.getAllObjects())
		{
			String assetName = asset.getString("name");
			if(!assetName.endsWith(compatibleSuffix))
				continue;
			
			return true;
		}
		
		return false;
	}
	
	public boolean isOutdated()
	{
		return outdated;
	}
	
	private String tryEncode(String s)
	{
		try
		{
			return URLEncoder.encode(s, "UTF-8");
			
		}catch(UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
	}
}
