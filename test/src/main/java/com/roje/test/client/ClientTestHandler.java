package com.roje.test.client;

import com.roje.bombak.proto.gate.LoginRequest;
import com.roje.bombak.proto.gate.LoginResponse;
import com.roje.bombak.proto.lobby.UserInfoResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientTestHandler extends SimpleChannelInboundHandler<OutboundMessage> {

    private int serial = 1;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, OutboundMessage msg) throws Exception {
        if (msg.getErrorCode() != 0) {
            log.info("错误码:{}",msg.getErrorCode());
        } else {
            switch (msg.getMessageId()) {
                case 1002:
                    LoginResponse response = LoginResponse.parseFrom(msg.getContent());
                    log.info("登录成功");
                    InboundMessage message = new InboundMessage();
                    message.setSerial(serial++);
                    message.setTimestamp(System.currentTimeMillis());
                    message.setServiceType((short) 200);
                    message.setMessageId((short) 2001);
                    ctx.writeAndFlush(message);
                    break;
                case 1003:
                    InboundMessage message1 = new InboundMessage();
                    message1.setSerial(serial++);
                    message1.setTimestamp(System.currentTimeMillis());
                    message1.setServiceType((short) 100);
                    message1.setMessageId((short) 1004);
                    ctx.writeAndFlush(message1);
                    break;
                case 2002:
                    UserInfoResponse userInfo = UserInfoResponse.parseFrom(msg.getContent());
                    log.info("收到用户信息");
                    log.info("id:" + userInfo.getId());
                    log.info("account:" + userInfo.getAccount());
                    log.info("nickname:" + userInfo.getNickname());
                    log.info("headImg:" + userInfo.getHeadImg());
                    log.info("sex:" + userInfo.getSex());
                    log.info("card:" + userInfo.getCard());
                    log.info("gold:" + userInfo.getGold());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("连接成功,开始登录");
        LoginRequest.Builder builder = LoginRequest.newBuilder();
        builder.setUid(1);
        builder.setToken("4ae528c1a21949118929c3a8750c4e14");
        InboundMessage message = new InboundMessage();
        message.setSerial(serial++);
        message.setTimestamp(System.currentTimeMillis());
        message.setServiceType((short) 100);
        message.setMessageId((short) 1001);
        message.setContent(builder.build().toByteArray());
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("断开连接");
    }

}
