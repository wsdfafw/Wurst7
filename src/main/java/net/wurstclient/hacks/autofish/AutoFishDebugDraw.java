/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks.autofish;

import java.awt.Color;
import java.util.stream.Stream;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.WurstClient;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.RenderUtils;

public final class AutoFishDebugDraw
{
	private final CheckboxSetting debugDraw = new CheckboxSetting("调试绘图",
		"显示叮咬发生的位置以及它们将被检测到的位置。有助于优化您的“有效范围”设置.",
		false);
	
	private final ColorSetting ddColor = new ColorSetting("DD颜色",
		"调试绘图的颜色(如果启用).", Color.RED);
	
	private final SliderSetting validRange;
	private Vec3d lastSoundPos;
	
	public AutoFishDebugDraw(SliderSetting validRange)
	{
		this.validRange = validRange;
	}
	
	public Stream<Setting> getSettings()
	{
		return Stream.of(debugDraw, ddColor);
	}
	
	public void reset()
	{
		lastSoundPos = null;
	}
	
	public void updateSoundPos(PlaySoundS2CPacket sound)
	{
		lastSoundPos = new Vec3d(sound.getX(), sound.getY(), sound.getZ());
	}
	
	public void render(float partialTicks)
	{
		if(!debugDraw.isChecked())
			return;
		
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glLineWidth(2);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		
		GL11.glPushMatrix();
		
		BlockPos camPos = RenderUtils.getCameraBlockPos();
		int regionX = (camPos.getX() >> 9) * 512;
		int regionZ = (camPos.getZ() >> 9) * 512;
		RenderUtils.applyRegionalRenderOffset(regionX, regionZ);
		
		FishingBobberEntity bobber = WurstClient.MC.player.fishHook;
		if(bobber != null)
			drawValidRange(partialTicks, bobber, regionX, regionZ);
		
		if(lastSoundPos != null)
			drawLastBite(regionX, regionZ);
		
		GL11.glPopMatrix();
		
		// GL resets
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	private void drawValidRange(float partialTicks, FishingBobberEntity bobber,
		int regionX, int regionZ)
	{
		GL11.glPushMatrix();
		Vec3d pos = EntityUtils.getLerpedPos(bobber, partialTicks);
		GL11.glTranslated(pos.getX() - regionX, pos.getY(),
			pos.getZ() - regionZ);
		
		float[] colorF = ddColor.getColorF();
		GL11.glColor4f(colorF[0], colorF[1], colorF[2], 0.5F);
		
		double vr = validRange.getValue();
		Box vrBox = new Box(-vr, -1 / 16.0, -vr, vr, 1 / 16.0, vr);
		RenderUtils.drawOutlinedBox(vrBox);
		
		GL11.glPopMatrix();
	}
	
	private void drawLastBite(int regionX, int regionZ)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(lastSoundPos.x - regionX, lastSoundPos.y,
			lastSoundPos.z - regionZ);
		
		float[] colorF = ddColor.getColorF();
		GL11.glColor4f(colorF[0], colorF[1], colorF[2], 0.5F);
		
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3d(-0.125, 0, -0.125);
		GL11.glVertex3d(0.125, 0, 0.125);
		GL11.glVertex3d(0.125, 0, -0.125);
		GL11.glVertex3d(-0.125, 0, 0.125);
		GL11.glEnd();
		
		GL11.glPopMatrix();
	}
}
