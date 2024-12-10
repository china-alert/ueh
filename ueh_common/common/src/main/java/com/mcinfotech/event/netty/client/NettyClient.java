package com.mcinfotech.event.netty.client;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcinfotech.event.domain.EventSourceType;
import com.mcinfotech.event.domain.ProbeEventMessage;
import com.mcinfotech.event.domain.ProbeInfo;
import com.mcinfotech.event.domain.ProbeType;
import com.mcinfotech.event.domain.ProjectInfo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * netty连接器
 * 1.开启与dispatcher端的心跳检测
 *

 */
public class NettyClient {
	private static Logger logger=LogManager.getLogger(NettyClient.class);
	private static final NettyClient nettyClient = new NettyClient();

	private Bootstrap bootstrap;
	private EventLoopGroup mainGroup;
	//private  static Channel channel = null;
	public static ConcurrentMap<String,Channel> channels = new ConcurrentHashMap<>();
	
	public static NettyClient getInstance() {
		return nettyClient;
	}
	private NettyClient() {
		if (bootstrap == null) {
			mainGroup=new NioEventLoopGroup(2);
			bootstrap = initBootstrap();
		}
	}
	
	public  ConcurrentMap<String,Channel> getChannels() {
		return channels;
	}
	private Bootstrap initBootstrap() {
		// 少线程
		//EventLoopGroup mainGroup = new NioEventLoopGroup(2);

		bootstrap = new Bootstrap();
		bootstrap.group(mainGroup)
		.handler(new LoggingHandler(LogLevel.DEBUG))
		.channel(NioSocketChannel.class)
				//.option(ChannelOption.SO_KEEPALIVE, true)
				//.option(ChannelOption.TCP_NODELAY, true)
		.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) {
						//ByteBuf delimiter = Unpooled.copiedBuffer(Constant.DELIMITER.getBytes());
				ch.pipeline()
						//.addLast(new DelimiterBasedFrameDecoder(Constant.MAX_LENGTH, delimiter))
						//.addLast(new StringDecoder())
				.addLast(new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.cacheDisabled(this.getClass().getClassLoader())))
				.addLast(new ObjectEncoder())
						//10秒没消息时，就发心跳包过去
						//.addLast(new IdleStateHandler(10, 0, 0,TimeUnit.MINUTES))
						//.addLast(new ReConnectionHandler())
				.addLast(new NettyClientHandler());
				}
		});
		return bootstrap;
	}

	public synchronized void reconnect(String channelName,String dispatcherHost,int port) {
		try {
			//ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
			//改用同步连接，不然无法启动监听
			ChannelFuture channelFuture = bootstrap.connect(dispatcherHost,port).sync();
			/*channelFuture.addListener(new ChannelFutureListener(){
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						Channel channel = channelFuture.channel();
						if(channel.isActive()){
							channels.put(channelName, channel);	
							logger.error("connect successfully !");
						}else{
							logger.error("connect not successfully !");
						}
					} else {
						logger.error("connect not successfully !");
						future.cause().printStackTrace();
						//mainGroup.shutdownGracefully(); // 关闭线程组
					}
				}
			});*/
			Channel channel=channelFuture.channel();
			if(channel.isActive()){
				channels.put(channelName, channel);	
				if(logger.isDebugEnabled()){
					logger.debug("connect successfully !");
				}				
			}else{
				logger.error("connect not successfully !");
			}
		} catch (Exception e) {
			channels.remove(channelName);
			e.printStackTrace();
		}
	}

	public synchronized void disconnect(String channelName){
		Channel channel=channels.get(channelName);
		if(channel!=null&&channel.isActive()){
			try {
				channel.closeFuture().sync();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			channel=null;
			channels.remove(channelName);
		}
	}
	public synchronized void disconnect(){
		for(String channelName:channels.keySet()){
			Channel channel=channels.get(channelName);
			if(channel!=null&&channel.isActive()){
				channel.close();
				channel=null;
				channels.remove(channelName);
			}
		}
		if(mainGroup!=null){
			mainGroup.shutdownGracefully();
		}
		this.bootstrap=null;
	}
	public synchronized void writeAndFlushMessage(String channelName,String dispatcherHost,int port,Object message){
		Channel channel=channels.get(channelName);
		if(channel==null){
			reconnect(channelName,dispatcherHost,port);
			channel=channels.get(channelName);
		}else{
			if(!channel.isActive()){
				channel.disconnect();
				reconnect(channelName,dispatcherHost,port);
				channel=channels.get(channelName);
			}
		}
		if(channel!=null&&channel.isActive()){
			channel.writeAndFlush(message);
		}else{
			logger.error("write and flush message not successfully because channel's state is disconnected , target host is "+dispatcherHost+" and port is "+port);
		}
	}
}
