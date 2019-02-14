package com.roje.bombak.common.api.exception;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/21
 **/
public class SystemException extends Exception {

    private static final long serialVersionUID = -6409428245507516126L;

    private final int errorCode;

    public SystemException(int errorCode) {
        this.errorCode = errorCode;
    }

    public int errorCode() {
        return errorCode;
    }
}
