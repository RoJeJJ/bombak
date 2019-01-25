package com.roje.bombak.room.api.processor;

import com.roje.bombak.room.api.player.Player;
import com.roje.bombak.room.api.room.Room;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/9
 **/
public interface RoomProcessor<P extends Player, R extends Room> {
    /**
     * 房间处理器
     * @param player 玩家
     * @param room 房间
     * @param data 附加数据
     * @throws Exception 处理异常
     */
    void process(R room, P player,byte[] data) throws Exception;
}
