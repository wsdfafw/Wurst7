/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.awt.Color;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.CameraTransformViewBobbingListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags({"item esp", "ItemTracers", "item tracers"})
public final class ItemEspHack extends Hack implements UpdateListener,
	CameraTransformViewBobbingListener, RenderListener
{
	private final EnumSetting<Style> style =
		new EnumSetting<>("风格", Style.values(), Style.BOXES);
	
	private final EnumSetting<BoxSize> boxSize = new EnumSetting<>("框框大小",
		"§l精确§r 模式显示一个精确\n的可打击的范围.\n§l更好§r 模式显示一个更大的\n框框,看起来会舒服点.",
		BoxSize.values(), BoxSize.FANCY);
	
	private final ColorSetting color = new ColorSetting("颜色",
		"物品将会以这种颜色高亮.", Color.YELLOW);
	
	private int itemBox;
	private final ArrayList<ItemEntity> items = new ArrayList<>();
	
	public ItemEspHack()
	{
		super("物品透视");
		setCategory(Category.RENDER);
		
		addSetting(style);
		addSetting(boxSize);
		addSetting(color);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(CameraTransformViewBobbingListener.class, this);
		EVENTS.add(RenderListener.class, this);
		
		itemBox = GL11.glGenLists(1);
		GL11.glNewList(itemBox, GL11.GL_COMPILE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		RenderUtils.drawOutlinedBox(new Box(-0.5, 0, -0.5, 0.5, 1, 0.5));
		GL11.glEndList();
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(CameraTransformViewBobbingListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		
		GL11.glDeleteLists(itemBox, 1);
	}
	
	@Override
	public void onUpdate()
	{
		items.clear();
		for(Entity entity : MC.world.getEntities())
			if(entity instanceof ItemEntity)
				items.add((ItemEntity)entity);
	}
	
	@Override
	public void onCameraTransformViewBobbing(
		CameraTransformViewBobbingEvent event)
	{
		if(style.getSelected().lines)
			event.cancel();
	}
	
	@Override
	public void onRender(float partialTicks)
	{
		// GL settings
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(2);
		GL11.glDisable(GL11.GL_LIGHTING);
		
		GL11.glPushMatrix();
		RenderUtils.applyRegionalRenderOffset();
		
		BlockPos camPos = RenderUtils.getCameraBlockPos();
		int regionX = (camPos.getX() >> 9) * 512;
		int regionZ = (camPos.getZ() >> 9) * 512;
		
		renderBoxes(partialTicks, regionX, regionZ);
		
		if(style.getSelected().lines)
			renderTracers(partialTicks, regionX, regionZ);
		
		GL11.glPopMatrix();
		
		// GL resets
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
	
	private void renderBoxes(double partialTicks, int regionX, int regionZ)
	{
		double extraSize = boxSize.getSelected().extraSize;
		
		for(ItemEntity e : items)
		{
			GL11.glPushMatrix();
			
			GL11.glTranslated(
				e.prevX + (e.getX() - e.prevX) * partialTicks - regionX,
				e.prevY + (e.getY() - e.prevY) * partialTicks,
				e.prevZ + (e.getZ() - e.prevZ) * partialTicks - regionZ);
			
			if(style.getSelected().boxes)
			{
				GL11.glPushMatrix();
				GL11.glScaled(e.getWidth() + extraSize,
					e.getHeight() + extraSize, e.getWidth() + extraSize);
				float[] colorF = color.getColorF();
				GL11.glColor4f(colorF[0], colorF[1], colorF[2], 0.5F);
				GL11.glCallList(itemBox);
				GL11.glPopMatrix();
			}
			
			GL11.glPopMatrix();
		}
	}
	
	private void renderTracers(double partialTicks, int regionX, int regionZ)
	{
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		float[] colorF = color.getColorF();
		GL11.glColor4f(colorF[0], colorF[1], colorF[2], 0.5F);
		
		Vec3d start =
			RotationUtils.getClientLookVec().add(RenderUtils.getCameraPos());
		
		GL11.glBegin(GL11.GL_LINES);
		for(ItemEntity e : items)
		{
			Vec3d end = e.getBoundingBox().getCenter()
				.subtract(new Vec3d(e.getX(), e.getY(), e.getZ())
					.subtract(e.prevX, e.prevY, e.prevZ)
					.multiply(1 - partialTicks));
			
			GL11.glVertex3d(start.x - regionX, start.y, start.z - regionZ);
			GL11.glVertex3d(end.x - regionX, end.y, end.z - regionZ);
		}
		GL11.glEnd();
	}
	
	private enum Style
	{
		BOXES("Boxes only", true, false),
		LINES("Lines only", false, true),
		LINES_AND_BOXES("Lines and boxes", true, true);
		
		private final String name;
		private final boolean boxes;
		private final boolean lines;
		
		private Style(String name, boolean boxes, boolean lines)
		{
			this.name = name;
			this.boxes = boxes;
			this.lines = lines;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
	
	private enum BoxSize
	{
		ACCURATE("Accurate", 0),
		FANCY("Fancy", 0.1);
		
		private final String name;
		private final double extraSize;
		
		private BoxSize(String name, double extraSize)
		{
			this.name = name;
			this.extraSize = extraSize;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
