package com.PintTheDragon.DiscordBridge;

import com.PintTheDragon.DiscordBridge.API.API;
import com.PintTheDragon.DiscordBridge.API.CommandHandler;
import com.PintTheDragon.DiscordBridge.integration.IntegrationConfig;
import com.PintTheDragon.DiscordBridge.integration.IntegrationHandler;
import com.PintTheDragon.DiscordBridge.JoinActions.ActionSpawn;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("rawtypes")
public class ClientHandler extends SimpleChannelInboundHandler {

	public SocketManager manager;
	private final WebSocketClientHandshaker handshaker;
	private ChannelPromise handshakeFuture;

	public ClientHandler(SocketManager t, WebSocketClientHandshaker h) {
		manager = t;
		API.hand = this;
		handshaker = h;
	}

	public ChannelFuture handshakeFuture() {
		return handshakeFuture;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		handshakeFuture = ctx.newPromise();
	}

	@Override
	public void channelActive(ChannelHandlerContext channelHandlerContext) {
		if (!manager.plugin.enabled) return;
		handshaker.handshake(channelHandlerContext.channel());
	}

	@Override
	protected void channelRead0(ChannelHandlerContext arg0, Object arg1) throws Exception {
		if (!manager.plugin.enabled) return;
		Channel ch = arg0.channel();
		if (!handshaker.isHandshakeComplete()) {
			try {
				handshaker.finishHandshake(ch, (FullHttpResponse) arg1);
				handshakeFuture.setSuccess();
				new java.util.Timer().schedule(
						new java.util.TimerTask() {
							@Override
							public void run() {
								arg0.writeAndFlush(new TextWebSocketFrame("LOGIN " + manager.name + " " + manager.auth + " " + manager.guildID));
								manager.context = arg0;
							}
						},
						1000
				);
			} catch (WebSocketHandshakeException e) {
				handshakeFuture.setFailure(e);
			}
			return;
		}
		if (arg0 instanceof FullHttpResponse) {
			FullHttpResponse response = (FullHttpResponse) arg0;
			throw new IllegalStateException(
					"Unexpected FullHttpResponse (getStatus=" + response.status() +
							", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
		}
		WebSocketFrame frame = (WebSocketFrame) arg1;
		if (frame instanceof TextWebSocketFrame) {
			TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
			String txt = textFrame.text();
			if (txt.equalsIgnoreCase("LCONF")) {
				//Bukkit.getLogger().info("Logged in to bot!");
				manager.login = true;
				return;
			} else {
				if (txt.startsWith("CHAT ") && txt.split(" ").length > 2) {
					String[] arr = txt.split(" ");
					String name = arr[1];
					String msg = "";
					for (int i = 2; i < arr.length; i++) {
						msg += arr[i] + " ";
					}
					manager.plugin.getServer().broadcastMessage("§6[§3Discord§6] §r" + name + ": §r" + msg);
					return;
				}
				if (txt.startsWith("SPAWN ") && txt.split(" ").length == 2) {
					if (!IntegrationConfig.isEnabled("spawn")) {
						manager.write("SEND This command has been disabled.");
						return;
					}
					if (Util.UUIDtoPlayer(txt.split(" ")[1]) == null) {
						JoinActionHandler h = JoinActionHandler.fromUUID(txt.split(" ")[1]);
						if (h == null)
							h = new JoinActionHandler(txt.split(" ")[1]);
						h.addAction(new ActionSpawn());
						h.save();
					} else {
						Util.UUIDtoPlayer(txt.split(" ")[1]).teleport(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
					}
					manager.write("SEND You have been sent to spawn!");
					return;
				}
				if (txt.startsWith("BAL ") && txt.split(" ").length == 2) {
					if (!IntegrationConfig.isEnabled("balance")) {
						manager.write("SEND This command has been disabled.");
						return;
					}
					String uuid = txt.split(" ")[1];
					OfflinePlayer pl = Util.UUIDtoOfflinePlayer(uuid);
					String bal = IntegrationHandler.getBal(pl);
					if (bal != null && bal != "") {
						manager.write("SEND Balance: " + bal);
					} else {
						manager.write("SEND This server does not appear to have a plugin for handling economy.");
					}
					return;
				}
				if (txt.startsWith("ACMD ")) {
					if(!manager.plugin.getConfig().getBoolean("enable_admin_channels")) return;
					String cmd = txt.split(" ", 3)[2];
					String channel = txt.split(" ", 3)[1];
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
					manager.write("SENDA " + channel + " Commad sent!");
					return;
				}
				if (txt.startsWith("MSGPUU ")) {
					String uuid = txt.split(" ", 3)[1];
					String msg = txt.split(" ", 3)[2];
					Util.UUIDtoPlayer(uuid).sendMessage(msg);
				}
				if (txt.startsWith("MSGPU ")) {
					String username = txt.split(" ", 3)[1];
					String msg = txt.split(" ", 3)[2];
					Bukkit.getServer().getPlayer(username).sendMessage(msg);
				}
				if (txt.startsWith("BC ")) {
					String msg = txt.split(" ", 2)[1];
					Bukkit.getServer().broadcastMessage(msg);
				}
				if (txt.startsWith("CCMD ")) {
					try {
						System.out.println(txt);
						String[] args = txt.split(" ", 6);
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
		} else if (frame instanceof CloseWebSocketFrame) {
			ch.close();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
		channelHandlerContext.close();
	}

}
