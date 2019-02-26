package com.roje.bombak.room.common.exception;

import com.roje.bombak.common.exception.SystemException;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/21
 **/
public class JoinRoomException extends SystemException {

    private static final long serialVersionUID = 5643345299311030843L;

    public JoinRoomException(int errorCode) {
        super(errorCode);
    }
}
