/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.util;

import static org.junit.jupiter.api.Assertions.*;

import org.joml.Quaternionf;
import org.junit.jupiter.api.Test;

import net.minecraft.util.math.Vec3d;

// 测试Rotation类的各种方法
class RotationTest
{
	// 测试getAngleTo方法，当两个Rotation对象的旋转角度相同时
	@Test
	void testGetAngleToSameRotation()
	{
		Rotation r1 = new Rotation(0, 0);
		Rotation r2 = new Rotation(0, 0);
		double angle = r1.getAngleTo(r2);
		assertEquals(0, angle);
		
		r1 = new Rotation(360, 0);
		r2 = new Rotation(0, 0);
		angle = r1.getAngleTo(r2);
		assertEquals(0, angle);
		
		r1 = new Rotation(0, 0);
		r2 = new Rotation(0, -360);
		angle = r1.getAngleTo(r2);
		assertEquals(0, angle);
	}
	
	// 测试getAngleTo方法，当两个Rotation对象的pitch相差90度时
	@Test
	void testGetAngleTo90DegPitch()
	{
		Rotation r1 = new Rotation(0, 0);
		Rotation r2 = new Rotation(90, 0);
		double angle = r1.getAngleTo(r2);
		assertEquals(90, angle);
		
		r1 = new Rotation(0, 0);
		r2 = new Rotation(-90, 0);
		angle = r1.getAngleTo(r2);
		assertEquals(90, angle);
		
		r1 = new Rotation(0, 0);
		r2 = new Rotation(270, 0);
		angle = r1.getAngleTo(r2);
		assertEquals(90, angle);
		
		r1 = new Rotation(0, 0);
		r2 = new Rotation(-270, 0);
		angle = r1.getAngleTo(r2);
		assertEquals(90, angle);
	}
	
	// 测试getAngleTo方法，当两个Rotation对象的yaw相差90度时
	@Test
	void testGetAngleTo90DegYaw()
	{
		Rotation r1 = new Rotation(0, 0);
		Rotation r2 = new Rotation(0, 90);
		double angle = r1.getAngleTo(r2);
		assertEquals(90, angle);
		
		r1 = new Rotation(0, 0);
		r2 = new Rotation(0, -90);
		angle = r1.getAngleTo(r2);
		assertEquals(90, angle);
		
		r1 = new Rotation(0, 0);
		r2 = new Rotation(0, 270);
		angle = r1.getAngleTo(r2);
		assertEquals(90, angle);
		
		r1 = new Rotation(0, 0);
		r2 = new Rotation(0, -270);
		angle = r1.getAngleTo(r2);
		assertEquals(90, angle);
	}
	
	// 测试getAngleTo方法，当两个Rotation对象的yaw跨越0度时
	@Test
	void testGetAngleAcrossZeroYaw()
	{
		Rotation r1 = new Rotation(1, 0);
		Rotation r2 = new Rotation(-1, 0);
		double angle = r1.getAngleTo(r2);
		assertEquals(2, angle);
		
		r1 = new Rotation(1, 0);
		r2 = new Rotation(359, 0);
		angle = r1.getAngleTo(r2);
		assertEquals(2, angle);
		
		r1 = new Rotation(361, 0);
		r2 = new Rotation(-1, 0);
		angle = r1.getAngleTo(r2);
		assertEquals(2, angle);
	}
	
	// 测试getAngleTo方法，当两个Rotation对象的pitch跨越0度时
	@Test
	void testGetAngleAcrossZeroPitch()
	{
		Rotation r1 = new Rotation(0, 1);
		Rotation r2 = new Rotation(0, -1);
		double angle = r1.getAngleTo(r2);
		assertEquals(2, angle);
		
		r2 = r2.withPitch(359);
		angle = r1.getAngleTo(r2);
		assertEquals(2, angle);
		
		r1 = r1.withPitch(361);
		angle = r1.getAngleTo(r2);
		assertEquals(2, angle);
	}
	
	// 测试toLookVec方法，将Rotation对象转换为Vec3d对象
	@Test
	void testToLookVec()
	{
		Rotation r = new Rotation(0, 0);
		Vec3d vec = r.toLookVec();
		assertAlmostEquals(0, vec.x);
		assertAlmostEquals(0, vec.y);
		assertAlmostEquals(1, vec.z);
		
		r = new Rotation(90, 0);
		vec = r.toLookVec();
		assertAlmostEquals(-1, vec.x);
		assertAlmostEquals(0, vec.y);
		assertAlmostEquals(0, vec.z);
		
		r = new Rotation(180, 0);
		vec = r.toLookVec();
		assertAlmostEquals(0, vec.x);
		assertAlmostEquals(0, vec.y);
		assertAlmostEquals(-1, vec.z);
		
		r = new Rotation(270, 0);
		vec = r.toLookVec();
		assertAlmostEquals(1, vec.x);
		assertAlmostEquals(0, vec.y);
		assertAlmostEquals(0, vec.z);
	}
	
	// 测试toQuaternion方法，将Rotation对象转换为Quaternionf对象
	@Test
	void testToQuaternion()
	{
		Rotation r = new Rotation(0, 0);
		Quaternionf q = r.toQuaternion();
		assertAlmostEquals(1, q.w);
		assertAlmostEquals(0, q.x);
		assertAlmostEquals(0, q.y);
		assertAlmostEquals(0, q.z);
		
		r = new Rotation(90, 0);
		q = r.toQuaternion();
		assertAlmostEquals(0.70710677F, q.w);
		assertAlmostEquals(0, q.x);
		assertAlmostEquals(-0.70710677F, q.y);
		assertAlmostEquals(0, q.z);
		
		r = new Rotation(180, 0);
		q = r.toQuaternion();
		assertAlmostEquals(0, q.w);
		assertAlmostEquals(0, q.x);
		assertAlmostEquals(1, q.y);
		assertAlmostEquals(0, q.z);
		
		r = new Rotation(270, 0);
		q = r.toQuaternion();
		assertAlmostEquals(0.70710677F, q.w);
		assertAlmostEquals(0, q.x);
		assertAlmostEquals(0.70710677F, q.y);
		assertAlmostEquals(0, q.z);
	}
	
	// 测试wrapped方法，创建一个Rotation对象并确保其yaw和pitch在合理范围内
	@Test
	void testWrapped()
	{
		Rotation r = Rotation.wrapped(0, 0);
		assertEquals(0, r.yaw());
		assertEquals(0, r.pitch());
		
		r = Rotation.wrapped(360, 360);
		assertEquals(0, r.yaw());
		assertEquals(0, r.pitch());
		
		r = Rotation.wrapped(270, 270);
		assertEquals(-90, r.yaw());
		assertEquals(-90, r.pitch());
		
		r = Rotation.wrapped(-270, -270);
		assertEquals(90, r.yaw());
		assertEquals(90, r.pitch());
	}
	
	// 断言两个浮点数几乎相等
	private void assertAlmostEquals(double expected, double actual)
	{
		if(Math.abs(expected - actual) > 1e-6)
			fail("expected: <" + expected + "> but was: <" + actual + ">");
	}
}
