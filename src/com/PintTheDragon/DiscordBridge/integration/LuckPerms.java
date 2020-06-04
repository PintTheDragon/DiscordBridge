package com.PintTheDragon.DiscordBridge.integration;

import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.UserData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;

public class LuckPerms {
	static LuckPermsApi api = null;

	public static boolean init() {
		RegisteredServiceProvider<LuckPermsApi> provider = Bukkit.getServicesManager().getRegistration(LuckPermsApi.class);
		if (provider != null) {
			api = provider.getProvider();
			return true;

		}
		return false;
	}

	public static String getPrefix(Player pl) {
		if (api == null) return "";
		User u = api.getUser(pl.getUniqueId().toString());
		UserData data = u.getCachedData();
		Contexts c = api.getContextForUser(u).get();
		return data.getMetaData(c).getPrefix();
	}

	public static String getSuffix(Player pl) {
		if (api == null) return "";
		User u = api.getUser(pl.getUniqueId().toString());
		UserData data = u.getCachedData();
		Contexts c = api.getContextForUser(u).get();
		return data.getMetaData(c).getSuffix();
	}

	public static boolean isInGroup(Player p, String group){
		if (api == null) return false;
		return p.hasPermission("group."+group);
	}

	public static String[] getGroups(Player p){
		PermissionAttachmentInfo[] arr = p.getEffectivePermissions().toArray(new PermissionAttachmentInfo[0]);
		ArrayList<String> groups = new ArrayList<String>();
		for(int i = 0; i < arr.length; i++){
			PermissionAttachmentInfo info = arr[i];
			String perm = info.getPermission();
			if(perm.startsWith("group.")){
				groups.add(perm.substring(0, 6));
			}
		}
		return groups.toArray(new String[0]);
	}
}
