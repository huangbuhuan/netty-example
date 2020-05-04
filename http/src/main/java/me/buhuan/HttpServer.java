package me.buhuan;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.security.cert.CertificateException;

/**
 * @author hbh
 * @date 2020/4/29
 */
public class HttpServer {

	private final int port;

	public HttpServer(int port) {
		this.port = port;
	}

	public static void main(String[] args) throws InterruptedException {
		new HttpServer(8081).start();
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
						pipeline.addLast(new HttpsServerHandler());
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
