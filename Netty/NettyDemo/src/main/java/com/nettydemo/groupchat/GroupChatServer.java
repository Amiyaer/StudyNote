package com.nettydemo.groupchat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class GroupChatServer {
    private final int port;

    public GroupChatServer(int port){
        this.port = port;
    }

    //编写run方法，处理客户端请求
    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //获取pipeline
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //decoder：解码器
                            pipeline.addLast("decoder",new StringDecoder());
                            //encoder:编码器
                            pipeline.addLast("encoder",new StringEncoder());
                            //加入自己的业务处理器
                            pipeline.addLast(new GroupChatServerHandler());
                        }
                    });

            System.out.println("chat server init successfully!");

            ChannelFuture channelFuture = b.bind(port).sync();

            //监听关闭
            channelFuture.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    public static void main(String[] args) throws Exception {
        new GroupChatServer(7000).run();
    }
}
