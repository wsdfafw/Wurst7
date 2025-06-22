/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.options;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.minecraft.util.Util.OperatingSystem;
import net.wurstclient.WurstClient;
import net.wurstclient.analytics.PlausibleAnalytics;
import net.wurstclient.commands.FriendsCmd;
import net.wurstclient.hacks.XRayHack;
import net.wurstclient.other_features.VanillaSpoofOtf;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.WurstColors;

public class WurstOptionsScreen extends Screen
{
	private Screen prevScreen;
	
	public WurstOptionsScreen(Screen prevScreen)
	{
		super(Text.literal(""));
		this.prevScreen = prevScreen;
	}
	
	@Override
	public void init()
	{
		addDrawableChild(ButtonWidget
			.builder(Text.literal("Back"), b -> client.setScreen(prevScreen))
			.dimensions(width / 2 - 100, height / 4 + 144 - 16, 200, 20)
			.build());
		
		addSettingButtons();
		addManagerButtons();
		addLinkButtons();
	}
	
	private void addSettingButtons()
	{
		WurstClient wurst = WurstClient.INSTANCE;
		FriendsCmd friendsCmd = wurst.getCmds().friendsCmd;
		CheckboxSetting middleClickFriends = friendsCmd.getMiddleClickFriends();
		PlausibleAnalytics plausible = wurst.getPlausible();
		VanillaSpoofOtf vanillaSpoofOtf = wurst.getOtfs().vanillaSpoofOtf;
		CheckboxSetting forceEnglish =
			wurst.getOtfs().translationsOtf.getForceEnglish();
		
		new WurstOptionsButton(-154, 24,
			() -> "点击朋友: " + (middleClickFriends.isChecked() ? "开启" : "关闭"),
			middleClickFriends.getWrappedDescription(200),
			b -> middleClickFriends
				.setChecked(!middleClickFriends.isChecked()));
		
		new WurstOptionsButton(-154, 48,
			() -> "Count Users: " + (plausible.isEnabled() ? "ON" : "OFF"),
			"Counts how many people are using Wurst and which versions are the"
				+ " most popular. This data helps me to decide when I can stop"
				+ " supporting old versions.\n\n"
				+ "These statistics are completely anonymous, never sold, and"
				+ " stay in the EU (I'm self-hosting Plausible in Germany)."
				+ " There are no cookies or persistent identifiers"
				+ " (see plausible.io).",
			b -> plausible.setEnabled(!plausible.isEnabled()));
		
		new WurstOptionsButton(-154, 72,
			() -> "原版伪装: " + (vanillaSpoofOtf.isEnabled() ? "开启" : "关闭"),
			vanillaSpoofOtf.getDescription(),
			b -> vanillaSpoofOtf.doPrimaryAction());
		
		new WurstOptionsButton(-154, 96,
			() -> "翻译: " + (!forceEnglish.isChecked() ? "ON" : "OFF"),
			"允许在 Wurst 中显示的文本以英语以外的其他语言显示。它将使用 Minecraft 所设置的相同语言。\n\n这是一个实验性的功能！",
			b -> forceEnglish.setChecked(!forceEnglish.isChecked()));
	}
	
	private void addManagerButtons()
	{
		XRayHack xRayHack = WurstClient.INSTANCE.getHax().xRayHack;
		
		new WurstOptionsButton(-50, 24, () -> "热键绑定",
			"按键绑定允许您通过简单地按下一个按钮来切换任何作弊或命令。",
			b -> client.setScreen(new KeybindManagerScreen(this)));
		
		new WurstOptionsButton(-50, 48, () -> "X-Ray Blocks", "X射线将显示的方块的管理器。",
			b -> xRayHack.openBlockListEditor(this));
		
		new WurstOptionsButton(-50, 72, () -> "Zoom", "缩放管理器允许您更改缩放键以及缩放的距离。",
			b -> client.setScreen(new ZoomManagerScreen(this)));
	}
	
