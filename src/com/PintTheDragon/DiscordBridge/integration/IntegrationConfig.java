package com.PintTheDragon.DiscordBridge.integration;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

public class IntegrationConfig {

	static HashMap<String, Boolean> cmd = new HashMap<String, Boolean>();
	static HashMap<String, Boolean> properties = new HashMap<String, Boolean>();

	public static void setup(FileConfiguration conf) {
		conf.set("integration.commands.balance", true);
		conf.set("integration.commands.spawn", true);
		conf.set("integration.usechatformatting", true);
		cmd.put("balance", true);
		cmd.put("spawn", true);
		properties.put("usechatformatting", true);
	}

	public static void init(FileConfiguration conf) {
		cmd.put("balance", conf.getBoolean("integration.commands.balance"));
		cmd.put("spawn", conf.getBoolean("integration.commands.balance"));
		properties.put("usechatformatting", conf.getBoolean("integration.usechatformatting"));
	}

	public static boolean isEnabled(String key) {
		return cmd.containsKey(key);
	}
}
