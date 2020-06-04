package com.PintTheDragon.DiscordBridge;

import com.PintTheDragon.DiscordBridge.integration.IntegrationConfig;
import com.PintTheDragon.DiscordBridge.integration.IntegrationHandler;
import org.bstatsDisB.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.parser.JSONParser;

import java.util.Base64;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class DiscordBridge extends JavaPlugin implements Listener {

    SocketManager sm;
    ConsoleHandler c;
    boolean enabled = true;
    boolean ran = false;

    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this, 7736);
        FileConfiguration conf = this.getConfig();
        if (!handleConf(conf)) return;
        IntegrationConfig.init(conf);
        IntegrationHandler.init();
        setupSM();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        if(this.getConfig().getBoolean("enable_admin_channels"))
        c = new ConsoleHandler(this);
        DiscordBridge d = this;
        new BukkitRunnable() {

            @Override
            public void run() {
                if (sm == null || sm.context == null || sm.context.channel() == null || !sm.context.channel().isActive())
                    setupSM();
                if (!ran && getConfig().getBoolean("enable_startup_status")) {
                    sm.write("SEND " + getConfig().getString("startup_status_message"));
                    ran = true;
                }
            }

        }.runTaskTimerAsynchronously(d, 0l, conf.getInt("reconnectTimeout") * 20);
    }

    @Override
    public void onDisable() {
        if (getConfig().getBoolean("enable_shutdown_status"))
            sm.write("SEND " + this.getConfig().getString("shutdown_status_message"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAchievement(PlayerAdvancementDoneEvent e) {
        if (!getConfig().getBoolean("display_achievements")) return;
        String name = e.getPlayer().getDisplayName();
        String achN = e.getAdvancement().toString();
        sm.write("SEND " + name + " just got the achievement " + achN + "!");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled() || !enabled) return;
        String sender = ChatColor.stripColor(e.getPlayer().getDisplayName());
        String message = e.getMessage();
        sm.write("SEND [Chat] " + IntegrationHandler.getPrefix(e.getPlayer()) + sender + IntegrationHandler.getSuffix(e.getPlayer()) + ": " + message);

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        if (!getConfig().getBoolean("display_player_deaths")) return;
        sm.write("SEND " + e.getDeathMessage());

    }

    public boolean handleConf(FileConfiguration conf) {
        HashMap<String, Object> options = new HashMap<String, Object>();
        //options.put("ip", "localhost");
        //options.put("port", "port");
        options.put("name", "server_name");
        options.put("auth", "auth_string");
        options.put("guildID", "1234");
        options.put("enable_startup_status", true);
        options.put("enable_shutdown_status", true);
        options.put("enable_admin_channels", false);
        options.put("startup_status_message", "Server Started");
        options.put("shutdown_status_message", "Server Stopping");
        options.put("reconnectTimeout", 5);
        options.put("display_achievements", true);
        options.put("display_player_deaths", true);
        options.put("integration.commands.balance", true);
        options.put("integration.commands.spawn", true);
        options.put("integration.usechatformatting", true);
        if (!conf.isSet("name")) {
            System.out.print("WARNING: Cofiguration file does not exist! Please configure this so that DiscordBridge may work!");
            //conf.set("ip", "localhost");
            //conf.set("port", "port");
            conf.set("name", "server_name");
            conf.set("auth", "auth_string");
            conf.set("guildID", "1234");
            conf.set("enable_startup_status", true);
            conf.set("enable_shutdown_status", true);
            conf.set("enable_admin_channels", false);
            conf.set("startup_status_message", "Server Started");
            conf.set("shutdown_status_message", "Server Stopping");
            conf.set("reconnectTimeout", 5);
            conf.set("display_achievements", true);
            conf.set("display_player_deaths", true);
            IntegrationConfig.setup(conf);
            this.saveConfig();
            System.out.println("Configuration created, disabling DiscordBridge!");
            enabled = false;
            return false;
        }
        options.forEach(new BiConsumer<String, Object>() {

            @Override
            public void accept(String t, Object u) {
                if (!conf.isSet(t)) {
                    conf.set(t, u);
                }

            }

        });
        this.saveConfig();
        return true;
    }

    public void reload() {
        this.reloadConfig();
        FileConfiguration conf = this.getConfig();
        if (!handleConf(conf)) return;
        IntegrationConfig.init(conf);
        setupSM();
        if(this.getConfig().getBoolean("enable_admin_channels"))
        c = new ConsoleHandler(this);
    }

    public void setupSM() {
        FileConfiguration conf = this.getConfig();
        sm = new SocketManager(this, conf.getString("name"), "wss://api.pint.cloud:443/discordbridge", "443", conf.getString("auth"), conf.getString("guildID"), conf.getInt("reconnectTimeout"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
            if (command.getName().equalsIgnoreCase("disbreload")) {
                reload();
                return true;
            }
            if (command.getName().equalsIgnoreCase("disbreconnect")) {
                setupSM();
                return true;
            }
            if(command.getName().equalsIgnoreCase("disbsetup")){
                if(args.length == 0){
                    sender.sendMessage("Invalid length of args.");
                    return true;
                }
                String code = args[0];
                String[] codes = new String(Base64.getDecoder().decode(code)).split("/");
                String guildID = new String(Base64.getDecoder().decode(codes[0]));
                String name = new String(Base64.getDecoder().decode(codes[1]));
                String auth = new String(Base64.getDecoder().decode(codes[2]));
                FileConfiguration conf = this.getConfig();
                conf.set("guildID", guildID);
                conf.set("name", name);
                conf.set("auth", auth);
                this.saveConfig();
                System.out.println("Server configured. Starting DiscordBridge.");
                enabled = true;
                IntegrationConfig.init(conf);
                IntegrationHandler.init();
                setupSM();
                Bukkit.getServer().getPluginManager().registerEvents(this, this);
                if(this.getConfig().getBoolean("enable_admin_channels"))
                c = new ConsoleHandler(this);
                DiscordBridge d = this;
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        if (sm == null || sm.context == null || sm.context.channel() == null || !sm.context.channel().isActive())
                            setupSM();
                        if (!ran && getConfig().getBoolean("enable_startup_status") && sm.context != null) {
                            sm.write("SEND " + getConfig().getString("startup_status_message"));
                            ran = true;
                        }
                    }

                }.runTaskTimerAsynchronously(d, 0l, conf.getInt("reconnectTimeout") * 20);
            return true;
            }

        if (!enabled) return false;

        if (command.getName().equalsIgnoreCase("disbunset")) {
            sm.write("DESYNC " + ((Player) sender).getUniqueId().toString());
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to run this command!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("setplayer")) {

            if (args.length != 1) return false;
            String UUID = Util.PlayertoUUID((Player) sender);
            sm.write("SYNC " + args[0] + " " + UUID);
            sender.sendMessage("Your player and discord account should be synced. If they are not, an error may have occured (invalid ID, you did not run |setplayer first, you have already set your player, etc.");
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!enabled) return;
        JoinActionHandler hand = JoinActionHandler.fromUUID(e.getPlayer().getUniqueId().toString());
        hand.iterate(e.getPlayer());
        if (!sm.login) return;
        if (IntegrationHandler.isVanished(e.getPlayer())) return;
        String sender = ChatColor.stripColor(e.getPlayer().getDisplayName());
        sm.write("SEND **" + IntegrationHandler.getPrefix(e.getPlayer()) + sender + IntegrationHandler.getSuffix(e.getPlayer()) + " has joined the game**");

    }

}
