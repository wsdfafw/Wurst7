/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.serverfinder;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.wurstclient.mixinterface.IMultiplayerScreen;

public class CleanUpScreen extends Screen
{
	private MultiplayerScreen prevScreen;
	private ButtonWidget cleanUpButton;
	
	private boolean removeAll;
	private boolean cleanupFailed = true;
	private boolean cleanupOutdated = true;
	private boolean cleanupRename = true;
	private boolean cleanupUnknown = true;
	private boolean cleanupGriefMe;
	
	public CleanUpScreen(MultiplayerScreen prevScreen)
	{
		super(Text.literal(""));
		this.prevScreen = prevScreen;
	}
	
	@Override
	public void init()
	{
		addDrawableChild(new CleanUpButton(width / 2 - 100,
			height / 4 + 168 + 12, () -> "取消", "", b -> close()));
		
		addDrawableChild(cleanUpButton = new CleanUpButton(width / 2 - 100,
			height / 4 + 144 + 12, () -> "清理",
			"Start the Clean Up with the settings\n" + "you specified above.\n"
				+ "It might look like the game is not\n"
				+ "responding for a couple of seconds.",
			b -> cleanUp()));
		
		addDrawableChild(
			new CleanUpButton(width / 2 - 100, height / 4 - 24 + 12,
				() -> "未知的主机: " + removeOrKeep(cleanupUnknown), "显然不存在的服务器.",
				b -> cleanupUnknown = !cleanupUnknown));
		
		addDrawableChild(new CleanUpButton(width / 2 - 100, height / 4 + 0 + 12,
			() -> "过期的版本: " + removeOrKeep(cleanupOutdated),
			"运行不同Minecraft版本的服务器，而不是你的.",
			b -> cleanupOutdated = !cleanupOutdated));
		
		addDrawableChild(
			new CleanUpButton(width / 2 - 100, height / 4 + 24 + 12,
				() -> "无法ping: " + removeOrKeep(cleanupFailed),
				"All servers that failed the last ping.\n"
					+ "Make sure that the last ping is complete\n"
					+ "before you do this. That means: Go back,\n"
					+ "press the refresh button and wait until\n"
					+ "all servers are done refreshing.",
				b -> cleanupFailed = !cleanupFailed));
		
		addDrawableChild(
			new CleanUpButton(width / 2 - 100, height / 4 + 48 + 12,
				() -> "\"Grief me\" 服务器: " + removeOrKeep(cleanupGriefMe),
				"All servers where name starts with \"Grief me\"\n"
					+ "用于移除由ServerFinder发现的服务器.",
				b -> cleanupGriefMe = !cleanupGriefMe));
		
		addDrawableChild(
			new CleanUpButton(width / 2 - 100, height / 4 + 72 + 12,
				() -> "\u00a7c移除所有的服务器: " + yesOrNo(removeAll),
				"This will completely clear your server\n"
					+ "list. \u00a7cUse with caution!\u00a7r",
				b -> removeAll = !removeAll));
		
		addDrawableChild(new CleanUpButton(width / 2 - 100,
			height / 4 + 96 + 12, () -> "重命名所有服务器: " + yesOrNo(cleanupRename),
			"Renames your servers to \"Grief me #1\",\n"
				+ "\"Grief me #2\", etc.",
			b -> cleanupRename = !cleanupRename));
	}
	
	private String yesOrNo(boolean b)
	{
		return b ? "是" : "否";
	}
	
	private String removeOrKeep(boolean b)
	{
		return b ? "移除" : "保留";
	}
	
	private void cleanUp()
	{
		for(int i = prevScreen.getServerList().size() - 1; i >= 0; i--)
		{
			ServerInfo server = prevScreen.getServerList().get(i);
			
			if(removeAll || shouldRemove(server))
				prevScreen.getServerList().remove(server);
		}
		
		if(cleanupRename)
			for(int i = 0; i < prevScreen.getServerList().size(); i++)
			{
				ServerInfo server = prevScreen.getServerList().get(i);
				server.name = "Grief me #" + (i + 1);
			}
		
		saveServerList();
		client.setScreen(prevScreen);
	}
	
