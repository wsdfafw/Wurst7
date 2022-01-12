/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.client.option.GameOptions;
import net.minecraft.util.math.MathHelper;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"Fullbrightness", "full brightness", "Fulbrightness",
	"ful brightness", "NightVision", "night vision", "FullLightness",
	"FulLightness", "full lightness", "FullGamma", "full gamma"})
public final class FullbrightHack extends Hack implements UpdateListener
{
	private final EnumSetting<Method> method = new EnumSetting("方法", "§l光亮Gamma§r 会设置你的\n的伽马值超过100%,但不会在有光影条件下工作.\n\n§l夜视§r 利用夜视效果的方法\n这个 §o通常§r 兼容\n光影水反效果.", (Enum[])Method.values(), (Enum)Method.GAMMA);
    private final CheckboxSetting fade = new CheckboxSetting("渐变", "在明亮和黑暗之间转变的渐变.", true);
    private final SliderSetting defaultGamma = new SliderSetting("默认亮度", "无限夜视功能 关闭后将会设置的你的\n光亮度回到这个数值.", 0.5, 0.0, 1.0, 0.01, SliderSetting.ValueDisplay.PERCENTAGE);
	
	private boolean wasGammaChanged;
	private float nightVisionStrength;
	
	public FullbrightHack()
	{
		super("Fullbright");
		setCategory(Category.RENDER);
		addSetting(method);
		addSetting(fade);
		addSetting(defaultGamma);
		
		checkGammaOnStartup();
		EVENTS.add(UpdateListener.class, this);
	}
	
	private void checkGammaOnStartup()
	{
		EVENTS.add(UpdateListener.class, new UpdateListener()
		{
			@Override
			public void onUpdate()
			{
				double gamma = MC.options.gamma;
				System.out.println("亮度开始在 " + gamma);
				
				if(gamma > 1)
					wasGammaChanged = true;
				else
					defaultGamma.setValue(gamma);
				
				EVENTS.remove(UpdateListener.class, this);
			}
		});
	}
	
	@Override
	public void onUpdate()
	{
		updateGamma();
		updateNightVision();
	}
	
	private void updateGamma()
	{
		boolean shouldChangeGamma =
			isEnabled() && method.getSelected() == Method.GAMMA;
		
		if(shouldChangeGamma)
		{
			setGamma(16);
			return;
		}
		
		if(wasGammaChanged)
			resetGamma(defaultGamma.getValue());
	}
	
	private void setGamma(double target)
	{
		wasGammaChanged = true;
		GameOptions options = MC.options;
		
		if(!fade.isChecked() || Math.abs(options.gamma - target) <= 0.5)
		{
			options.gamma = target;
			return;
		}
		
		if(options.gamma < target)
			options.gamma += 0.5;
		else
			options.gamma -= 0.5;
	}
	
	private void resetGamma(double target)
	{
		GameOptions options = MC.options;
		
		if(!fade.isChecked() || Math.abs(options.gamma - target) <= 0.5)
		{
			options.gamma = target;
			wasGammaChanged = false;
			return;
		}
		
		if(options.gamma < target)
			options.gamma += 0.5;
		else
			options.gamma -= 0.5;
	}
	
	private void updateNightVision()
	{
		boolean shouldGiveNightVision =
			isEnabled() && method.getSelected() == Method.NIGHT_VISION;
		
		if(fade.isChecked())
		{
			if(shouldGiveNightVision)
				nightVisionStrength += 0.03125;
			else
				nightVisionStrength -= 0.03125;
			
			nightVisionStrength = MathHelper.clamp(nightVisionStrength, 0, 1);
			
		}else if(shouldGiveNightVision)
			nightVisionStrength = 1;
		else
			nightVisionStrength = 0;
	}
	
	public boolean isNightVisionActive()
	{
		return nightVisionStrength > 0;
	}
	
	public float getNightVisionStrength()
	{
		return nightVisionStrength;
	}
	
	private static enum Method
	{
		GAMMA("伽马Gamma"),
		NIGHT_VISION("夜视药");
		
		private final String name;
		
		private Method(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
