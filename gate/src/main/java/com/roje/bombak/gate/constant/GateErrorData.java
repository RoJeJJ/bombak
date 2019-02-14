package com.roje.bombak.gate.constant;

import com.roje.bombak.common.api.error.ErrorData;

/**
 * @author pc
 * @version 1.0
 * @date 2018/12/28
 **/
public enum  GateErrorData implements ErrorData {
    /**
     * 无效的token
     */
    invalid_token((short)401,"无效的token"),
    ;

    private short code;

    private String msg;

    GateErrorData(short i, String msg) {
        this.code = i;
        this.msg = msg;
    }

    @Override
    public short errorCode() {
        return code;
    }

    @Override
    public String errorMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "error{code:"+code+",msg:"+msg+"}";
    }
}
