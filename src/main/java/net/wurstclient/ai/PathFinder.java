/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.WurstClient;
import net.wurstclient.WurstRenderLayers;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RegionPos;
import net.wurstclient.util.RenderUtils;

public class PathFinder
{
	private static final MinecraftClient MC = WurstClient.MC;
	
	private final PlayerAbilities abilities = PlayerAbilities.get();
	protected boolean fallingAllowed = true;
	protected boolean divingAllowed = true;
	
	private final PathPos start;
	protected PathPos current;
	private final BlockPos goal;
	
	private final HashMap<PathPos, Float> costMap = new HashMap<>();
	protected final HashMap<PathPos, PathPos> prevPosMap = new HashMap<>();
	private final PathQueue queue = new PathQueue();
	
	protected int thinkSpeed = 1024;
	protected int thinkTime = 200;
	private int iterations;
	
	protected boolean done;
	protected boolean failed;
	private final ArrayList<PathPos> path = new ArrayList<>();
	
	public PathFinder(BlockPos goal)
	{
		if(MC.player.isOnGround())
			start = new PathPos(BlockPos.ofFloored(MC.player.getX(),
				MC.player.getY() + 0.5, MC.player.getZ()));
		else
			start = new PathPos(BlockPos.ofFloored(MC.player.getPos()));
		this.goal = goal;
		
		costMap.put(start, 0F);
		queue.add(start, getHeuristic(start));
	}
	
	public PathFinder(PathFinder pathFinder)
	{
		this(pathFinder.goal);
		thinkSpeed = pathFinder.thinkSpeed;
		thinkTime = pathFinder.thinkTime;
	}
	
	public void think()
	{
		if(done)
			throw new IllegalStateException("路径已经找到了!");
		
		int i = 0;
		for(; i < thinkSpeed && !checkFailed(); i++)
		{
			// get next position from queue
			current = queue.poll();
			
			// check if path is found
			if(checkDone())
				return;
			
			// add neighbors to queue
			for(PathPos next : getNeighbors(current))
			{
				// check cost
				float newCost = costMap.get(current) + getCost(current, next);
				if(costMap.containsKey(next) && costMap.get(next) <= newCost)
					continue;
				
				// add to queue
				costMap.put(next, newCost);
				prevPosMap.put(next, current);
				queue.add(next, newCost + getHeuristic(next));
			}
		}
		iterations += i;
	}
	
	protected boolean checkDone()
	{
		return done = goal.equals(current);
	}
	
	private boolean checkFailed()
	{
		return failed = queue.isEmpty() || iterations >= thinkSpeed * thinkTime;
	}
	
	private ArrayList<PathPos> getNeighbors(PathPos pos)
	{
		ArrayList<PathPos> neighbors = new ArrayList<>();
		
		// abort if too far away
		if(Math.abs(start.getX() - pos.getX()) > 256
			|| Math.abs(start.getZ() - pos.getZ()) > 256)
			return neighbors;
		
		// get all neighbors
		BlockPos north = pos.north();
		BlockPos east = pos.east();
		BlockPos south = pos.south();
		BlockPos west = pos.west();
		
		BlockPos northEast = north.east();
		BlockPos southEast = south.east();
		BlockPos southWest = south.west();
		BlockPos northWest = north.west();
		
		BlockPos up = pos.up();
		BlockPos down = pos.down();
		
		// flying
		boolean flying = canFlyAt(pos);
		// walking
		boolean onGround = canBeSolid(down);
		
		// player can move sideways if flying, standing on the ground, jumping,
		// or inside of a block that allows sideways movement (ladders, webs,
		// etc.)
		if(flying || onGround || pos.isJumping()
			|| canMoveSidewaysInMidairAt(pos) || canClimbUpAt(pos.down()))
		{
			// north
			if(checkHorizontalMovement(pos, north))
				neighbors.add(new PathPos(north));
			
			// east
			if(checkHorizontalMovement(pos, east))
				neighbors.add(new PathPos(east));
			
			// south
			if(checkHorizontalMovement(pos, south))
				neighbors.add(new PathPos(south));
			
			// west
			if(checkHorizontalMovement(pos, west))
				neighbors.add(new PathPos(west));
			
			// north-east
			if(checkDiagonalMovement(pos, Direction.NORTH, Direction.EAST))
				neighbors.add(new PathPos(northEast));
			
			// south-east
			if(checkDiagonalMovement(pos, Direction.SOUTH, Direction.EAST))
				neighbors.add(new PathPos(southEast));
			
			// south-west
			if(checkDiagonalMovement(pos, Direction.SOUTH, Direction.WEST))
				neighbors.add(new PathPos(southWest));
			
			// north-west
			if(checkDiagonalMovement(pos, Direction.NORTH, Direction.WEST))
				neighbors.add(new PathPos(northWest));
		}
		
		// up
		if(pos.getY() < MC.world.getTopYInclusive() && canGoThrough(up.up())
			&& (flying || onGround || canClimbUpAt(pos))
			&& (flying || canClimbUpAt(pos) || goal.equals(up)
				|| canSafelyStandOn(north) || canSafelyStandOn(east)
				|| canSafelyStandOn(south) || canSafelyStandOn(west))
			&& (divingAllowed || BlockUtils.getBlock(up.up()) != Blocks.WATER))
			neighbors.add(new PathPos(up, onGround));
		
		// down
		if(pos.getY() > MC.world.getBottomY() && canGoThrough(down)
			&& canGoAbove(down.down()) && (flying || canFallBelow(pos))
			&& (divingAllowed || BlockUtils.getBlock(pos) != Blocks.WATER))
			neighbors.add(new PathPos(down));
		
		return neighbors;
	}
	
