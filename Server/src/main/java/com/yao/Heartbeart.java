package com.yao;

import com.yao.module.PongMsg;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by bryan on 2015/11/21.
 */
public class Heartbeart extends SimpleChannelInboundHandler<PongMsg> {

    private final static Log LOG = LogFactory.getLog(Heartbeart.class);


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                LOG.error("�������˼�⵽ ��ʱ��û���յ�����������Ϣ");
//                if (unRecPingTimes >= 2) {
//                    // �ж��Ƿ�������û���յ��ͻ��˷��͵�����
//                    ctx.channel().close();
//                } else {
//                    unRecPingTimes++;
//                }
                ctx.channel().close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
        LOG.error("�������� �����쳣���ر�����,����������Դ");

        ctx.channel().close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PongMsg msg) throws Exception {

    }
}

