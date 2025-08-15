/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.clickgui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.wurstclient.Category;
import net.wurstclient.Feature;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.components.FeatureButton;

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
	
	public static Map<String, SettingsGroup> createSettingsGroups()
	{
		LinkedHashMap<String, SettingsGroup> groups = new LinkedHashMap<>();
		
		// Create groups for each category
		for(Category category : Category.values())
			groups.put(category.getName(),
				new SettingsGroup(category.getName() + " 设置"));
		
		// Create special groups
		groups.put("UI", new SettingsGroup("界面设置"));
		groups.put("Commands", new SettingsGroup("命令设置"));
		
		return groups;
	}
	
	public static void populateSettingsGroups(Map<String, SettingsGroup> groups)
	{
		WurstClient wurst = WurstClient.INSTANCE;
		
		// Populate hack settings by category
		for(Feature hack : wurst.getHax().getAllHax())
		{
			Category category = hack.getCategory();
			if(category != null)
			{
				String groupName = category.getName();
				groups.get(groupName).addFeature(hack);
			}
		}
		
		// Populate UI settings
		SettingsGroup uiGroup = groups.get("UI");
		uiGroup.addFeature(wurst.getOtfs().wurstLogoOtf);
		uiGroup.addFeature(wurst.getOtfs().hackListOtf);
		uiGroup.addFeature(wurst.getOtfs().keybindManagerOtf);
		uiGroup.addFeature(wurst.getHax().clickGuiHack);
		
		// Populate command settings
		SettingsGroup cmdGroup = groups.get("Commands");
		for(Feature cmd : wurst.getCmds().getAllCmds())
			cmdGroup.addFeature(cmd);
	}
	
	public static List<Window> createSettingsWindows(
		Map<String, SettingsGroup> groups)
	{
		List<Window> settingsWindows = new ArrayList<>();
		
		for(SettingsGroup group : groups.values())
		{
			if(group.getFeatures().isEmpty())
				continue;
			
			Window window = new Window(group.getName());
			for(Feature feature : group.getFeatures())
				window.add(new FeatureButton(feature));
			
			settingsWindows.add(window);
		}
		
		return settingsWindows;
	}
}
