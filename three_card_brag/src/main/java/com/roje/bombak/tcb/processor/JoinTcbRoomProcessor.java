package com.roje.bombak.tcb.processor;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.manager.RoomManager;
import com.roje.bombak.room.common.processor.JoinRoomProcessor;
import com.roje.bombak.room.common.redis.RoomRedisDao;
import com.roje.bombak.tcb.player.TcbPlayer;
import com.roje.bombak.tcb.room.TcbRoom;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/21
 **/
@Component
@Message(id = RoomConstant.Cmd.JOIN_ROOM_REQ)
public class JoinTcbRoomProcessor extends JoinRoomProcessor<TcbPlayer, TcbRoom> {
    public JoinTcbRoomProcessor(RoomManager<TcbPlayer, TcbRoom> roomManager,
                                MessageSender sender,
                                @Qualifier("redissonSingle") RedissonClient redissonClient,
                                RoomRedisDao roomRedisDao) {
        super(roomManager, sender, redissonClient, roomRedisDao);
    }
}
