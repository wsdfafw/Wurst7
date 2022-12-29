/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.CameraTransformViewBobbingListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.filters.FilterInvisibleSetting;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags({"mob esp", "MobTracers", "mob tracers"})
public final class MobEspHack extends Hack implements UpdateListener,
	CameraTransformViewBobbingListener, RenderListener
{
	private final EnumSetting<Style> style =
		new EnumSetting<>("风格", Style.values(), Style.BOXES);
	
	private final EnumSetting<BoxSize> boxSize = new EnumSetting<>("框框大小",
		"§l精确§r 模式显示更加精确的\n可打击的框给每个生物.\n§l更好§r 模式会看起来框很大\n但看起来会更舒服点.",
		BoxSize.values(), BoxSize.FANCY);
	
	private final FilterInvisibleSetting filterInvisible =
		new FilterInvisibleSetting("不会显示隐身的生物.", false);
	
	private int mobBox;
	private final ArrayList<MobEntity> mobs = new ArrayList<>();
	
	public MobEspHack()
	{
		super("高亮生物");
		setCategory(Category.RENDER);
		addSetting(style);
		addSetting(boxSize);
		addSetting(filterInvisible);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(CameraTransformViewBobbingListener.class, this);
		EVENTS.add(RenderListener.class, this);
		
		mobBox = GL11.glGenLists(1);
		GL11.glNewList(mobBox, GL11.GL_COMPILE);
		Box bb = new Box(-0.5, 0, -0.5, 0.5, 1, 0.5);
		RenderUtils.drawOutlinedBox(bb);
		GL11.glEndList();
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(CameraTransformViewBobbingListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		
		GL11.glDeleteLists(mobBox, 1);
		mobBox = 0;
	}
	
	@Override
	public void onUpdate()
	{
		mobs.clear();
		
		Stream<MobEntity> stream =
			StreamSupport.stream(MC.world.getEntities().spliterator(), false)
				.filter(e -> e instanceof MobEntity).map(e -> (MobEntity)e)
				.filter(e -> !e.removed && e.getHealth() > 0);
		
		if(filterInvisible.isChecked())
			stream = stream.filter(filterInvisible);
		
		mobs.addAll(stream.collect(Collectors.toList()));
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
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(2);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		
		GL11.glPushMatrix();
		RenderUtils.applyRegionalRenderOffset();
		
		BlockPos camPos = RenderUtils.getCameraBlockPos();
		int regionX = (camPos.getX() >> 9) * 512;
		int regionZ = (camPos.getZ() >> 9) * 512;
		
		if(style.getSelected().boxes)
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
		
		for(MobEntity e : mobs)
		{
			GL11.glPushMatrix();
			
			GL11.glTranslated(
				e.prevX + (e.getX() - e.prevX) * partialTicks - regionX,
				e.prevY + (e.getY() - e.prevY) * partialTicks,
				e.prevZ + (e.getZ() - e.prevZ) * partialTicks - regionZ);
			
			GL11.glScaled(e.getWidth() + extraSize, e.getHeight() + extraSize,
				e.getWidth() + extraSize);
			
			float f = MC.player.distanceTo(e) / 20F;
			GL11.glColor4f(2 - f, f, 0, 0.5F);
			
			GL11.glCallList(mobBox);
			
			GL11.glPopMatrix();
		}
	}
	
	private void renderTracers(double partialTicks, int regionX, int regionZ)
	{
		Vec3d start =
			RotationUtils.getClientLookVec().add(RenderUtils.getCameraPos());
		
		GL11.glBegin(GL11.GL_LINES);
		for(MobEntity e : mobs)
		{
			Vec3d end = e.getBoundingBox().getCenter()
				.subtract(new Vec3d(e.getX(), e.getY(), e.getZ())
					.subtract(e.prevX, e.prevY, e.prevZ)
					.multiply(1 - partialTicks));
			
			float f = MC.player.distanceTo(e) / 20F;
			GL11.glColor4f(2 - f, f, 0, 0.5F);
			
			GL11.glVertex3d(start.x - regionX, start.y, start.z - regionZ);
			GL11.glVertex3d(end.x - regionX, end.y, end.z - regionZ);
		}
		GL11.glEnd();
	}
	
	private enum Style
	{
		BOXES("仅限框框", true, false),
		LINES("仅限线条", false, true),
		LINES_AND_BOXES("线条和框", true, true);
		
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
		ACCURATE("精确", 0),
		FANCY("更好", 0.1);
		
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
