package com.roje.bombak.nn.room;

import com.google.protobuf.Message;
import com.roje.bombak.nn.config.NnSetting;
import com.roje.bombak.nn.constant.NnConstant;
import com.roje.bombak.nn.player.BetStatus;
import com.roje.bombak.nn.player.CheckStatus;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.player.RushStatus;
import com.roje.bombak.nn.proto.NnMsg;
import com.roje.bombak.room.common.manager.RoomManager;
import com.roje.bombak.room.common.room.BaseRoom;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author pc
 * @version 1.0
 * @date 2018/12/18
 **/
@Slf4j
public class NnRoom extends BaseRoom<NnPlayer> {

    private static final int POKER_NUM = 52;

    private static final int MPQZ_4 = 4;

    private final NnSetting setting;

    private List<Integer> pokers;

    private List<NnPlayer> rushGamer;

    private int currentMaxRush;

    private NnPlayer banker;

    private ScheduledFuture rushFuture;

    private ScheduledFuture betFuture;

    private ScheduledFuture checkFuture;

    private long waitRushTime;

    private long waitCheckTime;

    private long waitBetTime;

//    public NnRoom(long id, long ownerId, String name, String gameType, RoomType type,
//                     int maxPlayer, NnSetting setting, EventExecutor executor,
//                     RoomMessageSender sender, RoomListener<NnPlayer, Room<NnPlayer>> listener, NnProperties nnProperties) {
//        super(id, ownerId, name, gameType, type, setting.seatSize, maxPlayer, executor, sender, listener);
//        this.config = setting;
//        this.nnProperties = nnProperties;
//        rushGamer = new ArrayList<>();
//        pokers = new ArrayList<>();
//    }

    public NnRoom(long id, RoomManager manager, NnSetting setting){
        super(id,manager);
        this.setting = setting;
        pokers = new ArrayList<>();
        rushGamer = new ArrayList<>();
    }

    public NnSetting getSetting() {
        return setting;
    }

    public void setWaitBetTime(long waitBetTime) {
        this.waitBetTime = waitBetTime;
    }

    public void setWaitCheckTime(long waitCheckTime) {
        this.waitCheckTime = waitCheckTime;
    }

    public void setWaitRushTime(long waitRushTime) {
        this.waitRushTime = waitRushTime;
    }

    @Override
    protected void onClosed() {
        endRush();
        endBet();
        endCheck();
    }

