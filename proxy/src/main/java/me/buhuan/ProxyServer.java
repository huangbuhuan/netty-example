package me.buhuan;

import com.sun.net.httpserver.HttpServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.net.InetSocketAddress;

/**
 * @author hbh
 * @date 2020/4/30
 */
public class ProxyServer {

	private final int port;

	public ProxyServer(int port) {
		this.port = port;
	}

	public static void main(String[] args) throws InterruptedException {
		new ProxyServer(8000).start();
	}

	public void start() throws InterruptedException {
		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup work = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(boss, work)
				.channel(NioServerSocketChannel.class)
				.localAddress(new InetSocketAddress(port))
				.childHandler(new ChannelInitializer<Channel>() {
					@Override
					protected void initChannel(Channel ch)  {
						ChannelPipeline pipeline = ch.pipeline();
						// 添加http解码器
						pipeline.addLast("httpCodec", new HttpServerCodec());
						// 添加消息聚合器e
						pipeline.addLast("httpObject", new HttpObjectAggregator(64 * 1024));
						// 添加自定义处理器
						pipeline.addLast(new ProxyServerHandler());
					}
				});
			ChannelFuture f = b.bind().sync();
			f.channel().closeFuture().sync();
		} finally {
			boss.shutdownGracefully().sync();
			work.shutdownGracefully().sync();
		}
	}
}
