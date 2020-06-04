package com.PintTheDragon.DiscordBridge.JoinActions;

import com.PintTheDragon.DiscordBridge.JoinAction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Base64;

public class ActionTeleport implements JoinAction {
    @Override
    public void doAction(Player p, String data) {
        String[] coords = data.split(" ");
        double x = 0,y = 0,z = 0;
        String world = "";
        Location l = p.getLocation();
        if(coords.length > 2) {
            x = Double.parseDouble(coords[0]);
            y = Double.parseDouble(coords[1]);
            z = Double.parseDouble(coords[2]);
            l.setX(x);
            l.setY(y);
            l.setZ(z);
        }
        if(coords.length > 3) {
            world = coords[3];
            l.setWorld(Bukkit.getWorld(world));
        }
        p.teleport(l);
    }
}
