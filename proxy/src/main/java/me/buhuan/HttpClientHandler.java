package me.buhuan;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.util.CharsetUtil;

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
		String s = response.content().toString(CharsetUtil.UTF_8);
		System.out.println(s);
		//修改http响应体返回至客户端
		response.headers().add("test","from proxy");
		clientChannel.writeAndFlush(msg);
		System.out.println(1);
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
