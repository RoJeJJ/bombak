package com.roje.bombak.nn.redis;

import com.roje.bombak.common.eureka.ServiceInfo;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/4
 **/
public interface UserRoomDao {

    ServiceInfo getUserRoomService(long uid);

    boolean setUserRoomService(long uid, ServiceInfo roomServiceInfo);

    void removeUserRoomService(long uid);
}
