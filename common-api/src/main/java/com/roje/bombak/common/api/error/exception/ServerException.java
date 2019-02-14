package com.roje.bombak.common.api.error.exception;

import com.roje.bombak.common.api.error.ErrorMsg;
import lombok.Getter;

/**
 * @author pc
 * @version 1.0
 * @date 2018/12/11
 **/
public class ServerException extends Exception {

    private static final long serialVersionUID = -8133018088821478597L;
    @Getter
    private ErrorMsg errorData;


    public ServerException(ErrorMsg data){
        this.errorData = data;
    }
}
