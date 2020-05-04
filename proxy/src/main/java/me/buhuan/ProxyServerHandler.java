package me.buhuan;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

/**
 * @author hbh
 * @date 2020/5/3
 */
public class ProxyServerHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof HttpRequest) {
			HttpRequest httpRequest = (HttpRequest) msg;
			HttpHeaders headers = httpRequest.headers();
			if (headers.get(HttpHeaderNames.HOST) == null) {
				return;
			}
			String[] ipAndPort = headers.get(HttpHeaderNames.HOST).split(":");
			int port = 80;
			if (ipAndPort.length > 1) {
				port = Integer.parseInt(ipAndPort[1]);
			} else {
				if (httpRequest.uri().contains("https" )) {
					port = 443;
				}
			}
			String host = ipAndPort[0];

			if (HttpMethod.CONNECT.name().equals(httpRequest.method().name())) {
				HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
				ctx.writeAndFlush(response);
				ctx.pipeline().remove("httpCodec");
				ctx.pipeline().remove("httpObject");
				return;
			}

			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(ctx.channel().eventLoop())
				.channel(ctx.channel().getClass())
				.handler(new ChannelInitializer<Channel>() {
					@Override
					protected void initChannel(Channel ch) {
						ChannelPipeline pipeline = ch.pipeline();
						pipeline.addLast(new HttpClientCodec());
						pipeline.addLast(new HttpObjectAggregator(64 * 1024));
						pipeline.addLast(new HttpClientHandler(ctx.channel()));
					}
				});
			ChannelFuture connect = bootstrap.connect(host, port);
			connect.addListener((ChannelFutureListener) future -> {
				if (future.isSuccess()) {
					future.channel().writeAndFlush(msg);
				} else {
					ctx.channel().close();
				}
			});
		} else {
			System.out.println(1);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
