package com.roje.bombak.tcb.player;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/26
 **/
public enum  BetStatus {
    /**
     * 默认状态
     */
    def(0),
    /**
     * 等待下注状态
     *
     */
    wait(1),
    /**
     * 已经下注状态
     */
    beted(2),
    /**
     * 弃牌状态
     */
    fold(3);

    private int code;

    public int getCode() {
        return code;
    }

    BetStatus(int i) {
        this.code = i;
    }
}

