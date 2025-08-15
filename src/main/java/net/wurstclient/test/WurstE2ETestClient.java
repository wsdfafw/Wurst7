/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.test;

import static net.wurstclient.test.WurstClientTestHelper.*;

import java.time.Duration;

import org.spongepowered.asm.mixin.MixinEnvironment;

import net.fabricmc.api.ModInitializer;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;

public final class WurstE2ETestClient implements ModInitializer
{
	@Override
	public void onInitialize()
	{
		if(System.getProperty("wurst.e2eTest") == null)
			return;
		
		Thread.ofVirtual().name("Wurst端到端测试")
			.uncaughtExceptionHandler((t, e) -> {
				e.printStackTrace();
				System.exit(1);
			}).start(this::runTests);
	}
	
	private void runTests()
	{
		System.out.println("开始Wurst端到端测试");
		waitForResourceLoading();
		
		if(submitAndGet(mc -> mc.options.onboardAccessibility))
		{
			System.out.println("引导已启用。正在等待");
			waitForScreen(AccessibilityOnboardingScreen.class);
			System.out.println("到达引导界面");
			clickButton("gui.continue");
		}
		
		waitForScreen(TitleScreen.class);
		waitForTitleScreenFade();
		System.out.println("到达标题界面");
		takeScreenshot("title_screen", Duration.ZERO);
		
		submitAndWait(AltManagerTest::testAltManagerButton);
		// TODO: Test more of AltManager
		
		System.out.println("点击单人游戏按钮");
		clickButton("menu.singleplayer");
		
		if(submitAndGet(mc -> !mc.getLevelStorage().getLevelList().isEmpty()))
		{
			System.out.println("世界列表非空。正在等待");
			waitForScreen(SelectWorldScreen.class);
			System.out.println("到达选择世界界面");
			takeScreenshot("select_world_screen");
			clickButton("selectWorld.create");
		}
		
		waitForScreen(CreateWorldScreen.class);
		System.out.println("到达创建世界界面");
		
		// Set MC version as world name
		setTextFieldText(0,
			"E2E测试 " + SharedConstants.getGameVersion().name());
		// Select creative mode
		clickButton("selectWorld.gameMode");
		clickButton("selectWorld.gameMode");
		takeScreenshot("create_world_screen");
		
		System.out.println("创建测试世界");
		clickButton("selectWorld.create");
		
		waitForWorldLoad();
		dismissTutorialToasts();
		waitForWorldTicks(200);
		runChatCommand("seed");
		System.out.println("Reached singleplayer world");
		takeScreenshot("in_game", Duration.ZERO);
		runChatCommand("gamerule doDaylightCycle false");
		runChatCommand("gamerule doWeatherCycle false");
		runChatCommand("gamerule doTraderSpawning false");
		runChatCommand("gamerule doPatrolSpawning false");
		runChatCommand("time set noon");
		clearChat();
		
		System.out.println("打开调试菜单");
		toggleDebugHud();
		takeScreenshot("debug_menu");
		
		System.out.println("关闭调试菜单");
		toggleDebugHud();
		
		System.out.println("检查损坏的mixins");
		MixinEnvironment.getCurrentEnvironment().audit();
		
		System.out.println("打开物品栏");
		openInventory();
		takeScreenshot("inventory");
		
		System.out.println("关闭物品栏");
		closeScreen();
		
		// TODO: Open ClickGUI and Navigator
		
		// Build a test platform and clear out the space above it
		runChatCommand("fill ~-7 ~-5 ~-7 ~7 ~-1 ~7 stone");
		runChatCommand("fill ~-7 ~ ~-7 ~7 ~30 ~7 air");
		runChatCommand("kill @e[type=!player,distance=..10]");
		
		// Clear inventory and chat before running tests
		// runChatCommand("clear");
		clearChat();
		
		// Test Wurst hacks
		AutoMineHackTest.testAutoMineHack();
		FreecamHackTest.testFreecamHack();
		NoFallHackTest.testNoFallHack();
		XRayHackTest.testXRayHack();
		
		// Test Wurst commands
		CopyItemCmdTest.testCopyItemCmd();
		GiveCmdTest.testGiveCmd();
		ModifyCmdTest.testModifyCmd();
		
		// TODO: Test more Wurst features
		
		// Test special cases
		PistonTest.testPistonDoesntCrash();
		
		System.out.println("打开游戏菜单");
		openGameMenu();
		takeScreenshot("game_menu");
		
		// TODO: Check Wurst Options
		
		System.out.println("返回标题界面");
		clickButton("menu.returnToMenu");
		waitForScreen(TitleScreen.class);
		
		System.out.println("停止游戏");
		clickButton("menu.quit");
	}
}
