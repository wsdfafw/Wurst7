/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.settings;

public final class NukerModeSetting
	extends EnumSetting<NukerModeSetting.NukerMode>
{
	public NukerModeSetting()
	{
		super("模式",
			"\u00a7l普通模式\u00a7r：普通模式会破坏你周围的一切。\n"
				+ "\u00a7lID模式\u00a7r：ID模式只会破坏选定的方块类型。"
				+ "左键点击一个方块来选择它。\n"
				+ "\u00a7lMultiID模式\u00a7r：MultiID模式只会破坏你在"
				+ "MultiID列表中的方块类型。\n"
				+ "\u00a7l平面模式\u00a7r：平面模式会平整你周围的地形，但"
				+ "不会向下挖掘。\n"
				+ "\u00a7l粉碎模式\u00a7r：粉碎模式只会破坏可以立即破坏的方块"
				+ "（例如：高草）。",
			NukerMode.values(), NukerMode.NORMAL);
	}
	
	public enum NukerMode
	{
		NORMAL("Normal"),
		ID("ID"),
		MULTI_ID("MultiID"),
		FLAT("Flat"),
		SMASH("Smash");
		
		private final String name;
		
		private NukerMode(String name)
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
