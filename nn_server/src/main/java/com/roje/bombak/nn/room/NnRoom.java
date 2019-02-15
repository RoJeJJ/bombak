package com.roje.bombak.nn.room;

import com.google.protobuf.Message;
import com.roje.bombak.nn.config.NnProperties;
import com.roje.bombak.nn.config.NnSetting;
import com.roje.bombak.nn.constant.NnConstant;
import com.roje.bombak.nn.player.BetStatus;
import com.roje.bombak.nn.player.CheckStatus;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.player.RushStatus;
import com.roje.bombak.nn.proto.NnMsg;
import com.roje.bombak.room.common.config.RoomProperties;
import com.roje.bombak.room.common.room.BaseRoom;
import com.roje.bombak.room.common.room.Room;
import com.roje.bombak.room.common.room.RoomListener;
import com.roje.bombak.room.common.room.RoomType;
import com.roje.bombak.room.common.utils.RoomMessageSender;
import io.netty.util.concurrent.EventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;
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

    private final NnSetting config;

    private List<Integer> pokers;

    private List<NnPlayer> rushGamer;

    private int currentMaxRush;

    private NnPlayer banker;

    private boolean startBet;

    private boolean startRush;

    private long startBetTime;

    private long startRushTime;

    private long startCheckTime;

    private boolean startCheck;

    private int round;

    private final NnProperties nnProperties;

    private Future rushFuture;

    private Future betFuture;

    private Future checkFuture;

    protected NnRoom(long id, long ownerId, String name, String gameType, RoomType type,
                     RoomProperties roomProperties, NnSetting setting, EventExecutor executor,
                     RoomMessageSender sender, RoomListener<NnPlayer, Room<NnPlayer>> listener, NnProperties nnProperties) {
        super(id, ownerId, name, gameType, type, setting.seatSize, roomProperties.getRoomMaxPlayers(), executor, sender, listener);
        this.config = setting;
        this.nnProperties = nnProperties;
        rushGamer = new ArrayList<>();
        pokers = new ArrayList<>();
    }


    public NnSetting config() {
        return config;
    }

    @Override
    protected void onClosed() {
        if (rushFuture != null) {
            rushFuture.cancel(false);
        }
        if (betFuture != null) {
            betFuture.cancel(false);
        }
        if (checkFuture != null) {
            checkFuture.cancel(false);
        }
    }

    @Override
    protected void initJoin(NnPlayer player) {}

    @Override
    protected void startGame0() {
        pokers.clear();
        for (int i = 0; i < POKER_NUM; i++) {
            pokers.add(i);
        }
        round++;
        Collections.shuffle(pokers);
        rushGamer.clear();
        currentMaxRush = 1;
        rushGamer.clear();
        startBet = false;
        startRush = false;
        startCheck = false;
        //通知房间中所有人游戏开始
        sender.sendMsg(players.values(), NnConstant.Cmd.START_GAME_RES);
        executor().schedule(() -> {
            if (isClosed()) {
                return;
            }
            switch (config.playWay) {
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
    protected void onPlayerJoin(NnPlayer player) {
        if (startRush) {
            noticeRush(player);
        }
        if (startBet) {
            noticeBet(player);
        }
        if (startCheck) {
            noticeCheck(player);
        }
    }

    /**
     * 抢庄初始化
     */
    private void initRush() {
        for (NnPlayer p : gamers) {
            p.setRushStatus(RushStatus.wait);
        }
        startRush = true;
        startRushTime = System.currentTimeMillis();
    }

    private void endRush() {
        for (NnPlayer p:gamers) {
            p.setRushStatus(RushStatus.def);
        }
        startRush = false;
        if (rushFuture != null) {
            rushFuture.cancel(false);
        }
    }

    private void endBet() {
        for (NnPlayer p:gamers) {
            p.setBetStatus(BetStatus.def);
        }
        startBet = false;
        if (betFuture != null) {
            betFuture.cancel(false);
        }
    }

    private void initBet() {
        for (NnPlayer p : gamers) {
            p.setBetStatus(BetStatus.wait);
        }
        startBet = true;
        startBetTime = System.currentTimeMillis();
    }

    public void rush(NnPlayer p, int bet) {

        if (bet > currentMaxRush) {
            rushGamer.clear();
            rushGamer.add(p);
        } else if (bet == currentMaxRush) {
            rushGamer.add(p);
        }
        if (bet == 0) {
            p.setRushStatus(RushStatus.not_rush);
        } else {
            p.setRushStatus(RushStatus.rush);
        }
        log.info("玩家{}抢庄,倍数{}",p.uid(),bet);
        NnMsg.RushRes.Builder builder = NnMsg.RushRes.newBuilder();
        builder.setMul(bet);
        builder.setUid(p.uid());
        sender.sendMsg(players.values(), NnConstant.Cmd.RUSH_RES,builder.build());

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
        p.setBetScore(bet);
        p.setBetStatus(BetStatus.beted);

        log.info("玩家{}下注倍数{}",p.uid(),bet);
        NnMsg.BetRes.Builder builder = NnMsg.BetRes.newBuilder();
        builder.setBet(bet);
        builder.setUid(p.uid());
        sender.sendMsg(players.values(),NnConstant.Cmd.BET_RES,builder.build());

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
            if (config.playWay == NnSetting.MPQZ) {
                deal(1);
            } else {
                deal(5);
            }
        }
    }

    private void checkPoker() {
        startCheck = true;
        startCheckTime = System.currentTimeMillis();
        noticeCheck(null);
        checkFuture = executor().schedule(this::checkTimeout, nnProperties.getCheckTime(), TimeUnit.SECONDS);
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
        startCheck = false;
        for (NnPlayer p:gamers) {
            p.setCheckStatus(CheckStatus.def);
        }
        if (checkFuture != null) {
            checkFuture.cancel(false);
        }
    }

    private void startCompare() {
        for (NnPlayer player : gamers) {
            player.checkHand(config);
        }

        showHandAndPoint(banker, NnResult.def,0);

        NnPlayer player = getNextPlayer(banker.getSeat());
        comparePlayer(player);
    }

    private void showHandAndPoint(NnPlayer player, NnResult result,int score) {
        NnMsg.HandCard.Builder builder = NnMsg.HandCard.newBuilder();
        builder.addAllHands(player.hands());
        builder.setUid(player.uid());
        builder.setNiu(player.getNiu());
        builder.setResult(result.ordinal());
        builder.setScore(score);
        sender.sendMsg(players.values(), NnConstant.Notice.HAND_CARD,builder.build());
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
            executor().schedule(() -> {
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
            if (roomType() == RoomType.card) {
                int score = currentMaxRush * player.getBetScore() * player.getNiu();
                player.addScore(score);
                banker.subScore(score);
                showHandAndPoint(player, NnResult.win,score);
            }
        } else {
            if (roomType() == RoomType.card) {
                int score = currentMaxRush * player.getBetScore() * player.getNiu();
                player.subScore(score);
                banker.addScore(score);
                showHandAndPoint(player, NnResult.lose,score);
            }
        }
    }

    private NnPlayer getNextPlayer(int seat) {
        int next = seat + 1;
        if (next > config.seatSize) {
            next = 0;
        }
        NnPlayer player = getSeat(next);
        if (player == null || !player.isInGame()) {
            return getNextPlayer(next);
        }
        return player;
    }

    /**
     * 计算剩余抢庄时间,通知玩家开始抢庄
     */
    private void noticeRush(NnPlayer p) {
        int intervalTime = (int) (System.currentTimeMillis() - startRushTime) / 1000;
        int rushTime = nnProperties.getRushTime() - intervalTime;
        // 通知玩家开始抢庄
        NnMsg.NoticeRush.Builder builder = NnMsg.NoticeRush.newBuilder();
        builder.setTime(rushTime);
        for (NnPlayer np:gamers) {
            NnMsg.RushData.Builder rb = NnMsg.RushData.newBuilder();
            rb.setUid(np.uid());
            rb.setStatus(np.getRushStatus().getCode());
            builder.addRushData(rb);
        }
        if (p == null) {
            sender.sendMsg(players.values(), NnConstant.Notice.RUSH, builder.build());
        } else {
            sender.sendMsg(p, NnConstant.Notice.RUSH, builder.build());
        }
    }

    /**
     * 计算剩余算牌时间,通知玩家开始算牌
     */
    private void noticeCheck(NnPlayer p) {
        int intervalTime = (int) (System.currentTimeMillis() - startCheckTime) / 1000;
        int calcTime = nnProperties.getCheckTime() - intervalTime;
        NnMsg.NoticeCheck.Builder builder = NnMsg.NoticeCheck.newBuilder();
        builder.setTime(calcTime);
        for (NnPlayer np:gamers) {
            NnMsg.CheckData.Builder cb = NnMsg.CheckData.newBuilder();
            cb.setUid(np.uid());
            cb.setStatus(np.getCheckStatus().getCode());
            builder.addCheckData(cb);
        }
        if (p == null) {
            sender.sendMsg(players.values(), NnConstant.Notice.CARD_CHECK, builder.build());
        } else {
            sender.sendMsg(p, NnConstant.Notice.CARD_CHECK, builder.build());
        }
    }

    /**
     * 计算剩余下注时间,通知玩家开始下注
     */
    private void noticeBet(NnPlayer player) {
        int intervalTime = (int) (System.currentTimeMillis() - startBetTime) / 1000;
        int betTime = nnProperties.getBetTime() - intervalTime;
        //通知玩家开始下注
        NnMsg.NoticeBet.Builder builder = NnMsg.NoticeBet.newBuilder();
        builder.setTime(betTime);
        for (NnPlayer np:gamers) {
            if (np != banker) {
                NnMsg.BetData.Builder bb = NnMsg.BetData.newBuilder();
                bb.setUid(np.uid());
                bb.setStatus(np.getBetStatus().getCode());
                if (np.getBetStatus() == BetStatus.beted) {
                    bb.setBet(np.getBetScore());
                }
                builder.addBetData(bb);
            }
        }
        if (player == null) {
            sender.sendMsg(gamers, NnConstant.Notice.BET, builder.build());
        } else {
            sender.sendMsg(player, NnConstant.Notice.BET, builder.build());
        }
    }

    /**
     * 确定庄家
     */
    private void determineBanker() {
        boolean ran = (config.playWay == NnSetting.NNSZ && banker == null) ||
                config.playWay == NnSetting.ZYQZ || config.playWay == NnSetting.MPQZ;
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
        builder.setUid(banker.uid());
        sender.sendMsg(players.values(), NnConstant.Notice.BANKER, builder.build());

        initBet();
        noticeBet(null);
        betFuture = executor().schedule(this::betTimeout, nnProperties.getBetTime(), TimeUnit.MILLISECONDS);
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
//        List<NnPlayer> players = new ArrayList<>(this.players.values());
        NnMsg.NoticeDeal.Builder builder = NnMsg.NoticeDeal.newBuilder();
        for (NnPlayer p:this.players.values()) {
            builder.clear();
            for (NnPlayer pp:gamers) {
                NnMsg.DealData.Builder pd = NnMsg.DealData.newBuilder();
                pd.setUid(pp.uid());
                pd.setSize(num);
                if (p == pp ) {
                    pd.addAllPokers(pp.dealCards());
                }
                builder.addDealData(pd);
            }
            sender.sendMsg(p, NnConstant.Notice.DEAL,builder.build());
        }
//        for (NnPlayer p : gamers) {
//            Nn.IndicateDeal.Builder builder = Nn.IndicateDeal.newBuilder();
//            for (NnPlayer player : gamers) {
//                Nn.DealData.Builder ddb = Nn.DealData.newBuilder();
//                ddb.setSeat(player.seat());
//                ddb.setSize(num);
//                if (p == player) {
//                    ddb.addAllPokers(player.dealCards());
//                }
//                builder.addDealData(ddb);
//            }
//            sender.send(p, NnConstant.Notice.DEAL, builder.build().toByteArray());
//            players.remove(p);
//        }
//        Nn.IndicateDeal.Builder builder = Nn.IndicateDeal.newBuilder();
//        for (NnPlayer p : gamers) {
//            Nn.DealData.Builder db = Nn.DealData.newBuilder();
//            db.setSeat(p.seat());
//            db.setSize(num);
//            builder.addDealData(db);
//        }
//        sender.send(players, NnConstant.Notice.DEAL, builder.build().toByteArray());

        if (config.playWay == NnSetting.MPQZ) {
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
        noticeRush(null);
        rushFuture = executor().schedule(this::rushTimeout, nnProperties.getRushTime(), TimeUnit.MILLISECONDS);
    }

    public boolean isStartRush() {
        return startRush;
    }

    /**
     * 抢庄超时处理
     */
    private void rushTimeout() {
        if (!startRush) {
            return;
        }
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
        if (!startBet) {
            return;
        }
        for (NnPlayer p : gamers) {
            if (p != banker && p.getBetStatus() == BetStatus.wait) {
                //默认一倍下注
                bet(p,1);
            }
        }
    }

    public boolean isStartCheck() {
        return startCheck;
    }

    /**
     * check超时
     */
    private void checkTimeout() {
        if (isClosed()) {
            return;
        }
        if (!startCheck) {
            return;
        }
        for (NnPlayer p : gamers) {
            if (p.getCheckStatus() == CheckStatus.wait) {
                check(p);
            }
        }
    }

    @Override
    public Message roomData(NnPlayer player) {
        NnMsg.RoomData.Builder builder = NnMsg.RoomData.newBuilder();
        builder.setId(id());
        builder.setOwnerId(ownerId());
        builder.setClosed(isClosed());
        builder.setRoomType(roomType().getCode());
        builder.setGameStart(isGameStart());
        builder.setCardRoundStart(isCardRoundStart());
        builder.setRound(round);
        builder.setRoomSetting(config().setting);
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
        if (config.playWay == NnSetting.GDZJ && banker == null) {
            log.info("固定庄家模式下,房主不参与游戏,无法开始游戏");
            return false;
        }
        if (config.autoStart == NnSetting.ROOM_OWNER_START_GAME) {
            return seatPlayers.size() >= startPersonCount();
        } else {
            return config.autoStart >= seatPlayers.size();
        }
    }

    public void check(NnPlayer player) {
        player.setCheckStatus(CheckStatus.checked);
        NnMsg.CheckRes.Builder builder = NnMsg.CheckRes.newBuilder();
        builder.setUid(player.uid());
        sender.sendMsg(players.values(),NnConstant.Cmd.CHECK_RES,builder.build());
        allCheck();
    }
}
