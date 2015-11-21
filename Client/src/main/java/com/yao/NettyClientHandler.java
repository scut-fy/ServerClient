package com.yao;

import com.yao.module.*;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;


public class NettyClientHandler extends SimpleChannelInboundHandler<BaseMsg> {


    /**
     * 该方法实际是来自ChannelInboundHandlerAdapter ,同时也是ChannelInboundHandler 接口的一部分
     * 该方法的执行方式是：通过ChannelHandlerContext.fireUserEventTriggered()转发到Pipeline注册的下一个ChannelInboundHandler
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case WRITER_IDLE:
                    PingMsg pingMsg = new PingMsg();
                    // 如果发送失败，需要关闭当前连接，然后发起重连，
                    // 所以在这里添加一个监听器
                    //  ctx.writeAndFlush(pingMsg);
                    ctx.writeAndFlush(pingMsg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                    System.out.println("send ping to server----------");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BaseMsg baseMsg) throws Exception {
        {
            MsgType msgType = baseMsg.getType();
            switch (msgType) {
                case LOGIN: {
                    //向服务器发起登录
                    LoginMsg loginMsg = new LoginMsg();
                    loginMsg.setPassword("yao");
                    loginMsg.setUserName("robin");
                    channelHandlerContext.writeAndFlush(loginMsg);
                }
                break;
                case PING: {
                    System.out.println("receive ping from server----------");
                }
                break;
                case ASK: {
                    ReplyClientBody replyClientBody = new ReplyClientBody("client info **** !!!");
                    ReplyMsg replyMsg = new ReplyMsg();
                    replyMsg.setBody(replyClientBody);
                    channelHandlerContext.writeAndFlush(replyMsg);
                }
                break;
                case REPLY: {
                    ReplyMsg replyMsg = (ReplyMsg) baseMsg;
                    ReplyServerBody replyServerBody = (ReplyServerBody) replyMsg.getBody();
                    System.out.println("receive client msg: " + replyServerBody.getServerInfo());
                }
                default:
                    break;
            }
            ReferenceCountUtil.release(msgType);
        }
    }
}
