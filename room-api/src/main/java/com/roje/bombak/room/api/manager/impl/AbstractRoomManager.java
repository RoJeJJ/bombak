package com.roje.bombak.room.api.manager.impl;

import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.message.InnerClientMessage;
import com.roje.bombak.room.api.config.RoomProperties;
import com.roje.bombak.room.api.constant.Constant;
import com.roje.bombak.room.api.exception.CreateRoomException;
import com.roje.bombak.room.api.executor.RoomCreateExecutorGroup;
import com.roje.bombak.room.api.executor.UserExecutorGroup;
import com.roje.bombak.room.api.manager.RoomManager;
import com.roje.bombak.room.api.player.Player;
import com.roje.bombak.room.api.proto.RoomMsg;
import com.roje.bombak.room.api.redis.RoomRedisDao;
import com.roje.bombak.room.api.room.Room;
import com.roje.bombak.room.api.utils.RoomMessageSender;
import io.netty.util.concurrent.EventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/21
 */
@Slf4j
public abstract class AbstractRoomManager<P extends Player, R extends Room<P>> implements RoomManager<P, R> {

    private static final String ENTER_LOCK = "enter_room_lock-";

    private final Map<Long,R> rooms = new ConcurrentHashMap<>();

    private final Map<Long, R> cardRooms = new ConcurrentHashMap<>();

    private final Map<Long,R> goldRooms = new ConcurrentHashMap<>();

    private final Map<Long, R> userRooms = new ConcurrentHashMap<>();

    protected final RoomMessageSender sender;

    private final RoomRedisDao roomRedisDao;

    private final ServiceInfo serviceInfo;

    private final UserExecutorGroup userExecutorGroup;

    private final RedissonClient redissonClient;

    private final RoomCreateExecutorGroup roomExecutor;

    private final RoomProperties roomProperties;

    protected AbstractRoomManager(
            RoomMessageSender messageSender,
            RoomRedisDao roomRedisDao,
            ServiceInfo serviceInfo,
            UserExecutorGroup executorGroup,
            RedissonClient redissonClient,
            RoomCreateExecutorGroup roomExecutor,
            RoomProperties roomProperties) {
        this.sender = messageSender;
        this.roomRedisDao = roomRedisDao;
        this.serviceInfo = serviceInfo;

        this.userExecutorGroup = executorGroup;
        this.redissonClient = redissonClient;
        this.roomExecutor = roomExecutor;
        this.roomProperties = roomProperties;
    }

    @Override
    public R getCardRoom(long roomId) {
        return cardRooms.get(roomId);
    }

    @Override
    public R getGoldRoom(long roomId) {
        return goldRooms.get(roomId);
    }

    @Override
    public R getPlayerRoom(long uid) {
        return userRooms.get(uid);
    }

    @Override
    public void removeRoomPlayer(R room, P player) {
        userRooms.remove(player.uid());
        roomRedisDao.removeUserRoomService(player.uid());
    }

    @Override
    public void removeRoom(R room) {
        if (room.roomType() == RoomMsg.RoomType.card) {
            cardRooms.remove(room.id());
        } else if (room.roomType() == RoomMsg.RoomType.gold){
            goldRooms.remove(room.id());
        }
        rooms.remove(room.id());
        roomRedisDao.removeRoomService(room.id());
        userRooms.entrySet().removeIf(entry -> {
            if (entry.getValue() == room) {
                roomRedisDao.removeUserRoomService(entry.getKey());
                return true;
            }
            return false;
        });
    }

    @Override
    public int getRoomMaxPlayer() {
        return roomProperties.getRoomMaxPlayers();
    }

    @Override
    public String getGameName() {
        return serviceInfo.getServiceType();
    }

    //    @Override
//    public void onFanoutMessage(InnerClientMessage message) {
//        R room = userRooms.get(message.getUid());
//        if (room != null) {
//            if (message.getMessageId() == GlobalConstant.LOGIN_BROADCAST) {
//                log.info("通知用户{}所在的房间号{}", message.getUid(), room.id());
//                RoomMsg.IndicateUserRoom.Builder builder = RoomMsg.IndicateUserRoom.newBuilder();
//                builder.setRoomId(room.id());
//                messageSender.send(message, Constant.Indicate.USER_ROOM, builder.build());
//            } else if (message.getMessageId() == GlobalConstant.DISCONNECT_BROADCAST) {
//                room.executor().execute(() -> {
//                    if (!room.isClosed()) {
//                        P player = room.getPlayer(message.getUid());
//                        if (player != null) {
//                            room.disconnect(player);
//                        }
//                    }
//                });
//            }
//        }
//    }


