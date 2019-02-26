package com.roje.bombak.common.constant;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/22
 **/
public interface Constant {

    String BROADCAST_QUEUE_NAME = "fanout.queue";

    String BROADCAST_EXCHANGE_NAME = "amq.fanout";

    int DISCONNECT_BROADCAST = 999;
    interface Cmd {
        int LOGIN_BROADCAST = 888;
    }
}
