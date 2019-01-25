package com.roje.bombak.nn.room;

import com.google.protobuf.Message;
import com.roje.bombak.common.redis.dao.UserRedisDao;
import com.roje.bombak.nn.config.NnProperties;
import com.roje.bombak.nn.constant.NnConstant;
import com.roje.bombak.nn.config.NnRoomConfig;
import com.roje.bombak.nn.player.NnPlayer;
import com.roje.bombak.nn.proto.Nn;
import com.roje.bombak.room.api.manager.RoomManager;
import com.roje.bombak.room.api.proto.RoomMsg;
import com.roje.bombak.room.api.room.AbstractRoom;
import com.roje.bombak.room.api.utils.RoomMessageSender;
import io.netty.util.concurrent.EventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author pc
 * @version 1.0
 * @date 2018/12/18
 **/
@Slf4j
public class NnRoom extends AbstractRoom<NnPlayer> {

    private static final int POKER_NUM = 52;

    private static final int MPQZ_4 = 4;

    private final NnRoomConfig config;

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

    private NnProperties nnProperties;

    public NnRoom(long id, long ownerId, String name, EventExecutor executor, NnRoomConfig config, RoomMsg.RoomType type,
                  RoomMessageSender sender, UserRedisDao userRedisDao, RoomManager roomManager, NnProperties nnProperties) {
        super(id, ownerId, name, executor, type, config.personNum, sender, roomManager,userRedisDao);
        this.config = config;
        pokers = new ArrayList<>();
        rushGamer = new ArrayList<>();
        this.nnProperties = nnProperties;
        round = 0;
    }

    public NnRoomConfig config() {
        return config;
    }

    public boolean isStartBet() {
        return startBet;
    }

    @Override
    public NnPlayer newPlayer(long uid) {
        return new NnPlayer(uid);
    }

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
        sender.send(players.values(), NnConstant.Cmd.START_GAME_RES);
        executor().schedule(() -> {
            if (isClosed()) {
                return;
            }
            switch (config.playWay) {
                case NnRoomConfig.MPQZ:
                    log.info("开始游戏,模式:明牌抢庄");
                    deal(4);
                    break;
                case NnRoomConfig.ZYQZ:
                    log.info("开始游戏,模式:自由抢庄");
                    rush();
                    break;
                case NnRoomConfig.NNSZ:
                    log.info("开始游戏,模式:牛牛上庄");
                    determineBanker();
                    break;
                default:
                    break;
            }
        }, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void initializeJoin(NnPlayer player) {}

    @Override
    protected void onPlayerJoin(NnPlayer player) {
        if (player.getRushFlag() == Nn.RushStatus.WaitRush) {
            indicateRush(player);
        }
        if (player.getBetFlag() == Nn.BetStatus.WaitBet) {
            indicateBet(player);
        }
        if (player.getCheckFlag() == Nn.CheckStatus.WaitCheck) {
            indicateCheck(player);
        }
    }

    /**
     * 抢庄初始化
     */
    private void initRush() {
        for (NnPlayer p : gamers) {
            p.setRushFlag(Nn.RushStatus.WaitRush);
        }
        startRush = true;
        startRushTime = System.currentTimeMillis();
    }

    private void endRush() {
        for (NnPlayer p:gamers) {
            p.setRushFlag(Nn.RushStatus.DefRush);
        }
        startRush = false;
    }

    private void endBet() {
        for (NnPlayer p:gamers) {
            p.setBetFlag(Nn.BetStatus.DefBet);
        }
        startBet = false;
    }

    private void initBet() {
        for (NnPlayer p : gamers) {
            p.setBetFlag(Nn.BetStatus.WaitBet);
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
            p.setRushFlag(Nn.RushStatus.NegativeRush);
        } else {
            p.setRushFlag(Nn.RushStatus.PositiveRush);
        }
        log.info("玩家{}抢庄,倍数{}",p.uid(),bet);
        Nn.RushRes.Builder builder = Nn.RushRes.newBuilder();
        builder.setMul(bet);
        builder.setUid(p.uid());
        sender.send(players.values(), NnConstant.Cmd.RUSH_RES,builder.build().toByteArray());

        checkRush();
    }

    private void checkRush() {
        boolean rush = true;
        for (NnPlayer p : gamers) {
            if (p.getRushFlag() == Nn.RushStatus.WaitRush) {
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
        p.setBetFlag(Nn.BetStatus.Bet);

        log.info("玩家{}下注倍数{}",p.uid(),bet);
        Nn.BetRes.Builder builder = Nn.BetRes.newBuilder();
        builder.setBet(bet);
        builder.setUid(p.uid());
        sender.send(players.values(),NnConstant.Cmd.BET_RES,builder.build().toByteArray());

        checkBet();
    }

    private void checkBet() {
        boolean bet = true;
        for (NnPlayer p : gamers) {
            if (p != banker && p.getBetFlag() == Nn.BetStatus.WaitBet) {
                bet = false;
            }
        }
        //所有人下好注了
        if (bet) {
            endBet();
            if (config.playWay == NnRoomConfig.MPQZ) {
                deal(1);
            } else {
                deal(5);
            }
        }
    }

    private void checkPoker() {
        startCheck = true;
        startCheckTime = System.currentTimeMillis();
        indicateCheck(null);
        executor().schedule(this::checkTimeout, nnProperties.getCheckTime(), TimeUnit.SECONDS);
    }

    private void allCheck() {
        boolean check = true;
        for (NnPlayer p : gamers) {
            if (p.getCheckFlag() == Nn.CheckStatus.WaitCheck) {
                check = false;
                break;
            }
        }
        if (check) {
            startCheck = false;
        }
        startCompare();
    }

    private void startCompare() {
        for (NnPlayer player : gamers) {
            player.checkHand(config);
        }

        showHandAndPoint(banker, Nn.Result.draw,0);

        NnPlayer player = getNextPlayer(banker.seat());
        comparePlayer(player);
    }

    private void showHandAndPoint(NnPlayer player, Nn.Result result,int score) {
        Nn.HandCard.Builder builder = Nn.HandCard.newBuilder();
        builder.addAllHands(player.hands());
        builder.setUid(player.uid());
        builder.setNiu(player.getNiu());
        builder.setResult(result);
        builder.setScore(score);
        sender.send(players.values(),NnConstant.Indicate.HAND_CARD,builder.build().toByteArray());
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
                NnPlayer player1 = getNextPlayer(banker.seat());
                comparePlayer(player1);
            },2000,TimeUnit.MILLISECONDS);
        }
    }

