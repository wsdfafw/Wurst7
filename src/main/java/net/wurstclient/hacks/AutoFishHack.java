/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.autofish.AutoFishDebugDraw;
import net.wurstclient.hacks.autofish.AutoFishRodSelector;
import net.wurstclient.mixinterface.IFishingBobberEntity;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.ChatUtils;

@SearchTags({"FishBot", "auto fish", "fish bot", "fishing"})
public final class AutoFishHack extends Hack
	implements UpdateListener, PacketInputListener, RenderListener
{
	private final SliderSetting validRange = new SliderSetting("有效范围",
		"在这个范围之外咬钩的鱼将被忽略\n如果范围内没有发现鱼,请扩大你的给定范围\n如果能检测到其他玩家钓到鱼,请缩小给定范围",
		1.5, 0.25, 8, 0.25, ValueDisplay.DECIMAL);
	
	private int castRodTimer;
	private int reelInTimer;
	
	private final AutoFishDebugDraw debugDraw = new AutoFishDebugDraw();
	private final AutoFishRodSelector rodSelector = new AutoFishRodSelector();
	
	private boolean wasOpenWater;
	
	public AutoFishHack()
	{
		super("自动钓鱼");
		
		setCategory(Category.OTHER);
		addSetting(validRange);
		debugDraw.getSettings().forEach(this::addSetting);
	}
	
	@Override
	public void onEnable()
	{
		WURST.getHax().airPlaceHack.setEnabled(false);
		
		castRodTimer = 0;
		reelInTimer = -1;
		rodSelector.reset();
		debugDraw.reset();
		wasOpenWater = true;
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(PacketInputListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(PacketInputListener.class, this);
		EVENTS.remove(RenderListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		debugDraw.updateValidRange(validRange.getValue());
		
		if(reelInTimer > 0)
			reelInTimer--;
		
		ClientPlayerEntity player = MC.player;
		
		if(rodSelector.hasScheduledClick())
		{
			rodSelector.doScheduledClick();
			castRodTimer = 15;
			return;
		}
		
		rodSelector.updateBestRod();
		
		if(!rodSelector.hasARod())
		{
			ChatUtils.message("钓鱼的鱼竿已经被用完了");
			setEnabled(false);
			return;
		}
		
		if(!rodSelector.isBestRodAlreadySelected())
		{
			rodSelector.selectBestRod();
			return;
		}
		
		// wait for timer
		if(castRodTimer > 0)
		{
			castRodTimer--;
			return;
		}
		
		// cast rod
		if(player.fishHook == null || player.fishHook.isRemoved())
		{
			rightClick();
			castRodTimer = 15;
			reelInTimer = 1200;
		}
		
		// reel in after 60s
		if(reelInTimer == 0)
		{
			reelInTimer--;
			rightClick();
			castRodTimer = 15;
		}
	}
	
	@Override
	public void onReceivedPacket(PacketInputEvent event)
	{
		ClientPlayerEntity player = MC.player;
		if(player == null || player.fishHook == null)
			return;
		
		if(!(event.getPacket() instanceof PlaySoundS2CPacket))
			return;
		
		// check sound type
		PlaySoundS2CPacket sound = (PlaySoundS2CPacket)event.getPacket();
		if(!SoundEvents.ENTITY_FISHING_BOBBER_SPLASH
			.equals(sound.getSound().value()))
			return;
		
		debugDraw.updateSoundPos(sound);
		
		// check position
		FishingBobberEntity bobber = player.fishHook;
		if(Math.abs(sound.getX() - bobber.getX()) > validRange.getValue()
			|| Math.abs(sound.getZ() - bobber.getZ()) > validRange.getValue())
			return;
		
		// check open water
		boolean isOpenWater = isInOpenWater(bobber);
		if(!isOpenWater && wasOpenWater)
		{
			ChatUtils.warning("您目前正在浅水区钓鱼");
			ChatUtils.message(
				"像这样钓鱼时无法获得任何宝物");
			
			if(!WURST.getHax().openWaterEspHack.isEnabled())
				ChatUtils.message("使用 OpenWaterESP 查找开放水域");
		}
		
		// catch fish
		rightClick();
		castRodTimer = 15;
		wasOpenWater = isOpenWater;
	}
	
	private boolean isInOpenWater(FishingBobberEntity bobber)
	{
		return ((IFishingBobberEntity)bobber)
			.checkOpenWaterAround(bobber.getBlockPos());
	}
	
	private void rightClick()
	{
		// check held item
		ItemStack stack = MC.player.getInventory().getMainHandStack();
		if(stack.isEmpty() || !(stack.getItem() instanceof FishingRodItem))
			return;
		
		// right click
		IMC.rightClick();
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		debugDraw.render(matrixStack, partialTicks);
	}
}
