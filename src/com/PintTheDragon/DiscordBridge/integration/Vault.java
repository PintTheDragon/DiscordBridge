package com.PintTheDragon.DiscordBridge.integration;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault {
	private static Economy eco = null;
	private static Chat chat = null;

	public static String getBal(OfflinePlayer pl) {
		return Double.toString(eco.getBalance(pl));
	}

	public static boolean setupEco() {
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        eco = rsp.getProvider();
        return eco != null;
	}

	public static boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
	}

	public static String getPrefix(Player p) {
		return chat.getPlayerPrefix(p);
	}

	public static String getSuffix(Player p) {
		return chat.getPlayerSuffix(p);
	}

	public static boolean isInGroup(Player p, String group){ return chat.playerInGroup(p, group); }

	public static String[] getGroups(Player p){ return chat.getGroups(); }
}
