/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.keybinds.PossibleKeybind;

public class NavigatorRemoveKeybindScreen extends NavigatorScreen
{
	private NavigatorFeatureScreen parent;
	private TreeMap<String, PossibleKeybind> existingKeybinds;
	private String hoveredKey = "";
	private String selectedKey = "";
	private String text = "选择你想要移除的按键.";
	private ButtonWidget removeButton;
	
	public NavigatorRemoveKeybindScreen(
		TreeMap<String, PossibleKeybind> existingKeybinds,
		NavigatorFeatureScreen parent)
	{
		this.existingKeybinds = existingKeybinds;
		this.parent = parent;
	}
	
	@Override
	protected void onResize()
	{
		removeButton =
			ButtonWidget.builder(Text.literal("Remove"), b -> remove())
				.dimensions(width / 2 - 151, height - 65, 149, 18).build();
		removeButton.active = !selectedKey.isEmpty();
		addDrawableChild(removeButton);
		
		addDrawableChild(ButtonWidget
			.builder(Text.literal("Cancel"), b -> client.setScreen(parent))
			.dimensions(width / 2 + 2, height - 65, 149, 18).build());
	}
	
	private void remove()
	{
		String oldCommands =
			WurstClient.INSTANCE.getKeybinds().getCommands(selectedKey);
		if(oldCommands == null)
			return;
		
		ArrayList<String> commandsList =
			new ArrayList<>(Arrays.asList(oldCommands.replace(";", "\u00a7")
				.replace("\u00a7\u00a7", ";").split("\u00a7")));
		
		for(int i = 0; i < commandsList.size(); i++)
			commandsList.set(i, commandsList.get(i).trim());
		
		String command = existingKeybinds.get(selectedKey).getCommand();
		while(commandsList.contains(command))
			commandsList.remove(command);
		
		if(commandsList.isEmpty())
			WurstClient.INSTANCE.getKeybinds().remove(selectedKey);
		else
		{
			String newCommands = String.join("\u00a7", commandsList)
				.replace(";", "\u00a7\u00a7").replace("\u00a7", ";");
			WurstClient.INSTANCE.getKeybinds().add(selectedKey, newCommands);
		}
		
		WurstClient.INSTANCE.getNavigator()
			.addPreference(parent.getFeature().getName());
		
		client.setScreen(parent);
	}
	
	@Override
	protected void onKeyPress(int keyCode, int scanCode, int int_3)
	{
		if(keyCode == GLFW.GLFW_KEY_ESCAPE
			|| keyCode == GLFW.GLFW_KEY_BACKSPACE)
			client.setScreen(parent);
	}
	
	@Override
	protected void onMouseClick(double x, double y, int button)
	{
		// back button
		if(button == GLFW.GLFW_MOUSE_BUTTON_4)
		{
			WurstClient.MC.setScreen(parent);
			return;
		}
		
		// commands
		if(!hoveredKey.isEmpty())
		{
			selectedKey = hoveredKey;
			removeButton.active = true;
		}
	}
	
	@Override
	protected void onUpdate()
	{
		setContentHeight(existingKeybinds.size() * 24 - 10);
	}
	
	@Override
	protected void onRender(DrawContext context, int mouseX, int mouseY,
		float partialTicks)
	{
		ClickGui gui = WurstClient.INSTANCE.getGui();
		TextRenderer tr = client.textRenderer;
		int txtColor = gui.getTxtColor();
		
		// title bar
		context.drawCenteredTextWithShadow(tr, "删除 Keybind", middleX, 32,
			txtColor);
		
		// background
		int bgx1 = middleX - 154;
		int bgx2 = middleX + 154;
		int bgy1 = 60;
		int bgy2 = height - 43;
		boolean noButtons = Screens.getButtons(this).isEmpty();
		int bgy3 = bgy2 - (noButtons ? 0 : 24);
		
		context.enableScissor(bgx1, bgy1, bgx2, bgy3);
		
		// possible keybinds
		hoveredKey = "";
		int yi = bgy1 - 12 + scroll;
		for(Entry<String, PossibleKeybind> entry : existingKeybinds.entrySet())
		{
			String key = entry.getKey();
			PossibleKeybind keybind = entry.getValue();
			yi += 24;
			
			// positions
			int x1 = bgx1 + 2;
			int x2 = bgx2 - 2;
			int y1 = yi;
			int y2 = y1 + 20;
			
			// color
			int buttonColor;
			if(mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2)
			{
				hoveredKey = key;
				if(key.equals(selectedKey))
					buttonColor = 0x6000FF00;
				else
					buttonColor = 0x60404040;
			}else if(key.equals(selectedKey))
				buttonColor = 0x4000FF00;
			else
				buttonColor = 0x40404040;
			
			// button
			drawBox(context, x1, y1, x2, y2, buttonColor);
			
			// text
			context.state.goUpLayer();
			context.drawTextWithShadow(tr, key.replace("key.keyboard.", "")
				+ ": " + keybind.getDescription(), x1 + 1, y1 + 1, txtColor);
			context.drawTextWithShadow(tr, keybind.getCommand(), x1 + 1,
				y1 + 1 + tr.fontHeight, txtColor);
			context.state.goDownLayer();
		}
		
		// text
		int textY = bgy1 + scroll + 2;
		context.state.goUpLayer();
		for(String line : text.split("\n"))
		{
			context.drawTextWithShadow(tr, line, bgx1 + 2, textY, txtColor);
			textY += tr.fontHeight;
		}
		context.state.goDownLayer();
		
		context.disableScissor();
		
		// buttons below scissor box
		for(ClickableWidget button : Screens.getButtons(this))
		{
			// positions
			int x1 = button.getX();
			int x2 = x1 + button.getWidth();
			int y1 = button.getY();
			int y2 = y1 + 18;
			
			// color
			int buttonColor;
			if(!button.active)
				buttonColor = 0x40000000;
			else if(mouseX >= x1 && mouseX <= x2 && mouseY >= y1
				&& mouseY <= y2)
				buttonColor = 0x40606060;
			else
				buttonColor = 0x40404040;
			
			// button
			drawBox(context, x1, y1, x2, y2, buttonColor);
			
			// text
			String buttonText = button.getMessage().getString();
			context.state.goUpLayer();
			context.drawCenteredTextWithShadow(tr, buttonText, (x1 + x2) / 2,
				y1 + 5, txtColor);
			context.state.goDownLayer();
		}
	}
	
	@Override
	protected void onMouseDrag(double mouseX, double mouseY, int button,
		double double_3, double double_4)
	{
		
	}
	
	@Override
	protected void onMouseRelease(double x, double y, int button)
	{
		
	}
	
	@Override
	public boolean shouldCloseOnEsc()
	{
		return false;
	}
}
