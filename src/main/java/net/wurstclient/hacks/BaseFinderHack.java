/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.BlockListSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.BlockVertexCompiler;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.RenderUtils;

@SearchTags({"base finder", "factions"})
public final class BaseFinderHack extends Hack
	implements UpdateListener, RenderListener
{
	private final BlockListSetting naturalBlocks = new BlockListSetting(
		"自然方块",
		"这些方块会被认为成\n自然生产的一部分.\n\n他们不会被高亮\n作为玩家的基地.",
		"minecraft:acacia_leaves", "minecraft:acacia_log", "minecraft:air",
		"minecraft:allium", "minecraft:andesite", "minecraft:azure_bluet",
		"minecraft:bedrock", "minecraft:birch_leaves", "minecraft:birch_log",
		"minecraft:blue_orchid", "minecraft:brown_mushroom",
		"minecraft:brown_mushroom_block", "minecraft:bubble_column",
		"minecraft:cave_air", "minecraft:clay", "minecraft:coal_ore",
		"minecraft:cobweb", "minecraft:cornflower", "minecraft:dandelion",
		"minecraft:dark_oak_leaves", "minecraft:dark_oak_log",
		"minecraft:dead_bush", "minecraft:diamond_ore", "minecraft:diorite",
		"minecraft:dirt", "minecraft:emerald_ore", "minecraft:fern",
		"minecraft:gold_ore", "minecraft:granite", "minecraft:grass",
		"minecraft:grass_block", "minecraft:gravel", "minecraft:ice",
		"minecraft:infested_stone", "minecraft:iron_ore",
		"minecraft:jungle_leaves", "minecraft:jungle_log", "minecraft:kelp",
		"minecraft:kelp_plant", "minecraft:lapis_ore", "minecraft:large_fern",
		"minecraft:lava", "minecraft:lilac", "minecraft:lily_of_the_valley",
		"minecraft:lily_pad", "minecraft:mossy_cobblestone",
		"minecraft:mushroom_stem", "minecraft:nether_quartz_ore",
		"minecraft:netherrack", "minecraft:oak_leaves", "minecraft:oak_log",
		"minecraft:obsidian", "minecraft:orange_tulip", "minecraft:oxeye_daisy",
		"minecraft:peony", "minecraft:pink_tulip", "minecraft:poppy",
		"minecraft:red_mushroom", "minecraft:red_mushroom_block",
		"minecraft:red_tulip", "minecraft:redstone_ore", "minecraft:rose_bush",
		"minecraft:sand", "minecraft:sandstone", "minecraft:seagrass",
		"minecraft:snow", "minecraft:spawner", "minecraft:spruce_leaves",
		"minecraft:spruce_log", "minecraft:stone", "minecraft:sunflower",
		"minecraft:tall_grass", "minecraft:tall_seagrass", "minecraft:vine",
		"minecraft:water", "minecraft:white_tulip");
	
	private final ColorSetting color = new ColorSetting("颜色", "手动设置的方块将会\n以这种颜色高亮.", Color.RED);
	
	private ArrayList<String> blockNames;
	
	private final HashSet<BlockPos> matchingBlocks = new HashSet<>();
	private ArrayList<int[]> vertices = new ArrayList<>();
	private int displayList;
	
	private int messageTimer = 0;
	private int counter;
	
	private Integer oldRegionX;
	private Integer oldRegionZ;
	
	public BaseFinderHack()
	{
		super("基地寻找");
		setCategory(Category.RENDER);
		addSetting(naturalBlocks);
		addSetting(color);
	}
	
	@Override
	public String getRenderName()
	{
		String name = getName() + " [";
		
		// counter
		if(counter >= 10000)
			name += "10000+ blocks";
		else if(counter == 1)
			name += "1 block";
		else if(counter == 0)
			name += "nothing";
		else
			name += counter + " blocks";
		
		name += " found]";
		return name;
	}
	
	@Override
	public void onEnable()
	{
		// reset timer
		messageTimer = 0;
		blockNames = new ArrayList<>(naturalBlocks.getBlockNames());
		displayList = GL11.glGenLists(1);
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		
		matchingBlocks.clear();
		vertices.clear();
		oldRegionX = null;
		oldRegionZ = null;
		
		GL11.glDeleteLists(displayList, 1);
	}
	
	@Override
	public void onRender(float partialTicks)
	{
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		
		GL11.glPushMatrix();
		RenderUtils.applyRegionalRenderOffset();
		
		float[] colorF = color.getColorF();
		GL11.glColor4f(colorF[0], colorF[1], colorF[2], 0.15F);
		GL11.glCallList(displayList);
		
		GL11.glPopMatrix();
		
		// GL resets
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glColor4f(1, 1, 1, 1);
	}
	
	@Override
	public void onUpdate()
	{
		int modulo = MC.player.age % 64;
		
		if(BlockEntityRenderDispatcher.INSTANCE.camera == null)
			return;
		
		BlockPos camPos = RenderUtils.getCameraBlockPos();
		Integer regionX = (camPos.getX() >> 9) * 512;
		Integer regionZ = (camPos.getZ() >> 9) * 512;
		
		if(modulo == 0 || !regionX.equals(oldRegionX)
			|| !regionZ.equals(oldRegionZ))
		{
			GL11.glNewList(displayList, GL11.GL_COMPILE);
			
			GL11.glBegin(GL11.GL_QUADS);
			
			for(int[] vertex : vertices)
				GL11.glVertex3d(vertex[0] - regionX, vertex[1],
					vertex[2] - regionZ);
			
			GL11.glEnd();
			GL11.glEndList();
			
			oldRegionX = regionX;
			oldRegionZ = regionZ;
		}
		
		// reset matching blocks
		if(modulo == 0)
			matchingBlocks.clear();
		
		int startY = 255 - modulo * 4;
		int endY = startY - 4;
		
		BlockPos playerPos =
			new BlockPos(MC.player.getX(), 0, MC.player.getZ());
		
		// search matching blocks
		loop: for(int y = startY; y > endY; y--)
			for(int x = 64; x > -64; x--)
				for(int z = 64; z > -64; z--)
				{
					if(matchingBlocks.size() >= 10000)
						break loop;
					
					BlockPos pos = playerPos.add(x, y, z);
					
					if(Collections.binarySearch(blockNames,
						BlockUtils.getName(pos)) >= 0)
						continue;
					
					matchingBlocks.add(pos);
				}
			
		if(modulo != 63)
			return;
		
		// update timer
		if(matchingBlocks.size() < 10000)
			messageTimer--;
		else
		{
			// show message
			if(messageTimer <= 0)
			{
				ChatUtils
					.warning("基地寻找找到 §l大量§r 的方块.");
				ChatUtils.message(
					"为了防止大量卡顿, 这将会优先显示高亮前 10000 方块.");
			}
			
			// reset timer
			messageTimer = 3;
		}
		
		// update counter
		counter = matchingBlocks.size();
		
		// calculate vertices
		vertices = BlockVertexCompiler.compile(matchingBlocks);
	}
}