    private void calcScore(NnPlayer player,boolean win) {
        if (win) {
            if (roomType() == RoomMsg.RoomType.card) {
                int score = currentMaxRush * player.getBetScore() * player.getNiu();
                player.addScore(score);
                banker.subScore(score);
                showHandAndPoint(player, Nn.Result.win,score);
            }
        } else {
            if (roomType() == RoomMsg.RoomType.card) {
                int score = currentMaxRush * player.getBetScore() * player.getNiu();
                player.subScore(score);
                banker.addScore(score);
                showHandAndPoint(player, Nn.Result.lose,score);
            }
        }
    }

    private NnPlayer getNextPlayer(int seat) {
        int next = seat + 1;
        if (next > config.personNum) {
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
    private void indicateRush(NnPlayer p) {
        int intervalTime = (int) (System.currentTimeMillis() - startRushTime) / 1000;
        int rushTime = nnProperties.getRushTime() - intervalTime;
        // 通知玩家开始抢庄
        Nn.IndicateRush.Builder builder = Nn.IndicateRush.newBuilder();
        builder.setTime(rushTime);
        if (p == null) {
            sender.send(players.values(), NnConstant.Indicate.RUSH, builder.build().toByteArray());
        } else {
            sender.send(p, NnConstant.Indicate.RUSH, builder.build().toByteArray());
        }
    }

    /**
     * 计算剩余算牌时间,通知玩家开始算牌
     */
    private void indicateCheck(NnPlayer p) {
        int intervalTime = (int) (System.currentTimeMillis() - startCheckTime) / 1000;
        int calcTime = nnProperties.getCheckTime() - intervalTime;
        Nn.CalcCard.Builder builder = Nn.CalcCard.newBuilder();
        builder.setTime(calcTime);
        if (p == null) {
            sender.send(players.values(), NnConstant.Indicate.CARD_CHECK, builder.build().toByteArray());
        } else {
            sender.send(p, NnConstant.Indicate.CARD_CHECK, builder.build().toByteArray());
        }
    }

    /**
     * 计算剩余下注时间,通知玩家开始下注
     */
    private void indicateBet(NnPlayer player) {
        int intervalTime = (int) (System.currentTimeMillis() - startBetTime) / 1000;
        int betTime = nnProperties.getBetTime() - intervalTime;
        //通知玩家开始下注
        Nn.IndicateBet.Builder builder = Nn.IndicateBet.newBuilder();
        builder.setTime(betTime);
        if (player == null) {
            sender.send(gamers, NnConstant.Indicate.BET, builder.build().toByteArray());
        } else {
            sender.send(player, NnConstant.Indicate.BET, builder.build().toByteArray());
        }
    }

    /**
     * 确定庄家
     */
    private void determineBanker() {
        boolean ran = (config.playWay == NnRoomConfig.NNSZ && banker == null) ||
                config.playWay == NnRoomConfig.ZYQZ || config.playWay == NnRoomConfig.MPQZ;
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
        Nn.Banker.Builder builder = Nn.Banker.newBuilder();
        builder.setUid(banker.seat());
        sender.send(players.values(), NnConstant.Indicate.BANKER, builder.build().toByteArray());

        initBet();
        indicateBet(null);
        executor().schedule(this::betTimeout, nnProperties.getBetTime(), TimeUnit.MILLISECONDS);
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
        Nn.IndicateDeal.Builder builder = Nn.IndicateDeal.newBuilder();
        for (NnPlayer p:this.players.values()) {
            builder.clear();
            for (NnPlayer pp:gamers) {
                Nn.DealData.Builder pd = Nn.DealData.newBuilder();
                pd.setUid(pp.uid());
                pd.setSize(num);
                if (p == pp && num == MPQZ_4) {
                    pd.addAllPokers(pp.dealCards());
                }
                builder.addDealData(pd);
            }
            sender.send(p,NnConstant.Indicate.DEAL,builder.build().toByteArray());
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
//            sender.send(p, NnConstant.Indicate.DEAL, builder.build().toByteArray());
//            players.remove(p);
//        }
//        Nn.IndicateDeal.Builder builder = Nn.IndicateDeal.newBuilder();
//        for (NnPlayer p : gamers) {
//            Nn.DealData.Builder db = Nn.DealData.newBuilder();
//            db.setSeat(p.seat());
//            db.setSize(num);
//            builder.addDealData(db);
//        }
//        sender.send(players, NnConstant.Indicate.DEAL, builder.build().toByteArray());

        if (config.playWay == NnRoomConfig.MPQZ) {
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
        indicateRush(null);
        executor().schedule(this::rushTimeout, nnProperties.getRushTime(), TimeUnit.MILLISECONDS);
    }

    public boolean isStartRush() {
        return startRush;
    }

    /**
     * 抢庄超时处理
     */
    private void rushTimeout() {
        if (isClosed()) {
            return;
        }
        if (!startRush) {
            return;
        }
        for (NnPlayer p : gamers) {
            if (p.getRushFlag() == Nn.RushStatus.WaitRush) {
                //超时没有抢庄,默认不抢庄
                rush(p,0);
            }
        }
    }

    /**
     * 下注超时处理
     */
    private void betTimeout() {
        if (isClosed()) {
            return;
        }
        if (!startBet) {
            return;
        }
        for (NnPlayer p : gamers) {
            if (p != banker && p.getBetFlag() == Nn.BetStatus.WaitBet) {
                //默认一倍下注
                bet(p,1);
            }
        }
        checkBet();
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
            if (p.getCheckFlag() == Nn.CheckStatus.WaitCheck) {
                check(p);
            }
        }
        startCheck = false;
        startCompare();
    }

    @Override
    public Message roomData(NnPlayer player) {
        Nn.RoomData.Builder builder = Nn.RoomData.newBuilder();
        builder.setId(id());
        builder.setOwnerId(ownerId());
        builder.setClosed(isClosed());
        builder.setRoomType(roomType());
        builder.setGameStart(isGameStart());
        builder.setCardRoundStart(isCardRoundStart());
        builder.setRound(round);
        builder.setConfig(config().protoConfig);
        for (NnPlayer p : seatPlayers.values()) {
            if (p != null) {
                if (p != player) {
                    builder.addPlayerData(p.playerDataToOthers());
                } else {
                    builder.addPlayerData(p.playerDataToSelf());
                }
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
        if (config.playWay == NnRoomConfig.GDZJ && banker == null) {
            log.info("固定庄家模式下,房主不参与游戏,无法开始游戏");
            return false;
        }
        if (config.autoStart == NnRoomConfig.ROOM_OWNER_START_GAME) {
            return getPersonSize() >= startPersonCount();
        } else {
            return config.autoStart >= getPersonSize();
        }
    }

    public void check(NnPlayer player) {
        player.setCheckFlag(Nn.CheckStatus.Checked);
        Nn.CheckRes.Builder builder = Nn.CheckRes.newBuilder();
        builder.setUid(player.seat());
        sender.send(players.values(),NnConstant.Cmd.CHECK_RES,builder.build().toByteArray());

        allCheck();
    }
}
