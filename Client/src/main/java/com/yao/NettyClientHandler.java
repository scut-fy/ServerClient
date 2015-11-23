package com.yao;

import com.yao.module.*;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;


public class NettyClientHandler extends SimpleChannelInboundHandler<BaseMsg> {

    private static final Log LOG = LogFactory.getLog(NettyClientHandler.class);
    // 心跳发送次数
    private int sendpingTimes = 0;

    private Lock pingLock ;

    public NettyClientHandler(){
        pingLock = new ReentrantLock();
    }

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
                    LOG.info("send ping to server----------");
                    //  将发送次数加1
                    pingPP();
                    if(sendpingTimes>3){
                        // 如果发送心跳的次数已经大于三了，关闭连接，开启重连
                        LOG.error("心跳三次失败，准备重连");
                        ctx.channel().close();
                    }
                    break;
                case READER_IDLE:
                    LOG.error("长时间没有收到消息");
                    if(sendpingTimes>3){
                        // 如果发送心跳的次数已经大于三了，关闭连接，开启重连
                        LOG.error("长时间没有收到心跳回复，连接失败，重连");
                        ctx.channel().close();
                    }
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
                case PONG: {
                    LOG.info("receive Pong from server----------");
                    // 将发送次数置零
                    pingTZ();
                }
                break;
                case PING:{
                    LOG.error("收到来自server的 ping  ");
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
                    LOG.info("receive client msg: " + replyServerBody.getServerInfo());
                }
                default:
                    break;
            }
            ReferenceCountUtil.release(msgType);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("连接异常 ---------");
        ctx.fireExceptionCaught(cause);
    }

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    class AskReqRun implements Runnable{

        private ChannelHandlerContext ctx;

        AskReqRun(ChannelHandlerContext ctx){
            this.ctx = ctx;
        }
        public void run() {

            AskMsg askMsg = new AskMsg();
            AskParams askParams = new AskParams();
            askParams.setAuth("authToken");
            askMsg.setParams(askParams);
//            bootstrap.socketChannel.writeAndFlush(askMsg);
            ctx.channel().writeAndFlush(askMsg);
            LOG.debug("发送请求");
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("调用  channelActive --------------");

        /**
         * 创建并执行一个在给定初始延迟后首次启用的定期操作，后续操作具有给定的周期；
         * 也就是将在 initialDelay 后开始执行，
         * 然后在 initialDelay+period 后执行，接着在 initialDelay + 2 * period 后执行，依此类推。
         */

    }

    /**
     * 心跳次数+1
     */
    private void pingPP(){
        pingLock.lock();
        try{
            sendpingTimes++;
        }finally {
            pingLock.unlock();
        }
    }
    private void pingTZ(){
        pingLock.lock();
        try{
            sendpingTimes=0;
        }finally {
            pingLock.unlock();
        }
    }
}
