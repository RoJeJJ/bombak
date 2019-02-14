package com.roje.bombak.common.api.constant;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/22
 **/
public interface GlobalConstant {

    String BROADCAST_QUEUE_NAME = "fanout.queue";

    String BROADCAST_EXCHANGE_NAME = "amq.fanout";

    int LOGIN_BROADCAST = 888;

    int DISCONNECT_BROADCAST = 999;
}
