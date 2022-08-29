<<<<<<< HEAD
/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
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

@SearchTags({"languages", "localizations", "localisations",
	"internationalization", "internationalisation", "i18n", "sprachen",
	"übersetzungen", "force english"})
@DontBlock
public final class TranslationsOtf extends OtherFeature
{
	private final CheckboxSetting forceEnglish =
		new CheckboxSetting("Force English",
			"用英语显示Wurst客户端,即使Minecraft设置为不同的语言即使Minecraft被设置为不同的语言.",
			false);
	
	public TranslationsOtf()
	{
		super("Translations",
			"本地化设置.\n"
				+ "§c这是一个实验性功能！\n我们还没有很多翻译.\n如果您会说英语和其他语言,请添加更多翻译来帮助我们.");
		addSetting(forceEnglish);
	}
	
	public CheckboxSetting getForceEnglish()
	{
		return forceEnglish;
	}
}
>>>>>>> upstream/1.19
