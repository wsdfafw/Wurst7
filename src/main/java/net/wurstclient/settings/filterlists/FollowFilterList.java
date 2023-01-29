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

public final class FollowFilterList extends EntityFilterList
{
	private FollowFilterList(List<EntityFilter> filters)
	{
		super(filters);
	}
	
	public static FollowFilterList create()
	{
		ArrayList<EntityFilter> builder = new ArrayList<>();
		
		builder.add(
			new FilterPlayersSetting("不会跟随其他玩家.", false));
		
		builder.add(
			new FilterSleepingSetting("不会跟随睡觉的玩家.", false));
		
		builder.add(new FilterFlyingSetting(
			"不会跟随距离地面至少给定距离的玩家.",
			0));
		
		builder.add(new FilterMonstersSetting(
			"不会跟随僵尸、爬行者等.", true));
		
		builder
			.add(new FilterPigmenSetting("不会跟随僵尸猪人.", true));
		
		builder.add(new FilterEndermenSetting("不会跟随末影人.", true));
		
		builder.add(
			new FilterAnimalsSetting("不会跟随猪、牛等.", true));
		
		builder.add(new FilterBabiesSetting(
			"不会跟随小猪、小村民等.", true));
		
		builder.add(new FilterPetsSetting(
			"不会跟随驯服的狼、驯服的马等.", true));
		
		builder.add(new FilterTradersSetting(
			"不会跟随村民、流浪商人等.", true));
		
		builder.add(new FilterGolemsSetting(
			"不会跟随铁傀儡、雪傀儡和潜影贝.", true));
		
		builder.add(new FilterInvisibleSetting(
			"不会跟随隐形实体.", false));
		
		builder.add(
			new FilterArmorStandsSetting("不会跟随盔甲架.", true));
		
		builder
			.add(new FilterMinecartsSetting("不会跟随矿车.", true));
		
		return new FollowFilterList(builder);
	}
}
