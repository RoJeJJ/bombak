package com.roje.bombak.gate.session;


import com.roje.bombak.common.proto.ServerMsg;

/**
 * @author pc
 */
public interface GateSession {


    int serial();

    boolean checkSerial(int serial);
    /**
     * 用户uid
     * @return uid
     */
    long uid();

    /**
     * session登录调用次方法
     * @param uid uid
     */
    void login(long uid);

    /**
     * 发送消息
     * @param message msg
     */
    void send(ServerMsg.S2CMessage message);

    /**
     * 判断是否登录
     * @return 是否登录
     */
    boolean isLogged();

    /**
     * 关闭session
     */
    void close();

    /**
     * session是否关闭
     * @return 关闭true 否则false
     */
    boolean isClosed();
}