	private boolean checkHorizontalMovement(BlockPos current, BlockPos next)
	{
		if(isPassable(next) && (canFlyAt(current) || canGoThrough(next.down())
			|| canSafelyStandOn(next.down())))
			return true;
		
		return false;
	}
	
	private boolean checkDiagonalMovement(BlockPos current,
		Direction direction1, Direction direction2)
	{
		BlockPos horizontal1 = current.offset(direction1);
		BlockPos horizontal2 = current.offset(direction2);
		BlockPos next = horizontal1.offset(direction2);
		
		if(isPassableWithoutMining(horizontal1)
			&& isPassableWithoutMining(horizontal2)
			&& checkHorizontalMovement(current, next))
			return true;
		
		return false;
	}
	
	protected boolean isPassable(BlockPos pos)
	{
		if(!canGoThrough(pos) && !isMineable(pos))
			return false;
		
		BlockPos up = pos.up();
		if(!canGoThrough(up) && !isMineable(up))
			return false;
		
		if(!canGoAbove(pos.down()))
			return false;
		
		if(!divingAllowed && BlockUtils.getBlock(up) == Blocks.WATER)
			return false;
		
		return true;
	}
	
	protected boolean isPassableWithoutMining(BlockPos pos)
	{
		if(!canGoThrough(pos))
			return false;
		
		BlockPos up = pos.up();
		if(!canGoThrough(up))
			return false;
		
		if(!canGoAbove(pos.down()))
			return false;
		
		if(!divingAllowed && BlockUtils.getBlock(up) == Blocks.WATER)
			return false;
		
		return true;
	}
	
	protected boolean isMineable(BlockPos pos)
	{
		return false;
	}
	
	@SuppressWarnings("deprecation")
	protected boolean canBeSolid(BlockPos pos)
	{
		BlockState state = BlockUtils.getState(pos);
		Block block = state.getBlock();
		
		return state.blocksMovement() && !(block instanceof AbstractSignBlock)
			|| block instanceof LadderBlock || abilities.jesus()
				&& (block == Blocks.WATER || block == Blocks.LAVA);
	}
	
	@SuppressWarnings("deprecation")
	private boolean canGoThrough(BlockPos pos)
	{
		// check if loaded
		// Can't see why isChunkLoaded() is deprecated. Still seems to be widely
		// used with no replacement.
		if(!MC.world.isChunkLoaded(pos))
			return false;
		
		// check if solid
		BlockState state = BlockUtils.getState(pos);
		Block block = state.getBlock();
		if(state.blocksMovement() && !(block instanceof AbstractSignBlock))
			return false;
		
		// check if trapped
		if(block instanceof TripwireBlock
			|| block instanceof PressurePlateBlock)
			return false;
		
		// check if safe
		if(!abilities.invulnerable()
			&& (block == Blocks.LAVA || block instanceof AbstractFireBlock))
			return false;
		
		return true;
	}
	
	private boolean canGoAbove(BlockPos pos)
	{
		// check for fences, etc.
		Block block = BlockUtils.getBlock(pos);
		if(block instanceof FenceBlock || block instanceof WallBlock
			|| block instanceof FenceGateBlock)
			return false;
		
		return true;
	}
	
	private boolean canSafelyStandOn(BlockPos pos)
	{
		// check if solid
		if(!canBeSolid(pos))
			return false;
		
		// check if safe
		BlockState state = BlockUtils.getState(pos);
		Fluid fluid = state.getFluidState().getFluid();
		if(!abilities.invulnerable() && (state.getBlock() instanceof CactusBlock
			|| fluid instanceof LavaFluid))
			return false;
		
		return true;
	}
	
