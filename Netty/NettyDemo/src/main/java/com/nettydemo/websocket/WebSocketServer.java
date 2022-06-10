package com.nettydemo.websocket;

import com.nettydemo.heartbeat.HeartBeatServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class WebSocketServer {
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();

                            //基于http协议，所以使用http的编解码器
                            pipeline.addLast(new HttpServerCodec());
                            //是以块方式写，添加ChunkWrite处理器
                            pipeline.addLast(new ChunkedWriteHandler());

                            //将多个分段的Http数据聚合
                            pipeline.addLast(new HttpObjectAggregator(8192));

                            //1.websocket的数据是以帧的形式传递
                            //2.识别请求的资源
                            //3.WebSocketServerProtocolHandler将http协议升级为ws协议保持长连接,webSocket先通过
                            // http1.1建立连接后返回状态码101再将http协议换成ws
                            pipeline.addLast(new WebSocketServerProtocolHandler("/hello"));

                            //自定义的handler，处理业务逻辑
                            pipeline.addLast(new WebSocketFrameHandler());

                        }
                    });

            ChannelFuture channelFuture = bootstrap.bind(7000).sync();
            channelFuture.channel().closeFuture().sync();

        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
