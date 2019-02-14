package com.roje.bombak.gate.constant;

/**
 * @author pc
 * @version 1.0
 * @date 2018/12/27
 **/
public interface GateConstant {

    interface Cmd {

        int LOGIN_REQ = 1001;

        int LOGIN_RES = 1002;

        int HEART_BEAT_REQ = 1003;

        int HEART_BEAT_RES = 1004;
    }

    //错误码

    interface ErrorCode {
        int EMPTY_TOKEN = 41001;

        int USER_NOT_FOUND = 41002;

        int INVALID_TOKEN = 41003;

        int LOGIN_ANOTHER_GATE = 41004;

        int NOT_LOGIN = 41005;

        int LOGIN_REPEAT = 41006;

        int SERIAL_NUMBER_ERROR = 41007;

        int SERVICE_NOT_AVAILABLE = 41008;
    }
}
