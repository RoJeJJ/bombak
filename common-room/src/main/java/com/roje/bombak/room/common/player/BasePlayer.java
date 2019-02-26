package com.roje.bombak.room.common.player;

import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.model.User;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/9
 **/
public abstract class BasePlayer implements Player {

    private final long uid;

    private User user;

    private String sessionId;

    private ServiceInfo gateInfo;

    private boolean exit;

    private boolean offline;

    private int seat;

    private VoteStatus voteStatus;

    private boolean inGame;

    public BasePlayer(long uid) {
        this.uid = uid;
    }

    @Override
    public boolean isExit() {
        return exit;
    }

    @Override
    public void setExit(boolean exit) {
        this.exit = exit;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void setGateInfo(ServiceInfo gateInfo) {
        this.gateInfo = gateInfo;
    }

    @Override
    public ServiceInfo getGateInfo() {
        return gateInfo;
    }

    @Override
    public int getSeat() {
        return seat;
    }

    @Override
    public void setSeat(int seat) {
        this.seat = seat;
    }

    @Override
    public long getUid() {
        return uid;
    }

    @Override
    public void setVoteStatus(VoteStatus voteStatus) {
        this.voteStatus = voteStatus;
    }

    @Override
    public VoteStatus getVoteStatus() {
        return voteStatus;
    }

    @Override
    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }


    public void newGame(){
        if (!inGame) {
            inGame = true;
        }
        onNewGame();
    }

    /**
     * 开始新一局游戏调用,用户初始化玩家数据
     */
    protected abstract void onNewGame();
}
