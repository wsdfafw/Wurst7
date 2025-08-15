/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.settings;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.wurstclient.Feature;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Window;

public class SettingsOrganizer
{
	public static class SettingsGroup
	{
		private final String name;
		private final List<Feature> features;
		
		public SettingsGroup(String name)
		{
			this.name = name;
			this.features = new ArrayList<>();
		}
		
		public String getName()
		{
			return name;
		}
		
		public List<Feature> getFeatures()
		{
			return features;
		}
		
		public void addFeature(Feature feature)
		{
			features.add(feature);
		}
	}
	
	public static Map<String, SettingsGroup> organizeSettings()
	{
		LinkedHashMap<String, SettingsGroup> groups = new LinkedHashMap<>();
		
		// Create default groups
		groups.put("Hacks", new SettingsGroup("作弊功能"));
		groups.put("Commands", new SettingsGroup("命令"));
		groups.put("Other Features", new SettingsGroup("其他功能"));
		groups.put("UI Settings", new SettingsGroup("界面设置"));
		
		// Organize hacks
		WurstClient wurst = WurstClient.INSTANCE;
		for(Feature hack : wurst.getHax().getAllHax())
			groups.get("Hacks").addFeature(hack);
		
		// Organize commands
		for(Feature cmd : wurst.getCmds().getAllCmds())
			groups.get("Commands").addFeature(cmd);
		
		// Organize other features
		for(Feature otf : wurst.getOtfs().getAllOtfs())
			groups.get("Other Features").addFeature(otf);
		
		// Add UI settings
		groups.get("UI Settings").addFeature(wurst.getHax().clickGuiHack);
		
		return groups;
	}
	
	public static void createSettingsWindows(Map<String, SettingsGroup> groups,
		List<Window> windows)
	{
		for(SettingsGroup group : groups.values())
		{
			if(group.getFeatures().isEmpty())
				continue;
			
			Window window = new Window(group.getName() + " 设置");
			for(Feature feature : group.getFeatures())
			{
				// Add feature button for each feature
				// This would need to be implemented in the ClickGui class
			}
			windows.add(window);
		}
	}
}
