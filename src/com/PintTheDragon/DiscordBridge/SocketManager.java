package com.PintTheDragon.DiscordBridge;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

public class SocketManager {
	public String name, ip, port, auth, guildID;
	public boolean login = false;
	public ChannelHandlerContext context;
	public int timeout;
	DiscordBridge plugin;

	public SocketManager(DiscordBridge pl, String t_name, String t_ip, String t_port, String t_auth, String t_guildID,  int t_timeout) {
		plugin = pl;
		name = t_name;
		ip = t_ip;
		port = t_port;
		auth = t_auth;
		guildID = t_guildID;
		timeout = t_timeout;
		setupServer();
	}

	public void setupServer() {
		SocketManager m = this;
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap clientBootstrap = new Bootstrap();

			clientBootstrap.group(group);
			clientBootstrap.channel(NioSocketChannel.class);
			clientBootstrap.remoteAddress(new InetSocketAddress(ip, Integer.parseInt(port)));
			clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
				protected void initChannel(SocketChannel socketChannel) throws Exception {
					socketChannel.pipeline().addLast(new ClientHandler(m));
				}
			});
			try {
				clientBootstrap.connect().sync();
			} catch (Exception e) {

			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void write(String in) {
		context.writeAndFlush(Unpooled.copiedBuffer(in, CharsetUtil.UTF_8));
	}

}