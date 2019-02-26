package com.roje.bombak.nn.player;


import com.google.protobuf.Message;
import com.roje.bombak.nn.config.NnSetting;
import com.roje.bombak.nn.proto.NnMsg;
import com.roje.bombak.room.common.player.BasePlayer;
import com.roje.bombak.room.common.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author pc
 */
public class NnPlayer extends BasePlayer {

    private List<Integer> hands;

    private RushStatus rushStatus;

    private int rushTime;

    private BetStatus betStatus;

    private int betScore;

    private List<Integer> openCards;

    private List<Integer> dealCards;

    private CheckStatus checkStatus;

    private int niu;

    private int score;

    public List<Integer> dealCards() {
        return new ArrayList<>(dealCards);
    }

    public NnPlayer(long uid) {
        super(uid);
        hands = new ArrayList<>();
        openCards = new ArrayList<>();
        dealCards = new ArrayList<>();
        score = 0;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public void subScore(int score) {
        this.score -= score;
    }

    @Override
    public void onNewGame() {
        hands.clear();
        betStatus = BetStatus.def;
        rushStatus = RushStatus.def;
        checkStatus = CheckStatus.def;
        betScore = 0;
        openCards.clear();
        dealCards.clear();
    }

    public int getRushTime() {
        return rushTime;
    }

    public void setRushTime(int rushTime) {
        this.rushTime = rushTime;
    }

    public List<Integer> hands() {
        return new ArrayList<>(hands);
    }

    public RushStatus getRushStatus() {
        return rushStatus;
    }

    public void setRushStatus(RushStatus rushFlag) {
        this.rushStatus = rushFlag;
    }

    public BetStatus getBetStatus() {
        return betStatus;
    }

    public void setBetStatus(BetStatus betFlag) {
        this.betStatus = betFlag;
    }

    public int getBetScore() {
        return betScore;
    }

    public void setBetScore(int betScore) {
        this.betScore = betScore;
    }

    @Override
    public Message playerData(Player p) {
        NnMsg.PlayerData.Builder builder = NnMsg.PlayerData.newBuilder();
        builder.setUid(getUid());
        String nickname = getUser().getNickname();
        if (nickname != null) {
            builder.setNickname(nickname);
        }
        String headImg = getUser().getHeadImg();
        if (headImg != null) {
            builder.setHeadImg(headImg);
        }
        builder.setSeat(getSeat());
        builder.setOffline(isOffline());
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

    public CheckStatus getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(CheckStatus checkFlag) {
        this.checkStatus = checkFlag;
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

    public void checkHand(NnSetting config) {
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
}
