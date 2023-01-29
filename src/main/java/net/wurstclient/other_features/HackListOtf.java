/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.other_features;

import java.awt.Color;
import java.util.Comparator;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstClient;
import net.wurstclient.hack.Hack;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.EnumSetting;

@SearchTags({"ArrayList", "ModList", "CheatList", "mod list", "array list",
	"hack list", "cheat list"})
@DontBlock
public final class HackListOtf extends OtherFeature
{
	private final EnumSetting<Mode> mode = new EnumSetting<>("模式","§l自动§r 模式 将会展示全部但如果\n显示不全将会显示数量.\n§l数量§r 模式 只展示数字\n已激活的功能.\n§l隐藏§r 模式 任何东西不显示",
		Mode.values(), Mode.AUTO);
	
	private final EnumSetting<Position> position = new EnumSetting<>("位置",
		"HackList 应该显示在屏幕的哪一侧.使用 TaGUI 时将其更改为 \u00a7 Right\u00a7r.",
		Position.values(), Position.LEFT);
	
	private final ColorSetting color = new ColorSetting("颜色",
		"HackList文本的颜色.\n" + "Only visible when \u00a76RainbowUI\u00a7r is disabled.",
		Color.WHITE);
	
	private final EnumSetting<SortBy> sortBy = new EnumSetting<>("排序方式",
		"Determines how the HackList entries are sorted.\n"
			+ "Only visible when \u00a76Mode\u00a7r is set to \u00a76Auto\u00a7r.",
		SortBy.values(), SortBy.NAME);
	
	private final CheckboxSetting revSort =
		new CheckboxSetting("反向排序", false);
	
	private final CheckboxSetting animations = new CheckboxSetting("动画","启用后，条目会随着 hacks 的启用和禁用而滑入和滑出 HackList.",
		true);
	
	private SortBy prevSortBy;
	private Boolean prevRevSort;
	
	public HackListOtf()
	{
		super("Hack列表", "在屏幕上显示活动黑客列表.");
		
		addSetting(mode);
		addSetting(position);
		addSetting(color);
		addSetting(sortBy);
		addSetting(revSort);
		addSetting(animations);
	}
	
	public Mode getMode()
	{
		return mode.getSelected();
	}
	
	public Position getPosition()
	{
		return position.getSelected();
	}
	
	public boolean isAnimations()
	{
		return animations.isChecked();
	}
	
	public Comparator<Hack> getComparator()
	{
		if(revSort.isChecked())
			return sortBy.getSelected().comparator.reversed();
		
		return sortBy.getSelected().comparator;
	}
	
	public boolean shouldSort()
	{
		try
		{
			// width of a renderName could change at any time
			// must sort the HackList every tick
			if(sortBy.getSelected() == SortBy.WIDTH)
				return true;
			
			if(sortBy.getSelected() != prevSortBy)
				return true;
			
			if(!Boolean.valueOf(revSort.isChecked()).equals(prevRevSort))
				return true;
			
			return false;
			
		}finally
		{
			prevSortBy = sortBy.getSelected();
			prevRevSort = revSort.isChecked();
		}
	}
	
	public int getColor()
	{
		return color.getColorI() & 0x00FFFFFF;
	}
	
	public static enum Mode
	{
		AUTO("自动"),
		
		COUNT("数量"),
		
		HIDDEN("隐藏");
		
		private final String name;
		
		private Mode(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
	
	public static enum Position
	{
		LEFT("左边"),
		
		RIGHT("右边");
		
		private final String name;
		
		private Position(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
	
	public static enum SortBy
	{
		NAME("名字", (a, b) -> a.getName().compareToIgnoreCase(b.getName())),
		
		WIDTH("长度", Comparator.comparingInt(
			h -> WurstClient.MC.textRenderer.getWidth(h.getRenderName())));
		
		private final String name;
		private final Comparator<Hack> comparator;
		
		private SortBy(String name, Comparator<Hack> comparator)
		{
			this.name = name;
			this.comparator = comparator;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
