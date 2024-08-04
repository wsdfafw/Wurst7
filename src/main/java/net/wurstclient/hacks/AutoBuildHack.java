/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.RightClickListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.FileSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.settings.SwingHandSetting.SwingHand;
import net.wurstclient.util.*;
import net.wurstclient.util.BlockPlacer.BlockPlacingParams;
import net.wurstclient.util.json.JsonException;

public final class AutoBuildHack extends Hack
	implements UpdateListener, RightClickListener, RenderListener
{
	private final FileSetting templateSetting = new FileSetting("模板",
		"决定要去建什么.\n\n模板是一个 JSON 文件. 感受自由的去\n添加或编辑你想要的模板,你也可以删除\n默认的模板.\n\n如果你搞得一团糟,你只需要点击\n'重设默认值'按钮或者\n删除文件夹.",
		"autobuild", DefaultAutoBuildTemplates::createFiles);
	
	private final SliderSetting range =
		new SliderSetting("范围", "放方块的时候多少格才放.\n推荐数值:\n6.0 是原版\n4.25 是为了绕过反作弊",
			6, 1, 10, 0.05, ValueDisplay.DECIMAL);
	
	private final CheckboxSetting checkLOS = new CheckboxSetting("检查视线",
		"确保你不会隔墙建造\n这一般对于有反作弊的服务器来说\n但会导致放慢建造速度.", false);
	
	private final CheckboxSetting instaBuild = new CheckboxSetting("瞬间构建",
		"瞬间建完一个模板的建筑 (小于或等于 64 方块).\n为了更好的效果,你最好靠近你正在放方块的附近", true);
	
	private final CheckboxSetting fastPlace =
		new CheckboxSetting("永远快速放置", "设置'快速放置'为开启,\n尽管是关闭着的", true);
	
	private Status status = Status.NO_TEMPLATE;
	private AutoBuildTemplate template;
	private LinkedHashSet<BlockPos> remainingBlocks = new LinkedHashSet<>();
	
	public AutoBuildHack()
	{
		super("自动构建");
		setCategory(Category.BLOCKS);
		addSetting(templateSetting);
		addSetting(range);
		addSetting(checkLOS);
		addSetting(instaBuild);
		addSetting(fastPlace);
	}
	
	@Override
	public String getRenderName()
	{
		String name = getName();
		
		switch(status)
		{
			case NO_TEMPLATE:
			break;
			
			case LOADING:
			name += " [载入中...]";
			break;
			
			case IDLE:
			name += " [" + template.getName() + "]";
			break;
			
			case BUILDING:
			double total = template.size();
			double placed = total - remainingBlocks.size();
			double progress = Math.round(placed / total * 1e4) / 1e2;
			name += " [" + template.getName() + "] " + progress + "%";
			break;
		}
		
		return name;
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RightClickListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RightClickListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		
		remainingBlocks.clear();
		
		if(template == null)
			status = Status.NO_TEMPLATE;
		else
			status = Status.IDLE;
	}
	
	@Override
	public void onRightClick(RightClickEvent event)
	{
		if(status != Status.IDLE)
			return;
		
		HitResult hitResult = MC.crosshairTarget;
		if(hitResult == null || hitResult.getType() != HitResult.Type.BLOCK
			|| !(hitResult instanceof BlockHitResult blockHitResult))
			return;
		
		BlockPos hitResultPos = blockHitResult.getBlockPos();
		if(!BlockUtils.canBeClicked(hitResultPos))
			return;
		
		BlockPos startPos = hitResultPos.offset(blockHitResult.getSide());
		Direction direction = MC.player.getHorizontalFacing();
		remainingBlocks = template.getPositions(startPos, direction);
		
		if(instaBuild.isChecked() && template.size() <= 64)
			buildInstantly();
		else
			status = Status.BUILDING;
	}
	
	@Override
	public void onUpdate()
	{
		switch(status)
		{
			case NO_TEMPLATE:
			loadSelectedTemplate();
			break;
			
			case LOADING:
			break;
			
			case IDLE:
			if(!template.isSelected(templateSetting))
				loadSelectedTemplate();
			break;
			
			case BUILDING:
			buildNormally();
			break;
		}
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		if(status != Status.BUILDING)
			return;
		
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		matrixStack.push();
		
		RegionPos region = RenderUtils.getCameraRegion();
		RenderUtils.applyRegionalRenderOffset(matrixStack, region);
		
		RenderSystem.setShaderColor(0, 0, 0, 0.5F);
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		
		double boxStart = 1 / 16.0;
		double boxEnd = 15 / 16.0;
		Box box = new Box(boxStart, boxStart, boxStart, boxEnd, boxEnd, boxEnd)
			.offset(region.negate().toBlockPos());
		
		ArrayList<BlockPos> blocksToDraw = remainingBlocks.stream()
			.filter(pos -> BlockUtils.getState(pos).isReplaceable()).limit(1024)
			.collect(Collectors.toCollection(ArrayList::new));
		
		GL11.glDepthMask(false);
		RenderSystem.setShaderColor(0, 1, 0, 0.15F);
		
		Vec3d eyesPos = RotationUtils.getEyesPos();
		double rangeSq = range.getValueSq();
		blocksToDraw.stream()
			.filter(pos -> pos.getSquaredDistance(eyesPos) <= rangeSq)
			.map(pos -> box.offset(pos)).forEach(
				offsetBox -> RenderUtils.drawSolidBox(offsetBox, matrixStack));
		
		GL11.glDepthMask(true);
		RenderSystem.setShaderColor(0, 0, 0, 0.5F);
		
		blocksToDraw.stream().map(pos -> box.offset(pos)).forEach(
			offsetBox -> RenderUtils.drawOutlinedBox(offsetBox, matrixStack));
		
		matrixStack.pop();
		
		// GL resets
		GL11.glDisable(GL11.GL_BLEND);
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}
	
	private void buildNormally()
	{
		remainingBlocks
			.removeIf(pos -> !BlockUtils.getState(pos).isReplaceable());
		
		if(remainingBlocks.isEmpty())
		{
			status = Status.IDLE;
			return;
		}
		
		if(!fastPlace.isChecked() && MC.itemUseCooldown > 0)
			return;
		
		double rangeSq = range.getValueSq();
		for(BlockPos pos : remainingBlocks)
		{
			BlockPlacingParams params = BlockPlacer.getBlockPlacingParams(pos);
			if(params == null || params.distanceSq() > rangeSq)
				continue;
			if(checkLOS.isChecked() && !params.lineOfSight())
				continue;
			
			MC.itemUseCooldown = 4;
			RotationUtils.getNeededRotations(params.hitVec())
				.sendPlayerLookPacket();
			InteractionSimulator.rightClickBlock(params.toHitResult());
			break;
		}
	}
	
	private void buildInstantly()
	{
		double rangeSq = range.getValueSq();
		
		for(BlockPos pos : remainingBlocks)
		{
			if(!BlockUtils.getState(pos).isReplaceable())
				continue;
			
			BlockPlacingParams params = BlockPlacer.getBlockPlacingParams(pos);
			if(params == null || params.distanceSq() > rangeSq)
				continue;
			
			InteractionSimulator.rightClickBlock(params.toHitResult(),
				SwingHand.OFF);
		}
		
		remainingBlocks.clear();
	}
	
	private void loadSelectedTemplate()
	{
		status = Status.LOADING;
		Path path = templateSetting.getSelectedFile();
		
		try
		{
			template = AutoBuildTemplate.load(path);
			status = Status.IDLE;
			
		}catch(IOException | JsonException e)
		{
			Path fileName = path.getFileName();
			ChatUtils.error("无法加载模板 '" + fileName + "'.");
			
			String simpleClassName = e.getClass().getSimpleName();
			String message = e.getMessage();
			ChatUtils.message(simpleClassName + ": " + message);
			
			e.printStackTrace();
			setEnabled(false);
		}
	}
	
	private enum Status
	{
		NO_TEMPLATE,
		LOADING,
		IDLE,
		BUILDING;
	}
}
