package com.PintTheDragon.DiscordBridge.JoinActions;

import com.PintTheDragon.DiscordBridge.JoinAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionSpawn implements JoinAction {

	@Override
	public void doAction(Player p, String data) {
		p.teleport(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
		p.performCommand("spawn");
	}

}
