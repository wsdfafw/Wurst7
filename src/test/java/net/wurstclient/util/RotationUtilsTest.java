/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.util;
 
import static org.junit.jupiter.api.Assertions.*;
 
import org.junit.jupiter.api.Test;
 
// 测试RotationUtils类的工具方法
class RotationUtilsTest
{
 // 大部分其他方法依赖于MC.player，
 // 这使得为它们编写测试非常困难。
  
 // 测试限制角度变化的方法，带最大限制
 @Test
 void testLimitAngleChangeWithMax()
 {
  float result = RotationUtils.limitAngleChange(0, 179, 90);
  assertEquals(90, result);
   
  result = RotationUtils.limitAngleChange(0, -179, 90);
  assertEquals(-90, result);
   
  result = RotationUtils.limitAngleChange(179, -179, 90);
  assertEquals(181, result);
   
  result = RotationUtils.limitAngleChange(-179, 179, 90);
  assertEquals(-181, result);
 }
  
 // 测试限制角度变化的方法，不带最大限制
 @Test
 void testLimitAngleChangeWithoutMax()
 {
  float result = RotationUtils.limitAngleChange(0, 179);
  assertEquals(179, result);
   
  result = RotationUtils.limitAngleChange(0, -179);
  assertEquals(-179, result);
   
  result = RotationUtils.limitAngleChange(179, -179);
  assertEquals(181, result);
   
  result = RotationUtils.limitAngleChange(-179, 179);
  assertEquals(-181, result);
 }
}