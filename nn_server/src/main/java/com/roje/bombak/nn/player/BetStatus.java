package com.roje.bombak.nn.player;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/15
 **/
public enum  BetStatus {
    /**
     * 默认
     */
    def(0),
    /**
     * 等待下注
     */
    wait(1),
    /**
     * 已经下注
     */
    beted(2);

    private int code;

    private int multiple;

    BetStatus(int i) {
        this.code = i;
    }

    public int getCode() {
        return code;
    }

    public void setMultiple(int multiple) {
        this.multiple = multiple;
    }

    public int getMultiple() {
        return multiple;
    }
}
