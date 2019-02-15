package com.roje.bombak.nn.player;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/15
 **/
public enum  CheckStatus {
    /**
     * 默认
     */
    def(0),
    /**
     * 等待
     */
    wait(1),
    /**
     * 完成
     */
    checked(2);

    private int code;

    CheckStatus(int i) {
        this.code = i;
    }

    public int getCode() {
        return code;
    }
}