	private void addLinkButtons()
	{
		OperatingSystem os = Util.getOperatingSystem();
		
		new WurstOptionsButton(54, 24, () -> "Official Website",
			"§n§lWurstClient.net",
			b -> os.open("https://www.wurstclient.net/options-website/"));
		
		new WurstOptionsButton(54, 48, () -> "Wurst Wiki", "§n§lWurst.Wiki",
			b -> os.open("https://www.wurstclient.net/options-wiki/"));
		
		new WurstOptionsButton(54, 72, () -> "WurstForum", "§n§lWurstForum.net",
			b -> os.open("https://www.wurstclient.net/options-forum/"));
		
		new WurstOptionsButton(54, 96, () -> "Twitter", "@Wurst_Imperium",
			b -> os.open("https://www.wurstclient.net/options-twitter/"));
		
		new WurstOptionsButton(54, 120, () -> "Donate",
			"§n§lWurstClient.net/donate\n"
				+ "Donate now to help me keep the Wurst Client alive and free"
				+ " to use for everyone.\n\n"
				+ "Every bit helps and is much appreciated! You can also get a"
				+ " few cool perks in return.",
			b -> os.open("https://docs.qq.com/doc/DYWJKZ2ZtdmVPZmVY"));
	}
	
	@Override
	public void close()
	{
		client.setScreen(prevScreen);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY,
		float partialTicks)
	{
		renderTitles(context);
		
		for(Drawable drawable : drawables)
			drawable.render(context, mouseX, mouseY, partialTicks);
		
		renderButtonTooltip(context, mouseX, mouseY);
	}
	
	private void renderTitles(DrawContext context)
	{
		TextRenderer tr = client.textRenderer;
		int middleX = width / 2;
		int y1 = 40;
		int y2 = height / 4 + 24 - 28;
		
		context.drawCenteredTextWithShadow(tr,
			"Wurst选择,作者id:lroj,qq:750215287,感谢逆向燃烧帮忙汉化", middleX, y1, Colors.WHITE);
		
		context.drawCenteredTextWithShadow(tr, "设置选项", middleX - 104, y2,
			WurstColors.VERY_LIGHT_GRAY);
		context.drawCenteredTextWithShadow(tr, "Managers", middleX, y2,
			WurstColors.VERY_LIGHT_GRAY);
		context.drawCenteredTextWithShadow(tr, "链接", middleX + 104, y2,
			WurstColors.VERY_LIGHT_GRAY);
	}
	
	private void renderButtonTooltip(DrawContext context, int mouseX,
		int mouseY)
	{
		for(ClickableWidget button : Screens.getButtons(this))
		{
			if(!button.isSelected() || !(button instanceof WurstOptionsButton))
				continue;
			
			WurstOptionsButton woButton = (WurstOptionsButton)button;
			
			if(woButton.tooltip.isEmpty())
				continue;
			
			context.drawTooltip(textRenderer, woButton.tooltip, mouseX, mouseY);
			break;
		}
	}
	
	private final class WurstOptionsButton extends ButtonWidget
	{
		private final Supplier<String> messageSupplier;
		private final List<Text> tooltip;
		
		public WurstOptionsButton(int xOffset, int yOffset,
			Supplier<String> messageSupplier, String tooltip,
			PressAction pressAction)
		{
			super(WurstOptionsScreen.this.width / 2 + xOffset,
				WurstOptionsScreen.this.height / 4 - 16 + yOffset, 100, 20,
				Text.literal(messageSupplier.get()), pressAction,
				ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
			
			this.messageSupplier = messageSupplier;
			
			if(tooltip.isEmpty())
				this.tooltip = Arrays.asList();
			else
			{
				String[] lines = ChatUtils.wrapText(tooltip, 200).split("\n");
				
				Text[] lines2 = new Text[lines.length];
				for(int i = 0; i < lines.length; i++)
					lines2[i] = Text.literal(lines[i]);
				
				this.tooltip = Arrays.asList(lines2);
			}
			
			addDrawableChild(this);
		}
		
		@Override
		public void onPress()
		{
			super.onPress();
			setMessage(Text.literal(messageSupplier.get()));
		}
	}
}
