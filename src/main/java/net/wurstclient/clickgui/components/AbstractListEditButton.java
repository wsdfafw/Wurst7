/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.clickgui.components;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.Component;
import net.wurstclient.settings.Setting;

public abstract class AbstractListEditButton extends Component
{
	protected static final MinecraftClient MC = WurstClient.MC;
	
	private final String buttonText = "Edit...";
	private final int buttonWidth;
	
	public AbstractListEditButton()
	{
		buttonWidth = MC.textRenderer.getWidth(buttonText);
	}
	
	protected abstract void openScreen();
	
	protected abstract String getText();
	
	protected abstract Setting getSetting();
	
	@Override
	public void handleMouseClick(double mouseX, double mouseY, int mouseButton)
	{
		if(mouseButton != 0)
			return;
		
		if(mouseX < getX() + getWidth() - buttonWidth - 4)
			return;
		
		openScreen();
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY,
		float partialTicks)
	{
		ClickGui gui = WurstClient.INSTANCE.getGui();
		float[] bgColor = gui.getBgColor();
		float[] acColor = gui.getAcColor();
		int txtColor = gui.getTxtColor();
		float opacity = gui.getOpacity();
		
		int x1 = getX();
		int x2 = x1 + getWidth();
		int x3 = x2 - buttonWidth - 4;
		int y1 = getY();
		int y2 = y1 + getHeight();
		
		int scroll = getParent().isScrollingEnabled()
			? getParent().getScrollOffset() : 0;
		boolean hovering = mouseX >= x1 && mouseY >= y1 && mouseX < x2
			&& mouseY < y2 && mouseY >= -scroll
			&& mouseY < getParent().getHeight() - 13 - scroll;
		boolean hText = hovering && mouseX < x3;
		boolean hBox = hovering && mouseX >= x3;
		
		// tooltip
		if(hText)
			gui.setTooltip(getSetting().getWrappedDescription(200));
		
		// background
		GL11.glColor4f(bgColor[0], bgColor[1], bgColor[2], opacity);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(x1, y1);
		GL11.glVertex2i(x1, y2);
		GL11.glVertex2i(x3, y2);
		GL11.glVertex2i(x3, y1);
		GL11.glEnd();
		
		// box
		GL11.glColor4f(bgColor[0], bgColor[1], bgColor[2],
			hBox ? opacity * 1.5F : opacity);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2i(x3, y1);
		GL11.glVertex2i(x3, y2);
		GL11.glVertex2i(x2, y2);
		GL11.glVertex2i(x2, y1);
		GL11.glEnd();
		GL11.glColor4f(acColor[0], acColor[1], acColor[2], 0.5F);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glVertex2i(x3, y1);
		GL11.glVertex2i(x3, y2);
		GL11.glVertex2i(x2, y2);
		GL11.glVertex2i(x2, y1);
		GL11.glEnd();
		
		// setting name
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		TextRenderer fr = MC.textRenderer;
		fr.draw(matrixStack, getText(), x1, y1 + 2, txtColor);
		fr.draw(matrixStack, buttonText, x3 + 2, y1 + 2, txtColor);
		GL11.glEnable(GL11.GL_BLEND);
	}
	
	@Override
	public int getDefaultWidth()
	{
		TextRenderer fr = MC.textRenderer;
		return fr.getWidth(getText()) + buttonWidth + 6;
	}
	
	@Override
	public int getDefaultHeight()
	{
		return 11;
	}
}
