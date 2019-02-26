package com.roje.bombak.room.common.processor;

import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.processor.GateToServerMessageProcessor;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.common.thread.NamedThreadFactory;
import com.roje.bombak.room.common.config.RoomProperties;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.exception.CreateRoomException;
import com.roje.bombak.room.common.executor.RoomCreateExecutor;
import com.roje.bombak.room.common.manager.RoomManager;
import com.roje.bombak.room.common.player.Player;
import com.roje.bombak.room.common.proto.RoomMsg;
import com.roje.bombak.room.common.room.Room;
import com.roje.bombak.room.common.utils.RoomMessageSender;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/19
 **/
@Slf4j
public class CreateRoomProcessor<P extends Player,R extends Room<P>> implements GateToServerMessageProcessor {

    private final RoomManager<P,R> roomManager;

    private final RoomCreateExecutor roomCreateExecutor;

    private final RoomProperties roomProperties;

    private final RoomMessageSender sender;

    private final EventExecutorGroup roomExecutorGroup;

    private final ServiceInfo serviceInfo;

    public CreateRoomProcessor(RoomManager<P, R> roomManager,
                               RoomCreateExecutor roomCreateExecutor,
                               RoomProperties roomProperties,
                               RoomMessageSender sender,
                               ServiceInfo serviceInfo) {
        this.roomManager = roomManager;
        this.roomCreateExecutor = roomCreateExecutor;
        this.roomProperties = roomProperties;
        this.sender = sender;
        roomExecutorGroup = new DefaultEventExecutorGroup(roomProperties.getRoomExecutorSize(),new NamedThreadFactory("room"));
        this.serviceInfo = serviceInfo;
    }

    @Override
    public void process(ServerMsg.GateToServerMessage message) {
        roomCreateExecutor.executor().execute(() -> {
            if (roomManager.getRoomSize() >= roomProperties.getMaxRoomSize()) {
                log.info("服务器房间满了");
                sender.sendErrMsgToGate(message, RoomConstant.ErrorCode.SERVER_ROOM_FULL);
                return;
            }
            try {
                R room = roomManager.createRoom(message.getUserId(),message.getData());
                if (room != null) {
                    room.setOwnerId(message.getUserId());
                    room.setExecutor(roomExecutorGroup.next());
                    room.setSender(sender);
                    room.setGameType(serviceInfo.getServiceType());
                    room.setCapacity(roomProperties.getRoomMaxPlayers());
                    room.setWaitVoteTime(roomProperties.getVoteSecondTime() * 1000);
                    roomManager.addRoom(room);
                    RoomMsg.CreateRoomRes.Builder builder = RoomMsg.CreateRoomRes.newBuilder();
                    builder.setRoomId(room.getId());
                    sender.sendMsgToGate(message,RoomConstant.Cmd.CREATE_ROOM_RES,builder.build());
                }
            } catch (CreateRoomException e) {
                sender.sendErrMsgToGate(message,e.errorCode());
            }
        });
    }
}
