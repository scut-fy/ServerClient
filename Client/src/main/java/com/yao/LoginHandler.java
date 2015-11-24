package com.yao;

import com.yao.module.LoginMsg;
import com.yao.module.LoginRep;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author bryan
 * @date 2015/11/24.
 */

public class LoginHandler extends ChannelInboundHandlerAdapter {


    private final static Log LOG = LogFactory.getLog(LoginHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
        ctx.fireExceptionCaught(cause);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        super.channelActive(ctx);
        LoginMsg loginMsg = new LoginMsg();
        loginMsg.setPassword("yao");
        loginMsg.setUserName("robin");
        ctx.channel().writeAndFlush(loginMsg);
        LOG.info("user send login msg");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        super.channelRead(ctx, msg);
        if (msg instanceof LoginRep) {
            LOG.info("Login is ok : ");
            ctx.fireChannelRead(msg);
            ctx.fireChannelActive();
        } else if (msg instanceof LoginMsg) {
            LOG.error("验证失败");
            switch (((LoginMsg) msg).getType()){
                case LOGIN: {
                    //向服务器发起登录
                    LoginMsg loginMsg = new LoginMsg();
                    loginMsg.setPassword("yao");
                    loginMsg.setUserName("robin");
                    ctx.writeAndFlush(loginMsg);
                }
                break;
            }
            LOG.info("用户重新登录");
//            ctx.channel().close();

        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
