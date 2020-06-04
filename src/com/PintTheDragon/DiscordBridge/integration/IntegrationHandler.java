package com.PintTheDragon.DiscordBridge.integration;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.HashMap;

public class IntegrationHandler {

	private static HashMap<String, Boolean> exists = new HashMap<String, Boolean>();
	private static boolean vaultEco = false;
	private static boolean vaultChat = false;

	public static void init() {
		tryExists();
	}

	private static void tryExists() {
		try {
			Class.forName("net.milkbowl.vault.economy.Economy");
			exists.put("vault", true);
			if (Vault.setupEco()) vaultEco = true;
			if (Vault.setupChat()) vaultChat = true;
		} catch (Exception e) {
			exists.put("vault", false);
		}
		try {
			Class.forName("me.lucko.luckperms.api.LuckPerms");
			if (LuckPerms.init()) {
				exists.put("luckperms", true);
			} else {
				exists.put("luckperms", false);
			}
		} catch (Exception e) {
			exists.put("luckperms", false);
		}

		try {
			Class.forName("me.clip.placeholderapi.PlaceholderAPI");
			if (LuckPerms.init()) {
				exists.put("placeholderapi", true);
			} else {
				exists.put("placeholderapi", false);
			}
		} catch (Exception e) {
			exists.put("placeholderapi", false);
		}
	}

	public static boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
	}

	public static boolean exists(String in) {
		if (!exists.containsKey(in)) return false;
		return exists.get(in);
	}

	public static String getBal(OfflinePlayer pl) {
		if (exists("vault")) {
			if (!vaultEco) return "";
			return Vault.getBal(pl);
		}
		return null;
	}

	public static String getPrefix(Player pl) {
		if (!IntegrationConfig.isEnabled("usechatformatting")) return "";
		if (exists("vault")) {
			if (!vaultChat) return "";
			return Vault.getPrefix(pl);
		}
		if (exists("luckperms")) {
			return LuckPerms.getPrefix(pl);
		}
		return "";
	}

	public static String getSuffix(Player pl) {
		if (!IntegrationConfig.isEnabled("usechatformatting")) return "";
		if (exists("vault")) {
			if (!vaultChat) return "";
			return Vault.getSuffix(pl);
		}
		if (exists("luckperms")) {
			return LuckPerms.getSuffix(pl);
		}
		return "";
	}

	public static String replace(Player p, String in){
		if (exists("placeholderapi")) {
			return PlaceholderAPI.replace(p, in);
		}
		return in;
	}
}
