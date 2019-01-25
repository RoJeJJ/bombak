package com.roje.bombak.nn.player;


import com.roje.bombak.nn.config.NnRoomConfig;
import com.roje.bombak.nn.proto.Nn;
import com.roje.bombak.room.api.player.AbstractPlayer;
import com.roje.bombak.room.api.proto.RoomMsg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author pc
 */
public class NnPlayer extends AbstractPlayer {

    private List<Integer> hands;

    private Nn.RushStatus rushFlag;

    private Nn.BetStatus betFlag;

    private int betScore;

    private List<Integer> openCards;

    private List<Integer> dealCards;

    private Nn.CheckStatus checkFlag;

    private int niu;

    private int score;

    public List<Integer> dealCards() {
        return new ArrayList<>(dealCards);
    }

    public NnPlayer(long uid) {
        super(uid);
        hands = new ArrayList<>();
        rushFlag = Nn.RushStatus.DefRush;
        betFlag = Nn.BetStatus.DefBet;
        openCards = new ArrayList<>();
        dealCards = new ArrayList<>();
        checkFlag = Nn.CheckStatus.DefCheck;
        score = 0;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public void subScore(int score) {
        this.score -= score;
    }

    @Override
    public void newGame() {
        hands.clear();
        betScore = 0;
        openCards.clear();
        dealCards.clear();
    }

    public List<Integer> hands() {
        return new ArrayList<>(hands);
    }

    public Nn.RushStatus getRushFlag() {
        return rushFlag;
    }

    public void setRushFlag(Nn.RushStatus rushFlag) {
        this.rushFlag = rushFlag;
    }

    public Nn.BetStatus getBetFlag() {
        return betFlag;
    }

    public void setBetFlag(Nn.BetStatus betFlag) {
        this.betFlag = betFlag;
    }

    public int getBetScore() {
        return betScore;
    }

    public void setBetScore(int betScore) {
        this.betScore = betScore;
    }

    public Nn.PlayerData playerData(NnPlayer p) {
        Nn.PlayerData.Builder builder = Nn.PlayerData.newBuilder();
        builder.setUid(uid());
        if (getNickname() != null) {
            builder.setNickname(getNickname());
        }
        if (getHeadImg() != null) {
            builder.setHeadImg(getHeadImg());
        }
        builder.setSeat(seat());
        builder.setOffline(isOffline());
        builder.setReady(isReady());
        builder.setInGame(isInGame());
        builder.setHandCardSize(hands.size());
        if (this == p) {
            builder.addAllOpenCards(openCards);
        }
        return builder.build();
    }

    public void deal(List<Integer> pokers) {
        dealCards = pokers;
        hands.addAll(pokers);
    }

    public Nn.CheckStatus getCheckFlag() {
        return checkFlag;
    }

    public void setCheckFlag(Nn.CheckStatus checkFlag) {
        this.checkFlag = checkFlag;
    }

    private int totalPoint() {
        int total = 0;
        for (int a : hands) {
            int f = a / 4;
            total += f > 10 ? 10 : f;
        }
        return total;
    }

    private int point(int a) {
        int fa = a / 4;
        return fa > 10 ? 10 : fa;
    }

    public int getNiu() {
        return niu;
    }

    public int getMaxCard() {
        return Collections.max(hands);
    }

    public void checkHand(NnRoomConfig config) {
        niu = 0;
        int total = totalPoint();
        Collections.sort(hands);
        boolean straight = true;
        boolean flash = true;
        boolean tiny = total < 10;
        int[] n = new int[13];
        boolean big = true;
        for (int i = 0; i < hands.size(); i++) {
            int fa = hands.get(i) / 4;
            int ca = hands.get(i) % 4;
            if (i < hands.size() - 1) {
                int fb = hands.get(i + 1) / 4;
                int cb = hands.get(i + 1) % 4;
                if (straight) {
                    boolean sz = (fa + 1 == fb || (i == 0 && fa + 9 == fb));
                    if (!sz) {
                        straight = false;
                    }
                }
                if (flash) {
                    if (ca != cb) {
                        flash = false;
                    }
                }
            }
            if (tiny) {
                if (fa >= 4 ) {
                    tiny = false;
                }
            }
            n[fa]++;
            if (big) {
                if (fa <= 9) {
                    big = false;
                }
            }
        }
        if (config.ths && straight && flash) {
            niu = 17;
        }
        if (config.wxn && tiny) {
            niu = 16;
        }
        if (config.zdn) {
            int max = -1;
            for (int i:n) {
                if (max == -1) {
                    max = i;
                } else if (i > max) {
                    max = i;
                }
            }
            if (max == 4) {
                niu = 15;
            }
        }
        if (config.hln) {
            int pair = 0,three = 0;
            for (int i:n) {
                if (i == 2) {
                    pair++;
                }else if (i == 3) {
                    three++;
                }
            }
            if (pair > 0 && three > 0) {
                niu = 14;
            }
        }
        if (config.whn && big) {
            niu = 13;
        }
        if (config.thn && flash) {
            niu = 12;
        }
        if (config.szn && straight) {
            niu = 11;
        }
        for (int i = 0; i < hands.size(); i++) {
            for (int j = 1; j < hands.size(); i++) {
                int point = point(hands.get(i)) + point(hands.get(j));
                if ((total - point) % 10 == 0) {
                    int a = point % 10;
                    if (a == 0) {
                        niu = 10;
                    } else {
                        niu = a;
                    }
                }
            }
        }
    }

    @Override
    public void setDisbandStatus(RoomMsg.DisbandStatus status) {

    }

    @Override
    public RoomMsg.DisbandStatus getDisbandStatus() {
        return null;
    }

    @Override
    public Nn.PlayerData playerDataToSelf() {
        return null;
    }

    @Override
    public Nn.PlayerData playerDataToOthers() {
        return null;
    }
}
