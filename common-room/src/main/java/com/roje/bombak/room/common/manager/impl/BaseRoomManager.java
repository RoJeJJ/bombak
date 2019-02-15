package com.roje.bombak.room.common.manager.impl;

import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.room.common.config.RoomProperties;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.exception.CreateRoomException;
import com.roje.bombak.room.common.executor.JoinRoomExecutorGroup;
import com.roje.bombak.room.common.executor.RoomCreateExecutorGroup;
import com.roje.bombak.room.common.manager.RoomManager;
import com.roje.bombak.room.common.player.Player;
import com.roje.bombak.room.common.proto.RoomMsg;
import com.roje.bombak.room.common.redis.RoomRedisDao;
import com.roje.bombak.room.common.room.Room;
import com.roje.bombak.room.common.room.RoomType;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/21
 */
@Slf4j
public abstract class BaseRoomManager<P extends Player, R extends Room<P>> implements RoomManager<P, R> {

    private static final String JOIN_ROOM_LOCK = "join_room_lock-";

    private final Map<Long,R> rooms = new ConcurrentHashMap<>();

    private final Map<Long,R> cardRooms = new ConcurrentHashMap<>();

    private final Map<Long,R> goldRooms = new ConcurrentHashMap<>();

    private final Map<Long,R> usersRoom = new ConcurrentHashMap<>();

    private final RoomCreateExecutorGroup roomCreateExecutorGroup;

    private final JoinRoomExecutorGroup joinRoomExecutorGroup;

    private final RoomProperties roomProperties;

    private final MessageSender sender;

    private final RoomRedisDao roomRedisDao;

    private final ServiceInfo serviceInfo;

    private final RedissonClient redissonClient;

    protected BaseRoomManager(RoomProperties roomProperties, MessageSender sender,
                              RoomRedisDao roomRedisDao, ServiceInfo serviceInfo,
                              RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        this.roomCreateExecutorGroup = new RoomCreateExecutorGroup();
        this.joinRoomExecutorGroup = new JoinRoomExecutorGroup(roomProperties.getUserExecutorSize());
        this.roomProperties = roomProperties;
        this.sender = sender;
        this.roomRedisDao = roomRedisDao;
        this.serviceInfo = serviceInfo;
    }

    @Override
    public R getPlayerRoom(long uid) {
        return usersRoom.get(uid);
    }

    @Override
    public void createRoom(ServerMsg.ForwardClientMessage message) {
        roomCreateExecutorGroup.executor().execute(() -> {
            if (rooms.size() >= roomProperties.getMaxRoomSize()) {
                log.info("房间满了,请稍后再试");
                sender.replyClientErrMsg(message, RoomConstant.ErrorCode.SERVER_ROOM_FULL);
            } else {
                try {
                    R room = createRoom0(message);
                    if (room != null) {
                        if (room.roomType() == RoomType.card ) {
                            cardRooms.put(room.id(),room);
                        } else if (room.roomType() == RoomType.gold) {
                            goldRooms.put(room.id(),room);
                        }
                        rooms.put(room.id(),room);
                        roomRedisDao.setRoomService(room.id(),serviceInfo);
                        RoomMsg.CreateRoomRes.Builder builder = RoomMsg.CreateRoomRes.newBuilder();
                        builder.setRoomId(room.id());
                        sender.replyClientMsg(message,RoomConstant.Cmd.CREATE_ROOM_RES,builder.build());
                    }
                } catch (CreateRoomException e) {
                    sender.replyClientErrMsg(message,e.errorCode());
                }
            }
        });
    }

    @Override
    public void joinRoom(ServerMsg.ForwardClientMessage message) throws Exception {
        RoomMsg.JoinRoomReq joinRoomReq = message.getCsMessage().getData().unpack(RoomMsg.JoinRoomReq.class);
        R room = rooms.get(joinRoomReq.getRoomId());
        if (room == null) {
            log.info("没有找到该房间{}", joinRoomReq.getRoomId());
            sender.replyClientErrMsg(message,  RoomConstant.ErrorCode.ROOM_NOT_FOUND);
            return;
        }
        long uid = message.getUid();
        EventExecutor executor = joinRoomExecutorGroup.selectExecutor(uid);
        executor.execute(() -> {
            RLock lock = redissonClient.getLock(JOIN_ROOM_LOCK + uid);
            boolean locked = lock.tryLock();
            if (!locked) {
                log.info("redisson 获取加入房间锁失败,可能正在加入另一个房间,本次请求忽略");
                return;
            }
            try {
                ServiceInfo info = roomRedisDao.getUserRoomService(uid);
                if (info != null && !info.equals(serviceInfo)) {
                    log.info("玩家已经加入了别的游戏服务器房间{}", info);
                    sender.replyClientErrMsg(message, RoomConstant.ErrorCode.JOINED_OTHER_ROOM);
                    return;
                }
                R old = usersRoom.get(uid);
                if (old != null && old != room) {
                    log.info("玩家{}已经加入了别的房间{}", message.getUid(), old.id());
                    sender.replyClientErrMsg(message,  RoomConstant.ErrorCode.JOINED_OTHER_ROOM);
                    return;
                }
                P player = room.getPlayer(uid);
                if (player != null && !player.isExit() && !player.isOffline()) {
                    log.info("玩家已经在房间中");
                    sender.replyClientErrMsg(message,  RoomConstant.ErrorCode.PLAYER_ALREADY_IN_ROOM);
                    return;
                }
                boolean newJoin = true;
                if (player == null) {
                    newJoin = false;
                    player = newPlayer(message);
                }
                P joinPlayer = player;
                Future<Boolean> future = room.executor().submit(() -> {
                    if (room.isClosed()) {
                        log.info("房间已经关闭");
                        sender.replyClientErrMsg(message,  RoomConstant.ErrorCode.ROOM_CLOSED);
                        return false;
                    } else {
                        return room.requestJoin(joinPlayer);
                    }
                });
                if (future.get() && newJoin) {
                    usersRoom.put(uid,room);
                    roomRedisDao.setUserRoomService(uid,serviceInfo);
                }
            }catch (Exception e) {
                log.warn("加入房间异常",e);
            }
            finally {
                lock.unlock();
            }
        });
    }

    @Override
    public void roomClosed(R room) {
        if (room.roomType() == RoomType.card) {
            cardRooms.remove(room.id());
        } else if (room.roomType() == RoomType.gold) {
            goldRooms.remove(room.id());
        }
        rooms.remove(room.id());
        roomRedisDao.removeRoomService(room.id());
        Iterator<Map.Entry<Long,R>> it = usersRoom.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long,R> entry = it.next();
            if (entry.getValue() == room) {
                it.remove();
                roomRedisDao.removeUserRoomService(entry.getKey());
            }
        }
    }

    @Override
    public void leaveRoom(P player, R room) {
        usersRoom.remove(player.uid());
        roomRedisDao.removeUserRoomService(player.uid());
    }

    /**
     * 创建房间,返回相应的房间实例
     * @param message 客户端消息
     * @return 房间对象
     * @throws CreateRoomException 创建失败抛出异常
     */
    protected abstract R createRoom0(ServerMsg.ForwardClientMessage message) throws CreateRoomException;

    /**
     * 生成新玩家对象
     * @param message 客户端消息
     * @return 玩家对象
     */
    protected abstract P newPlayer(ServerMsg.ForwardClientMessage message);
}
