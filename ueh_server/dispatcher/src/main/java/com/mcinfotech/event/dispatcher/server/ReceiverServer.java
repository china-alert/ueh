package com.mcinfotech.event.dispatcher.server;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.filter.IFilter;
import com.mcinfotech.event.utils.CpuNum;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 接收从Probe送来的消息，并处理
 *

 */
public class ReceiverServer {
	private Logger logger=LogManager.getLogger(ReceiverServer.class);
	
	private List<IFilter<ProbeEventMessage>> messageFilters;
	//private ProbeInfo probe;
	
	public void startNettyServer(int port) throws Exception {
		// boss单线程
		EventLoopGroup bossGroup = new NioEventLoopGroup(2);
		EventLoopGroup workerGroup = new NioEventLoopGroup(CpuNum.workerCount());
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.handler(new LoggingHandler(LogLevel.DEBUG))
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
			logger.error("application start failed , and then it will be stop !",e);
			System.exit(0);
		} finally {
			// 优雅退出，释放线程池资源
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	/**
	 * Server Handler注册器
	 */
	private class ReceiverServerInitializer extends ChannelInitializer<Channel> {

		@Override
		protected void initChannel(Channel ch) {
			ReceiverServerHandler serverHandler = new ReceiverServerHandler();
			serverHandler.addMessageFilters(messageFilters);
			//serverHandler.setProbe(probe);
			//ByteBuf delimiter = Unpooled.copiedBuffer(Constant.DELIMITER.getBytes());
			ch.pipeline()
			//.addLast(new IdleStateHandler(10,0,0,TimeUnit.SECONDS))
			//.addLast(new DelimiterBasedFrameDecoder(Constant.MAX_LENGTH, delimiter))
			//.addLast(new DelimiterBasedFrameDecoder(8192,Delimiters.lineDelimiter()))
			//.addLast(new StringDecoder())
			//.addLast(new StringEncoder())
			.addLast(new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.cacheDisabled(this.getClass().getClassLoader())))
			.addLast(new ObjectEncoder())
			.addLast(serverHandler);
		}
	}

	public void setMessageFilters(List<IFilter<ProbeEventMessage>> messageFilters) {
		this.messageFilters = messageFilters;
	}

	/*public void setProbe(ProbeInfo probe) {
		this.probe = probe;
	}*/
}
