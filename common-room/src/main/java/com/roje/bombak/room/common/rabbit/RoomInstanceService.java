package com.roje.bombak.room.common.rabbit;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;

import java.util.Map;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/22
 **/
public class RoomInstanceService {

    private final ApplicationInfoManager infoManager;

    public RoomInstanceService(ApplicationInfoManager infoManager) {
        this.infoManager = infoManager;
    }

    public void changeRoomSize(int size) {
        InstanceInfo instanceInfo = infoManager.getInfo();
        Map<String,String> metadata = instanceInfo.getMetadata();
        metadata.put("roomSize",String.valueOf(size));
    }
}
