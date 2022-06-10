package com.nettydemo.groupchat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;

public class GroupChatServerHandler extends SimpleChannelInboundHandler<String> {

    //定义一个channel组管理所有的channel
    //所有线程组共享(static)
    //GlobalEventExecutor.INSTANCE是一个全局的事件执行器，是一个单例
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //表示连接建立，一旦连接，第一个执行
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        //将该客户加入聊天的信息推送给其他在线的客户端
        //该方法会将group中所有的channel遍历并发送信息
        channelGroup.writeAndFlush("[Client]"+channel.remoteAddress()+" joined chat\n");
        channelGroup.add(channel);
    }

    //表示断开连接，通知所有的其他客户端
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channelGroup.writeAndFlush("[Client]"+channel.remoteAddress()+" left chat\n");
        //channelGroup会自动把channel移除
    }

    //表示channel处于活动状态，提示服务器某某某上线
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+" is connected~");
    }

    //表示channel处于非活动状态，提示服务器某某某下线
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress()+" is out connect.");
    }

    //读取数据
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        //获取当前channel
        Channel channel = ctx.channel();

        //遍历channelGroup，根据不同的channel(是不是自己)发送不同的信息
        channelGroup.forEach(ch -> {
            if(ch != channel){
                ch.writeAndFlush("[User]"+channel.remoteAddress()+" send a message:"+msg+"\n");
            }else{
                ch.writeAndFlush("you send a message:"+msg+"\n");
            }
        });
    }

    //捕获异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
