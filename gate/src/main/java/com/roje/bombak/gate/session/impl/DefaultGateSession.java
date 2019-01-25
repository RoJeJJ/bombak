package com.roje.bombak.gate.session.impl;

import com.google.protobuf.Message;
import com.roje.bombak.gate.session.GateSession;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/2
 **/
@Slf4j
public class DefaultGateSession implements GateSession {

    private long uid;

    private int serial;

    private Channel channel;

    private boolean logged;

    private volatile boolean closed;

    public DefaultGateSession( Channel channel) {
        this.channel = channel;
        closed = false;
        serial = 0;
    }

    @Override
    public void login(long uid) {
        this.uid = uid;
        this.logged = true;
    }

    @Override
    public long uid() {
        return uid;
    }

    @Override
    public int serial() {
        return serial;
    }

    @Override
    public boolean checkSerial(int serial) {
        if ( this.serial + 1 == serial) {
            this.serial = serial;
            return true;
        }
        return false;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }



    @Override
    public void close() {
        log.info("session关闭");
        if (!closed) {
            closed = true;
        }
        if (channel != null && channel.isActive()) {
            channel.close();
        }
    }

    @Override
    public void send(Message message) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(message);
        }
    }

    @Override
    public boolean isLogged() {
        return logged;
    }
}
