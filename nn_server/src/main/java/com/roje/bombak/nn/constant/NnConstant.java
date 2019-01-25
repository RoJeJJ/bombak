package com.roje.bombak.nn.constant;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/4
 **/
public interface NnConstant {

    interface Cmd{

        int RUSH_REQ = 5005;

        int RUSH_RES = 5006;

        int BET_REQ = 5007;

        int BET_RES = 5008;

        int START_GAME_REQ = 5009;

        int START_GAME_RES = 5010;

        int CHECK_REQ = 5013;

        int CHECK_RES = 5014;
    }

    interface ErrorCode {

        int PROHIBIT_JOIN_HALF = 45001;

        int NO_EMPTY_SEAT = 45002;

    }

    /**
     * 服务器通知消息
     */
    interface Indicate {

        int BET = 5400;

        int RUSH = 5401;

        int DEAL = 5402;

        int BANKER = 5403;

        int CARD_CHECK = 5404;

        int HAND_CARD = 5405;
    }
}
