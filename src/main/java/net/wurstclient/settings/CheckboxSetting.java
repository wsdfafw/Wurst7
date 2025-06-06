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
import net.wurstclient.clickgui.components.CheckboxComponent;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.text.WText;

public class CheckboxSetting extends Setting implements CheckboxLock
{
	private boolean checked;
	private final boolean checkedByDefault;
	private CheckboxLock lock;
	
	public CheckboxSetting(String name, WText description, boolean checked)
	{
		super(name, description);
		this.checked = checked;
		checkedByDefault = checked;
	}
	
	public CheckboxSetting(String name, String descriptionKey, boolean checked)
	{
		this(name, WText.translated(descriptionKey), checked);
	}
	
	public CheckboxSetting(String name, boolean checked)
	{
		this(name, WText.empty(), checked);
	}
	
	@Override
	public final boolean isChecked()
	{
		return isLocked() ? lock.isChecked() : checked;
	}
	
	public final boolean isCheckedByDefault()
	{
		return checkedByDefault;
	}
	
	public final void setChecked(boolean checked)
	{
		if(isLocked())
			return;
		
		setCheckedIgnoreLock(checked);
	}
	
	private void setCheckedIgnoreLock(boolean checked)
	{
		this.checked = checked;
		update();
		
		WurstClient.INSTANCE.saveSettings();
	}
	
	public final boolean isLocked()
	{
		return lock != null;
	}
	
	public final void lock(CheckboxLock lock)
	{
		this.lock = lock;
		update();
	}
	
	public final void unlock()
	{
		lock = null;
		update();
	}
	
	@Override
	public final Component getComponent()
	{
		return new CheckboxComponent(this);
	}
	
	@Override
	public final void fromJson(JsonElement json)
	{
		if(!JsonUtils.isBoolean(json))
			return;
		
		setCheckedIgnoreLock(json.getAsBoolean());
	}
	
	@Override
	public final JsonElement toJson()
	{
		return new JsonPrimitive(checked);
	}
	
	@Override
	public JsonObject exportWikiData()
	{
		JsonObject json = new JsonObject();
		json.addProperty("name", getName());
		json.addProperty("description", getDescription());
		json.addProperty("type", "Checkbox");
		json.addProperty("checkedByDefault", checkedByDefault);
		return json;
	}
	
	@Override
	public final Set<PossibleKeybind> getPossibleKeybinds(String featureName)
	{
		String fullName = featureName + " " + getName();
		
		String command = ".setcheckbox " + featureName.toLowerCase() + " ";
		command += getName().toLowerCase().replace(" ", "_") + " ";
		
		LinkedHashSet<PossibleKeybind> pkb = new LinkedHashSet<>();
		pkb.add(new PossibleKeybind(command + "toggle", "切换 " + fullName));
		pkb.add(new PossibleKeybind(command + "on", "检查 " + fullName));
		pkb.add(new PossibleKeybind(command + "off", "不检查 " + fullName));
		
		return pkb;
	}
}
