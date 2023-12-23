package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"friction", "no friction", "slippery", "slipperiness"})
public final class NoFrictionHack extends Hack
{
	public final SliderSetting friction = new SliderSetting("摩擦/滑溜", 0.989, 0.8,
		1.1, 0.001, ValueDisplay.DECIMAL);
	
	public NoFrictionHack()
	{
		super("无摩擦");
		setCategory(Category.MOVEMENT);
		addSetting(friction);
	}
}
