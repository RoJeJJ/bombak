package com.roje.bombak.nn.processor;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.manager.RoomManager;
import com.roje.bombak.room.common.processor.JoinRoomProcessor;
import com.roje.bombak.room.common.redis.RoomRedisDao;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/20
 **/
@Component
@Message(id = RoomConstant.Cmd.JOIN_ROOM_REQ)
public class JoinNnRoomProcessor extends JoinRoomProcessor<NnPlayer, NnRoom> {
    public JoinNnRoomProcessor(RoomManager<NnPlayer, NnRoom> roomManager,
                               MessageSender messageSender,
                               @Qualifier("redissonSingle") RedissonClient redissonClient,
                               RoomRedisDao roomRedisDao) {
        super(roomManager, messageSender, redissonClient, roomRedisDao);
    }
}
