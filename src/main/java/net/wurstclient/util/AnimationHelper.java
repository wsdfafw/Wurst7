/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.util;

public class AnimationHelper
{
	private long startTime;
	private long duration;
	private boolean isAnimating;
	private boolean isReverse;
	private long pauseTime;
	private boolean isPaused;
	
	public AnimationHelper(long duration)
	{
		this.duration = duration;
		this.startTime = System.currentTimeMillis();
		this.isAnimating = false;
		this.isReverse = false;
		this.isPaused = false;
	}
	
	public void start()
	{
		startTime = System.currentTimeMillis()
			- (isPaused ? (pauseTime - startTime) : 0);
		isAnimating = true;
		isPaused = false;
		isReverse = false;
	}
	
	public void startReverse()
	{
		startTime = System.currentTimeMillis()
			- (isPaused ? (pauseTime - startTime) : 0);
		isAnimating = true;
		isPaused = false;
		isReverse = true;
	}
	
	public void pause()
	{
		if(isAnimating && !isPaused)
		{
			pauseTime = System.currentTimeMillis();
			isPaused = true;
		}
	}
	
	public void resume()
	{
		if(isAnimating && isPaused)
		{
			startTime = System.currentTimeMillis() - (pauseTime - startTime);
			isPaused = false;
		}
	}
	
	public void stop()
	{
		isAnimating = false;
		isPaused = false;
	}
	
	public boolean isAnimating()
	{
		return isAnimating && !isFinished();
	}
	
	public boolean isFinished()
	{
		if(!isAnimating)
			return true;
		
		if(isPaused)
			return false;
		
		long elapsed = System.currentTimeMillis() - startTime;
		return elapsed >= duration;
	}
	
	public float getProgress()
	{
		if(!isAnimating)
			return 0f;
		
		if(isPaused)
			return Math.min(1f,
				Math.max(0f, (pauseTime - startTime) / (float)duration));
		
		long elapsed = System.currentTimeMillis() - startTime;
		float progress = Math.min(1f, Math.max(0f, elapsed / (float)duration));
		
		return isReverse ? (1f - progress) : progress;
	}
	
	public float getSmoothProgress()
	{
		float progress = getProgress();
		// Use smoothstep function for smoother animation
		return progress * progress * (3 - 2 * progress);
	}
	
	public void setDuration(long duration)
	{
		this.duration = duration;
	}
	
	public long getDuration()
	{
		return duration;
	}
	
	public static float easeOut(float t)
	{
		return t * t * t;
	}
	
	public static float easeInOut(float t)
	{
		return t < 0.5 ? 4 * t * t * t
			: (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
	}
}
