package com.mcinfotech.event.probe.server;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcinfotech.event.domain.Constant;
import com.mcinfotech.event.filter.IFilter;
import com.mcinfotech.event.utils.CpuNum;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 用于接收从监控工具报送的事件信息
 *

 */
public class ReceiverServer {
	private Logger logger=LogManager.getLogger(ReceiverServer.class);
	
	private List<IFilter<String>> messageFilters;
	
	public void startNettyServer(int port) throws Exception {
		// boss单线程
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup(CpuNum.workerCount());
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.handler(new LoggingHandler(LogLevel.INFO))
			.option(ChannelOption.SO_BACKLOG, 1024)
					// 保持长连接
			.childOption(ChannelOption.SO_KEEPALIVE, true)
					// 出来网络io事件，如记录日志、对消息编解码等
			.childHandler(new ReceiverServerInitializer());
			// 绑定端口，同步等待成功
			ChannelFuture future = bootstrap.bind(port).sync();
			/*Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				bossGroup.shutdownGracefully(1000, 3000, TimeUnit.MILLISECONDS);
				workerGroup.shutdownGracefully(1000, 3000, TimeUnit.MILLISECONDS);
			}));*/
			// 等待服务器监听端口关闭
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("receiver server stop !");
		} finally {
			// 优雅退出，释放线程池资源
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	/**
	 * Server Handler注册器
	 */
	private class ReceiverServerInitializer extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel ch) {
			ReceiverServerHandler serverHandler = new ReceiverServerHandler();
			serverHandler.addMessageFilters(messageFilters);
			ByteBuf delimiter = Unpooled.copiedBuffer(Constant.DELIMITER.getBytes());
			ch.pipeline()
			.addLast(new DelimiterBasedFrameDecoder(Constant.MAX_LENGTH, delimiter))
			//.addLast(new DelimiterBasedFrameDecoder(8192,Delimiters.lineDelimiter()))
			.addLast(new StringDecoder(Charset.forName("UTF8")))
			.addLast(new StringEncoder(Charset.forName("UTF8")))
			.addLast(serverHandler);
		}
	}

	public void setMessageFilters(List<IFilter<String>> messageFilters) {
		this.messageFilters = messageFilters;
	}
}
