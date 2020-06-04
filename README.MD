#DiscordBridge

Link a minecraft and discord server in 2 commands.

DiscordBridge is a Bukkit (Minecraft) plugin and Discord Bot to build a complex bridge between a Discord server and several Minecraft servers. DiscordBridge is easy to configure, but also features complex configuration options to easily allow or disallow certain commands on a per-server basis. You can configure all of DiscordBridge's commands (such as |bal and |spawn). One of DiscordBridge's main features is called "player setting". It allows players to link their Minecraft accounts with their Discord accounts, to give them a multitude of commands for interacting with their Minecraft player. If they get stuck in a crash zone, they can always run |spawn (or whatever prefix you set with |cp ). If they want to show off their balance, they can run |bal or |balance. There are a lot of commands avalible (especially if you have Essentials), and DiscordBridge integrates with a lot of other plugins.



##Features
DiscordBridge offers many features. Here is provided a list of some of its biggest features. For usage instructions (and every command), you should type |help ingame.

    Link Minecraft and Discord accounts to access player-specific commands, such as teleporting to spawn (even when you're offline) with |spawn, or checking your balance with |balance (|bal). All of these player-specific commands are documented in the |help command.
    Bridge all chat between a minecraft server and a channel. Multiple servers are supported. All player messages from the Minecraft server will be sent to the Discord server in a defined channel, and every message in a specific channel in the Discord server will be sent to the Minecraft server.
    Messages for players joining that respect popular vanish plugins.
    DiscordBridge is insanely easy to configure, but offers a lot of optional configuration to customize what your bot does. These configurations are player specific.
    DiscordBridge offers "Admin Channels", which display most console output, and allow administrators to privately run commands that are still tied to the server. One example of this is the |acmd command, (|acmd ), which runs a command as console. Only administrators can run this, and it can be used in either the public server channel, or an Admin Channel.



##Installing
To install DiscordBridge, just put the jar in your plugins directory, then restart your server. Then, you need to add the bot to your discord server by going to https://discord.com/oauth2/authorize?client_id=717635004783394877&permissions=8&scope=bot. Now, create a channel in your discord server for your bot and then run |setupserver <name> <#channel> (make sure to replace | with a custom prefix if you changed yours), replacing <name> with your server's name and <#channel> with a mention of the channel. The bot will not pm you a command. Run this command on your minecraft server (as op or console). That's it! You're all setup now. You can repeat these steps (excluding inviting the bot) for any other servers in your network.



##Adding Admin Channels
One feature of DiscordBridge is the ability to add Admin Channels, which essentially give you access to see the server's console and to run commands in it. Once you've setup DiscordBridge for that server, you can run |setadminchannel <name> <#channel>, replacing <name> with the name of your server (the one you chose earlier) and <#channel> with a mention of the channel you want to use as an admin channel. That's it! DiscordBridge is not 100% ready for use!

If you need any help with setup, or run into any issues, PLEASE let me know and I will do my best to help!

If you are a developer and want to make a plugin using DiscordBridge's API, go here.
