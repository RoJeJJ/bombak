package com.roje.bombak.nn.manager;

import com.google.protobuf.InvalidProtocolBufferException;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.message.InnerClientMessage;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.nn.config.NnProperties;
import com.roje.bombak.nn.config.NnSetting;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.proto.NnMsg;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.common.config.RoomProperties;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.exception.CreateRoomException;
import com.roje.bombak.room.common.manager.RoomIdGenerator;
import com.roje.bombak.room.common.manager.impl.BaseRoomManager;
import com.roje.bombak.room.common.proto.RoomMsg;
import com.roje.bombak.room.common.redis.RoomRedisDao;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/25
 **/
@Slf4j
@Component
public class NnRoomManager extends BaseRoomManager<NnPlayer, NnRoom> {

    private final NnProperties nnProperties;

    private final RoomIdGenerator idGenerator;

    protected NnRoomManager(RoomProperties roomProperties, MessageSender sender, RoomRedisDao roomRedisDao, ServiceInfo serviceInfo,
                            RedissonClient redissonClient, NnProperties nnProperties, RoomIdGenerator idGenerator) {
        super(roomProperties, sender, roomRedisDao, serviceInfo, redissonClient);
        this.nnProperties = nnProperties;
        this.idGenerator = idGenerator;
    }

    @Override
    public NnRoom createCardRoom(InnerClientMessage message) throws CreateRoomException {
        Nn.RoomConfig config;
        try {
            config = Nn.RoomConfig.parseFrom(message.getContent());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return null;
        }
        NnSetting nnConfig = new NnSetting(config,true,nnProperties);
        long id = roomIdGenerator.getId();
        String name = "牛牛房卡房" + id;
        return new NnRoom(id,message.getUid(),name,roomExecutorGroup.next(),nnConfig, RoomMsg.RoomType.card,
                sender,userRedisDao,this,nnProperties, nnProperties);
    }

    @Override
    protected NnRoom createRoom0(ServerMsg.ForwardClientMessage message) throws CreateRoomException {
        NnMsg.RoomSetting roomSetting;
        try {
            roomSetting = message.getCsMessage().getData().unpack(NnMsg.RoomSetting.class);
        } catch (InvalidProtocolBufferException e) {
            log.warn("room setting 解析异常",e);
            return null;
        }
        if (message.getCsMessage().getMessageId() == RoomConstant.Cmd.CREATE_CARD_ROOM_REQ) {
            NnSetting setting = new NnSetting(roomSetting,true,nnProperties);
            long id = idGenerator.getId();
            String name = "牛牛房卡房";
            return new NnRoom();
        }

        return null;
    }

    @Override
    protected NnPlayer newPlayer(ServerMsg.ForwardClientMessage message) {
        return null;
    }
}
