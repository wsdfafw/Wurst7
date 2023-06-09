/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks.autolibrarian;

import java.util.function.Consumer;

import net.minecraft.util.math.Vec3d;
import net.wurstclient.WurstClient;
import net.wurstclient.settings.EnumSetting;

public final class FacingSetting extends EnumSetting<FacingSetting.Facing>
{
	protected static final WurstClient WURST = WurstClient.INSTANCE;
	
	public FacingSetting()
	{
		super("面对", "如何面对村民和工地.\n\n"
			+ "\u00a7lOff\u00a7r - 根本不面对村民。会被反作弊插件检测到。\n\n"
			+ "\u00a7l服务器端\u00a7r - 在服务器端面对村民，同时仍"
			+ " 然允许你在客户端自由移动摄像机.\n\n"
			+ "\u00a7l客户端\u00a7r - 通过在客户端移动你的相机来面对村民。这是最合法的选择，但看起来可能会让人迷失方向.", Facing.values(),
			Facing.SERVER);
	}
	
	public enum Facing
	{
		OFF("Off", v -> {}),
		
		SERVER("服务器端",
			v -> WURST.getRotationFaker().faceVectorPacket(v)),
		
		CLIENT("客户端",
			v -> WURST.getRotationFaker().faceVectorClient(v));
		
		private String name;
		private Consumer<Vec3d> face;
		
		private Facing(String name, Consumer<Vec3d> face)
		{
			this.name = name;
			this.face = face;
		}
		
		public void face(Vec3d v)
		{
			face.accept(v);
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
