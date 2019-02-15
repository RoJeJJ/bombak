package com.roje.bombak.room.common.room;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/15
 **/
public enum  RoomType {
    /**
     * 房卡房
     */
    card(0),
    /**
     * 金币房
     */
    gold(1),
    ;

    private int code;

    RoomType(int i) {
        this.code = i;
    }

    public int getCode() {
        return code;
    }
}
