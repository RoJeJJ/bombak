package com.roje.bombak.nn.manager;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.redis.dao.UserRedisDao;
import com.roje.bombak.nn.config.NnProperties;
import com.roje.bombak.nn.config.NnSetting;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.proto.NnMsg;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.common.exception.CreateRoomException;
import com.roje.bombak.room.common.manager.RoomIdGenerator;
import com.roje.bombak.room.common.manager.impl.BaseRoomManager;
import com.roje.bombak.room.common.rabbit.RoomInstanceService;
import com.roje.bombak.room.common.redis.RoomRedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/25
 **/
@Slf4j
@Component
public class NnRoomManager extends BaseRoomManager<NnPlayer,NnRoom> {

    private final NnProperties nnProperties;

    private final RoomIdGenerator idGenerator;

    protected NnRoomManager(RoomRedisDao roomRedisDao,
                            ServiceInfo serviceInfo,
                            UserRedisDao userRedisDao,
                            NnProperties nnProperties,
                            RoomIdGenerator idGenerator,
                            RoomInstanceService roomInstanceService) {
        super(roomRedisDao, serviceInfo, userRedisDao, roomInstanceService);
        this.nnProperties = nnProperties;
        this.idGenerator = idGenerator;
    }

    @Override
    public NnRoom createRoom(long creatorId, Any data) throws CreateRoomException {
        try {
            NnMsg.RoomSetting setting = data.unpack(NnMsg.RoomSetting.class);
            NnSetting nnSetting = new NnSetting(setting,nnProperties);
            NnRoom room = new NnRoom(idGenerator.getId(),this,nnSetting);
            room.setName("牛牛房卡房");
            room.setRoomType(nnSetting.roomType);
            room.setSeatSize(nnSetting.seatSize);
            room.setWaitRushTime(nnProperties.getRushSecondTime() * 1000);
            room.setWaitBetTime(nnProperties.getBetSecondTime() * 1000);
            room.setWaitCheckTime(nnProperties.getCheckSecondTime() * 1000);
            return room;
        } catch (InvalidProtocolBufferException e) {
            log.warn("room setting 解析异常",e);
            return null;
        }
    }
}
