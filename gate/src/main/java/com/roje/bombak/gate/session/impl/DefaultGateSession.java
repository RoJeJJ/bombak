package com.roje.bombak.gate.session.impl;

import com.roje.bombak.common.proto.ServerMsg;
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

    private String id;

    private long uid;

    private Channel channel;

    private boolean logged;

    private volatile boolean closed;

    public DefaultGateSession( Channel channel) {
        this.channel = channel;
        closed = false;
        id = channel.id().asShortText();
    }

    @Override
    public void login(long uid) {
        this.uid = uid;
        this.logged = true;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public long uid() {
        return uid;
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
    public void send(ServerMsg.GateToClientMessage message) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(message);
        }
    }

    @Override
    public boolean isLogged() {
        return logged;
    }
}