	private boolean canFallBelow(PathPos pos)
	{
		// check if player can keep falling
		BlockPos down2 = pos.down(2);
		if(fallingAllowed && canGoThrough(down2))
			return true;
		
		// check if player can stand below
		if(!canSafelyStandOn(down2))
			return false;
		
		// check if fall damage is off
		if(abilities.immuneToFallDamage() && fallingAllowed)
			return true;
		
		// check if fall ends with slime block
		if(BlockUtils.getBlock(down2) instanceof SlimeBlock && fallingAllowed)
			return true;
		
		// check fall damage
		BlockPos prevPos = pos;
		for(int i = 0; i <= (fallingAllowed ? 3 : 1); i++)
		{
			// check if prevPos does not exist, meaning that the pathfinding
			// started during the fall and fall damage should be ignored because
			// it cannot be prevented
			if(prevPos == null)
				return true;
				
			// check if point is not part of this fall, meaning that the fall is
			// too short to cause any damage
			if(!pos.up(i).equals(prevPos))
				return true;
			
			// check if block resets fall damage
			Block prevBlock = BlockUtils.getBlock(prevPos);
			BlockState prevState = BlockUtils.getState(prevPos);
			if(prevState.getFluidState().getFluid() instanceof WaterFluid
				|| prevBlock instanceof LadderBlock
				|| prevBlock instanceof VineBlock
				|| prevBlock instanceof CobwebBlock)
				return true;
			
			prevPos = prevPosMap.get(prevPos);
		}
		
		return false;
	}
	
	private boolean canFlyAt(BlockPos pos)
	{
		return abilities.flying() || !abilities.noWaterSlowdown()
			&& BlockUtils.getBlock(pos) == Blocks.WATER;
	}
	
	private boolean canClimbUpAt(BlockPos pos)
	{
		// check if this block works for climbing
		Block block = BlockUtils.getBlock(pos);
		if(!abilities.spider() && !(block instanceof LadderBlock)
			&& !(block instanceof VineBlock))
			return false;
		
		// check if any adjacent block is solid
		BlockPos up = pos.up();
		if(!canBeSolid(pos.north()) && !canBeSolid(pos.east())
			&& !canBeSolid(pos.south()) && !canBeSolid(pos.west())
			&& !canBeSolid(up.north()) && !canBeSolid(up.east())
			&& !canBeSolid(up.south()) && !canBeSolid(up.west()))
			return false;
		
		return true;
	}
	
	private boolean canMoveSidewaysInMidairAt(BlockPos pos)
	{
		// check feet
		Block blockFeet = BlockUtils.getBlock(pos);
		if(BlockUtils.getBlock(pos) instanceof FluidBlock
			|| blockFeet instanceof LadderBlock
			|| blockFeet instanceof VineBlock
			|| blockFeet instanceof CobwebBlock)
			return true;
		
		// check head
		Block blockHead = BlockUtils.getBlock(pos.up());
		if(BlockUtils.getBlock(pos.up()) instanceof FluidBlock
			|| blockHead instanceof CobwebBlock)
			return true;
		
		return false;
	}
	
	private float getCost(BlockPos current, BlockPos next)
	{
		float[] costs = {0.5F, 0.5F};
		BlockPos[] positions = {current, next};
		
		for(int i = 0; i < positions.length; i++)
		{
			BlockPos pos = positions[i];
			Block block = BlockUtils.getBlock(pos);
			
			// liquids
			if(block == Blocks.WATER && !abilities.noWaterSlowdown())
				costs[i] *= 1.3164437838225804F;
			else if(block == Blocks.LAVA)
				costs[i] *= 4.539515393656079F;
			
			// soul sand
			if(!canFlyAt(pos)
				&& BlockUtils.getBlock(pos.down()) instanceof SoulSandBlock)
				costs[i] *= 2.5F;
			
			// mining
			if(isMineable(pos))
				costs[i] *= 2F;
			if(isMineable(pos.up()))
				costs[i] *= 2F;
		}
		
		float cost = costs[0] + costs[1];
		
		// diagonal movement
		if(current.getX() != next.getX() && current.getZ() != next.getZ())
			cost *= 1.4142135623730951F;
		
		return cost;
	}
	
	private float getHeuristic(BlockPos pos)
	{
		float dx = Math.abs(pos.getX() - goal.getX());
		float dy = Math.abs(pos.getY() - goal.getY());
		float dz = Math.abs(pos.getZ() - goal.getZ());
		return 1.001F * (dx + dy + dz - 0.5857864376269049F * Math.min(dx, dz));
	}
	
