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
		String damageWarning = "\n如果他们离有效目标或现有锚太近，他们仍然会受到伤害.";
		
		builder.add(new FilterPlayersSetting("自动放置锚点时不会瞄准其他玩家." + damageWarning,
			false));
		
		builder.add(new FilterHostileSetting("Won't target hostile mobs like"
			+ " zombies and creepers when auto-placing anchors."
			+ damageWarning, true));
		
		builder.add(new FilterNeutralSetting("Won't target neutral mobs like"
			+ " endermen and wolves when auto-placing anchors." + damageWarning,
			AttackDetectingEntityFilter.Mode.ON));
		
		builder.add(new FilterPassiveSetting("Won't target animals like pigs"
			+ " and cows, ambient mobs like bats, and water mobs like fish,"
			+ " squid and dolphins when auto-placing anchors." + damageWarning,
			true));
		
		builder.add(new FilterPassiveWaterSetting("Won't target passive water"
			+ " mobs like fish, squid, dolphins and axolotls when auto-placing"
			+ " anchors." + damageWarning, true));
		
		builder.add(new FilterBatsSetting("Won't target bats and any other"
			+ " \"ambient\" mobs when auto-placing anchors." + damageWarning,
			true));
		
		builder.add(new FilterSlimesSetting("Won't target slimes when"
			+ " auto-placing anchors." + damageWarning, true));
		
		builder.add(new FilterVillagersSetting("Won't target villagers and"
			+ " wandering traders when auto-placing anchors." + damageWarning,
			true));
		
		builder.add(new FilterZombieVillagersSetting("Won't target zombified"
			+ " villagers when auto-placing anchors." + damageWarning, true));
		
		builder.add(new FilterGolemsSetting("Won't target iron golems and snow"
			+ " golems when auto-placing anchors." + damageWarning, true));
		
		builder.add(new FilterPiglinsSetting(
			"Won't target piglins when auto-placing anchors.",
			AttackDetectingEntityFilter.Mode.ON));
		
		builder.add(new FilterZombiePiglinsSetting("Won't target"
			+ " zombified piglins when auto-placing anchors." + damageWarning,
			AttackDetectingEntityFilter.Mode.ON));
		
		builder.add(new FilterShulkersSetting("Won't target shulkers when"
			+ " auto-placing anchors." + damageWarning, true));
		
		builder.add(new FilterAllaysSetting(
			"Won't target allays when auto-placing anchors." + damageWarning,
			true));
		
		builder.add(new FilterInvisibleSetting(
			"自动放置锚点时不会瞄准不可见的实体." + damageWarning, false));
		
		builder.add(new FilterNamedSetting(
			"自动放置锚点时不会以名称标记的实体为目标." + damageWarning, false));
		
		builder.add(new FilterArmorStandsSetting(
			"自动放置锚时不会瞄准盔甲架." + damageWarning, true));
		
		return new AnchorAuraFilterList(builder);
	}
}
