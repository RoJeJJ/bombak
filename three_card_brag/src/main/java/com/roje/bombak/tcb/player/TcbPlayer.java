package com.roje.bombak.tcb.player;

import com.google.protobuf.Message;
import com.roje.bombak.room.common.player.BasePlayer;
import com.roje.bombak.room.common.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pc
 * @version 1.0
 * @date 2019/2/21
 **/
public class TcbPlayer extends BasePlayer {

    private List<Integer> hands;

    private int score;

    private BetStatus betStatus;

    public TcbPlayer(long uid) {
        super(uid);
        hands = new ArrayList<>();
    }

    public List<Integer> getHands() {
        return hands;
    }

    @Override
    protected void onNewGame() {
        hands.clear();
    }

    @Override
    public Message playerData(Player player) {
        return null;
    }

    public void addPoker(Integer card) {
        hands.add(card);
    }

    public void subScore(int score) {
        this.score -= score;
    }

    public BetStatus getBetStatus() {
        return betStatus;
    }

    public void setBetStatus(BetStatus betStatus) {
        this.betStatus = betStatus;
    }
}
