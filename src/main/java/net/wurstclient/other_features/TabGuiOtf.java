/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.other_features;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.EnumSetting;

@SearchTags({"tab gui", "HackMenu", "hack menu", "SideBar", "side bar",
	"blocks movement combat render chat fun items other"})
@DontBlock
public final class TabGuiOtf extends OtherFeature
{
	private final EnumSetting<Status> status =
		new EnumSetting<>("Status", Status.values(), Status.DISABLED);
	
	public TabGuiOtf()
	{
		super("TabGUI", "允许你在游戏时快速切换黑客.使用方向键进行导航.\n\n"
			+ "改变 \u00a76黑客名单 \u00a76位置\u00a7r setting to \u00a76Right\u00a7r 以防止TabGUI与HackList重叠.");
		
		addSetting(status);
	}
	
	public boolean isHidden()
	{
		return status.getSelected() == Status.DISABLED;
	}
	
	private enum Status
	{
		ENABLED("启用"),
		DISABLED("已禁用");
		
		private final String name;
		
		private Status(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
