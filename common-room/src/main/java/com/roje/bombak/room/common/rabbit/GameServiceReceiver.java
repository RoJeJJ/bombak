package com.roje.bombak.room.common.rabbit;

import com.roje.bombak.common.mq.ForwardClientMessageReceiver;
import com.roje.bombak.common.processor.Dispatcher;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.manager.RoomManager;
import com.roje.bombak.room.common.player.Player;
import com.roje.bombak.room.common.processor.RoomProcessor;
import com.roje.bombak.room.common.room.Room;
import lombok.extern.slf4j.Slf4j;

/**
 * @author pc
 */
@Slf4j
public class GameServiceReceiver<P extends Player,R extends Room<P>> extends ForwardClientMessageReceiver {

    private final RoomManager<P,R> roomManager;

    private final MessageSender sender;

    public GameServiceReceiver(Dispatcher dispatcher,
                               RoomManager<P, R> roomManager,
                               MessageSender sender) {
        super(dispatcher);
        this.roomManager = roomManager;
        this.sender = sender;
    }

    protected void receiver(byte[] data) {
        ServerMsg.ForwardClientMessage message = parseClientMessage(data);
        if (message == null) {
            return;
        }
        int messageId = message.getCsMessage().getMessageId();
        if (messageId != RoomConstant.Cmd.ROOM_CMD) {
            process(message);
        } else {
            int roomMsgId = message.getCsMessage().getSecondMsgId();
            RoomProcessor<P,R> roomProcessor = dispatcher.processor(roomMsgId);
            if (roomProcessor == null) {
                log.warn("不支持的消息,消息号:{}",roomMsgId);
            } else {
                R room = roomManager.getPlayerRoom(message.getUid());
                if (room == null) {
                    log.info("玩家{}请求消息号{},但是玩家并不在房间中",message.getUid(),roomMsgId);
                    sender.replyClientErrMsg(message,RoomConstant.ErrorCode.PLAYER_NOT_IN_ROOM);
                } else {
                    room.executor().execute(() -> {
                        if (room.isClosed()) {
                            log.info("房间{}已经关闭,不再处理消息",room.id());
                            return;
                        }
                        P player = room.getPlayer(message.getUid());
                        if (player == null) {
                            log.warn("玩家id:{}向房间{}发送请求号:{},但是房间中未找到该用户",
                                    message.getUid(),room.id(),roomMsgId);
                        } else {
                            try {
                                roomProcessor.process(room,player,message.getCsMessage().getData());
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
