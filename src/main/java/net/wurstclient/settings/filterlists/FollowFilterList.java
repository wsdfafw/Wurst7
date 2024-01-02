/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
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
		
		builder.add(new FilterPlayersSetting("不会跟随其他玩家.", false));
		
		builder.add(new FilterSleepingSetting("不会跟随睡觉的玩家.", false));
		
		builder.add(new FilterFlyingSetting("不会跟随距离地面至少给定距离的玩家.", 0));
		
		builder.add(new FilterHostileSetting(
			"Won't follow hostile mobs like zombies and creepers.", true));
		
		builder.add(FilterNeutralSetting.onOffOnly(
			"Won't follow neutral mobs like endermen and wolves.", true));
		
		builder.add(new FilterPassiveSetting("Won't follow animals like pigs"
			+ " and cows, ambient mobs like bats, and water mobs like"
			+ " fish, squid and dolphins.", true));
		
		builder.add(new FilterPassiveWaterSetting("Won't follow passive water"
			+ " mobs like fish, squid, dolphins and axolotls.", true));
		
		builder.add(new FilterBabiesSetting("不会跟随小猪、小村民等", true));
		
		builder.add(new FilterBatsSetting("Won't follow bats and any other"
			+ " \"ambient\" mobs that might be added by mods.", true));
		
		builder.add(new FilterSlimesSetting("Won't follow slimes.", true));
		
		builder.add(new FilterBatsSetting("Won't follow bats and any other"
			+ " \"ambient\" mobs that might be added by mods.", true));
		
		builder.add(new FilterSlimesSetting("Won't follow slimes.", true));
		
		builder.add(new FilterPetsSetting("不会跟随驯服的狼、驯服的马等.", true));
		
		builder.add(new FilterVillagersSetting(
			"Won't follow villagers and wandering traders.", true));
		
		builder.add(new FilterZombieVillagersSetting(
			"Won't follow zombified villagers.", true));
		
		builder.add(new FilterGolemsSetting(
			"Won't follow iron golems and snow golems.", true));
		
		builder
			.add(FilterPiglinsSetting.onOffOnly("Won't follow piglins.", true));
		
		builder.add(FilterZombiePiglinsSetting
			.onOffOnly("Won't follow zombified piglins.", true));
		
		builder.add(
			FilterEndermenSetting.onOffOnly("Won't follow endermen.", true));
		
		builder.add(new FilterShulkersSetting("Won't follow shulkers.", true));
		
		builder.add(new FilterAllaysSetting("Won't follow allays.", true));
		
		builder.add(new FilterInvisibleSetting("不会跟随隐形实体.", false));
		
		builder.add(new FilterArmorStandsSetting("不会跟随盔甲架.", true));
		
		builder.add(new FilterMinecartsSetting("不会跟随矿车.", true));
		
		return new FollowFilterList(builder);
	}
}
