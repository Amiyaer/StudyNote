package com.nettydemo.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

//自定义一个handler，需要继承netty规定好的某个HandlerAdapter
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    //重写一些方法

    //读取客户端发送的消息
    //ChannelHandlerContext上下文对象，包含pipeline，channel，地址等
    //msg：客户端发送的数据，默认Object
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //这里有一个非常耗时的业务，我们可以把它提交到当前channel对应的taskQueue中异步执行
        //定时任务为ctx.channel().eventLoop().schedule
        ctx.channel().eventLoop().execute(()->{
            try {
                Thread.sleep(1000*5);
                ctx.writeAndFlush(Unpooled.copiedBuffer("阻塞5秒后的消息",CharsetUtil.UTF_8));
            }catch (Exception e){
                e.printStackTrace();
            }
        });

        System.out.println("server ctx = "+ctx);
        //将msg转成bytebuffer处理,Netty提供的ByteBuf性能更高
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("From client "+ctx.channel().remoteAddress()+":"+buf.toString(CharsetUtil.UTF_8));
    }

    //数据读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //write+flush
        //将数据写入到缓存并刷新
        //对发送的数据进行编码
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello,client~",CharsetUtil.UTF_8));
    }

    //处理异常，一般是关闭通道
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
