package com.roje.bombak.room.api.player;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/9
 **/
public abstract class AbstractPlayer implements Player {

    private final long uid;

    private String nickname;

    private String headImg;

    private String serviceType;

    private String serviceId;

    private long roomCard;

    private long gold;

    private boolean ready;

    private boolean inGame;

    private int seat;

    private boolean offline;

    private boolean exit;

    public AbstractPlayer(long uid) {
        this.uid = uid;
        seat = 0;
        exit = false;
    }

    public void newGame() {
        inGame = true;
    }

    @Override
    public long uid() {
        return uid;
    }

    @Override
    public void sit(int seat) {
        this.seat = seat;
    }

    @Override
    public int seat() {
        return seat;
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
    public String getServiceType() {
        return serviceType;
    }

    @Override
    public void setServiceType(String gateType) {
        this.serviceType = gateType;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public void setServiceId(String gateId) {
        this.serviceId = gateId;
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    @Override
    public boolean isInGame() {
        return inGame;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public boolean isExit() {
        return exit;
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    @Override
    public void setRoomCard(long roomCard) {
        this.roomCard = roomCard;
    }

    @Override
    public long getRoomCard() {
        return roomCard;
    }

    @Override
    public void setGold(long gold) {
        this.gold = gold;
    }

    @Override
    public long getGold() {
        return gold;
    }
}
