package com.roje.bombak.room.common.manager.impl;

import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.model.User;
import com.roje.bombak.common.redis.dao.UserRedisDao;
import com.roje.bombak.room.common.manager.RoomManager;
import com.roje.bombak.room.common.player.Player;
import com.roje.bombak.room.common.rabbit.RoomInstanceService;
import com.roje.bombak.room.common.redis.RoomRedisDao;
import com.roje.bombak.room.common.room.Room;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/21
 */
@Slf4j
public abstract class BaseRoomManager<P extends Player,R extends Room<P>> implements RoomManager<P,R> {

    private final Map<Long,R> rooms = new ConcurrentHashMap<>();

    private final Map<Long,R> cardRooms = new ConcurrentHashMap<>();

    private final Map<Long,R> goldRooms = new ConcurrentHashMap<>();

    private final Map<Long,R> usersRoom = new ConcurrentHashMap<>();

    private final RoomRedisDao roomRedisDao;

    private final ServiceInfo serviceInfo;

    private final UserRedisDao userRedisDao;

    private final RoomInstanceService roomInstanceService;

    protected BaseRoomManager(RoomRedisDao roomRedisDao,
                              ServiceInfo serviceInfo,
                              UserRedisDao userRedisDao,
                              RoomInstanceService roomInstanceService) {
        this.roomRedisDao = roomRedisDao;
        this.serviceInfo = serviceInfo;
        this.userRedisDao = userRedisDao;
        this.roomInstanceService = roomInstanceService;
    }

    @Override
    public int getRoomSize() {
        return rooms.size();
    }

    @Override
    public R getJoinRoom(long uid) {
        return usersRoom.get(uid);
    }

    @Override
    public R getRoom(long roomId) {
        return rooms.get(roomId);
    }

    @Override
    public void addRoom(R room) {
        if (room.getRoomType() == Room.CARD) {
            cardRooms.put(room.getId(),room);
        } else if (room.getRoomType() == Room.GOLD) {
            goldRooms.put(room.getId(),room);
        }
        rooms.put(room.getId(),room);
        roomRedisDao.setRoomService(room.getId(),serviceInfo);
        roomInstanceService.changeRoomSize(rooms.size());
    }

    @Override
    public void roomClosed(R room) {
        if (room.getRoomType() == Room.CARD) {
            cardRooms.remove(room.getId());
        } else if (room.getRoomType() == Room.GOLD) {
            goldRooms.remove(room.getId());
        }
        rooms.remove(room.getId());
        roomRedisDao.removeRoomService(room.getId());
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
    public void playerJoinedRoom(P player, R room) {
        usersRoom.put(player.getUid(),room);
        roomRedisDao.setUserRoomService(player.getUid(),serviceInfo);
    }

    @Override
    public ServiceInfo getUserGateInfo(long uid) {
        return userRedisDao.getGateInfo(uid);
    }

    @Override
    public User getUser(long uid) {
        return userRedisDao.getUser(uid);
    }

    @Override
    public void playerLeaveRoom(P player, R room) {
        usersRoom.remove(player.getUid());
        roomRedisDao.removeUserRoomService(player.getUid());
    }

    @Override
    public boolean minusGoldIfEnough(P player, int gold) {
        return userRedisDao.minusGoldIfEnough(player.getUid(),gold);
    }


}
