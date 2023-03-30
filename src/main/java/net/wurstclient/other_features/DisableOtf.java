/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.other_features;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags({"turn off", "hide wurst logo", "ghost mode", "stealth mode",
	"vanilla Minecraft"})
@DontBlock
public final class DisableOtf extends OtherFeature
{
	private final CheckboxSetting hideEnableButton = new CheckboxSetting(
		"Hide enable button",
		"Removes the \"Enable Wurst\" button as soon as you close the Statistics screen."
			+ " You will have to restart the game to re-enable Wurst.",
		false);
	
	public DisableOtf()
	{
		super("要禁用香肠","请转到统计屏幕并按“禁用香肠”按钮.\n一旦按下它就会变成一个“启用Wurst”按钮.");
		addSetting(hideEnableButton);
	}
	
	public boolean shouldHideEnableButton()
	{
		return !WURST.isEnabled() && hideEnableButton.isChecked();
	}
}
