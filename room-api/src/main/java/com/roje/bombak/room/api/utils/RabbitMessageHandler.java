package com.roje.bombak.room.api.utils;

import com.roje.bombak.common.dispatcher.CommonProcessor;
import com.roje.bombak.common.dispatcher.Dispatcher;
import com.roje.bombak.common.message.InnerClientMessage;
import com.roje.bombak.room.api.constant.Constant;
import com.roje.bombak.room.api.manager.RoomManager;
import com.roje.bombak.room.api.player.Player;
import com.roje.bombak.room.api.processor.RoomProcessor;
import com.roje.bombak.room.api.room.Room;
import lombok.extern.slf4j.Slf4j;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/23
 **/
@Slf4j
public class RabbitMessageHandler<P extends Player,R extends Room<P>> {

    private final Dispatcher<CommonProcessor> dispatcher;

    private final Dispatcher<RoomProcessor<P,R>> roomMsgDispatcher;

    private final RoomManager<P,R> roomManager;

    private final RoomMessageSender sender;

    public RabbitMessageHandler(Dispatcher<CommonProcessor> dispatcher,
                                Dispatcher<RoomProcessor<P, R>> roomMsgDispatcher,
                                RoomManager<P, R> roomManager, RoomMessageSender sender) {
        this.dispatcher = dispatcher;
        this.roomMsgDispatcher = roomMsgDispatcher;
        this.roomManager = roomManager;
        this.sender = sender;
    }

    public void handle(InnerClientMessage message) {
        if (message.getMessageId() > 0) {
            CommonProcessor processor = dispatcher.processor(message.getMessageId());
            if (processor != null) {
                try {
                    processor.process(message);
                } catch (Exception e) {
                    log.error("处理任务异常",e);
                }
            } else {
                log.info("服务器不支持此消息号:{}",message.getMessageId());
            }
        } else {
            message.setMessageId( - message.getMessageId());
            RoomProcessor<P,R> processor = roomMsgDispatcher.processor(message.getMessageId());
            if (processor == null) {
                log.info("服务器不支持此消息号:{}",message.getMessageId());
                return;
            }
            R room = roomManager.getPlayerRoom(message.getUid());
            if (room == null) {
                log.info("玩家{}请求消息号{},但是玩家并不在房间中",message.getUid(),message.getMessageId());
                sender.sendError(message, Constant.ErrorCode.PLAYER_NOT_IN_ROOM);
                return;
            }
            room.executor().execute(() -> {
                if (room.isClosed()) {
                    log.info("房间{}已经关闭,不再处理消息",room.id());
                    return;
                }
                P p = room.getPlayer(message.getUid());
                if (p == null) {
                    log.warn("id:{}向房间{}发送请求号:{},但是房间中未找到该用户",
                            message.getUid(),room.id(),message.getMessageId());
                    return;
                }
                try {
                    processor.process(room,p,message.getContent());
                } catch (Exception e) {
                    log.error("处理任务异常",e);
                }
            });
        }
    }
}
