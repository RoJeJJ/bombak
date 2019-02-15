package com.roje.bombak.room.common.constant;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/8
 **/
public interface RoomConstant {

    interface Cmd {

        //房间外的请求

        int CREATE_CARD_ROOM_REQ = 3001;

        int CREATE_GOLD_ROOM_REQ = 3002;

        int CREATE_ROOM_RES = 3003;

        int JOIN_ROOM_REQ = 3004;

        int JOIN_ROOM_RES = 3005;

        //房间中的请求

        int ROOM_CMD = 3100;

        int DISBAND_CARD_ROOM_REQ = 3101;

        int DISBAND_CARD_ROOM_RES = 3102;

        int DISBAND_CARD_ROOM_VOTE_REQ = 3103;

        int DISBAND_CARD_ROOM_VOTE_RES = 3104;

        int EXIT_ROOM_REQ = 3105;

        int EXIT_ROOM_RES = 3106;

        int SIT_DOWN_REQ = 3107;

        int SIT_DOWN_RES = 3108;

        int GET_UP_REQ = 3109;

        int GET_UP_RES = 3110;
    }

    interface RedisConstant {

        String ROOM = "room_redis";

        String USER_ROOM = "user_room_redis";

        String ROOM_NO_REDIS = "room_no";
    }

    interface Notice {

        //房间通知

        int REJOIN = 3201;

        int PLAYER_OFFLINE = 3202;

        int USER_ROOM = 3203;

        int ROOM_CLOSE = 3204;

        int PLAYER_EXIT = 3205;
    }

    interface ErrorCode {

        //房间通用错误码

        int ROOM_TYPE_IS_EMPTY = 43001;

        int NO_SUCH_GAME = 43002;

        int ROOM_NOT_FOUND = 43003;

        int SERVER_ROOM_FULL = 43004;

        int ROOM_CLOSED = 43005;

        int PLAYER_NOT_IN_ROOM = 43006;

        int JOINED_OTHER_ROOM = 43007;

        int PLAYER_ALREADY_IN_ROOM = 43008;

        int ROOM_FULL = 43009;
    }
}