	public PathPos getCurrentPos()
	{
		return current;
	}
	
	public BlockPos getGoal()
	{
		return goal;
	}
	
	public int countProcessedBlocks()
	{
		return prevPosMap.size();
	}
	
	public int getQueueSize()
	{
		return queue.size();
	}
	
	public float getCost(BlockPos pos)
	{
		return costMap.get(pos);
	}
	
	public boolean isDone()
	{
		return done;
	}
	
	public boolean isFailed()
	{
		return failed;
	}
	
	public ArrayList<PathPos> formatPath()
	{
		if(!done && !failed)
			throw new IllegalStateException("未找到路径!");
		if(!path.isEmpty())
			throw new IllegalStateException("路径已经格式化!");
		
		// get last position
		PathPos pos;
		if(!failed)
			pos = current;
		else
		{
			pos = start;
			for(PathPos next : prevPosMap.keySet())
				if(getHeuristic(next) < getHeuristic(pos)
					&& (canFlyAt(next) || canBeSolid(next.down())))
					pos = next;
		}
		
		// get positions
		while(pos != null)
		{
			path.add(pos);
			pos = prevPosMap.get(pos);
		}
		
		// reverse path
		Collections.reverse(path);
		
		return path;
	}
	
	public void renderPath(MatrixStack matrixStack, boolean debugMode,
		boolean depthTest)
	{
		VertexConsumerProvider.Immediate vcp =
			MC.getBufferBuilders().getEntityVertexConsumers();
		VertexConsumer buffer =
			vcp.getBuffer(WurstRenderLayers.getLines(depthTest));
		
		matrixStack.push();
		
		RegionPos region = RenderUtils.getCameraRegion();
		Vec3d regionOffset = region.negate().toVec3d();
		RenderUtils.applyRegionalRenderOffset(matrixStack, region);
		
		if(debugMode)
		{
			int thingsRendered = 0;
			
			// queue (yellow)
			for(PathPos element : queue.toArray())
			{
				if(thingsRendered >= 5000)
					break;
				
				Box box = new Box(element).offset(regionOffset).contract(0.4);
				RenderUtils.drawNode(matrixStack, buffer, box, 0xC0FFFF00);
				thingsRendered++;
			}
			
			// processed (red or magenta)
			for(Entry<PathPos, PathPos> entry : prevPosMap.entrySet())
			{
				if(thingsRendered >= 5000)
					break;
				
				int color =
					entry.getKey().isJumping() ? 0xC0FF00FF : 0xC0FF0000;
				
				RenderUtils.drawArrow(matrixStack, buffer, entry.getValue(),
					entry.getKey(), region, color);
				thingsRendered++;
			}
		}
		
		// path (blue or green)
		int pathColor = debugMode ? 0xC00000FF : 0xC000FF00;
		for(int i = 0; i < path.size() - 1; i++)
			RenderUtils.drawArrow(matrixStack, buffer, path.get(i),
				path.get(i + 1), region, pathColor);
		
		matrixStack.pop();
		
		vcp.drawCurrentLayer();
	}
	
	public boolean isPathStillValid(int index)
	{
		if(path.isEmpty())
			throw new IllegalStateException("路径没有被格式化!");
		
		// check player abilities
		if(!abilities.equals(PlayerAbilities.get()))
			return false;
		
		// if index is zero, check if first pos is safe
		if(index == 0)
		{
			PathPos pos = path.get(0);
			if(!isPassable(pos) || !canFlyAt(pos) && !canGoThrough(pos.down())
				&& !canSafelyStandOn(pos.down()))
				return false;
		}
		
		// check path
		for(int i = Math.max(1, index); i < path.size(); i++)
			if(!getNeighbors(path.get(i - 1)).contains(path.get(i)))
				return false;
			
		return true;
	}
	
	public PathProcessor getProcessor()
	{
		if(abilities.flying())
			return new FlyPathProcessor(path, abilities.creativeFlying());
		
		return new WalkPathProcessor(path);
	}
	
	public void setThinkSpeed(int thinkSpeed)
	{
		this.thinkSpeed = thinkSpeed;
	}
	
	public void setThinkTime(int thinkTime)
	{
		this.thinkTime = thinkTime;
	}
	
	public void setFallingAllowed(boolean fallingAllowed)
	{
		this.fallingAllowed = fallingAllowed;
	}
	
	public void setDivingAllowed(boolean divingAllowed)
	{
		this.divingAllowed = divingAllowed;
	}
	
	public List<PathPos> getPath()
	{
		return Collections.unmodifiableList(path);
	}
}
