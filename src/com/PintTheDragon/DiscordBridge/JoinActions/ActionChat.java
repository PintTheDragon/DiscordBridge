package com.PintTheDragon.DiscordBridge.JoinActions;

import com.PintTheDragon.DiscordBridge.JoinAction;
import com.PintTheDragon.DiscordBridge.integration.IntegrationHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionChat implements JoinAction {
    @Override
    public void doAction(Player p, String data) {
        String cmd = data;
        cmd = cmd.replaceAll("\\${username}", p.getName()).replaceAll("\\${uuid}", p.getUniqueId().toString()).replaceAll("\\${posX}", Double.toString(p.getLocation().getX())).replaceAll("\\${posY}", Double.toString(p.getLocation().getY())).replaceAll("\\${posZ}", Double.toString(p.getLocation().getZ())).replaceAll("\\${world}", p.getLocation().getWorld().getName());
        cmd = IntegrationHandler.replace(p, cmd);
        p.chat(cmd);
    }
}
