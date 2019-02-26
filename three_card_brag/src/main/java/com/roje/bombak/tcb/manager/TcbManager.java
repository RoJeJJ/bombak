package com.roje.bombak.tcb.manager;

import com.google.protobuf.Any;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.redis.dao.UserRedisDao;
import com.roje.bombak.room.common.exception.CreateRoomException;
import com.roje.bombak.room.common.manager.impl.BaseRoomManager;
import com.roje.bombak.room.common.rabbit.RoomInstanceService;
import com.roje.bombak.room.common.redis.RoomRedisDao;
import com.roje.bombak.tcb.player.TcbPlayer;
import com.roje.bombak.tcb.room.TcbRoom;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/21
 **/
@Component
public class TcbManager extends BaseRoomManager<TcbPlayer, TcbRoom> {


    protected TcbManager(RoomRedisDao roomRedisDao,
                         ServiceInfo serviceInfo,
                         UserRedisDao userRedisDao,
                         RoomInstanceService roomInstanceService) {
        super(roomRedisDao, serviceInfo, userRedisDao, roomInstanceService);
    }

    @Override
    public TcbRoom createRoom(long creatorId, Any roomSetting) throws CreateRoomException {
        return null;
    }
}
