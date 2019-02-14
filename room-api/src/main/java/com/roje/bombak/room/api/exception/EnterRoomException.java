package com.roje.bombak.room.api.exception;

import com.roje.bombak.common.api.exception.SystemException;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/21
 **/
public class EnterRoomException extends SystemException {

    private static final long serialVersionUID = 6685390449449159904L;

    public EnterRoomException(int errorCode) {
        super(errorCode);
    }
}