    @Override
    public void createRoom(InnerClientMessage message, RoomMsg.RoomType type) {
        roomExecutor.executor().execute(() -> {
            if (rooms.size() >= roomProperties.getMaxRoomSize()) {
                log.info("房间满了,请稍后再试");
                sender.sendError(message, Constant.ErrorCode.SERVER_ROOM_FULL);
                return;
            }
            R room = null;
            try {
                if (type == Constant.RoomType.card) {
                    room = createCardRoom(message);
                } else if (type == Constant.RoomType.gold) {
                    room = createGoldRoom(message);
                }
                if (room != null) {
                    log.info("{}:{}创建成功", room.name(),room.id());
                    if (room.roomType() == Constant.RoomType.card) {
                        cardRooms.put(room.id(),room);
                    } else if (room.roomType() == Constant.RoomType.gold){
                        goldRooms.put(room.id(),room);
                    } else {
                        log.info("不支持的房间类型");
                        return;
                    }
                    rooms.put(room.id(),room);
                    roomRedisDao.setRoomService(room.id(),serviceInfo);
                    RoomMsg.CreateRoomRes.Builder builder = RoomMsg.CreateRoomRes.newBuilder();
                    builder.setRoomId(room.id());
                    sender.send(message,Constant.Cmd.CREATE_CARD_ROOM_RES,builder.build());
                }
            }catch (CreateRoomException e) {
                sender.sendError(message,e.errorCode());
            }
        });
    }

    /**
     * 创建房卡房
     * @param message 客户端消息
     * @return 房卡房room实例
     * @throws CreateRoomException 创建失败抛出异常
     */
    public abstract R createCardRoom(InnerClientMessage message) throws CreateRoomException;

    /**
     * 创建金币房
     * @param message 客户端消息
     * @return 金币房room实例
     * @throws CreateRoomException 创建失败抛出异常
     */
    public abstract R createGoldRoom(InnerClientMessage message) throws CreateRoomException;

    @Override
    public void joinRoom(InnerClientMessage message, R room) {
        EventExecutor executor = userExecutorGroup.selectExecutor(message.getUid());
        executor.execute(() -> {
            RLock lock = redissonClient.getLock(ENTER_LOCK + message.getUid());
            boolean locked = lock.tryLock();
            if (!locked) {
                log.info("redisson 获取加入房间锁失败,可能正在加入另一个房间,本次请求忽略");
                return;
            }
            try {
                ServiceInfo info = roomRedisDao.getUserRoomService(message.getUid());
                if (info != null && !info.equals(serviceInfo)) {
                    log.info("玩家已经加入了别的游戏服务器房间{}", info);
                    sender.sendError(message, Constant.ErrorCode.JOINED_OTHER_ROOM);
                    return;
                }
                R old = userRooms.get(message.getUid());
                if (old != null && old != room) {
                    log.info("玩家{}已经加入了别的房间{}", message.getUid(), old.id());
                    sender.sendError(message, Constant.ErrorCode.JOINED_OTHER_ROOM);
                    return;
                }
                P player = room.getPlayer(message.getUid());
                if (!player.isExit() && !player.isOffline()) {
                    log.info("玩家已经在房间中");
                    sender.sendError(message, Constant.ErrorCode.PLAYER_ALREADY_IN_ROOM);
                    return;
                }
                Future<Boolean> future = room.executor().submit(() -> {
                    if (room.isClosed()) {
                        sender.sendError(message, Constant.ErrorCode.ROOM_CLOSED);
                        return false;
                    }
                    return room.join(message);
                });
                try {
                    if (future.get()) {
                        userRooms.put(message.getUid(),room);
                        roomRedisDao.setUserRoomService(message.getUid(),serviceInfo);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.warn("加入房间异常",e);
                }
            }finally {
                lock.unlock();
            }
        });

    }
}
