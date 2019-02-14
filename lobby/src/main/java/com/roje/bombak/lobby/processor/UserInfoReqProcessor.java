package com.roje.bombak.lobby.processor;

import com.roje.bombak.common.api.annotation.Message;
import com.roje.bombak.common.api.dispatcher.CommonProcessor;
import com.roje.bombak.common.api.message.InnerClientMessage;
import com.roje.bombak.common.api.model.User;
import com.roje.bombak.common.api.redis.dao.UserRedisDao;
import com.roje.bombak.common.api.utils.MessageSender;
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
public class UserInfoReqProcessor implements CommonProcessor {

    private final UserRedisDao userRedisDao;

    private final MessageSender messageSender;


    public UserInfoReqProcessor(UserRedisDao userRedisDao, MessageSender messageSender) {
        this.userRedisDao = userRedisDao;
        this.messageSender = messageSender;
    }


    @Override
    public void process(InnerClientMessage message) {
        User user = userRedisDao.getUser(message.getUid());
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
            messageSender.send(message,LobbyConstant.USER_INFO_RES,builder.build());
        }
    }
}
