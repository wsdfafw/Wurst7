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
		
		builder.add(new FilterHostileSetting(
			"在自动放置锚点时不会瞄准敌对生物,比如僵尸和爬行者." + damageWarning, true));
		
		builder.add(new FilterNeutralSetting(
			"不会自动瞄准中立生物，如末影人和狼，当自动放置锚点时" + damageWarning,
			AttackDetectingEntityFilter.Mode.ON));
		
		builder.add(new FilterPassiveSetting(
			"不会针对像猪和牛这样的动物、像蝙蝠这样的环境生物，以及像鱼、鱿鱼和海豚这样的水中生物，在自动放置锚点时进行目标选择。"
				+ damageWarning,
			true));
		
		builder.add(new FilterPassiveWaterSetting(
			"在自动放置锚点时，不会针对 passiWater 生物，例如鱼、鱿鱼、海豚和水獭进行目标选择" + damageWarning,
			true));
		
		builder.add(new FilterBatsSetting(
			"在自动放置锚点时，不会以目标为蝙蝠和其他“环境”生物" + damageWarning, true));
		
		builder.add(
			new FilterSlimesSetting("在自动放置锚点时不会以史莱姆为目标" + damageWarning, true));
		
		builder.add(new FilterVillagersSetting(
			"不会在自动放置锚点时瞄准村民和流浪商人" + damageWarning, true));
		
		builder.add(new FilterZombieVillagersSetting(
			"不会针对僵化的村民在自动放置锚点时进行目标定位." + damageWarning, true));
		
		builder.add(new FilterGolemsSetting(
			"当自动放置锚点时，不会瞄准铁傀儡和雪傀儡." + damageWarning, true));
		
		builder.add(new FilterPiglinsSetting("在自动放置锚点时不会攻击猪灵.",
			AttackDetectingEntityFilter.Mode.ON));
		
		builder.add(
			new FilterZombiePiglinsSetting("自动放置锚石时不会瞄准僵化的猪灵." + damageWarning,
				AttackDetectingEntityFilter.Mode.ON));
		
		builder.add(
			new FilterShulkersSetting("不会在自动放置锚点时瞄准虫箱" + damageWarning, true));
		
		builder.add(new FilterAllaysSetting(
			"当自动放置锚点时，不会选择所有的避风港." + damageWarning, true));
		
		builder.add(new FilterInvisibleSetting(
			"自动放置锚点时不会瞄准不可见的实体." + damageWarning, false));
		
		builder.add(new FilterNamedSetting(
			"自动放置锚点时不会以名称标记的实体为目标." + damageWarning, false));
		
		builder.add(new FilterArmorStandsSetting(
			"自动放置锚时不会瞄准盔甲架." + damageWarning, true));
		
		return new AnchorAuraFilterList(builder);
	}
}
