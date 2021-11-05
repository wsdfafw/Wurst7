/*
 * Copyright (c) 2014-2021 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.BlockUtils;

public final class GlideHack extends Hack implements UpdateListener
{
	private final SliderSetting fallSpeed = new SliderSetting("掉落速度", 0.125, 0.005, 0.25, 0.005, SliderSetting.ValueDisplay.DECIMAL);
    private final SliderSetting moveSpeed = new SliderSetting("移动速度", "水平移动系数.", 1.2, 1.0, 5.0, 0.05, SliderSetting.ValueDisplay.PERCENTAGE);
    private final SliderSetting minHeight = new SliderSetting("最少高度", "因太接近这个高度地面\n不会进行滑翔操作.", 0.0, 0.0, 2.0, 0.01, v -> v == 0.0 ? "关闭" : SliderSetting.ValueDisplay.DECIMAL.getValueString(v));

	public GlideHack()
	{
		super("缓降");
		
		setCategory(Category.MOVEMENT);
		addSetting(fallSpeed);
		addSetting(moveSpeed);
		addSetting(minHeight);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		ClientPlayerEntity player = MC.player;
		Vec3d v = player.getVelocity();
		
		if(player.isOnGround() || player.isTouchingWater() || player.isInLava()
			|| player.isClimbing() || v.y >= 0)
			return;
		
		if(minHeight.getValue() > 0)
		{
			Box box = player.getBoundingBox();
			box = box.union(box.offset(0, -minHeight.getValue(), 0));
			if(!MC.world.isSpaceEmpty(box))
				return;
			
			BlockPos min =
				new BlockPos(new Vec3d(box.minX, box.minY, box.minZ));
			BlockPos max =
				new BlockPos(new Vec3d(box.maxX, box.maxY, box.maxZ));
			Stream<BlockPos> stream = StreamSupport
				.stream(BlockUtils.getAllInBox(min, max).spliterator(), true);
			
			// manual collision check, since liquids don't have bounding boxes
			if(stream.map(BlockUtils::getState).map(BlockState::getMaterial)
				.anyMatch(Material::isLiquid))
				return;
		}
		
		player.setVelocity(v.x, Math.max(v.y, -fallSpeed.getValue()), v.z);
		player.flyingSpeed *= moveSpeed.getValueF();
	}
}
