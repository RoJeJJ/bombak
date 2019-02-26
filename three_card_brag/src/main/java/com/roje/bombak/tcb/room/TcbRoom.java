package com.roje.bombak.tcb.room;

import com.google.protobuf.Message;
import com.roje.bombak.room.common.manager.RoomManager;
import com.roje.bombak.room.common.proto.RoomMsg;
import com.roje.bombak.room.common.room.BaseRoom;
import com.roje.bombak.tcb.config.TcbSetting;
import com.roje.bombak.tcb.constant.TcbConstant;
import com.roje.bombak.tcb.player.BetStatus;
import com.roje.bombak.tcb.player.TcbPlayer;
import com.roje.bombak.tcb.proto.TcbMsg;

import java.util.*;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/21
 **/
public class TcbRoom extends BaseRoom<TcbPlayer> {

    private static final int POKER_SIZE = 52;

    private static final int HAND_CARD_SIZE = 3;

    private final TcbSetting setting;

    private final List<Integer> pokers;

    private TcbPlayer banker;

    private Random ran;

    private int pool;

    private TcbPlayer curPlayer;

    private int turn;

    public TcbRoom(long id, RoomManager manager, TcbSetting setting) {
        super(id, manager);
        this.setting = setting;
        pokers = new ArrayList<>();
        ran = new Random();
        curPlayer = null;
    }

    @Override
    protected void onClosed() {

    }

    @Override
    protected void onNoticePlayer(TcbPlayer player) {

    }

    @Override
    protected TcbPlayer newPlayer(long uid) {
        return new TcbPlayer(uid);
    }

    @Override
    protected int startPersonCount() {
        return 2;
    }

    @Override
    protected boolean checkCardRoundStart() {
        return false;
    }

    @Override
    protected void onStartGame() {
        //初始化扑克
        for (int i = 0; i < POKER_SIZE; i++) {
            pokers.add(i);
        }
        Collections.shuffle(pokers);
        pool = 0;
        turn = 0;

        //投底
        addPool();
        noticeBanker();
        deal();
        getNextBetPlayer(curPlayer);
    }


    private void noticeBet(TcbPlayer nextPlayer) {
        nextPlayer.setBetStatus(BetStatus.wait);
        TcbMsg.NoticeBet.Builder builder = TcbMsg.NoticeBet.newBuilder();
        builder.setUid(nextPlayer.getUid());
    }

    private void getNextBetPlayer(TcbPlayer curPlayer) {
        if (allBeted() && turn == setting.maxTurn) {
            //所有人都下好注
        }
        if (gamers.size() == 1) {
            //只有一个玩家了,直接胜出
            win(gamers.get(0));
        } else {
            int index = gamers.indexOf(curPlayer);
            int nextIndex = index + 1 < gamers.size() ? index : 0;
            TcbPlayer next = gamers.get(nextIndex);
            noticeBet(next);
        }
    }

    private boolean allBeted() {
        int defNum = 0;
        for (TcbPlayer p:gamers) {
            if (p.getBetStatus() == BetStatus.def) {
                defNum++;
            }
        }
        return true;
    }

    private void win(TcbPlayer tcbPlayer) {

    }

    @Override
    protected void onPlayerExit(TcbPlayer player) {
        if (banker == player) {
            banker = null;
        }
    }


    private void addPool() {
        int s = setting.baseScore + setting.baseScore * setting.men;
        TcbMsg.NoticeAddPool.Builder builder = TcbMsg.NoticeAddPool.newBuilder();
        Iterator<TcbPlayer> it = gamers.iterator();
        while (it.hasNext()) {
            TcbPlayer p = it.next();
            if (getRoomType() == CARD) {
                p.subScore(s);
            } else if (getRoomType() == GOLD && !minusGoldIfEnough(p, s)) {
                it.remove();
                continue;
            }
            pool += s;
            TcbMsg.AddPoolData.Builder ab = TcbMsg.AddPoolData.newBuilder();
            ab.setUid(p.getUid())
                    .setPoint(s);
            builder.addData(ab);
        }
        sender.sendMsgToGate(players.values(), TcbConstant.Notice.ADD_POOL, builder.build());
    }

    private void deal() {
        TcbMsg.NoticeDeal.Builder builder = TcbMsg.NoticeDeal.newBuilder();
        for (TcbPlayer p : gamers) {
            for (int i = 0; i < HAND_CARD_SIZE; i++) {
                p.addPoker(pokers.remove(0));
                TcbMsg.DealData.Builder db = TcbMsg.DealData.newBuilder();
                db.setUid(p.getUid())
                        .setPokerSize(p.getHands().size())
                        .addAllPokers(p.getHands());
                builder.addData(db);
            }
        }
        sender.sendMsgToGate(players.values(), TcbConstant.Notice.DEAL, builder.build());
    }

    private void noticeBanker() {
        if (banker == null) {
            int ranIndex = ran.nextInt(gamers.size());
            banker = gamers.get(ranIndex);
        }
        curPlayer = banker;
        RoomMsg.PlayerInfo.Builder builder = RoomMsg.PlayerInfo.newBuilder();
        builder.setUid(banker.getUid());
        sender.sendMsgToGate(players.values(), TcbConstant.Notice.BANKER, builder.build());
    }

    @Override
    public Message roomData(TcbPlayer player) {
        return null;
    }
}