    @Override
    protected void onStartGame() {
        pokers.clear();
        for (int i = 0; i < POKER_NUM; i++) {
            pokers.add(i);
        }
        Collections.shuffle(pokers);
        rushGamer.clear();
        currentMaxRush = 0;
        rushGamer.clear();
        //通知房间中所有人游戏开始
        getExecutor().schedule(() -> {
            if (isClosed()) {
                return;
            }
            switch (setting.playWay) {
                case NnSetting.MPQZ:
                    log.info("开始游戏,模式:明牌抢庄");
                    deal(4);
                    break;
                case NnSetting.ZYQZ:
                    log.info("开始游戏,模式:自由抢庄");
                    rush();
                    break;
                case NnSetting.NNSZ:
                    log.info("开始游戏,模式:牛牛上庄");
                    determineBanker();
                    break;
                default:
                    break;
            }
        }, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onNoticePlayer(NnPlayer player) {
        noticeRush(player);
        noticeBet(player);
        noticeCheck(player);
    }

    /**
     * 抢庄初始化
     */
    private void initRush() {
        for (NnPlayer p : gamers) {
            p.setRushStatus(RushStatus.wait);
        }
    }

    private void endRush() {
        for (NnPlayer p:gamers) {
            p.setRushStatus(RushStatus.def);
        }
        if (rushFuture != null) {
            rushFuture.cancel(false);
            rushFuture = null;
        }
    }

    private void endBet() {
        for (NnPlayer p:gamers) {
            p.setBetStatus(BetStatus.def);
        }
        if (betFuture != null) {
            betFuture.cancel(false);
            betFuture = null;
        }
    }

    private void initBet() {
        for (NnPlayer p : gamers) {
            p.setBetStatus(BetStatus.wait);
        }
    }

    public void rush(NnPlayer p, int mul) {
        if (p.getRushStatus() != RushStatus.wait) {
            return;
        }
        if (mul > setting.rushMultiple) {
            log.info("抢庄倍数设置错误:{}",mul);
            mul = setting.rushMultiple;
        } else if (mul < 0) {
            log.info("抢庄倍数设置错误:{}",mul);
            mul = 0;
        }

        if (mul > currentMaxRush) {
            rushGamer.clear();
            rushGamer.add(p);
        } else if (mul == currentMaxRush) {
            rushGamer.add(p);
        }
        if (mul == 0) {
            p.setRushStatus(RushStatus.not_rush);
        } else {
            p.setRushStatus(RushStatus.rush);
        }
        p.setRushTime(mul);
        log.info("玩家{}抢庄,倍数{}",p.getUid(),mul);
        NnMsg.RushRes.Builder builder = NnMsg.RushRes.newBuilder();
        builder.setMul(mul);
        builder.setUid(p.getUid());
        sender.sendMsgToGate(players.values(), NnConstant.Cmd.RUSH_RES,builder.build());

        checkRush();
    }

    private void checkRush() {
        boolean rush = true;
        for (NnPlayer p : gamers) {
            if (p.getRushStatus() == RushStatus.wait) {
                rush = false;
                break;
            }
        }
        if (rush) {
            endRush();
            determineBanker();
        }
    }

    public void bet(NnPlayer p, int bet) {
        if (bet > setting.betMultiple) {
            log.info("下注倍数设置错误:{}",bet);
            bet = setting.betMultiple;
        } else if (bet < 1) {
            log.info("下注倍数设置错误:{}",bet);
            bet = 1;
        }
        p.setBetScore(bet * setting.baseScore);
        p.setBetStatus(BetStatus.beted);

        log.info("玩家{}下注倍数{}",p.getUid(),bet);
        NnMsg.BetRes.Builder builder = NnMsg.BetRes.newBuilder();
        builder.setBet(p.getBetScore());
        builder.setUid(p.getUid());
        sender.sendMsgToGate(players.values(),NnConstant.Cmd.BET_RES,builder.build());

        checkBet();
    }

    private void checkBet() {
        boolean bet = true;
        for (NnPlayer p : gamers) {
            if (p != banker && p.getBetStatus() == BetStatus.wait) {
                bet = false;
            }
        }
        //所有人下好注了
        if (bet) {
            endBet();
            if (setting.playWay == NnSetting.MPQZ) {
                deal(1);
            } else {
                deal(5);
            }
        }
    }

    private void checkPoker() {
        checkFuture = getExecutor().schedule(this::checkTimeout, waitCheckTime, TimeUnit.SECONDS);
        noticeCheck(null);
    }

    private void allCheck() {
        boolean check = true;
        for (NnPlayer p : gamers) {
            if (p.getCheckStatus() == CheckStatus.wait) {
                check = false;
                break;
            }
        }
        if (check) {
            endCheck();
            startCompare();
        }
    }

    private void endCheck() {
        for (NnPlayer p:gamers) {
            if (p.getCheckStatus() != CheckStatus.def) {
                p.setCheckStatus(CheckStatus.def);
            }
        }
        if (checkFuture != null) {
            checkFuture.cancel(false);
            checkFuture = null;
        }
    }

    private void startCompare() {
        for (NnPlayer player : gamers) {
            player.checkHand(setting);
        }

        showHandAndPoint(banker, NnResult.def,0);

        NnPlayer player = getNextPlayer(banker.getSeat());
        comparePlayer(player);
    }

    private void showHandAndPoint(NnPlayer player, NnResult result,int score) {
        NnMsg.HandCard.Builder builder = NnMsg.HandCard.newBuilder();
        builder.addAllHands(player.hands());
        builder.setUid(player.getUid());
        builder.setNiu(player.getNiu());
        builder.setResult(result.ordinal());
        builder.setScore(score);
        sender.sendMsgToGate(players.values(), NnConstant.Notice.HAND_CARD,builder.build());
    }

    private void comparePlayer(NnPlayer player) {
        if (player == banker) {
            log.info("比牌结束");
        } else {
            if (player.getNiu() > banker.getNiu()) {
                calcScore(player,true);
            } else if (player.getNiu() < banker.getNiu()) {
                calcScore(player,false);
            } else if (player.getMaxCard() > banker.getMaxCard()) {
                calcScore(player,true);
            } else {
                calcScore(player,false);
            }
            getExecutor().schedule(() -> {
                if (isClosed()) {
                    return;
                }
                NnPlayer player1 = getNextPlayer(banker.getSeat());
                comparePlayer(player1);
            },2000,TimeUnit.MILLISECONDS);
        }
    }

    private void calcScore(NnPlayer player,boolean win) {
        if (win) {
            if (setting.roomType == CARD) {
                int score = currentMaxRush * player.getBetScore() * player.getNiu();
                player.addScore(score);
                banker.subScore(score);
                showHandAndPoint(player, NnResult.win,score);
            }
        } else {
            if (setting.roomType == GOLD) {
                int score = currentMaxRush * player.getBetScore() * player.getNiu();
                player.subScore(score);
                banker.addScore(score);
                showHandAndPoint(player, NnResult.lose,score);
            }
        }
    }

    private NnPlayer getNextPlayer(int seat) {
        int next = seat + 1;
        if (next > setting.seatSize) {
            next = 0;
        }
        NnPlayer player = getSeat(next);
        if (player == null || !player.isInGame()) {
            return getNextPlayer(next);
        }
        return player;
    }

    /**
     * 通知玩家开始抢庄
     */
    private void noticeRush(NnPlayer p) {
        if (rushFuture == null) {
            return;
        }
        // 通知玩家开始抢庄
        NnMsg.NoticeRush.Builder builder = NnMsg.NoticeRush.newBuilder();
        builder.setTime(rushFuture.getDelay(TimeUnit.MILLISECONDS));
        for (NnPlayer np:gamers) {
            NnMsg.RushData.Builder rb = NnMsg.RushData.newBuilder();
            rb.setUid(np.getUid());
            rb.setStatus(np.getRushStatus().getCode());
            builder.addRushData(rb);
        }
        if (p == null) {
            sender.sendMsgToGate(players.values(), NnConstant.Notice.RUSH, builder.build());
        } else {
            sender.sendMsgToGate(p, NnConstant.Notice.RUSH, builder.build());
        }
    }

    /**
     * 计算剩余算牌时间,通知玩家开始算牌
     */
    private void noticeCheck(NnPlayer p) {
        if (checkFuture == null) {
            return;
        }
        NnMsg.NoticeCheck.Builder builder = NnMsg.NoticeCheck.newBuilder();
        builder.setTime(checkFuture.getDelay(TimeUnit.MILLISECONDS));
        for (NnPlayer np:gamers) {
            NnMsg.CheckData.Builder cb = NnMsg.CheckData.newBuilder();
            cb.setUid(np.getUid());
            cb.setStatus(np.getCheckStatus().getCode());
            builder.addCheckData(cb);
        }
        if (p == null) {
            sender.sendMsgToGate(players.values(), NnConstant.Notice.CARD_CHECK, builder.build());
        } else {
            sender.sendMsgToGate(p, NnConstant.Notice.CARD_CHECK, builder.build());
        }
    }

    /**
     * 计算剩余下注时间,通知玩家开始下注
     */
    private void noticeBet(NnPlayer player) {
        if (betFuture == null) {
            return;
        }
        //通知玩家开始下注
        NnMsg.NoticeBet.Builder builder = NnMsg.NoticeBet.newBuilder();
        builder.setTime(betFuture.getDelay(TimeUnit.MILLISECONDS));
        for (NnPlayer np:gamers) {
            if (np != banker) {
                NnMsg.BetData.Builder bb = NnMsg.BetData.newBuilder();
                bb.setUid(np.getUid());
                bb.setStatus(np.getBetStatus().getCode());
                if (np.getBetStatus() == BetStatus.beted) {
                    bb.setBet(np.getBetScore());
                }
                builder.addBetData(bb);
            }
        }
        if (player == null) {
            sender.sendMsgToGate(players.values(), NnConstant.Notice.BET, builder.build());
        } else {
            sender.sendMsgToGate(player, NnConstant.Notice.BET, builder.build());
        }
    }

    /**
     * 确定庄家
     */
    private void determineBanker() {
        boolean ran = (setting.playWay == NnSetting.NNSZ && banker == null) ||
                setting.playWay == NnSetting.ZYQZ || setting.playWay == NnSetting.MPQZ;
        if (ran) {
            if (rushGamer.size() == 0) {
                banker = gamers.get(new Random().nextInt(gamers.size()));
            } else if (rushGamer.size() == 1) {
                banker = rushGamer.get(0);
            } else {
                banker = rushGamer.get(new Random().nextInt(rushGamer.size()));
            }
        }

        //通知房间所有人庄家的位置
        NnMsg.NoticeBanker.Builder builder = NnMsg.NoticeBanker.newBuilder();
        builder.setUid(banker.getUid());
        sender.sendMsgToGate(players.values(), NnConstant.Notice.BANKER, builder.build());

        initBet();
        betFuture = getExecutor().schedule(this::betTimeout, waitBetTime, TimeUnit.MILLISECONDS);
        noticeBet(null);
    }

    /**
     * 发牌
     *
     * @param num 发牌的数量
     */
    private void deal(int num) {

        for (NnPlayer p : gamers) {
            List<Integer> pokers = new ArrayList<>();
            for (int i = 0; i < num; i++) {
                pokers.add(this.pokers.remove(0));
            }
            p.deal(pokers);
        }

        //通知房间内的各玩家

        NnMsg.NoticeDeal.Builder builder = NnMsg.NoticeDeal.newBuilder();
        for (NnPlayer p:players.values()) {
            builder.clear();
            for (NnPlayer pp:gamers) {
                NnMsg.DealData.Builder pd = NnMsg.DealData.newBuilder();
                pd.setUid(pp.getUid());
                pd.setSize(num);
                if (p == pp ) {
                    pd.addAllPokers(pp.dealCards());
                }
                builder.addDealData(pd);
            }
            sender.sendMsgToGate(p, NnConstant.Notice.DEAL,builder.build());
        }

        if (setting.playWay == NnSetting.MPQZ) {
            //如果玩法是明牌抢庄
            if (num == MPQZ_4) {
                //并且是发四张牌,开始抢庄
                rush();
            } else {
                //发一张牌
                checkPoker();
            }
        } else {
            checkPoker();
        }
    }

    /**
     * 抢庄
     */
    private void rush() {
        initRush();
        rushFuture = getExecutor().schedule(this::rushTimeout, waitRushTime, TimeUnit.MILLISECONDS);
        noticeRush(null);
    }

    /**
     * 抢庄超时处理
     */
    private void rushTimeout() {
        for (NnPlayer p : gamers) {
            if (p.getRushStatus() == RushStatus.wait) {
                //超时没有抢庄,默认不抢庄
                rush(p,0);
            }
        }
    }

    /**
     * 下注超时处理
     */
    private void betTimeout() {
        for (NnPlayer p : gamers) {
            if (p != banker && p.getBetStatus() == BetStatus.wait) {
                //默认一倍下注
                bet(p,1);
            }
        }
    }

    /**
     * check超时
     */
    private void checkTimeout() {

        for (NnPlayer p : gamers) {
            if (p.getCheckStatus() == CheckStatus.wait) {
                check(p);
            }
        }
    }

    @Override
    public Message roomData(NnPlayer player) {
        NnMsg.RoomData.Builder builder = NnMsg.RoomData.newBuilder();
        builder.setId(getId());
        builder.setOwnerId(getOwnerId());
        builder.setClosed(isClosed());
        builder.setRoomType(setting.roomType);
        builder.setGameStart(isGameStart());
        builder.setCardRoundStart(isCardRoundStart());
        builder.setRound(getRound());
        builder.setRoomSetting(setting.setting);
        for (NnPlayer p : seatPlayers.values()) {
            if (p != null) {
                builder.addPlayerData((NnMsg.PlayerData) p.playerData(player));
            }
        }
        return builder.build();
    }

    @Override
    protected int startPersonCount() {
        return 2;
    }

    @Override
    protected boolean checkCardRoundStart() {
        if (setting.playWay == NnSetting.GDZJ && banker == null) {
            log.info("固定庄家模式下,房主不参与游戏,无法开始游戏");
            return false;
        }
        if (setting.autoStart == NnSetting.ROOM_OWNER_START_GAME) {
            return seatPlayers.size() >= startPersonCount();
        } else {
            return setting.autoStart >= seatPlayers.size();
        }
    }

    public void check(NnPlayer player) {
        player.setCheckStatus(CheckStatus.checked);
        NnMsg.CheckRes.Builder builder = NnMsg.CheckRes.newBuilder();
        builder.setUid(player.getUid());
        sender.sendMsgToGate(players.values(),NnConstant.Cmd.CHECK_RES,builder.build());
        allCheck();
    }

    @Override
    protected NnPlayer newPlayer(long uid) {
        return new NnPlayer(uid);
    }
}
