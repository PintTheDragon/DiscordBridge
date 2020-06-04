package com.PintTheDragon.DiscordBridge.API;

public interface CommandHandler {
    public void runCommand(String channelID, String authorID, boolean isAdmin, String authorUUID, String[] args);
}
