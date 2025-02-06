package com.mcinfotech.event.transmit.push;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NettyUdpClient {
    private String message;

    public NettyUdpClient(String message) {
        this.message = message;
    }

    public void connect(String host, int port) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) throws Exception {
                            ch.pipeline().addLast(new UdpClientHandler());
                        }
                    });

            // 连接到服务器
            ChannelFuture f = b.connect(host, port).sync();

            // 等待关闭
            f.channel().closeFuture().sync();
        }catch (InterruptedException e){
            e.printStackTrace();
        }finally {
            // 关闭线程池
            group.shutdownGracefully();
        }
    }

    public class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(Unpooled.copiedBuffer(message.getBytes(Charset.forName("GBK"))));
            closeConnection(ctx);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            ByteBuf buf = msg.content();
            byte[] received = new byte[buf.readableBytes()];
            buf.getBytes(0, received);
            System.out.println("Received from server: " + new String(received, Charset.forName("GBK")));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
        public void closeConnection(ChannelHandlerContext ctx) {
            Channel channel = ctx.channel();
            // 关闭连接，但不立即释放资源，而是等待所有未完成的写操作完成
            channel.close().addListener((ChannelFuture future) -> {
                if (future.isSuccess()) {
                    System.out.println("Connection closed successfully.");
                } else {
                    System.err.println("Failed to close the connection: " + future.cause());
                }
            });
        }
    }
}
