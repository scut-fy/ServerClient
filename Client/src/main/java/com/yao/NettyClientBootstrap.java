package com.yao;

import com.yao.module.AskMsg;
import com.yao.module.AskParams;
import com.yao.module.Constants;
import com.yao.module.LoginMsg;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by yaozb on 15-4-11.
 */
public class NettyClientBootstrap {

    private final static Log LOG = LogFactory.getLog(NettyClientBootstrap.class);

    private int port;
    private String host;
    private SocketChannel socketChannel;
    private static final EventExecutorGroup group = new DefaultEventExecutorGroup(20);

    public NettyClientBootstrap(int port, String host) throws InterruptedException {
        this.port = port;
        this.host = host;
        start();
    }

    private void start() throws InterruptedException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.group(eventLoopGroup);
        bootstrap.remoteAddress(host, port);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                // 这里定义空闲状态处理，构造函数中三个参数的意思是：
                // int readerIdleTimeSeconds,int writerIdleTimeSeconds,int allIdleTimeSeconds)
                // 分别表示多久没有读了，就触发一个空闲事件；多久没有写了，就触发一个空闲事件；
                // 多久没有写或者读了，就触发一个事件。 如果设置为0  表示失效。
                socketChannel.pipeline().addLast(new IdleStateHandler(0, 5, 0));
                socketChannel.pipeline().addLast(new ObjectEncoder());
                socketChannel.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                socketChannel.pipeline().addLast(new NettyClientHandler());
            }
        });
        ChannelFuture future = bootstrap.connect(host, port).sync();
        if (future.isSuccess()) {
            socketChannel = (SocketChannel) future.channel();
            LOG.info("connect server  成功---------");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Constants.setClientId("001");
        NettyClientBootstrap bootstrap = new NettyClientBootstrap(9999, "localhost");

        LoginMsg loginMsg = new LoginMsg();
        loginMsg.setPassword("yao");
        loginMsg.setUserName("robin");
        bootstrap.socketChannel.writeAndFlush(loginMsg);
        LOG.info("发送认证");
        while (true) {
            TimeUnit.SECONDS.sleep(6);
            AskMsg askMsg = new AskMsg();
            AskParams askParams = new AskParams();
            askParams.setAuth("authToken");
            askMsg.setParams(askParams);
            bootstrap.socketChannel.writeAndFlush(askMsg);
            LOG.debug("发送请求");
        }
    }
}
