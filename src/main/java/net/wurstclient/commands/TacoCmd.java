/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import net.wurstclient.Category;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.events.GUIRenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.util.RenderUtils;

public final class TacoCmd extends Command
	implements GUIRenderListener, UpdateListener
{
	private final Identifier[] tacos =
		{Identifier.of("wurst", "dancingtaco1.png"),
			Identifier.of("wurst", "dancingtaco2.png"),
			Identifier.of("wurst", "dancingtaco3.png"),
			Identifier.of("wurst", "dancingtaco4.png")};
	
	private boolean enabled;
	private int ticks = 0;
	
	public TacoCmd()
	{
		super("taco", "在你的物品栏上方生成一个塔可.\n"
			+ "\"I love that little guy. So cute!\" -WiZARD");
		setCategory(Category.FUN);
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length != 0)
			throw new CmdSyntaxError("墨西哥卷不需要任何参数!");
		
		enabled = !enabled;
		
		if(enabled)
		{
			EVENTS.add(GUIRenderListener.class, this);
			EVENTS.add(UpdateListener.class, this);
			
		}else
		{
			EVENTS.remove(GUIRenderListener.class, this);
			EVENTS.remove(UpdateListener.class, this);
		}
	}
	
	@Override
	public String getPrimaryAction()
	{
		return "成为老板!";
	}
	
	@Override
	public void doPrimaryAction()
	{
		WURST.getCmdProcessor().process("taco");
	}
	
	@Override
	public void onUpdate()
	{
		if(ticks >= 31)
			ticks = 0;
		else
			ticks++;
	}
	
	@Override
	public void onRenderGUI(DrawContext context, float partialTicks)
	{
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		if(WURST.getHax().rainbowUiHack.isEnabled())
			RenderUtils.setShaderColor(WURST.getGui().getAcColor(), 1);
		else
			RenderSystem.setShaderColor(1, 1, 1, 1);
		
		Window sr = MC.getWindow();
		int x = sr.getScaledWidth() / 2 - 32 + 76;
		int y = sr.getScaledHeight() - 32 - 19;
		int w = 64;
		int h = 32;
		context.drawTexture(RenderLayer::getGuiTextured, tacos[ticks / 8], x, y,
			0, 0, w, h, w, h);
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
