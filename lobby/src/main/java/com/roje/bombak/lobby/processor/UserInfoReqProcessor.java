package com.roje.bombak.lobby.processor;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.common.dispatcher.CommonProcessor;
import com.roje.bombak.common.message.InnerClientMessage;
import com.roje.bombak.common.model.User;
import com.roje.bombak.common.redis.dao.UserRedisDao;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.lobby.constant.LobbyConstant;
import com.roje.bombak.lobby.proto.Lobby;
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
            Lobby.UserInfoResponse.Builder builder = Lobby.UserInfoResponse.newBuilder();
            builder.setId(user.id());
            builder.setAccount(user.account());
            if (user.getNickname() != null) {
                builder.setNickname(user.getNickname());
            }
            if (user.getHeadImg() != null) {
                builder.setHeadImg(user.getHeadImg());
            }
            builder.setSex(user.getSex());
            builder.setCard(user.getRoomCard());
            builder.setGold(user.getGold());
            messageSender.send(message,LobbyConstant.USER_INFO_RES,builder.build());
        }
    }
}
