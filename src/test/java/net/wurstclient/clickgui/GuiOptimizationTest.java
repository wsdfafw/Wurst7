/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.clickgui;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import net.wurstclient.util.AnimationHelper;

public class GuiOptimizationTest
{
	@Test
	public void testWindowAnimation()
	{
		Window window = new Window("Test Window");
		
		// Test initial state
		assertEquals("Test Window", window.getTitle());
		assertFalse(window.isMinimized());
		
		// Test minimize animation
		float initialProgress = window.getMinimizeProgress();
		assertEquals(1.0f, initialProgress, 0.01f);
		
		// Minimize the window
		window.setMinimized(true);
		
		// Check that animation started
		float progressAfterMinimize = window.getMinimizeProgress();
		assertTrue(
			progressAfterMinimize >= 0.0f && progressAfterMinimize <= 1.0f);
		
		// Expand the window again
		window.setMinimized(false);
		
		// Check that animation started
		float progressAfterExpand = window.getMinimizeProgress();
		assertTrue(progressAfterExpand >= 0.0f && progressAfterExpand <= 1.0f);
	}
	
	@Test
	public void testAnimationHelper()
	{
		AnimationHelper animation = new AnimationHelper(100); // 100ms duration
		
		// Test initial state
		assertFalse(animation.isAnimating());
		assertTrue(animation.isFinished());
		assertEquals(0.0f, animation.getProgress(), 0.01f);
		
		// Start animation
		animation.start();
		assertTrue(animation.isAnimating());
		assertFalse(animation.isFinished());
		
		// Test progress increases over time
		try
		{
			Thread.sleep(50); // Wait 50ms
		}catch(InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
		
		float progress = animation.getProgress();
		assertTrue(progress > 0.0f && progress < 1.0f);
		
		// Wait for animation to complete
		try
		{
			Thread.sleep(100); // Wait 100ms
		}catch(InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
		
		assertTrue(animation.isFinished());
		assertEquals(1.0f, animation.getProgress(), 0.01f);
	}
	
	@Test
	public void testThemeSetting()
	{
		// Test theme setting functionality
		net.wurstclient.settings.ThemeSetting.Theme classic =
			net.wurstclient.settings.ThemeSetting.Theme.CLASSIC;
		net.wurstclient.settings.ThemeSetting.Theme dark =
			net.wurstclient.settings.ThemeSetting.Theme.DARK;
		net.wurstclient.settings.ThemeSetting.Theme light =
			net.wurstclient.settings.ThemeSetting.Theme.LIGHT;
		
		assertEquals("经典", classic.getName());
		assertEquals("暗色", dark.getName());
		assertEquals("亮色", light.getName());
		
		// Test color values
		assertEquals(0x404040, classic.getBgColor());
		assertEquals(0x101010, classic.getAcColor());
		assertEquals(0xF0F0F0, classic.getTxtColor());
		
		assertEquals(0x2D2D2D, dark.getBgColor());
		assertEquals(0x151515, dark.getAcColor());
		assertEquals(0xFFFFFF, dark.getTxtColor());
		
		assertEquals(0xF5F5F5, light.getBgColor());
		assertEquals(0xDDDDDD, light.getAcColor());
		assertEquals(0x000000, light.getTxtColor());
	}
}
