/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.settings.filters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;

public final class FilterPigmenSetting extends EntityFilterCheckbox
{
	public FilterPigmenSetting(String description, boolean checked)
	{
		super("排除猪人", description, checked);
	}
	
	@Override
	public boolean test(Entity e)
	{
		return !(e instanceof ZombifiedPiglinEntity);
	}
	
	public static FilterPigmenSetting genericCombat(boolean checked)
	{
		return new FilterPigmenSetting("不会攻击僵尸猪人.", checked);
	}
	
	public static FilterPigmenSetting genericVision(boolean checked)
	{
		return new FilterPigmenSetting("Won't show zombie pigmen.", checked);
	}
}
