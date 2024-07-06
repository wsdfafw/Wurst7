/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.ai;
import java.util.List;
import java.util.LinkedList; // 采用LinkedList以应对可能的频繁插入/删除操作

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.wurstclient.util.RotationUtils;

public class FlyPathProcessor extends PathProcessor
{
    private final boolean creativeFlying;
    
    public FlyPathProcessor(List<PathPos> path, boolean creativeFlying)
    {
        super(path);
        // 使用LinkedList代替ArrayList
        if (path instanceof ArrayList) {
            this.path = new LinkedList<>(path);
        } else {
            this.path = path;
        }
        this.creativeFlying = creativeFlying;
    }
    
    @Override
    public void process()
    {
        if (path.isEmpty() || index >= path.size()) {
            // 如果路径为空或索引超出范围，直接返回，避免进一步处理
            return;
        }
        
        BlockPos pos = BlockPos.ofFloored(MC.player.getPos());
        Vec3d posVec = MC.player.getPos();
        BlockPos nextPos = path.get(index);
        int posIndex = path.indexOf(pos);
        
        if (posIndex == -1) {
            ticksOffPath++;
        } else {
            ticksOffPath = 0;
        }
        
        Box nextBox = calculateNextBox(nextPos); // 提取方法，提高代码的可读性
        
        // 更新索引
        if (posIndex > index || isPlayerWithinNextBox(posVec, nextBox)) {
            advanceIndex();
        }
        
        if (index >= path.size()) {
            done = true;
            return;
        }
        
        lockControls();
        MC.player.getAbilities().flying = creativeFlying;
        
        if (!attemptHorizontalMovement(nextPos, posVec)) {
            // 面向下一个位置
            facePosition(nextPos);
        }
        
        // 跳过空中节点
        skipMidAirNodes();
        
        if (creativeFlying) {
            modifyPlayerVelocity();
        }
        
        attemptLanding(nextPos, posVec);
    }
    
    private Box calculateNextBox(BlockPos nextPos) {
        return new Box(nextPos.getX() + 0.3, nextPos.getY(),
            nextPos.getZ() + 0.3, nextPos.getX() + 0.7, nextPos.getY() + 0.2,
            nextPos.getZ() + 0.7);
    }
    
    private boolean isPlayerWithinNextBox(Vec3d posVec, Box nextBox) {
        return posVec.x >= nextBox.minX && posVec.x <= nextBox.maxX
            && posVec.y >= nextBox.minY && posVec.y <= nextBox.maxY
            && posVec.z >= nextBox.minZ && posVec.z <= nextBox.maxZ;
    }
    
    private void advanceIndex() {
        if (path.indexOf(MC.player.getBlockPos()) > index) {
            index = path.indexOf(MC.player.getBlockPos()) + 1;
        } else {
            index++;
        }
    }
    
    private boolean attemptHorizontalMovement(BlockPos nextPos, Vec3d posVec) {
        boolean horizontal = posVec.x < nextBox.minX || posVec.x > nextBox.maxX
            || posVec.z < nextBox.minZ || posVec.z > nextBox.maxZ;
        
        if (horizontal) {
            facePosition(nextPos);
            return Math.abs(MathHelper.wrapDegrees(RotationUtils
                .getHorizontalAngleToLookVec(Vec3d.ofCenter(nextPos)))) > 1;
        }
        return false;
    }
    
    private void skipMidAirNodes() {
        Vec3i offset = path.get(index).subtract(path.get(index - 1));
        while (index < path.size() - 1
            && path.get(index).add(offset).equals(path.get(index + 1))) {
            index++;
        }
    }
    
    private void modifyPlayerVelocity() {
        Vec3d v = MC.player.getVelocity();
        MC.player.setVelocity(
            v.x / Math.max(Math.abs(v.x) * 50, 1),
            v.y / Math.max(Math.abs(v.y) * 50, 1),
            v.z / Math.max(Math.abs(v.z) * 50, 1)
        );
    }
    
    private void attemptLanding(BlockPos nextPos, Vec3d posVec) {
        Vec3d vecInPos = new Vec3d(nextPos.getX() + 0.5, nextPos.getY() + 0.1, nextPos.getZ() + 0.5);
        if (posVec.y < nextBox.maxY) {
            MC.options.sneakKey.setPressed(true);
        } else if (posVec.y > nextBox.minY) {
            MC.options.jumpKey.setPressed(true);
        }
        
        if (MC.player.verticalCollision) {
            MC.options.sneakKey.setPressed(false);
            MC.options.forwardKey.setPressed(true);
        }
    }
    
    // 其他方法保持不变
}