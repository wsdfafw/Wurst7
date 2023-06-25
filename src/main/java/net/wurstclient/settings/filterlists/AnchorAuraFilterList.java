/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.settings.filterlists;

import java.util.ArrayList;
import java.util.List;

import net.wurstclient.settings.filters.*;

public final class AnchorAuraFilterList extends EntityFilterList
{
	private AnchorAuraFilterList(List<EntityFilter> filters)
	{
		super(filters);
	}
	
	public static AnchorAuraFilterList create()
	{
		ArrayList<EntityFilter> builder = new ArrayList<>();
		String damageWarning =
			"\n如果他们离有效目标或现有锚太近，他们仍然会受到伤害.";
		
		builder.add(new FilterPlayersSetting(
			"自动放置锚点时不会瞄准其他玩家."
				+ damageWarning,
			false));
		
		builder.add(new FilterMonstersSetting(
			"自动放置锚时不会瞄准僵尸、爬行者等."
				+ damageWarning,
			true));
		
		builder.add(new FilterAnimalsSetting(
			"自动放置锚时不会瞄准猪、牛等."
				+ damageWarning,
			true));
		
		builder.add(new FilterTradersSetting(
			"自动放置锚时不会瞄准村民、流浪商人等."
				+ damageWarning,
			true));
		
		builder.add(new FilterGolemsSetting(
			"自动放置锚时不会瞄准铁傀儡、雪傀儡和潜影贝."
				+ damageWarning,
			true));
		
		builder.add(new FilterAllaysSetting(
			"Won't target allays when auto-placing anchors." + damageWarning,
			true));
		
		builder.add(new FilterInvisibleSetting(
			"自动放置锚点时不会瞄准不可见的实体."
				+ damageWarning,
			false));
		
		builder.add(new FilterNamedSetting(
			"自动放置锚点时不会以名称标记的实体为目标."
				+ damageWarning,
			false));
		
		builder.add(new FilterArmorStandsSetting(
			"自动放置锚时不会瞄准盔甲架."
				+ damageWarning,
			true));
		
		return new AnchorAuraFilterList(builder);
	}
}
