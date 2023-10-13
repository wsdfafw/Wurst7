/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.settings.filters;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public final class FilterSleepingSetting extends EntityFilterCheckbox
{
	public FilterSleepingSetting(String description, boolean checked)
	{
		super("排除正在睡觉", description, checked);
	}
	
	@Override
	public boolean test(Entity e)
	{
		if(!(e instanceof PlayerEntity))
			return true;
		
		return !((PlayerEntity)e).isSleeping();
	}
	
	public static FilterSleepingSetting genericCombat(boolean checked)
	{
		return new FilterSleepingSetting("不会攻击睡觉的玩家.\n\n"
			+ "对于像Mineplex这样将睡觉的玩家放在地上以使它们看起来像尸体的服务器非常有用.", checked);
	}
	
	public static FilterSleepingSetting genericVision(boolean checked)
	{
		return new FilterSleepingSetting("不显示正在睡觉的玩家.",
			checked);
	}
}
