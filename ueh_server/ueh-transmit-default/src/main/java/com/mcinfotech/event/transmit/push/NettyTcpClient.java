package com.mcinfotech.event.transmit.push;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NettyTcpClient {

    private String message;

    private static Logger logger = LogManager.getLogger(NettyTcpClient.class);

    public NettyTcpClient(String message) {
        this.message = message;
    }

    public void connect(String host, int port) {
        // 创建用于处理网络事件的线程组
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 创建客户端启动辅助类
            Bootstrap b = new Bootstrap();
            // 设置线程组
            b.group(workerGroup);
            // 设置通道类型为NioSocketChannel
            b.channel(NioSocketChannel.class);
            // 设置TCP保持连接选项
            b.option(ChannelOption.SO_KEEPALIVE, true);

            // 添加初始化器，用于设置ChannelPipeline中的ChannelHandler
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    // 在这里添加自定义的ChannelHandler来处理各种事件
                    ch.pipeline().addLast(new MyClientHandler());
                }
            });

            // 连接到服务器，并同步等待连接成功
            ChannelFuture f = b.connect(host, port).sync();

            // 等待客户端链路关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.info(e);
            e.printStackTrace();
        } finally {
            // 关闭工作线程组
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 自定义处理器
     */
    public class MyClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            // 连接建立后，可以发送数据给服务器
            logger.info("开始发送消息！");
            try {
                byte[] bytes = message.getBytes(Charset.forName("GBK"));
                logger.info(new String(bytes));
                ByteBuf byteBuf = Unpooled.copiedBuffer(bytes);
                logger.info(byteBuf);
                ctx.writeAndFlush(byteBuf);
            } catch (Exception e) {
                logger.info(e);
                e.printStackTrace();
            }
            logger.info("结束发送消息！");
            closeConnection(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf in = (ByteBuf) msg;
            // ...处理从服务器接收到的数据...
            logger.info("Received data: " + in.toString(Charset.forName("GBK")));
            closeConnection(ctx);
        }
        public void closeConnection(ChannelHandlerContext ctx) {
            Channel channel = ctx.channel();
            // 关闭连接，但不立即释放资源，而是等待所有未完成的写操作完成
            channel.close().addListener((ChannelFuture future) -> {
                if (future.isSuccess()) {
                    logger.info("Connection closed successfully.");
                } else {
                    logger.info("Failed to close the connection: " + future.cause());
                }
            });
        }
        // ...其他事件处理方法...
    }

    public static void main(String[] args) {
        NettyTcpClient client = new NettyTcpClient("0632zabbixImv6Fb1111111a1%-11s%-600s 123456 哈哈哈!!!!!!!!!!!!!!!!!!!!!!!1真的棒");
        // 假设服务器地址是localhost，端口是8080
        client.connect("localhost", 60001);
    }
}
