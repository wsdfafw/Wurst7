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

@SearchTags({"friction", "no friction", "slippery", "slipperiness"})
public final class NoFrictionHack extends Hack
{
	public final SliderSetting friction = new SliderSetting("摩擦/滑溜", 0.989, 0.8,
		1.1, 0.001, ValueDisplay.DECIMAL);
	
	public NoFrictionHack()
	{
		super("无摩擦");
		setCategory(Category.MOVEMENT);
		addSetting(friction);
	}
}
