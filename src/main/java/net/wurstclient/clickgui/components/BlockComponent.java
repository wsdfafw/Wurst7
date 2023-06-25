/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.clickgui.components;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.screens.EditBlockScreen;
import net.wurstclient.settings.BlockSetting;
import net.wurstclient.util.RenderUtils;

public final class BlockComponent extends Component
{
	private static final int BLOCK_WITDH = 24;
	private final BlockSetting setting;
	
	public BlockComponent(BlockSetting setting)
	{
		this.setting = setting;
		
		setWidth(getDefaultWidth());
		setHeight(getDefaultHeight());
	}
	
	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton)
	{
		if(mouseX < getX() + getWidth() - BLOCK_WITDH)
			return;
		
		if(mouseButton == 0)
		{
			Screen currentScreen = WurstClient.MC.currentScreen;
			EditBlockScreen editScreen =
				new EditBlockScreen(currentScreen, setting);
			WurstClient.MC.openScreen(editScreen);
			
		}else if(mouseButton == 1)
			setting.resetToDefault();
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY,
		float partialTicks)
	{
		ClickGui gui = WurstClient.INSTANCE.getGui();
		float[] bgColor = gui.getBgColor();
		int txtColor = gui.getTxtColor();
		float opacity = gui.getOpacity();
		
		int x1 = getX();
		int x2 = x1 + getWidth();
		int x3 = x2 - BLOCK_WITDH;
		int y1 = getY();
		int y2 = y1 + getHeight();
		
		int scroll = getParent().isScrollingEnabled()
			? getParent().getScrollOffset() : 0;
		boolean hovering = mouseX >= x1 && mouseY >= y1 && mouseX < x2
			&& mouseY < y2 && mouseY >= -scroll
			&& mouseY < getParent().getHeight() - 13 - scroll;
		boolean hText = hovering && mouseX < x3;
		boolean hBlock = hovering && mouseX >= x3;
		
		ItemStack stack = new ItemStack(setting.getBlock());
		
		// tooltip
		if(hText)
			gui.setTooltip(setting.getWrappedDescription(200));
		else if(hBlock)
		{
			String tooltip = "\u00a76名字:\u00a7r " + getBlockName(stack);
			tooltip += "\n\u00a76ID:\u00a7r " + setting.getBlockName();
			tooltip += "\n\n\u00a7e[左键]\u00a7r 编辑";
			tooltip += "\n\u00a7e[右键]\u00a7r 重置";
			gui.setTooltip(tooltip);
		}
		
		// background
		GL11.glColor4f(bgColor[0], bgColor[1], bgColor[2], opacity);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(x1, y1);
		GL11.glVertex2i(x1, y2);
		GL11.glVertex2i(x2, y2);
		GL11.glVertex2i(x2, y1);
		GL11.glEnd();
		
		// setting name
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		TextRenderer fr = WurstClient.MC.textRenderer;
		String text = setting.getName() + ":";
		fr.draw(matrixStack, text, x1, y1 + 2, txtColor);
		
		RenderUtils.drawItem(matrixStack, stack, x3, y1, true);
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
	}
	
	@Override
	public int getDefaultWidth()
	{
		TextRenderer tr = WurstClient.MC.textRenderer;
		String text = setting.getName() + ":";
		return tr.getWidth(text) + BLOCK_WITDH + 4;
	}
	
	@Override
	public int getDefaultHeight()
	{
		return BLOCK_WITDH;
	}
	
	private String getBlockName(ItemStack stack)
	{
		if(stack.isEmpty())
			return "\u00a7ounknown block\u00a7r";
		return stack.getName().getString();
	}
}
