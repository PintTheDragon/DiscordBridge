package com.PintTheDragon.DiscordBridge.integration;

import org.bukkit.entity.Player;

public class PlaceholderAPI {

    public static String replace(Player p, String in){
        return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, in);
    }
}