	private boolean shouldRemove(ServerInfo server)
	{
		if(server == null)
			return false;
		
		if(cleanupUnknown && isUnknownHost(server))
			return true;
		
		if(cleanupOutdated && !isSameProtocol(server))
			return true;
		
		if(cleanupFailed && isFailedPing(server))
			return true;
		
		if(cleanupGriefMe && isGriefMeServer(server))
			return true;
		
		return false;
	}
	
	private boolean isUnknownHost(ServerInfo server)
	{
		if(server.label == null)
			return false;
		
		if(server.label.getString() == null)
			return false;
		
		return server.label.getString()
			.equals("\u00a74Can\'t resolve hostname");
	}
	
	private boolean isSameProtocol(ServerInfo server)
	{
		return server.protocolVersion == SharedConstants.getGameVersion()
			.protocolVersion();
	}
	
	private boolean isFailedPing(ServerInfo server)
	{
		return server.ping != -2L && server.ping < 0L;
	}
	
	private boolean isGriefMeServer(ServerInfo server)
	{
		return server.name != null && server.name.startsWith("Grief me");
	}
	
	private void saveServerList()
	{
		prevScreen.getServerList().saveFile();
		
		MultiplayerServerListWidget serverListSelector =
			((IMultiplayerScreen)prevScreen).getServerListSelector();
		
		serverListSelector.setSelected(null);
		serverListSelector.setServers(prevScreen.getServerList());
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int int_3)
	{
		if(keyCode == GLFW.GLFW_KEY_ENTER)
			cleanUpButton.onPress();
		
		return super.keyPressed(keyCode, scanCode, int_3);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		if(button == GLFW.GLFW_MOUSE_BUTTON_4)
		{
			close();
			return true;
		}
		
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY,
		float partialTicks)
	{
		context.drawCenteredTextWithShadow(textRenderer, "Clean Up", width / 2,
			20, 16777215);
		context.drawCenteredTextWithShadow(textRenderer,
			"Please select the servers you want to remove:", width / 2, 36,
			10526880);
		
		for(Drawable drawable : drawables)
			drawable.render(context, mouseX, mouseY, partialTicks);
		
		renderButtonTooltip(context, mouseX, mouseY);
	}
	
	private void renderButtonTooltip(DrawContext context, int mouseX,
		int mouseY)
	{
		for(ClickableWidget button : Screens.getButtons(this))
		{
			if(!button.isSelected() || !(button instanceof CleanUpButton))
				continue;
			
			CleanUpButton cuButton = (CleanUpButton)button;
			
			if(cuButton.tooltip.isEmpty())
				continue;
			
			context.drawTooltip(textRenderer, cuButton.tooltip, mouseX, mouseY);
			break;
		}
	}
	
	@Override
	public void close()
	{
		client.setScreen(prevScreen);
	}
	
	private final class CleanUpButton extends ButtonWidget
	{
		private final Supplier<String> messageSupplier;
		private final List<Text> tooltip;
		
		public CleanUpButton(int x, int y, Supplier<String> messageSupplier,
			String tooltip, PressAction pressAction)
		{
			super(x, y, 200, 20, Text.literal(messageSupplier.get()),
				pressAction, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
			this.messageSupplier = messageSupplier;
			
			if(tooltip.isEmpty())
				this.tooltip = Arrays.asList();
			else
			{
				String[] lines = tooltip.split("\n");
				
				Text[] lines2 = new Text[lines.length];
				for(int i = 0; i < lines.length; i++)
					lines2[i] = Text.literal(lines[i]);
				
				this.tooltip = Arrays.asList(lines2);
			}
		}
		
		@Override
		public void onPress()
		{
			super.onPress();
			setMessage(Text.literal(messageSupplier.get()));
		}
	}
}
