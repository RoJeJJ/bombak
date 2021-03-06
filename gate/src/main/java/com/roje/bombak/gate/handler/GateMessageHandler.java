package com.roje.bombak.gate.handler;

import com.roje.bombak.common.constant.Constant;
import com.roje.bombak.common.processor.Dispatcher;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.gate.config.GateProperties;
import com.roje.bombak.gate.constant.GateConstant;
import com.roje.bombak.gate.manager.GateSessionManager;
import com.roje.bombak.gate.processor.GateProcessor;
import com.roje.bombak.gate.session.GateSession;
import com.roje.bombak.gate.session.impl.DefaultGateSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Component;
import com.roje.bombak.common.proto.ServerMsg.ClientToGateMessage;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author pc
 * @version 1.0
 * @date 2018/12/27
 **/
@Slf4j
@Component
@ChannelHandler.Sharable
public class GateMessageHandler extends SimpleChannelInboundHandler<ClientToGateMessage> {

    private static final AttributeKey<GateSession> GATE_SESSION_ATTR = AttributeKey.newInstance("netty.channel.gate.session");

    private static final String CAUSE_MSG = "远程主机强迫关闭了一个现有的连接。";

    private static final AtomicInteger CHANNEL_COUNT = new AtomicInteger(0);

    private final GateProperties gateConfig;

    private final ServiceInfo gateInfo;

    private final Dispatcher dispatcher;

    private final AmqpTemplate amqpTemplate;

    private final LoadBalancerClient loadBalancerClient;

    private final GateSessionManager sessionManager;

    private final MessageSender sender;


    public GateMessageHandler(GateProperties gateConfig, ServiceInfo gateInfo,
                              AmqpTemplate amqpTemplate,
                              Dispatcher dispatcher,
                              LoadBalancerClient loadBalancerClient,
                              GateSessionManager sessionManager,
                              MessageSender sender) {
        this.gateConfig = gateConfig;
        this.gateInfo = gateInfo;
        this.amqpTemplate = amqpTemplate;
        this.dispatcher = dispatcher;
        this.loadBalancerClient = loadBalancerClient;
        this.sessionManager = sessionManager;
        this.sender = sender;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("新的连接:{},当前连接数:{}", ctx, CHANNEL_COUNT.incrementAndGet());
        GateSession session = new DefaultGateSession(ctx.channel());
        ctx.channel().attr(GATE_SESSION_ATTR).set(session);
        int timeout = gateConfig.getLoginTimeout();
        if (timeout > 0) {
            ctx.executor().schedule(() -> {
                synchronized (session) {
                    if (!session.isLogged()) {
                        log.info("session登录超时,关闭连接");
                        session.close();
                    }
                }
            },timeout, TimeUnit.SECONDS);
        }
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("连接断开:{},当前连接数:{}", ctx, CHANNEL_COUNT.decrementAndGet());
        GateSession session = ctx.channel().attr(GATE_SESSION_ATTR).getAndSet(null);
        if (session.isLogged()) {
            if (!session.isClosed()) {
                sessionManager.closeSession(session);
            }
            sessionDisConnect(session);
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ClientToGateMessage message) throws Exception {
        GateSession session = ctx.channel().attr(GATE_SESSION_ATTR).get();
        if (session == null || session.isClosed()) {
            log.info("session closed");
            return;
        }
        boolean gateService = StringUtils.equalsIgnoreCase(gateInfo.getServiceType(),message.getServiceType());

        if (gateService && message.getMsgId() == GateConstant.Cmd.LOGIN_REQ) {
            if (session.isLogged()) {
                log.info("已经登录了");
                session.send(sender.scErrMsg(GateConstant.ErrorCode.LOGIN_REPEAT));
                return;
            }
        } else {
            if (!session.isLogged()) {
                log.info("还有没有登录");
                session.send(sender.scErrMsg(GateConstant.ErrorCode.NOT_LOGIN));
                return;
            }
        }
        if (gateService) {
            GateProcessor processor = dispatcher.processor(message.getMsgId());
            if (processor != null) {
                processor.process(session,message);
            } else {
                log.info("消息没有被处理,msgId:{}", message.getMsgId());
            }
        } else {
            //其他服务消息
            ServiceInstance instance = loadBalancerClient.choose(message.getServiceType());
            if (instance != null) {
                ServerMsg.GateToServerMessage.Builder builder = ServerMsg.GateToServerMessage.newBuilder();
                builder.setMsgType(message.getMsgType())
                        .setUserId(session.uid())
                        .setSessionId(ctx.channel().id().asLongText())
                        .setMsgId(message.getMsgId())
                        .setData(message.getData())
                        .setServiceType(gateInfo.getServiceType())
                        .setServiceId(gateInfo.getServiceId());
                String routeKey = message.getServiceType() + "-" + instance.getMetadata().get("id");
                amqpTemplate.convertAndSend(routeKey, builder.build().toByteArray());
            } else {
                log.info("服务暂不可用");
                session.send(sender.scErrMsg(GateConstant.ErrorCode.SERVICE_NOT_AVAILABLE));
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        GateSession session = ctx.channel().attr(GATE_SESSION_ATTR).get();
        if (session != null && session.isLogged() && evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()) {
                case ALL_IDLE:
                    log.info("数据读写超时,发送心跳请求");
                    session.send(sender.scMsg(GateConstant.GATE_MSG,GateConstant.Cmd.HEART_BEAT_REQ));
                    break;
                case READER_IDLE:
                    log.info("读超时,客户端可能挂了,关闭连接");
                    session.close();
                    break;
                default:
                    break;
            }
        }
        ctx.fireUserEventTriggered(evt);
    }


    private void sessionDisConnect(GateSession session) {
        sender.allServerMsg(GateConstant.GATE_MSG, Constant.DISCONNECT_BROADCAST,null);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException && CAUSE_MSG.equals(cause.getMessage())) {
            log.info("客户端断开了");
        } else {
            log.warn("this causes an exception", cause);
        }
    }
}
