/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.altmanager;

import java.net.Proxy;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import net.minecraft.client.util.Session;
import net.wurstclient.WurstClient;

public enum LoginManager
{
	;
	
	public static void login(String email, String password)
		throws LoginException
	{
		YggdrasilUserAuthentication auth =
			(YggdrasilUserAuthentication)new YggdrasilAuthenticationService(
				Proxy.NO_PROXY, "").createUserAuthentication(Agent.MINECRAFT);
		
		auth.setUsername(email);
		auth.setPassword(password);
		
		try
		{
			auth.logIn();
			
			GameProfile profile = auth.getSelectedProfile();
			String username = profile.getName();
			String uuid = profile.getId().toString();
			String accessToken = auth.getAuthenticatedToken();
			
			Session session =
				new Session(username, uuid, accessToken, "mojang");
			
			WurstClient.IMC.setSession(session);
			
		}catch(AuthenticationUnavailableException e)
		{
			throw new LoginException("无法与登录服务器建立连接!",
				e);
			
		}catch(AuthenticationException e)
		{
			e.printStackTrace();
			String msg = e.getMessage().toLowerCase();
			
			if(msg.contains("invalid username or password."))
				throw new LoginException("错误的密码! (或者 黑号封禁)",
					e);
			
			if(msg.contains("account migrated"))
				throw new LoginException("账号仍然是 Mojang 旗下的账户.",
					e);
			
			if(msg.contains("migrated"))
				throw new LoginException(
					"该账号已迁移到Microsoft帐户的帐户.", e);
			
			throw new LoginException("无法与登录服务器建立连接!",
				e);
			
		}catch(NullPointerException e)
		{
			e.printStackTrace();
			
			throw new LoginException("错误的密码! (或者 黑号封禁)", e);
		}
	}
	
	public static void changeCrackedName(String newName)
	{
		Session session = new Session(newName, "", "", "mojang");
		WurstClient.IMC.setSession(session);
	}
}
