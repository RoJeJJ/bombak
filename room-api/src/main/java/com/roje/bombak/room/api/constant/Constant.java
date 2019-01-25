package com.roje.bombak.room.api.constant;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/8
 **/
public interface Constant {

    interface Cmd {

        int CREATE_CARD_ROOM_REQ = 3001;

        int CREATE_CARD_ROOM_RES = 3002;

        int JOIN_CARD_ROOM_REQ = 3003;

        int JOIN_ROOM_RES = 3005;

        int DISBAND_CARD_ROOM_REQ = 3006;

        int DISBAND_CARD_ROOM_RES = 3007;

        int DISBAND_CARD_ROOM_VOTE_REQ = 3008;

        int DISBAND_CARD_ROOM_VOTE_RES = 3009;

        int EXIT_ROOM_REQ = 3005;

        int EXIT_ROOM_RES = 3006;

        int SIT_DOWN_REQ = 3009;

        int SIT_DOWN_RES = 3010;

        int GET_UP_REQ = 3011;

        int GET_UP_RES = 3012;

        int ENTER_GOLD_ROOM_REQ = 3013;

        int ENTER_GOLD_ROOM_RES = 3014;

        int CREATE_GOLD_ROOM_REQ = 3015;

        int CREATE_GOLD_ROOM_RES = 3016;
    }

    interface RedisConstant {

        String ROOM = "room_redis";

        String USER_ROOM = "user_room_redis";

        String ROOM_NO_REDIS = "room_no";
    }

    interface Indicate{

        int PLAYER_ONLINE = 3401;

        int PLAYER_OFFLINE = 3402;

        int USER_ROOM = 3403;

        int ROOM_CLOSE = 3404;

        int PLAYER_EXIT = 3405;
    }

    interface ErrorCode {

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
