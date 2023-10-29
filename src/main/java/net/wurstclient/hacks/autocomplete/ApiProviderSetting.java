/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks.autocomplete;

import net.wurstclient.settings.EnumSetting;

public final class ApiProviderSetting
	extends EnumSetting<ApiProviderSetting.ApiProvider>
{
	public ApiProviderSetting()
	{
		super("API提供者",
			"\u00a7lOpenAI\u00a7r 允许你使用像ChatGPT这样的模型，但需要拥有API访问权限的账户，使用需要花费金钱，并将你的聊天历史发送到他们的服务器。其名称是一个谎言 - 它是闭源的.\n\n"
				+ "\u00a7loobabooga\u00a7r 允许你使用像LLaMA和许多其他模型一样。这是OpenAI的真正开源替代品，你可以在自己的计算机上本地运行它。它是免费的，不会将你的聊天历史发送到任何服务器。",
			ApiProvider.values(), ApiProvider.OOBABOOGA);
	}
	
	public enum ApiProvider
	{
		OPENAI("OpenAI"),
		OOBABOOGA("oobabooga");
		
		private final String name;
		
		private ApiProvider(String name)
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
