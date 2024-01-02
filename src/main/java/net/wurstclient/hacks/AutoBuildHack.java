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
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.RightClickListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IClientPlayerInteractionManager;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.FileSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.AutoBuildTemplate;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.DefaultAutoBuildTemplates;
import net.wurstclient.util.RegionPos;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;
import net.wurstclient.util.RotationUtils.Rotation;
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
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RightClickListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
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
	
	private void buildNormally()
	{
		updateRemainingBlocks();
		
		if(remainingBlocks.isEmpty())
		{
			status = Status.IDLE;
			return;
		}
		
		if(!fastPlace.isChecked() && MC.itemUseCooldown > 0)
			return;
		
		placeNextBlock();
	}
	
	private void updateRemainingBlocks()
	{
		for(Iterator<BlockPos> itr = remainingBlocks.iterator(); itr.hasNext();)
		{
			BlockPos pos = itr.next();
			BlockState state = BlockUtils.getState(pos);
			
			if(!state.isReplaceable())
				itr.remove();
		}
	}
	
	private void placeNextBlock()
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		double rangeSq = Math.pow(range.getValue(), 2);
		
		for(BlockPos pos : remainingBlocks)
			if(tryToPlace(pos, eyesPos, rangeSq))
				break;
	}
	
	private boolean tryToPlace(BlockPos pos, Vec3d eyesPos, double rangeSq)
	{
		Vec3d posVec = Vec3d.ofCenter(pos);
		double distanceSqPosVec = eyesPos.squaredDistanceTo(posVec);
		
		for(Direction side : Direction.values())
		{
			BlockPos neighbor = pos.offset(side);
			
			// check if neighbor can be right clicked
			if(!BlockUtils.canBeClicked(neighbor)
				|| BlockUtils.getState(neighbor).isReplaceable())
				continue;
			
			Vec3d dirVec = Vec3d.of(side.getVector());
			Vec3d hitVec = posVec.add(dirVec.multiply(0.5));
			
			// check if hitVec is within range
			if(eyesPos.squaredDistanceTo(hitVec) > rangeSq)
				continue;
			
			// check if side is visible (facing away from player)
			if(distanceSqPosVec > eyesPos.squaredDistanceTo(posVec.add(dirVec)))
				continue;
			
			// check line of sight
			if(checkLOS.isChecked()
				&& !BlockUtils.hasLineOfSight(eyesPos, hitVec))
				continue;
			
			// face block
			Rotation rotation = RotationUtils.getNeededRotations(hitVec);
			PlayerMoveC2SPacket.LookAndOnGround packet =
				new PlayerMoveC2SPacket.LookAndOnGround(rotation.getYaw(),
					rotation.getPitch(), MC.player.isOnGround());
			MC.player.networkHandler.sendPacket(packet);
			
			// place block
			IMC.getInteractionManager().rightClickBlock(neighbor,
				side.getOpposite(), hitVec);
			MC.player.swingHand(Hand.MAIN_HAND);
			MC.itemUseCooldown = 4;
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onRightClick(RightClickEvent event)
	{
		if(status != Status.IDLE)
			return;
		
		HitResult hitResult = MC.crosshairTarget;
		if(hitResult == null || hitResult.getPos() == null
			|| hitResult.getType() != HitResult.Type.BLOCK
			|| !(hitResult instanceof BlockHitResult))
			return;
		
		BlockHitResult blockHitResult = (BlockHitResult)hitResult;
		
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
	
	private void buildInstantly()
	{
		Vec3d eyesPos = RotationUtils.getEyesPos();
		IClientPlayerInteractionManager im = IMC.getInteractionManager();
		double rangeSq = Math.pow(range.getValue(), 2);
		
		for(BlockPos pos : remainingBlocks)
		{
			if(!BlockUtils.getState(pos).isReplaceable())
				continue;
			
			Vec3d posVec = Vec3d.ofCenter(pos);
			
			for(Direction side : Direction.values())
			{
				BlockPos neighbor = pos.offset(side);
				
				// check if neighbor can be right-clicked
				if(!BlockUtils.canBeClicked(neighbor))
					continue;
				
				Vec3d sideVec = Vec3d.of(side.getVector());
				Vec3d hitVec = posVec.add(sideVec.multiply(0.5));
				
				// check if hitVec is within range
				if(eyesPos.squaredDistanceTo(hitVec) > rangeSq)
					continue;
				
				// place block
				im.rightClickBlock(neighbor, side.getOpposite(), hitVec);
				
				break;
			}
		}
		
		remainingBlocks.clear();
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		if(status != Status.BUILDING)
			return;
		
		float scale = 1F * 7F / 8F;
		double offset = (1D - scale) / 2D;
		Vec3d eyesPos = RotationUtils.getEyesPos();
		double rangeSq = Math.pow(range.getValue(), 2);
		
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_CULL_FACE);
		RenderSystem.setShaderColor(0F, 0F, 0F, 0.5F);
		
		matrixStack.push();
		
		RegionPos region = RenderUtils.getCameraRegion();
		RenderUtils.applyRegionalRenderOffset(matrixStack, region);
		
		int blocksDrawn = 0;
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		for(Iterator<BlockPos> itr = remainingBlocks.iterator(); itr.hasNext()
			&& blocksDrawn < 1024;)
		{
			BlockPos pos = itr.next();
			if(!BlockUtils.getState(pos).isReplaceable())
				continue;
			
			matrixStack.push();
			matrixStack.translate(pos.getX() - region.x(), pos.getY(),
				pos.getZ() - region.z());
			matrixStack.translate(offset, offset, offset);
			matrixStack.scale(scale, scale, scale);
			
			Vec3d posVec = Vec3d.ofCenter(pos);
			
			if(eyesPos.squaredDistanceTo(posVec) <= rangeSq)
				drawGreenBox(matrixStack);
			else
				RenderUtils.drawOutlinedBox(matrixStack);
			
			matrixStack.pop();
			blocksDrawn++;
		}
		
		matrixStack.pop();
		
		// GL resets
		GL11.glDisable(GL11.GL_BLEND);
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}
	
	private void drawGreenBox(MatrixStack matrixStack)
	{
		GL11.glDepthMask(false);
		RenderSystem.setShaderColor(0F, 1F, 0F, 0.15F);
		RenderUtils.drawSolidBox(matrixStack);
		GL11.glDepthMask(true);
		
		RenderSystem.setShaderColor(0F, 0F, 0F, 0.5F);
		RenderUtils.drawOutlinedBox(matrixStack);
	}
	
	private enum Status
	{
		NO_TEMPLATE,
		LOADING,
		IDLE,
		BUILDING;
	}
}
