package com.yao.module;

/**
 * Created by yaozb on 15-4-11.
 * 心跳检测的消息类型
 */
public class PongMsg extends BaseMsg {
    public PongMsg() {
        super();
        setType(MsgType.PONG);
    }
}
