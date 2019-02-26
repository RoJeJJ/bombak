package com.roje.bombak.room.common.processor;

import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.processor.GateToServerMessageProcessor;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.manager.RoomManager;
import com.roje.bombak.room.common.player.Player;
import com.roje.bombak.room.common.proto.RoomMsg;
import com.roje.bombak.room.common.redis.RoomRedisDao;
import com.roje.bombak.room.common.room.Room;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/19
 **/
@Slf4j
public class JoinRoomProcessor<P extends Player,R extends Room<P>> implements GateToServerMessageProcessor {

    private static final String JOIN_ROOM_LOCK = "join_room_lock:";

    private final RoomManager<P,R> roomManager;

    private final MessageSender sender;

    private final RedissonClient redissonClient;

    private final RoomRedisDao roomRedisDao;

    public JoinRoomProcessor(RoomManager<P,R> roomManager,
                             MessageSender sender,
                             RedissonClient redissonClient,
                             RoomRedisDao roomRedisDao) {
        this.roomManager = roomManager;
        this.sender = sender;
        this.redissonClient = redissonClient;
        this.roomRedisDao = roomRedisDao;
    }

    @Override
    public void process(ServerMsg.GateToServerMessage message) throws Exception {
        RoomMsg.JoinRoomReq joinRoomReq = message.getData().unpack(RoomMsg.JoinRoomReq.class);
        long uid = message.getUserId();
        R reqRoom = roomManager.getRoom(joinRoomReq.getRoomId());
        R joinedRoom = roomManager.getJoinRoom(uid);
        if (joinedRoom != null) {
            joinedRoom.getExecutor().execute(() -> {
                if (joinedRoom.isClosed()) {
                    return;
                }
                P player = joinedRoom.getPlayer(uid);
                if (player != null) {
                    if (player.isExit()) {
                        joinedRoom.reJoin(player,message.getSessionId());
                    } else if (player.isOffline()) {
                        joinedRoom.online(player,message.getSessionId());
                    } else {
                        log.info("重复进入房间");
                    }
                } else {
                    log.warn("玩家重新进入房间,但是房间中并没有玩家数据???");
                }
            });
        } else if (reqRoom == null) {
            sender.sendErrMsgToGate(message, RoomConstant.ErrorCode.ROOM_NOT_FOUND);
        } else {
            reqRoom.getExecutor().execute(() -> {
                if (reqRoom.isClosed()) {
                    return;
                }
                RLock lock = redissonClient.getLock(JOIN_ROOM_LOCK + uid);
                if (lock.tryLock()) {
                    log.info("redisson 获取加入房间锁失败,可能正在加入另一个房间,本次请求忽略");
                    return;
                }
                try {
                    ServiceInfo sInfo = roomRedisDao.getUserRoomService(uid);
                    if (sInfo != null) {
                        log.info("玩家已经加入了别的游戏服务器房间{}", sInfo);
                        sender.sendErrMsgToGate(message,RoomConstant.ErrorCode.JOINED_OTHER_ROOM);
                    } else {
                        if (reqRoom.getCapacity() > 0 && reqRoom.getPlayerSize() >= reqRoom.getCapacity()) {
                            log.info("房间满了");
                            sender.sendErrMsgToGate(message,RoomConstant.ErrorCode.ROOM_FULL);
                        } else {
                            P player = reqRoom.getPlayer(uid);
                            if (player != null) {
                                log.warn("玩家加入一个房间,但是房间中已经有该玩家的数据???");
                            } else {
                                reqRoom.join(uid,message.getSessionId());
                            }
                        }
                    }
                }finally {
                    lock.unlock();
                }
            });
        }
    }
}
