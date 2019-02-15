package com.roje.bombak.nn.player;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/15
 **/
public enum  RushStatus {
    /**
     * 默认
     */
    def(0),
    /**
     * 等待抢庄
     */
    wait(1),
    /**
     * 抢庄
     */
    rush(2),
    /**
     * 不抢
     */
    not_rush(3);

    private int code;

    RushStatus(int i) {
        this.code = i;
    }

    public int getCode() {
        return code;
    }
}
