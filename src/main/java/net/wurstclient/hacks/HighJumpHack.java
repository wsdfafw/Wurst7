/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"high jump"})
public final class HighJumpHack extends Hack
{
	private final SliderSetting height = new SliderSetting("高度",
		"跳多少方块格高.\n这在较高的值时非常不准确", 6, 1, 100, 1, ValueDisplay.INTEGER);
	
	public HighJumpHack()
	{
		super("跳高");
		
		setCategory(Category.MOVEMENT);
		addSetting(height);
	}
	
	public float getAdditionalJumpMotion()
	{
		return isEnabled() ? height.getValueF() * 0.1F : 0;
	}
}
