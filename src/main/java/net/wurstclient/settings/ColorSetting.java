/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.settings;

import java.awt.Color;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.util.math.MathHelper;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.components.ColorComponent;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.util.ColorUtils;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.text.WText;

public final class ColorSetting extends Setting
{
	private Color color;
	private final Color defaultColor;
	
	public ColorSetting(String name, WText description, Color color)
	{
		super(name, description);
		this.color = Objects.requireNonNull(color);
		defaultColor = color;
	}
	
	public ColorSetting(String name, String descriptionKey, Color color)
	{
		this(name, WText.translated(descriptionKey), color);
	}
	
	public ColorSetting(String name, Color color)
	{
		this(name, WText.empty(), color);
	}
	
	public Color getColor()
	{
		return color;
	}
	
	public float[] getColorF()
	{
		float red = color.getRed() / 255F;
		float green = color.getGreen() / 255F;
		float blue = color.getBlue() / 255F;
		return new float[]{red, green, blue};
	}
	
	public int getColorI()
	{
		return color.getRGB() | 0xFF000000;
	}
	
	public int getColorI(int alpha)
	{
		return color.getRGB() & 0x00FFFFFF | alpha << 24;
	}
	
	public int getColorI(float alpha)
	{
		return getColorI((int)(MathHelper.clamp(alpha, 0, 1) * 255));
	}
	
	public int getRed()
	{
		return color.getRed();
	}
	
	public int getGreen()
	{
		return color.getGreen();
	}
	
	public int getBlue()
	{
		return color.getBlue();
	}
	
	public Color getDefaultColor()
	{
		return defaultColor;
	}
	
	public void setColor(Color color)
	{
		this.color = Objects.requireNonNull(color);
		WurstClient.INSTANCE.saveSettings();
	}
	
	@Override
	public Component getComponent()
	{
		return new ColorComponent(this);
	}
	
	@Override
	public void fromJson(JsonElement json)
	{
		if(!JsonUtils.isString(json))
			return;
		
		try
		{
			setColor(ColorUtils.parseHex(json.getAsString()));
			
		}catch(JsonException e)
		{
			e.printStackTrace();
			setColor(defaultColor);
		}
	}
	
	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(ColorUtils.toHex(color));
	}
	
	@Override
	public JsonObject exportWikiData()
	{
		JsonObject json = new JsonObject();
		json.addProperty("name", getName());
		json.addProperty("description", getDescription());
		json.addProperty("type", "Color");
		json.addProperty("defaultColor", ColorUtils.toHex(defaultColor));
		return json;
	}
	
	@Override
	public Set<PossibleKeybind> getPossibleKeybinds(String featureName)
	{
		String description = "设置 " + featureName + " " + getName() + " to ";
		String command = ".setcolor " + featureName.toLowerCase() + " "
			+ getName().toLowerCase().replace(" ", "_") + " ";
		
		LinkedHashSet<PossibleKeybind> pkb = new LinkedHashSet<>();
		addPKB(pkb, command + "#FF0000", description + "红色");
		addPKB(pkb, command + "#00FF00", description + "绿色");
		addPKB(pkb, command + "#0000FF", description + "蓝色");
		addPKB(pkb, command + "#FFFF00", description + "黄色");
		addPKB(pkb, command + "#00FFFF", description + "青色");
		addPKB(pkb, command + "#FF00FF", description + "品红");
		addPKB(pkb, command + "#FFFFFF", description + "白色");
		addPKB(pkb, command + "#000000", description + "黑色");
		return pkb;
	}
	
	private void addPKB(LinkedHashSet<PossibleKeybind> pkb, String command,
		String description)
	{
		pkb.add(new PossibleKeybind(command, description));
	}
}
