package me.buhuan;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * @author hbh
 * @date 2020/5/4
 */
public class HttpClientHandler extends ChannelInboundHandlerAdapter {
	private Channel clientChannel;

	public HttpClientHandler(Channel clientChannel) {
		this.clientChannel = clientChannel;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		FullHttpResponse response = (FullHttpResponse) msg;
		//修改http响应体返回至客户端
		response.headers().add("test","from proxy");
		clientChannel.writeAndFlush(msg);
	}
}
