package com.roje.bombak.nn.config;

import com.roje.bombak.nn.proto.NnMsg;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author pc
 * @version 1.0
 * @date 2018/12/18
 **/
@Slf4j
public class NnSetting {

    /**
     * 明牌抢庄
     */
    public static final int MPQZ = 1;
    /**
     * 自由抢庄
     */
    public static final int ZYQZ = 2;
    /**
     * 牛牛上庄
     */
    public static final int NNSZ = 3;
    /**
     * 固定庄家
     */
    public static final int GDZJ = 4;

    public static final int ROOM_OWNER_START_GAME = 0;

    private static final int ROOM_AUTO_START_MINIMUM_PERSON = 3;

//    /**
//     * 翻倍规则1:牛牛x3 牛九x2 牛八x2
//     */
//    private static final int TIME_RULE_1 = 1;
//
//    /**
//     * 翻倍规则1:牛牛x4 牛九x3 牛八x2 牛七x2
//     */
//    private static final int TIME_RULE_2 = 2;
//
//    /**
//     * 翻倍规则1:牛牛x10 ~ 牛一x1 无牛x1
//     */
//    private static final int TIME_RULE_3 = 3;

    /**
     * 人数
     */
    public final int seatSize;
    /**
     * 玩法
     */
    public final int playWay;

    /**
     * 底分
     */
    public final int baseScore;

    /**
     * 下注最大倍数
     */
    public final int betMultiple;

    /**
     * 最大抢庄倍数
     */
    public final int rushMultiple;

//    /**
//     * 翻倍规则
//     */
//    public final int doublingRule;

    /**
     * 开始方式
     */
    public final int autoStart;

    /**
     * 是否开启顺子牛
     */
    public final boolean szn;

    /**
     * 是否开启同花牛
     */
    public final boolean thn;

    /**
     * 是否开启五花牛
     */
    public final boolean whn;

    /**
     * 是否开启葫芦牛
     */
    public final boolean hln;

    /**
     * 是否开启炸弹牛
     */
    public final boolean zdn;

    /**
     * 是否开启五小牛
     */
    public final boolean wxn;

    /**
     * 是否开启同花顺
     */
    public final boolean ths;

    public final boolean joinHalfWay;

    public final int roundSize;

    public final boolean aa;

    public final NnMsg.RoomSetting setting;

    public NnSetting(NnMsg.RoomSetting setting, boolean cardRoom, NnProperties nnProp) {
        NnMsg.RoomSetting.Builder builder = setting.toBuilder();
        int person = setting.getSeatSize();
        if (person >= nnProp.getRoomMaxGamer() && person <= nnProp.getRoomMinGamer()) {
            seatSize = person;
        } else {
            log.warn("人数设置错误:{},设置为默认人数:{}",person,nnProp.getRoomMinGamer());
            seatSize = nnProp.getRoomMinGamer();
        }
        builder.setSeatSize(seatSize);
        int way = setting.getMethod();
        if (way == MPQZ || way == ZYQZ || way == NNSZ || way == GDZJ) {
            playWay = way;
        } else {
            log.warn("玩法设置错误:{},设置为默认玩法:{}",way,MPQZ);
            playWay = MPQZ;
        }
        builder.setMethod(way);
        int base = setting.getBaseScore();
        int index = Arrays.binarySearch(nnProp.getBaseScore(),base);
        if (index >= 0 && index < nnProp.getBaseScore().length) {
            baseScore = base;
        } else {
            log.warn("底分设置错误:{},设置为默认底分:{}",base,nnProp.getBaseScore()[0]);
            baseScore = nnProp.getBaseScore()[0];
        }
        builder.setBaseScore(baseScore);
        int betMultiple = setting.getBetMultiple();
        if (betMultiple < 1){
            log.warn("下注最大倍数设置错误:{},设置为默认倍数:{}",betMultiple,1);
            betMultiple = 1;
        } else if (betMultiple > nnProp.getMultiBetLimit()) {
            log.warn("下注最大倍数设置错误:{},设置为默认倍数:{}",betMultiple,nnProp.getMultiBetLimit());
            betMultiple = nnProp.getMultiBetLimit();
        }
        this.betMultiple = betMultiple;
        builder.setBetMultiple(betMultiple);
        if (playWay == MPQZ ) {
            int rushMultiple = setting.getRushMultiple();
            if (rushMultiple < 1) {
                log.warn("抢庄倍数设置错误:{},设置默认抢庄倍数:{}",rushMultiple,1);
                rushMultiple = 1;
            } else if (rushMultiple > nnProp.getMultiRushLimit()) {
                log.warn("抢庄倍数设置错误:{},设置默认抢庄倍数:{}",rushMultiple,nnProp.getMultiRushLimit());
                rushMultiple = nnProp.getMultiRushLimit();
            }
            this.rushMultiple = rushMultiple;
        } else {
            this.rushMultiple = 0;
        }
        builder.setRushMultiple(this.rushMultiple);
//        int timeRule = setting.getDoublingRule();
//        if (timeRule == TIME_RULE_1 || timeRule == TIME_RULE_2 || timeRule == TIME_RULE_3) {
//            this.doublingRule = timeRule;
//        } else {
//            log.warn("翻倍规则设置错误:{},重置为默认规则:{}",timeRule,TIME_RULE_1);
//            this.doublingRule = TIME_RULE_1;
//        }
//        builder.setDoublingRule(this.doublingRule);
        szn = setting.getSzn();
        thn = setting.getThn();
        whn = setting.getWhn();
        hln = setting.getHln();
        zdn = setting.getZdn();
        wxn = setting.getWxn();
        ths = setting.getThs();

        joinHalfWay = setting.getJoinHalfWay();

        this.aa = setting.getAa();

        int start = setting.getStartMode();
        if (start != ROOM_OWNER_START_GAME && (start < ROOM_AUTO_START_MINIMUM_PERSON || start > seatSize)) {
            int defaultValue = 0;
            if (start > seatSize) {
                defaultValue = seatSize;
            }
            log.warn("自动开始设置错误:{},重置为默认规则:{}",start,defaultValue);
            start = defaultValue;
        }
        this.autoStart = start;
        builder.setStartMode(this.autoStart);
        int round = setting.getRoundSize();
        if (cardRoom) {
            if (!nnProp.getRoundFee().containsKey(round)) {
                int defRound = Collections.min(nnProp.getRoundFee().keySet());
                log.warn("局数设置错误:{},设置为默认局数{}",round, defRound);
                round = defRound;
            }
        }
        this.roundSize = round;
        builder.setRoundSize(this.roundSize);
        this.setting = builder.build();
    }
}
