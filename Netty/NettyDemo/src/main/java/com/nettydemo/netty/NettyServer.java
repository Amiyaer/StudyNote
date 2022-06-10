package com.nettydemo.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.channels.Selector;

public class NettyServer {
    public static void main(String[] args) throws Exception {
        //创建两个Group
        //两个都是无限循环
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            //创建服务器端的启动对象，配置参数
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup)//两个线程组
                    .channel(NioServerSocketChannel.class)//使用的服务器通道
                    .option(ChannelOption.SO_BACKLOG,128)//设置线程队列得到连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE,true)//设置保持活动连接状态(长连接？)
                    .childHandler(new ChannelInitializer<SocketChannel>() {//创建一个通道测试对象(匿名对象)
                        //给pipeline设置处理器
                        @Override
                        protected void initChannel(SocketChannel ch)throws Exception {
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    });//给我们worker的EventLoop对应的管道设置处理器
            System.out.println("Server is ready...");
            ChannelFuture cf = bootstrap.bind(6668).sync();

            //给cf注册监听器，监控我们关心的事件
            cf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(cf.isSuccess()){
                        System.out.println("bind port 6668 successfully");
                    }else {
                        System.out.println("failed to bind port 6668");
                    }
                }
            });


            //对关闭通道进行监听
            cf.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
        }
    }
}
