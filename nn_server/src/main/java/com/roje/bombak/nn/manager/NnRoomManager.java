package com.roje.bombak.nn.manager;

import com.google.protobuf.InvalidProtocolBufferException;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.message.InnerClientMessage;
import com.roje.bombak.common.redis.dao.UserRedisDao;
import com.roje.bombak.nn.config.NnProperties;
import com.roje.bombak.nn.config.NnRoomConfig;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.proto.Nn;
import com.roje.bombak.nn.room.NnRoom;
import com.roje.bombak.room.api.config.RoomProperties;
import com.roje.bombak.room.api.exception.CreateRoomException;
import com.roje.bombak.room.api.executor.RoomCreateExecutorGroup;
import com.roje.bombak.room.api.executor.UserExecutorGroup;
import com.roje.bombak.room.api.manager.RoomIdGenerator;
import com.roje.bombak.room.api.manager.impl.AbstractRoomManager;
import com.roje.bombak.room.api.redis.RoomRedisDao;
import com.roje.bombak.room.api.utils.RoomMessageSender;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/25
 **/
@Slf4j
@Component
public class NnRoomManager extends AbstractRoomManager<NnPlayer, NnRoom> {

    private final RoomIdGenerator roomIdGenerator;

    private final EventExecutorGroup roomExecutorGroup;

    private final UserRedisDao userRedisDao;

    private final NnProperties nnProperties;

    protected NnRoomManager(RoomMessageSender messageSender,
                            RoomRedisDao roomRedisDao,
                            ServiceInfo serviceInfo,
                            UserExecutorGroup executorGroup,
                            @Qualifier("redissonSingle") RedissonClient redissonClient,
                            RoomCreateExecutorGroup roomExecutor,
                            RoomProperties roomProperties,
                            RoomIdGenerator roomIdGenerator,
                            UserRedisDao userRedisDao,
                            NnProperties nnProperties) {
        super(messageSender, roomRedisDao, serviceInfo, executorGroup, redissonClient, roomExecutor, roomProperties);
        this.roomIdGenerator = roomIdGenerator;
        roomExecutorGroup = new DefaultEventExecutorGroup(roomProperties.getRoomExecutorSize());
        this.userRedisDao = userRedisDao;
        this.nnProperties = nnProperties;
        roomRedisDao.setGoldRoomNo();
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
        NnRoomConfig nnConfig = new NnRoomConfig(config,true,nnProperties);
        long id = roomIdGenerator.getId();
        String name = "牛牛房卡房" + id;
        return new NnRoom(id,message.getUid(),name,roomExecutorGroup.next(),nnConfig,true,
                sender,userRedisDao,this,nnProperties);
    }

    @Override
    public NnRoom createGoldRoom(InnerClientMessage message) throws CreateRoomException {
        return null;
    }
}
