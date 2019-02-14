package com.roje.bombak.room.api.exception;

import com.roje.bombak.common.api.exception.SystemException;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/21
 **/
public class CreateRoomException extends SystemException {

    private static final long serialVersionUID = 5643345299311030843L;

    public CreateRoomException(int errorCode) {
        super(errorCode);
    }
}
