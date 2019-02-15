package com.roje.bombak.room.common.redis;

import com.roje.bombak.common.eureka.ServiceInfo;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/8
 **/
public interface RoomRedisDao {

    boolean setUserRoomService(long uid, ServiceInfo serviceInfo);

    ServiceInfo getUserRoomService(long uid);

    void removeUserRoomService(long uid);

    void setRoomService(long roomId,ServiceInfo serviceInfo);

    ServiceInfo getRoomService(long roomId);

    void removeRoomService(long roomId);

    void setGoldRoomNo();

    Long getGoldRoomNo();
}
