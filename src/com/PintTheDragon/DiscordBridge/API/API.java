package com.PintTheDragon.DiscordBridge.API;

import com.PintTheDragon.DiscordBridge.ClientHandler;
import com.PintTheDragon.DiscordBridge.JoinAction;
import com.PintTheDragon.DiscordBridge.JoinActionHandler;
import com.PintTheDragon.DiscordBridge.JoinActions.ActionSpawn;

import java.io.IOException;
import java.util.HashMap;

public class API {
    public static HashMap<String, CommandHandler> commandRegistry = new HashMap<String, CommandHandler>();
    public static ClientHandler hand = null;

    public static boolean addCommand(String name, CommandHandler handler){
        try {
            if (commandRegistry.containsKey(name.toLowerCase())) return false;
            commandRegistry.put(name.toLowerCase(), handler);
            return true;
        }
        catch(Exception e){ return false; }
    }

    public static boolean isRegistered(String name){
        if(commandRegistry.containsKey(name.toLowerCase())) return true;
        return false;
    }

    public static CommandHandler getHandler(String name){
        if(!isRegistered(name)) return null;
        return commandRegistry.get(name.toLowerCase());
    }

    public static void sendMessage(String channelID, String message){
        if(hand == null) return;
        hand.manager.write("SENDA "+channelID+" "+message);
    }
    public static void sendMessage(String message){
        if(hand == null){System.out.println("not ready"); return;}
        hand.manager.write("SEND "+message);
    }
    public static void addAction(String UUID, JoinAction a, String data){
        JoinActionHandler h = JoinActionHandler.fromUUID(UUID);
        if (h == null)
            h = new JoinActionHandler(UUID);
        h.addAction(a);
        try {
            h.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void addAction(String UUID, JoinAction a) {
        addAction(UUID, a, "");
    }
}
