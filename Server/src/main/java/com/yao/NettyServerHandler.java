package com.yao;

import com.yao.module.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by yaozb on 15-4-11.
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<BaseMsg> {


    private final static Log LOG = LogFactory.getLog(NettyServerHandler.class);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyChannelMap.remove((SocketChannel)ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BaseMsg baseMsg) throws Exception {

        if(MsgType.LOGIN.equals(baseMsg.getType())){
            LoginMsg loginMsg=(LoginMsg)baseMsg;
            if("robin".equals(loginMsg.getUserName())&&"yao".equals(loginMsg.getPassword())){
                //登录成功,把channel存到服务端的map中
                NettyChannelMap.add(loginMsg.getClientId(),(SocketChannel)channelHandlerContext.channel());
                LOG.info("client" + loginMsg.getClientId() + " 登录成功");
                // 告诉客户端你已经验证通过了
                // 需要再创建一个消息类型，登录成功的消息类型。

            }
        }else{
            if(NettyChannelMap.get(baseMsg.getClientId())==null){
                    //说明未登录，或者连接断了，服务器向客户端发起登录请求，让客户端重新登录
                    LoginMsg loginMsg=new LoginMsg();
                    channelHandlerContext.channel().writeAndFlush(loginMsg);
            }
        }
        switch (baseMsg.getType()){
            case PING:{
                PingMsg pingMsg=(PingMsg)baseMsg;
                PongMsg replyPing=new PongMsg();
                NettyChannelMap.get(pingMsg.getClientId()).writeAndFlush(replyPing);
                LOG.debug("收到一个心跳，回复pong");
            }break;
            case ASK:{
                //收到客户端的请求
                AskMsg askMsg=(AskMsg)baseMsg;
                if("authToken".equals(askMsg.getParams().getAuth())){
                    ReplyServerBody replyBody=new ReplyServerBody("server info $$$$ !!!");
                    ReplyMsg replyMsg=new ReplyMsg();
                    replyMsg.setBody(replyBody);
                    NettyChannelMap.get(askMsg.getClientId()).writeAndFlush(replyMsg);
                    LOG.info("收到一个请求，然后回复");
                }
            }break;
            case REPLY:{
                //收到客户端回复
                ReplyMsg replyMsg=(ReplyMsg)baseMsg;
                ReplyClientBody clientBody=(ReplyClientBody)replyMsg.getBody();
                LOG.info("receive client msg: " + clientBody.getClientInfo());
            }break;
            default:break;
        }
        ReferenceCountUtil.release(baseMsg);
    }


}
