/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.settings;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.text.WText;

public final class ThemeSetting extends Setting
{
	private Theme currentTheme;
	private final Theme defaultTheme;
	
	public enum Theme
	{
		CLASSIC("经典", 0x404040, 0x101010, 0xF0F0F0),
		DARK("暗色", 0x202020, 0x080808, 0xE0E0E0),
		LIGHT("亮色", 0xF0F0F0, 0xD0D0D0, 0x202020);
		
		private final String name;
		private final int bgColor;
		private final int acColor;
		private final int txtColor;
		
		private Theme(String name, int bgColor, int acColor, int txtColor)
		{
			this.name = name;
			this.bgColor = bgColor;
			this.acColor = acColor;
			this.txtColor = txtColor;
		}
		
		public String getName()
		{
			return name;
		}
		
		public int getBgColor()
		{
			return bgColor;
		}
		
		public int getAcColor()
		{
			return acColor;
		}
		
		public int getTxtColor()
		{
			return txtColor;
		}
	}
	
	public ThemeSetting(String name, WText description, Theme theme)
	{
		super(name, description);
		currentTheme = theme;
		defaultTheme = theme;
	}
	
	public ThemeSetting(String name, String descriptionKey, Theme theme)
	{
		this(name, WText.translated(descriptionKey), theme);
	}
	
	public ThemeSetting(String name, Theme theme)
	{
		this(name, WText.empty(), theme);
	}
	
	public Theme getCurrentTheme()
	{
		return currentTheme;
	}
	
	public void setCurrentTheme(Theme theme)
	{
		currentTheme = theme;
		WurstClient.INSTANCE.saveSettings();
	}
	
	public Theme getDefaultTheme()
	{
		return defaultTheme;
	}
	
	@Override
	public Component getComponent()
	{
		return new net.wurstclient.clickgui.components.ThemeComboBoxComponent(
			this);
	}
	
	@Override
	public void fromJson(JsonElement json)
	{
		if(!JsonUtils.isString(json))
			return;
		
		try
		{
			setCurrentTheme(Theme.valueOf(json.getAsString().toUpperCase()));
		}catch(IllegalArgumentException e)
		{
			setCurrentTheme(defaultTheme);
		}
	}
	
	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(currentTheme.name());
	}
	
	@Override
	public JsonObject exportWikiData()
	{
		JsonObject json = new JsonObject();
		json.addProperty("name", getName());
		json.addProperty("description", getDescription());
		json.addProperty("type", "Theme");
		json.addProperty("defaultTheme", defaultTheme.name());
		return json;
	}
	
	@Override
	public Set<PossibleKeybind> getPossibleKeybinds(String featureName)
	{
		String description = "设置 " + featureName + " " + getName() + " to ";
		String command = ".settheme " + featureName.toLowerCase() + " "
			+ getName().toLowerCase().replace(" ", "_") + " ";
		
		LinkedHashSet<PossibleKeybind> pkb = new LinkedHashSet<>();
		for(Theme theme : Theme.values())
			pkb.add(new PossibleKeybind(command + theme.name().toLowerCase(),
				description + theme.getName()));
		
		return pkb;
	}
}
