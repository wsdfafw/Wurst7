/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags({"name tags"})
public final class NameTagsHack extends Hack
{
	private final CheckboxSetting unlimitedRange =
		new CheckboxSetting("Unlimited range", "取消了名片的64个街区距离限制.", true);
	
	private final CheckboxSetting seeThrough = new CheckboxSetting("透视模式",
		"在透明文本层上呈现姓名标签。这使得它们在墙后更容易阅读，但是在水和其他透明的东西后面就很难阅读了。", false);
	
	private final CheckboxSetting forceNametags =
		new CheckboxSetting("强制名片", "强制所有玩家的姓名标签可见，甚至是你自己的.", false);
	
	public NameTagsHack()
	{
		super("名字标签");
		setCategory(Category.RENDER);
		addSetting(unlimitedRange);
		addSetting(seeThrough);
		addSetting(forceNametags);
	}
	
	public boolean isUnlimitedRange()
	{
		return isEnabled() && unlimitedRange.isChecked();
	}
	
	public boolean isSeeThrough()
	{
		return isEnabled() && seeThrough.isChecked();
	}
	
	public boolean shouldForceNametags()
	{
		return isEnabled() && forceNametags.isChecked();
	}
	
	// See LivingEntityRendererMixin and
	// EntityRendererMixin.wurstRenderLabelIfPresent()
}
