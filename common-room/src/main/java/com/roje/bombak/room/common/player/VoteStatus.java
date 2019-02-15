package com.roje.bombak.room.common.player;

/**
 * 玩家投票解散状态
 * @author pc
 * @version 1.0
 * @date 2019/2/15
 **/
public enum VoteStatus {
    /**
     * 默认
     */
    def(0),
    /**
     * 等待
     */
    wait(1),
    /**
     * 同意
     */
    agree(2),
    /**
     * 拒绝
     */
    refuse(3);

    private int code;

    VoteStatus(int i) {
        this.code = i;
    }

    public int getCode() {
        return code;
    }
}
