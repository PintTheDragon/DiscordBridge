package com.PintTheDragon.DiscordBridge;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

public class Util {

	public static OfflinePlayer UUIDtoOfflinePlayer(String uuid) {
		return Bukkit.getOfflinePlayer(UUID.fromString(uuid));
	}

	public static Player UUIDtoPlayer(String uuid) {
		return Bukkit.getPlayer(UUID.fromString(uuid));
	}

	public static String PlayertoUUID(Player p) {
		return p.getUniqueId().toString();
	}

	public static String toString(ArrayList<String[]> t) throws IOException {
        String msg = "";
		for (int i = 0; i < t.size(); i++) {
			msg += Base64.getEncoder().encodeToString((t.get(i)[0]+"/"+t.get(i)[1]).getBytes());
			if (i != t.size() - 1) msg += "/";
        }
		return Base64.getEncoder().encodeToString(msg.getBytes());
    }

	public static ArrayList<String[]> fromString(String s) throws IOException,
			ClassNotFoundException {
		String[] data = new String(Base64.getDecoder().decode(s)).split("/");
		ArrayList<String[]> out = new ArrayList<String[]>();
		for (int i = 0; i < data.length; i++) {
			String[] data2 = new String(Base64.getDecoder().decode(data[i])).split("/");
			if(data2.length == 1){
				String old = data2[0];
				data2 = new String[2];
				data2[0] = old;
				data2[1] = "";
			}
			out.add(data2);
		}
		return out;
	}
}
