/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.Comparator;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PostMotionListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.settings.filters.FilterBabiesSetting;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags({"feed aura", "BreedAura", "breed aura", "AutoBreeder",
	"auto breeder"})
public final class FeedAuraHack extends Hack
	implements UpdateListener, PostMotionListener, RenderListener
{
	private final SliderSetting range = new SliderSetting("范围",
		"决定 范围喂养 的范围\n来喂动物食物.\n任何东西一旦远离\n指定的数值不会喂",
		5, 1, 10, 0.05, ValueDisplay.DECIMAL);
	
	private final EnumSetting<Priority> priority = new EnumSetting<>("有限度",
		"决定哪只动物会优先喂养.\n§l距离§r - 喂养最近的动物.\n§l角度§r - 喂养动物所需要的\n最后头位置所可以喂养的角度.\n§l生命§r - 喂养血量最少的动物",
		Priority.values(), Priority.ANGLE);
	
	private final FilterBabiesSetting filterBabies = new FilterBabiesSetting(
		"不会喂食任何的婴儿.\n节省食物,但长大会很慢.",
		false);
	
	private AnimalEntity target;
	private AnimalEntity renderTarget;
	
	public FeedAuraHack()
	{
		super("喂食光环");
		setCategory(Category.OTHER);
		addSetting(range);
		addSetting(priority);
		addSetting(filterBabies);
	}
	
	@Override
	protected void onEnable()
	{
		// disable other auras
		WURST.getHax().clickAuraHack.setEnabled(false);
		WURST.getHax().fightBotHack.setEnabled(false);
		WURST.getHax().killauraLegitHack.setEnabled(false);
		WURST.getHax().multiAuraHack.setEnabled(false);
		WURST.getHax().protectHack.setEnabled(false);
		WURST.getHax().triggerBotHack.setEnabled(false);
		WURST.getHax().tpAuraHack.setEnabled(false);
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(PostMotionListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(PostMotionListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		
		target = null;
		renderTarget = null;
	}
	
	@Override
	public void onUpdate()
	{
		ClientPlayerEntity player = MC.player;
		ItemStack heldStack = player.inventory.getMainHandStack();
		
		double rangeSq = Math.pow(range.getValue(), 2);
		Stream<AnimalEntity> stream =
			StreamSupport.stream(MC.world.getEntities().spliterator(), true)
				.filter(e -> !e.removed).filter(e -> e instanceof AnimalEntity)
				.map(e -> (AnimalEntity)e).filter(e -> e.getHealth() > 0)
				.filter(e -> player.squaredDistanceTo(e) <= rangeSq)
				.filter(e -> e.isBreedingItem(heldStack))
				.filter(AnimalEntity::canEat);
		
		if(filterBabies.isChecked())
			stream = stream.filter(filterBabies);
		
		target = stream.min(priority.getSelected().comparator).orElse(null);
		renderTarget = target;
		if(target == null)
			return;
		
		WURST.getRotationFaker()
			.faceVectorPacket(target.getBoundingBox().getCenter());
	}
	
	@Override
	public void onPostMotion()
	{
		if(target == null)
			return;
		
		ClientPlayerInteractionManager im = MC.interactionManager;
		ClientPlayerEntity player = MC.player;
		Hand hand = Hand.MAIN_HAND;
		
		EntityHitResult hitResult = new EntityHitResult(target);
		ActionResult actionResult =
			im.interactEntityAtLocation(player, target, hitResult, hand);
		
		if(!actionResult.isAccepted())
			actionResult = im.interactEntity(player, target, hand);
		
		if(actionResult.isAccepted() && actionResult.shouldSwingHand())
			player.swingHand(hand);
		
		target = null;
	}
	
	@Override
	public void onRender(float partialTicks)
	{
		if(renderTarget == null)
			return;
		
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(2);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		
		GL11.glPushMatrix();
		RenderUtils.applyRenderOffset();
		
		Box box = new Box(BlockPos.ORIGIN);
		float p = 1;
		LivingEntity le = renderTarget;
		p = (le.getMaxHealth() - le.getHealth()) / le.getMaxHealth();
		float red = p * 2F;
		float green = 2 - red;
		
		GL11.glTranslated(
			renderTarget.prevX
				+ (renderTarget.getX() - renderTarget.prevX) * partialTicks,
			renderTarget.prevY
				+ (renderTarget.getY() - renderTarget.prevY) * partialTicks,
			renderTarget.prevZ
				+ (renderTarget.getZ() - renderTarget.prevZ) * partialTicks);
		GL11.glTranslated(0, 0.05, 0);
		GL11.glScaled(renderTarget.getWidth(), renderTarget.getHeight(),
			renderTarget.getWidth());
		GL11.glTranslated(-0.5, 0, -0.5);
		
		if(p < 1)
		{
			GL11.glTranslated(0.5, 0.5, 0.5);
			GL11.glScaled(p, p, p);
			GL11.glTranslated(-0.5, -0.5, -0.5);
		}
		
		GL11.glColor4f(red, green, 0, 0.25F);
		RenderUtils.drawSolidBox(box);
		
		GL11.glColor4f(red, green, 0, 0.5F);
		RenderUtils.drawOutlinedBox(box);
		
		GL11.glPopMatrix();
		
		// GL resets
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
	
	private enum Priority
	{
		DISTANCE("距离", e -> MC.player.squaredDistanceTo(e)),
		
		ANGLE("角度",
			e -> RotationUtils
				.getAngleToLookVec(e.getBoundingBox().getCenter())),
		
		HEALTH("血量", e -> e instanceof LivingEntity
			? ((LivingEntity)e).getHealth() : Integer.MAX_VALUE);
		
		private final String name;
		private final Comparator<Entity> comparator;
		
		private Priority(String name, ToDoubleFunction<Entity> keyExtractor)
		{
			this.name = name;
			comparator = Comparator.comparingDouble(keyExtractor);
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
