package com.PintTheDragon.DiscordBridge;

import com.PintTheDragon.DiscordBridge.API.API;
import com.PintTheDragon.DiscordBridge.API.CommandHandler;
import com.PintTheDragon.DiscordBridge.integration.IntegrationConfig;
import com.PintTheDragon.DiscordBridge.integration.IntegrationHandler;
import com.PintTheDragon.DiscordBridge.JoinActions.ActionSpawn;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("rawtypes")
public class ClientHandler extends SimpleChannelInboundHandler {

	public SocketManager manager;

	public ClientHandler(SocketManager t) {
		manager = t;
		API.hand = this;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, Object arg1) throws Exception {
		if (!manager.plugin.enabled) return;
		if (((ByteBuf) arg1).toString(CharsetUtil.UTF_8).equalsIgnoreCase("LCONF")) {
			Bukkit.getLogger().info("Logged in to bot!");
			manager.login = true;
			return;
		} else {
			if (((ByteBuf) arg1).toString(CharsetUtil.UTF_8).startsWith("CHAT ") && ((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ").length > 2) {
				String[] arr = ((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ");
				String name = arr[1];
				String msg = "";
				for (int i = 2; i < arr.length; i++) {
					msg += arr[i] + " ";
				}
				manager.plugin.getServer().broadcastMessage("§6[§3Discord§6] §r" + name + ": §r" + msg);
				return;
			}
			if (((ByteBuf) arg1).toString(CharsetUtil.UTF_8).startsWith("SPAWN ") && ((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ").length == 2) {
				if (!IntegrationConfig.isEnabled("spawn")) {
					manager.write("SEND This command has been disabled.");
					return;
				}
				if (Util.UUIDtoPlayer(((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ")[1]) == null) {
					JoinActionHandler h = JoinActionHandler.fromUUID(((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ")[1]);
					if (h == null)
						h = new JoinActionHandler(((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ")[1]);
					h.addAction(new ActionSpawn());
					h.save();
				} else {
					Util.UUIDtoPlayer(((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ")[1]).teleport(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
				}
				manager.write("SEND You have been sent to spawn!");
				return;
			}
			if (((ByteBuf) arg1).toString(CharsetUtil.UTF_8).startsWith("BAL ") && ((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ").length == 2) {
				if (!IntegrationConfig.isEnabled("balance")) {
					manager.write("SEND This command has been disabled.");
					return;
				}
				String uuid = ((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ")[1];
				OfflinePlayer pl = Util.UUIDtoOfflinePlayer(uuid);
				String bal = IntegrationHandler.getBal(pl);
				if (bal != null && bal != "") {
					manager.write("SEND Balance: " + bal);
				} else {
					manager.write("SEND This server does not appear to have a plugin for handling economy.");
				}
				return;
			}
			if (((ByteBuf) arg1).toString(CharsetUtil.UTF_8).startsWith("ACMD ")) {
				String cmd = ((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ", 3)[2];
				String channel = ((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ", 3)[1];
				Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
				manager.write("SENDA " + channel + " Commad sent!");
				return;
			}
			if (((ByteBuf) arg1).toString(CharsetUtil.UTF_8).startsWith("MSGPUU ")) {
				String uuid = ((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ", 3)[1];
				String msg = ((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ", 3)[2];
				Util.UUIDtoPlayer(uuid).sendMessage(msg);
			}
			if (((ByteBuf) arg1).toString(CharsetUtil.UTF_8).startsWith("MSGPU ")) {
				String username = ((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ", 3)[1];
				String msg = ((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ", 3)[2];
				Bukkit.getServer().getPlayer(username).sendMessage(msg);
			}
			if (((ByteBuf) arg1).toString(CharsetUtil.UTF_8).startsWith("BC ")) {
				String msg = ((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ", 2)[1];
				Bukkit.getServer().broadcastMessage(msg);
			}
			if (((ByteBuf) arg1).toString(CharsetUtil.UTF_8).startsWith("CCMD ")) {
				try {
					System.out.println(((ByteBuf) arg1).toString(CharsetUtil.UTF_8));
					String[] args = ((ByteBuf) arg1).toString(CharsetUtil.UTF_8).split(" ", 6);
					String[] cmdArgs = args[5].split(" ");
					System.out.println(Arrays.deepToString(cmdArgs));
					String name = cmdArgs[0];
					CommandHandler handlerCMD = API.getHandler(name);
					ArrayList<String> cmdArgsAL = new ArrayList<String>(Arrays.asList(cmdArgs));
					cmdArgsAL.remove(0);
					cmdArgs = cmdArgsAL.toArray(new String[0]);
					System.out.println(Arrays.deepToString(cmdArgs));
					String channelID = args[1];
					String authorID = args[2];
					boolean isAdmin = Boolean.parseBoolean(args[3]);
					String uuid = args[4];
					if (uuid.equalsIgnoreCase("null")) uuid = null;
					handlerCMD.runCommand(channelID, authorID, isAdmin, uuid, cmdArgs);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext channelHandlerContext) {
		if (!manager.plugin.enabled) return;
		channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer("LOGIN " + manager.name + " " + manager.auth + " " + manager.guildID, CharsetUtil.UTF_8));
        manager.context = channelHandlerContext;
    }

	@Override
	public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
		channelHandlerContext.close();
	}

}
