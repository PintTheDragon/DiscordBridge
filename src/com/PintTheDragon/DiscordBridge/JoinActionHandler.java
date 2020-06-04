package com.PintTheDragon.DiscordBridge;

import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class JoinActionHandler {
	public ArrayList<String[]> jaar = new ArrayList<String[]>();
	public String UUID;

	public JoinActionHandler(String u) {
		UUID = u;
	}

	public JoinActionHandler(String u, ArrayList<String[]> t) {
		UUID = u;
		jaar = t;
	}

	public static JoinActionHandler fromString(String str) throws ClassNotFoundException, IOException {
		String[] split = str.split("/", 2);
		if (split.length != 2) return null;
		String UUID = split[0];
		String arr = split[1];
		return new JoinActionHandler(UUID, Util.fromString(arr));
	}

	public static JoinActionHandler fromUUID(String UUID) {
		try {
			new File(new File("plugins", "DiscordBridge"), "playerdata").mkdirs();
			File f = new File(new File(new File("plugins", "DiscordBridge"), "playerdata"), UUID + ".data");
			if (!f.exists()) return new JoinActionHandler(UUID);
			BufferedReader br = new BufferedReader(new FileReader(f));
			String text = br.lines().collect(Collectors.joining(System.lineSeparator()));
			br.close();
			JoinActionHandler hand = JoinActionHandler.fromString(text);
			if (hand == null) return new JoinActionHandler(UUID);
			return hand;
		} catch (Exception e1) {
			return new JoinActionHandler(UUID);
		}
	}

	public void addAction(JoinAction a, String data) {
		String[] arr = {a.getClass().getCanonicalName(), data};
		jaar.add(arr);
	}
	public void addAction(JoinAction a) {
		String[] arr = {a.getClass().getCanonicalName(), ""};
		jaar.add(arr);
	}

	public void iterate(Player p) {
		for (int i = 0; i < jaar.size(); i++) {
			try {
				((JoinAction) Class.forName(jaar.get(i)[0]).newInstance()).doAction(p, jaar.get(i)[1]);
			} catch (Exception e) {
			}
		}
		jaar = new ArrayList<String[]>();
		try {
			this.save();
		} catch (IOException e) {
		}
	}

	@Override
	public String toString() {
		try {
			return UUID + "/" + Util.toString(jaar);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void save() throws IOException {
		new File(new File("plugins", "DiscordBridge"), "playerdata").mkdirs();
		File f = new File(new File(new File("plugins", "DiscordBridge"), "playerdata"), UUID + ".data");
		if (!f.exists()) f.createNewFile();
		else {
			f.delete();
			f.createNewFile();
		}
		BufferedWriter w = new BufferedWriter(new FileWriter(f));
		w.write(this.toString());
		w.flush();
		w.close();
	}
}
