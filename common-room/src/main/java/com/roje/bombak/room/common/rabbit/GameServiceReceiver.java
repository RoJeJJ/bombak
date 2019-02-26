package com.roje.bombak.room.common.rabbit;

import com.roje.bombak.common.mq.GateToServerMessageReceiver;
import com.roje.bombak.common.processor.Dispatcher;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.manager.RoomManager;
import com.roje.bombak.room.common.player.Player;
import com.roje.bombak.room.common.processor.RoomProcessor;
import com.roje.bombak.room.common.room.GameModule;
import com.roje.bombak.room.common.room.Room;
import lombok.extern.slf4j.Slf4j;

/**
 * @author pc
 */
@Slf4j
public class GameServiceReceiver<P extends Player,R extends Room<P>> extends GateToServerMessageReceiver {

    private final RoomManager<P,R> roomManager;

    private final MessageSender sender;

    public GameServiceReceiver(Dispatcher dispatcher,
                               RoomManager<P,R> roomManager,
                               MessageSender sender) {
        super(dispatcher);
        this.roomManager = roomManager;
        this.sender = sender;
    }

    protected void receiver(byte[] data) {
        ServerMsg.GateToServerMessage message = parseMessage(data);
        if (message == null) {
            return;
        }
        if (message.getMsgType() == RoomConstant.ROOM_SERVICE) {
            process(message);
        } else if (message.getMsgType() == RoomConstant.ROOM_CMD){
            int msgId = message.getMsgId();
            RoomProcessor<P,R> roomProcessor = dispatcher.processor(msgId);
            if (roomProcessor == null) {
                log.warn("不支持的消息,消息号:{}",msgId);
            } else {
                R room = roomManager.getJoinRoom(message.getUserId());
                if (room == null) {
                    log.info("玩家{}请求消息号{},但是玩家并不在房间中",message.getUserId(),msgId);
                    sender.sendErrMsgToGate(message,RoomConstant.ErrorCode.PLAYER_NOT_IN_ROOM);
                } else {
                    room.getExecutor().execute(() -> {
                        if (room.isClosed()) {
                            log.info("房间{}已经关闭,不再处理消息",room.getId());
                            return;
                        }
                        P player = room.getPlayer(message.getUserId());
                        if (player == null) {
                            log.warn("玩家id:{}向房间{}发送请求号:{},但是房间中未找到该用户",
                                    message.getUserId(),room.getId(),msgId);
                        } else {
                            try {
                                roomProcessor.process(room,player,message.getData());
                            } catch (Exception e) {
                                log.warn("处理任务异常",e);
                            }
                        }
                    });
                }
            }
        }
    }
}
