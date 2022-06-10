package com.nettydemo;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;

public class Main {
    public static void main(String[] args) {
        EventLoop loop = new NioEventLoopGroup().next();
        loop.execute(()->{
            System.out.println("nmsl");
        });
    }
}
