package com.PintTheDragon.DiscordBridge;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.CharsetUtil;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;

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
					String host = new URI(ip).getHost();
			ClientHandler handler = new ClientHandler(m, WebSocketClientHandshakerFactory.newHandshaker(
					new URI(ip), WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));
			SslContext ssl = SslContextBuilder.forClient()
					.build();
			Bootstrap clientBootstrap = new Bootstrap();

			clientBootstrap.group(group);
			clientBootstrap.channel(NioSocketChannel.class);
			clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
				protected void initChannel(SocketChannel socketChannel) throws Exception {
					ChannelPipeline p = socketChannel.pipeline();
						p.addLast("ssl-handler", ssl.newHandler(socketChannel.alloc(), host, Integer.parseInt(port)));
					p.addLast(
							new HttpClientCodec(),
							new HttpObjectAggregator(8192),
							WebSocketClientCompressionHandler.INSTANCE,
							handler);
				}
			});
			try {
				clientBootstrap.connect(host, Integer.parseInt(port)).sync().channel();
				handler.handshakeFuture().sync();
			} catch (Exception e) {
				System.out.print(e);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void write(String in) {
		context.writeAndFlush(new TextWebSocketFrame(in));
	}

}