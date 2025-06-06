/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerProfession;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.autolibrarian.BookOffer;
import net.wurstclient.hacks.autolibrarian.UpdateBooksSetting;
import net.wurstclient.mixinterface.IKeyBinding;
import net.wurstclient.settings.BookOffersSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.FacingSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.settings.SwingHandSetting;
import net.wurstclient.settings.SwingHandSetting.SwingHand;
import net.wurstclient.util.*;
import net.wurstclient.util.BlockBreaker.BlockBreakingParams;
import net.wurstclient.util.BlockPlacer.BlockPlacingParams;

@SearchTags({"auto librarian", "AutoVillager", "auto villager",
	"VillagerTrainer", "villager trainer", "LibrarianTrainer",
	"librarian trainer", "AutoHmmm", "auto hmmm"})
public final class AutoLibrarianHack extends Hack
	implements UpdateListener, RenderListener
{
	private final BookOffersSetting wantedBooks = new BookOffersSetting(
		"想要的附魔书",
		"你想让你的村民卖掉的魔法书的清单.\n\n" + "一旦自动图书馆学会出售这些书，它将停止训练当前的村民.\n\n"
			+ "你也可以为每本书设定一个最高价格，以防你已经有一个村民\n在卖这本书，但是你想要一个更便宜的价格.",
		"minecraft:depth_strider;3", "minecraft:efficiency;5",
		"minecraft:feather_falling;4", "minecraft:fortune;3",
		"minecraft:looting;3", "minecraft:mending;1", "minecraft:protection;4",
		"minecraft:respiration;3", "minecraft:sharpness;5",
		"minecraft:silk_touch;1", "minecraft:unbreaking;3");
	
	private final CheckboxSetting lockInTrade = new CheckboxSetting("锁定贸易",
		"一旦它学会出售你想要的书，它就会自动从村民那里买东西。这可以防止村民以后改变交易条件.\n\n"
			+ "使用此功能时，请确保您的库存中至少有24张纸和9颗绿宝石。或者，1本书和64颗绿宝石也可以。",
		false);
	
	private final UpdateBooksSetting updateBooks = new UpdateBooksSetting();
	
	private final SliderSetting range =
		new SliderSetting("范围", 5, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	private final FacingSetting facing =
		FacingSetting.withoutPacketSpam("AutoLibrarian 应该如何面向村民和工作地点。\n\n"
			+ "\u00a7l关闭\u00a7r - 完全不面向村民。这将被反作弊插件检测到。\n\n"
			+ "\u00a7l服务器端\u00a7r - 在服务器端面向村民，同时允许您在客户端自由移动视角。\n\n"
			+ "\u00a7l客户端\u00a7r - 通过在客户端移动视角来面向村民。这是最合法的选择，但可能会让人感到晕眩。");
	
	private final SwingHandSetting swingHand =
		new SwingHandSetting(this, SwingHand.SERVER);
	
	private final SliderSetting repairMode = new SliderSetting("修复模式",
		"当你的斧头的耐久度达到设定的临界值时，防止自动图书馆员使用你的斧头，因此你可以在它坏掉之前修理它.\n"
			+ "Can be adjusted from 0 (off) to 100 remaining uses.",
		1, 0, 100, 1, ValueDisplay.INTEGER.withLabel(0, "off"));
	
	private final OverlayRenderer overlay = new OverlayRenderer();
	private final HashSet<VillagerEntity> experiencedVillagers =
		new HashSet<>();
	
	private VillagerEntity villager;
	private BlockPos jobSite;
	
	private boolean placingJobSite;
	private boolean breakingJobSite;
	
	public AutoLibrarianHack()
	{
		super("自动图书馆员");
		setCategory(Category.OTHER);
		addSetting(wantedBooks);
		addSetting(lockInTrade);
		addSetting(updateBooks);
		addSetting(range);
		addSetting(facing);
		addSetting(swingHand);
		addSetting(repairMode);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		
		if(breakingJobSite)
		{
			MC.interactionManager.breakingBlock = true;
			MC.interactionManager.cancelBlockBreaking();
			breakingJobSite = false;
		}
		
		overlay.resetProgress();
		villager = null;
		jobSite = null;
		placingJobSite = false;
		breakingJobSite = false;
		experiencedVillagers.clear();
	}
	
	@Override
	public void onUpdate()
	{
		if(villager == null)
		{
			setTargetVillager();
			return;
		}
		
		if(jobSite == null)
		{
			setTargetJobSite();
			return;
		}
		
		if(placingJobSite && breakingJobSite)
			throw new IllegalStateException("试图同时放置和破坏作业现场。有点不对劲。");
		
		if(placingJobSite)
		{
			placeJobSite();
			return;
		}
		
		if(breakingJobSite)
		{
			breakJobSite();
			return;
		}
		
		if(!(MC.currentScreen instanceof MerchantScreen tradeScreen))
		{
			openTradeScreen();
			return;
		}
		
		// Can't see experience until the trade screen is open, so we have to
		// check it here and start over if the villager is already experienced.
		int experience = tradeScreen.getScreenHandler().getExperience();
		if(experience > 0)
		{
			ChatUtils.warning("Villager at "
				+ villager.getBlockPos().toShortString()
				+ " is already experienced, meaning it can't be trained anymore.");
			ChatUtils.message("寻找另一个村民...");
			experiencedVillagers.add(villager);
			villager = null;
			jobSite = null;
			closeTradeScreen();
			return;
		}
		
		// check which book the villager is selling
		BookOffer bookOffer =
			findEnchantedBookOffer(tradeScreen.getScreenHandler().getRecipes());
		
		if(bookOffer == null)
		{
			ChatUtils.message("村民不是在卖一本被施了魔法的书.");
			closeTradeScreen();
			breakingJobSite = true;
			System.out.println("打破工作现场...");
			return;
		}
		
		ChatUtils.message("村民在卖 " + bookOffer.getEnchantmentNameWithLevel()
			+ " for " + bookOffer.getFormattedPrice() + ".");
		
		// if wrong enchantment, break job site and start over
		if(!wantedBooks.isWanted(bookOffer))
		{
			breakingJobSite = true;
			System.out.println("打破工作现场...");
			closeTradeScreen();
			return;
		}
		
		// lock in the trade, if enabled
		if(lockInTrade.isChecked())
		{
			// select the first valid trade
			tradeScreen.getScreenHandler().setRecipeIndex(0);
			tradeScreen.getScreenHandler().switchTo(0);
			MC.getNetworkHandler()
				.sendPacket(new SelectMerchantTradeC2SPacket(0));
			
			// buy whatever the villager is selling
			MC.interactionManager.clickSlot(
				tradeScreen.getScreenHandler().syncId, 2, 0,
				SlotActionType.PICKUP, MC.player);
			
			// close the trade screen
			closeTradeScreen();
		}
		
		// update wanted books based on the user's settings
		updateBooks.getSelected().update(wantedBooks, bookOffer);
		
		ChatUtils.message("完成!");
		setEnabled(false);
	}
	
	private void breakJobSite()
	{
		if(jobSite == null)
			throw new IllegalStateException("工作地点为空.");
		
		BlockBreakingParams params =
			BlockBreaker.getBlockBreakingParams(jobSite);
		
		if(params == null || BlockUtils.getState(jobSite).isReplaceable())
		{
			System.out.println("工作站点已被破坏。取代...");
			breakingJobSite = false;
			placingJobSite = true;
			return;
		}
		
		// equip tool
		WURST.getHax().autoToolHack.equipBestTool(jobSite, false, true,
			repairMode.getValueI());
		
		// face block
		facing.getSelected().face(params.hitVec());
		
		// damage block and swing hand
		if(MC.interactionManager.updateBlockBreakingProgress(jobSite,
			params.side()))
			swingHand.swing(Hand.MAIN_HAND);
		
		// update progress
		overlay.updateProgress();
	}
	
	private void placeJobSite()
	{
		if(jobSite == null)
			throw new IllegalStateException("工作地点为空.");
		
		if(!BlockUtils.getState(jobSite).isReplaceable())
		{
			if(BlockUtils.getBlock(jobSite) == Blocks.LECTERN)
			{
				System.out.println("已放置工作地点.");
				placingJobSite = false;
				
			}else
			{
				System.out.println("在工作现场发现错误的块。打破...");
				breakingJobSite = true;
				placingJobSite = false;
			}
			
			return;
		}
		
		// check if holding a lectern
		if(!MC.player.isHolding(Items.LECTERN))
		{
			InventoryUtils.selectItem(Items.LECTERN, 36);
			return;
		}
		
		// get the hand that is holding the lectern
		Hand hand = MC.player.getMainHandStack().isOf(Items.LECTERN)
			? Hand.MAIN_HAND : Hand.OFF_HAND;
		
		// sneak-place to avoid activating trapdoors/chests/etc.
		IKeyBinding sneakKey = IKeyBinding.get(MC.options.sneakKey);
		sneakKey.setPressed(true);
		if(!MC.player.isSneaking())
			return;
		
		// get block placing params
		BlockPlacingParams params = BlockPlacer.getBlockPlacingParams(jobSite);
		if(params == null)
		{
			sneakKey.resetPressedState();
			return;
		}
		
		// face block
		facing.getSelected().face(params.hitVec());
		
		// place block
		ActionResult result = MC.interactionManager.interactBlock(MC.player,
			hand, params.toHitResult());
		
		// swing hand
		if(result instanceof ActionResult.Success success
			&& success.swingSource() == ActionResult.SwingSource.CLIENT)
			swingHand.swing(hand);
		
		// reset sneak
		sneakKey.resetPressedState();
	}
	
	private void openTradeScreen()
	{
		if(MC.itemUseCooldown > 0)
			return;
		
		ClientPlayerInteractionManager im = MC.interactionManager;
		ClientPlayerEntity player = MC.player;
		
		if(player.squaredDistanceTo(villager) > range.getValueSq())
		{
			ChatUtils.error("村民不在范围内。考虑困住村民，这样它就不会跑了.");
			setEnabled(false);
			return;
		}
		
		// create realistic hit result
		Box box = villager.getBoundingBox();
		Vec3d start = RotationUtils.getEyesPos();
		Vec3d end = box.getCenter();
		Vec3d hitVec = box.raycast(start, end).orElse(start);
		EntityHitResult hitResult = new EntityHitResult(villager, hitVec);
		
		// face end vector
		facing.getSelected().face(end);
		
		// click on villager
		Hand hand = Hand.MAIN_HAND;
		ActionResult actionResult =
			im.interactEntityAtLocation(player, villager, hitResult, hand);
		
		if(!actionResult.isAccepted())
			im.interactEntity(player, villager, hand);
		
		// swing hand
		if(actionResult instanceof ActionResult.Success success
			&& success.swingSource() == ActionResult.SwingSource.CLIENT)
			swingHand.swing(hand);
		
		// set cooldown
		MC.itemUseCooldown = 4;
	}
	
	private void closeTradeScreen()
	{
		MC.player.closeHandledScreen();
		MC.itemUseCooldown = 4;
	}
	
	private BookOffer findEnchantedBookOffer(TradeOfferList tradeOffers)
	{
		for(TradeOffer tradeOffer : tradeOffers)
		{
			ItemStack stack = tradeOffer.getSellItem();
			if(stack.getItem() != Items.ENCHANTED_BOOK)
				continue;
			
			Set<Entry<RegistryEntry<Enchantment>>> enchantmentLevelMap =
				EnchantmentHelper.getEnchantments(stack)
					.getEnchantmentEntries();
			if(enchantmentLevelMap.isEmpty())
				continue;
			
			Object2IntMap.Entry<RegistryEntry<Enchantment>> firstEntry =
				enchantmentLevelMap.stream().findFirst().orElseThrow();
			
			String enchantment = firstEntry.getKey().getIdAsString();
			int level = firstEntry.getIntValue();
			int price = tradeOffer.getDisplayedFirstBuyItem().getCount();
			BookOffer bookOffer = new BookOffer(enchantment, level, price);
			
			if(!bookOffer.isFullyValid())
			{
				System.out.println("Found invalid enchanted book offer.\n"
					+ "Component data: " + enchantmentLevelMap);
				continue;
			}
			
			return bookOffer;
		}
		
		return null;
	}
	
	private void setTargetVillager()
	{
		ClientPlayerEntity player = MC.player;
		double rangeSq = range.getValueSq();
		
		Stream<VillagerEntity> stream =
			StreamSupport.stream(MC.world.getEntities().spliterator(), true)
				.filter(e -> !e.isRemoved())
				.filter(VillagerEntity.class::isInstance)
				.map(e -> (VillagerEntity)e).filter(e -> e.getHealth() > 0)
				.filter(e -> player.squaredDistanceTo(e) <= rangeSq)
				.filter(e -> e.getVillagerData().profession().getKey()
					.orElse(null) == VillagerProfession.LIBRARIAN)
				.filter(e -> e.getVillagerData().level() == 1)
				.filter(e -> !experiencedVillagers.contains(e));
		
		villager = stream
			.min(Comparator.comparingDouble(e -> player.squaredDistanceTo(e)))
			.orElse(null);
		
		if(villager == null)
		{
			String errorMsg = "找不到附近的图书管理员.";
			int numExperienced = experiencedVillagers.size();
			if(numExperienced > 0)
				errorMsg += " (Except for " + numExperienced + " that "
					+ (numExperienced == 1 ? "is" : "are")
					+ " already experienced.)";
			
			ChatUtils.error(errorMsg);
			ChatUtils.message("确保从你站的地方可以够到图书管理员和讲台.");
			setEnabled(false);
			return;
		}
		
		System.out.println("发现村民在 " + villager.getBlockPos());
	}
	
	private void setTargetJobSite()
	{
		Vec3d eyesVec = RotationUtils.getEyesPos();
		double rangeSq = range.getValueSq();
		
		Stream<BlockPos> stream = BlockUtils
			.getAllInBoxStream(BlockPos.ofFloored(eyesVec),
				range.getValueCeil())
			.filter(pos -> eyesVec
				.squaredDistanceTo(Vec3d.ofCenter(pos)) <= rangeSq)
			.filter(pos -> BlockUtils.getBlock(pos) == Blocks.LECTERN);
		
		jobSite = stream
			.min(Comparator.comparingDouble(
				pos -> villager.squaredDistanceTo(Vec3d.ofCenter(pos))))
			.orElse(null);
		
		if(jobSite == null)
		{
			ChatUtils.error("找不到图书管理员的讲台.");
			ChatUtils.message("确保从你站的地方可以够到图书管理员和讲台.");
			setEnabled(false);
			return;
		}
		
		System.out.println("发现莱顿在 " + jobSite);
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		int green = 0xC000FF00;
		int red = 0xC0FF0000;
		
		if(villager != null)
			RenderUtils.drawOutlinedBox(matrixStack, villager.getBoundingBox(),
				green, false);
		
		if(jobSite != null)
			RenderUtils.drawOutlinedBox(matrixStack, new Box(jobSite), green,
				false);
		
		List<Box> expVilBoxes = experiencedVillagers.stream()
			.map(VillagerEntity::getBoundingBox).toList();
		RenderUtils.drawOutlinedBoxes(matrixStack, expVilBoxes, red, false);
		RenderUtils.drawCrossBoxes(matrixStack, expVilBoxes, red, false);
		
		if(breakingJobSite)
			overlay.render(matrixStack, partialTicks, jobSite);
	}
}
