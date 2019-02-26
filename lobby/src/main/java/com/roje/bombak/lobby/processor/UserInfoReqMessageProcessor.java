package com.roje.bombak.lobby.processor;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.common.processor.GateToServerMessageProcessor;
import com.roje.bombak.common.model.User;
import com.roje.bombak.common.redis.dao.UserRedisDao;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.lobby.constant.LobbyConstant;
import com.roje.bombak.lobby.proto.LobbyMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author pc
 */
@Slf4j
@Component
@Message(id = LobbyConstant.USER_INFO_REQ)
public class UserInfoReqMessageProcessor implements GateToServerMessageProcessor {

    private final UserRedisDao userRedisDao;

    private final MessageSender messageSender;


    public UserInfoReqMessageProcessor(UserRedisDao userRedisDao, MessageSender messageSender) {
        this.userRedisDao = userRedisDao;
        this.messageSender = messageSender;
    }


    @Override
    public void process(ServerMsg.GateToServerMessage message) {
        User user = userRedisDao.getUser(message.getUserId());
        if (user != null) {
            LobbyMsg.UserInfoRes.Builder builder = LobbyMsg.UserInfoRes.newBuilder();
            builder.setId(user.id())
                    .setAccount(user.account())
                    .setSex(user.getSex())
                    .setCard(user.getRoomCard())
                    .setGold(user.getGold());
            if (user.getNickname() != null) {
                builder.setNickname(user.getNickname());
            }
            if (user.getHeadImg() != null) {
                builder.setHeadImg(user.getHeadImg());
            }
            messageSender.sendMsgToGate(message,LobbyConstant.USER_INFO_RES,builder.build());
        }
    }
}
