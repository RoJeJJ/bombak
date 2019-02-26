package com.roje.bombak.room.common.room;

import com.google.protobuf.Any;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.manager.RoomManager;
import com.roje.bombak.room.common.player.BasePlayer;
import com.roje.bombak.room.common.player.VoteStatus;
import com.roje.bombak.room.common.proto.RoomMsg;
import com.roje.bombak.room.common.utils.RoomMessageSender;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/9
 */
@Slf4j
@SuppressWarnings("unchecked")
public abstract class BaseRoom<P extends BasePlayer> implements Room<P> {

    /**
     * 房间id
     */
    private long id;
    /**
     * 房主id
     */
    private long ownerId;
    /**
     * 房间名称
     */
    private String name;

    private String gameType;
    /**
     * 房间任务执行器
     */
    private EventExecutor executor;
    /**
     * 房间类型
     */
    private int roomType;

    private int seatSize;

    private int capacity;

    protected final Map<Long, P> players;

    protected final Map<Integer, P> seatPlayers;

    protected List<P> gamers;

    private boolean gameStart;

    private boolean cardRoundStart;

    private boolean closed;

    private P proposer;

    private ScheduledFuture disbandTask;

    private final RoomManager manager;

    protected RoomMessageSender sender;

    private long waitVoteTime;

    private int round;

//    public BaseRoom(long id, long ownerId, String name, String gameType, RoomType type, int seatCount,
//                       int maxPerson, EventExecutor executor, RoomMessageSender sender, RoomListener<P,Room<P>> listener) {
//        this.id = id;
//        this.ownerId = ownerId;
//        this.name = name;
//        this.gameType = gameType;
//        this.executor = executor;
//        this.roomType = type;
//        this.seatCount = seatCount;
//        this.maxPerson = maxPerson;
//        this.sender = sender;
//        this.listener = listener;
//        players = new HashMap<>();
//        seatPlayers = new HashMap<>();
//        gamers = new ArrayList<>();
//        disbandList = new ArrayList<>();
//    }

    public BaseRoom(long id, RoomManager manager) {
        this.id = id;
        players = new HashMap<>();
        seatPlayers = new HashMap<>();
        gamers = new ArrayList<>();
        this.manager = manager;
    }

    @Override
    public void setSender(RoomMessageSender sender) {
        this.sender = sender;
    }

    @Override
    public RoomMessageSender getSender() {
        return sender;
    }

    @Override
    public void setExecutor(EventExecutor executor) {
        this.executor = executor;
    }

    public int getRound() {
        return round;
    }

    public int getSeatSize() {
        return seatSize;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getOwnerId() {
        return ownerId;
    }

    @Override
    public void setWaitVoteTime(long waitVoteTime) {
        this.waitVoteTime = waitVoteTime;
    }

    @Override
    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public void setSeatSize(int seatSize) {
        this.seatSize = seatSize;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public P getPlayer(long uid) {
        return players.get(uid);
    }

    public boolean isGameStart() {
        return gameStart;
    }

    public boolean isCardRoundStart() {
        return cardRoundStart;
    }

    @Override
    public int getPlayerSize() {
        return players.size();
    }

    @Override
    public EventExecutor getExecutor() {
        return executor;
    }

    protected void foreachGamers(Consumer<P> consumer) {
        gamers.forEach(consumer);
    }

    private void sendRoomData(P player) {
        RoomMsg.JoinRoomRes.Builder builder = RoomMsg.JoinRoomRes.newBuilder();
        builder.setGameType(gameType);
        builder.setRoomData(Any.pack(roomData(player)));
        sender.sendMsgToGate(player, RoomConstant.Cmd.JOIN_ROOM_RES,builder.build());
    }

    @Override
    public void reJoin(P player, String sessionId) {
        player.setExit(false);
        player.setSessionId(sessionId);
        player.setGateInfo(manager.getUserGateInfo(player.getUid()));
        if (player.getSeat() != 0) {
            RoomMsg.PlayerInfo.Builder builder = RoomMsg.PlayerInfo.newBuilder();
            builder.setUid(player.getUid());
            sender.sendMsgToGate(getPlayersExcept(player),RoomConstant.Notice.REJOIN,builder.build());
        }
        sendRoomData(player);
        noticeDisbandVote(player);
        onNoticePlayer(player);
    }

    private Collection<P> getPlayersExcept(P player) {
        Collection<P> ps = new ArrayList<>(players.values());
        ps.remove(player);
        return ps;
    }

    @Override
    public void online(P player, String sessionId) {
        player.setOffline(false);
        player.setSessionId(sessionId);
        player.setGateInfo(manager.getUserGateInfo(player.getUid()));
        if (player.getSeat() != 0) {
            RoomMsg.PlayerInfo.Builder builder = RoomMsg.PlayerInfo.newBuilder();
            builder.setUid(player.getUid());
            sender.sendMsgToGate(getPlayersExcept(player),RoomConstant.Notice.ONLINE,builder.build());
        }
        sendRoomData(player);
        noticeDisbandVote(player);
        onNoticePlayer(player);
    }

    @Override
    public void join(long uid,String sessionId) {
        P player = newPlayer(uid);
        player.setSessionId(sessionId);
        player.setUser(manager.getUser(uid));
        player.setGateInfo(manager.getUserGateInfo(uid));
        players.put(player.getUid(),player);
        manager.playerJoinedRoom(player,this);
        sendRoomData(player);
        noticeDisbandVote(player);
        onNoticePlayer(player);
    }

    private void close(String reason) {
        closed = true;
        endDisbandVote();
        onClosed();
        manager.roomClosed(this);
        RoomMsg.RoomClosed.Builder builder = RoomMsg.RoomClosed.newBuilder();
        builder.setReason(reason);
        sender.sendMsgToGate(players.values(), RoomConstant.Notice.ROOM_CLOSE, builder.build());
    }

    public P getSeat(int seat) {
        return seatPlayers.get(seat);
    }


    /**
     * 开始游戏
     */
    private void startGame() {
        gameStart = true;
        if (roomType == CARD) {
            if (!cardRoundStart) {
                cardRoundStart = true;
            }
            round++;
        }
        gamers.clear();
        gamers = new ArrayList<>(seatPlayers.values());
        gamers.sort(Comparator.comparingInt(BasePlayer::getSeat));
        log.info("开始游戏");
        sender.sendMsgToGate(players.values(), RoomConstant.Cmd.START_GAME_RES);

        for (P p:gamers) {
            p.newGame();
        }
        onStartGame();
    }

    public void checkStart() {
        if (gameStart) {
            return;
        }
        boolean start = false;
        if (roomType == GOLD) {
            //金币房,满足人数就可以开始游戏
            start = seatPlayers.size() >= startPersonCount();
        } else if (roomType == CARD){
            start = checkCardRoundStart();
        }
        if (start) {
            startGame();
        }
    }


    /**
     *解散申请
     */
    public void applyDisband(P player) {
        if (roomType != CARD) {
            log.info("还有房卡房才能解散房间");
            return;
        }
        if (!cardRoundStart) {
            //牌局还没开始,可以解散房间
            if (player.getUid() == ownerId) {
                //验证身份,只有房主可以解散房间
                close("房间被房主解散");
            }
        } else {
            log.info("牌局已经开始了,申请解散");
            if (proposer != null || disbandTask != null) {
                log.info("已经有人申请过解散了,不能再申请");
            } else {
                if (player.isInGame()) {
                    //游戏中的玩家才能申请解散房间
                    initDisbandVote(proposer);
                    disbandTask = executor.schedule(this::disbandTimeout, waitVoteTime, TimeUnit.SECONDS);
                    noticeDisbandVote(null);
                }
            }

        }
    }

    /**
     * 解散超时
     */
    private void disbandTimeout() {
        if (proposer == null) {
            return;
        }
        for (P p:gamers) {
            if (p.getVoteStatus() == VoteStatus.wait) {
                p.setVoteStatus(VoteStatus.refuse);
            }
        }
        checkVoteResult();
    }

    private void initDisbandVote(P proposer) {
        this.proposer = proposer;
        for (P p:gamers) {
            if (p != proposer) {
                p.setVoteStatus(VoteStatus.wait);
            } else {
                p.setVoteStatus(VoteStatus.agree);
            }
        }
    }

    private void endDisbandVote() {
        proposer = null;
        for (P p:gamers) {
            if (p.getVoteStatus() != VoteStatus.def) {
                p.setVoteStatus(VoteStatus.def);
            }
        }
        if (disbandTask != null) {
            disbandTask.cancel(false);
            disbandTask = null;
        }
    }

    private void noticeDisbandVote(P player) {
        if (proposer == null || disbandTask == null) {
            return;
        }
        RoomMsg.DisbandRoomRes.Builder builder = RoomMsg.DisbandRoomRes.newBuilder();
        builder.setTime(disbandTask.getDelay(TimeUnit.MILLISECONDS));
        for (P p:gamers) {
            if (p.getVoteStatus() != VoteStatus.def) {
                RoomMsg.DisbandData.Builder b = RoomMsg.DisbandData.newBuilder();
                b.setUid(p.getUid());
                b.setStatus(p.getVoteStatus().getCode());
                builder.addDisbandData(b);
            }
        }
        if (player == null) {
            sender.sendMsgToGate(players.values(), RoomConstant.Cmd.DISBAND_CARD_ROOM_RES,builder.build());
        } else {
            sender.sendMsgToGate(player, RoomConstant.Cmd.DISBAND_CARD_ROOM_RES,builder.build());
        }
    }

    public void disbandVote(P p, boolean vote) {
        if (proposer == null || disbandTask == null) {
            log.info("投票现在不可用");
            return;
        }
        if (p.getVoteStatus() == VoteStatus.wait) {
            if (vote) {
                p.setVoteStatus(VoteStatus.agree);
            } else {
                p.setVoteStatus(VoteStatus.refuse);
            }
            RoomMsg.DisbandData.Builder builder = RoomMsg.DisbandData.newBuilder();
            builder.setUid(p.getUid());
            builder.setStatus(p.getVoteStatus().getCode());
            sender.sendMsgToGate(players.values(), RoomConstant.Cmd.DISBAND_CARD_ROOM_VOTE_RES,builder.build());

            checkVoteResult();
        }
    }

    /**
     * 检查投票结果,只要有人拒绝,就取消解散
     */
    private void checkVoteResult() {
        //未投票人数
        int wait = 0;
        int all = 0;
        int agree = 0;
        for (P p:gamers) {
            if (p.getVoteStatus() == VoteStatus.refuse) {
                endDisbandVote();
                return;
            } else if (p.getVoteStatus() == VoteStatus.wait) {
                wait++;
                all++;
            } else if (p.getVoteStatus() == VoteStatus.agree) {
                agree++;
            }
        }
        if (wait == 0) {
            endDisbandVote();
            close("房间已被解散");
        }
    }

    public void exit(P p) {
        //玩家在游戏中或者房卡房玩家在牌局中,退出房间不移除玩家数据
        List<P> ps = new ArrayList<>(players.values());
        if (p.isInGame()) {
            p.setExit(true);
            RoomMsg.PlayerInfo.Builder builder = RoomMsg.PlayerInfo.newBuilder();
            builder.setUid(p.getUid());
            sender.sendMsgToGate(ps, RoomConstant.Notice.PLAYER_EXIT,builder.build());
        } else {
            players.remove(p.getUid());
            manager.playerLeaveRoom(p,this);
            onPlayerExit(p);
            RoomMsg.PlayerInfo.Builder builder = RoomMsg.PlayerInfo.newBuilder();
            builder.setUid(p.getUid());
            if (p.getSeat() != 0) {
                //在座位上
                seatPlayers.remove(p.getSeat());
                sender.sendMsgToGate(ps, RoomConstant.Cmd.EXIT_ROOM_RES,builder.build());
            } else {
                //不在座位上,通知本人就可以了
                sender.sendMsgToGate(p, RoomConstant.Cmd.EXIT_ROOM_RES,builder.build());
            }
        }
    }

    public void getUp(P p) {
        //此判断跟退出房间类似,玩家在游戏中或者房卡房玩家在牌局中,不能离开座位
        if (!p.isInGame()) {
            if (p.getSeat() != 0) {
                p.setSeat(0);
                RoomMsg.PlayerInfo.Builder builder = RoomMsg.PlayerInfo.newBuilder();
                builder.setUid(p.getUid());
                sender.sendMsgToGate(players.values(), RoomConstant.Cmd.GET_UP_RES,builder.build());
            } else {
                log.info("先选个座位坐下");
            }
        } else {
            log.info("现在不能离开座位");
        }
    }

    /**
     * 坐下
     */
    public void sitDown(P p,int seat) {
        if (seat < 1 || seat > seatSize) {
            log.info("无效的座位号");
            return;
        }
        if (p.getSeat() != 0) {
            log.info("已经坐下了");
            return;
        }
        if (seatPlayers.get(seat) != null) {
            log.info("座位上有人了");
            return;
        }
        p.setSeat(seat);
        seatPlayers.put(seat, p);
        List<P> others = new ArrayList<>(players.values());
        others.remove(p);
        RoomMsg.SitDownRes.Builder builder = RoomMsg.SitDownRes.newBuilder();
        builder.setPlayerData(Any.pack(p.playerData(p)));
        sender.sendMsgToGate(p, RoomConstant.Cmd.SIT_DOWN_RES,builder.build());
        builder.setPlayerData(Any.pack(p.playerData(null)));
        sender.sendMsgToGate(others, RoomConstant.Cmd.SIT_DOWN_RES, builder.build());

        checkStart();
    }

    protected boolean minusGoldIfEnough(P player,int gold) {
        return manager.minusGoldIfEnough(player,gold);
    }

    /**
     * 房间关闭
     */
    protected abstract void onClosed();

    /**
     * 玩家加入房间
     * @param player 玩家
     */
    protected abstract void onNoticePlayer(P player);

    /**
     * 生成新的玩家对象
     * @param uid 玩家id
     * @return 玩家
     */
    protected abstract P newPlayer(long uid);

    /**
     * 开始游戏需要的最小人数
     *
     * @return 人数
     */
    protected abstract int startPersonCount();

    /**
     * 房卡房游戏开始判断
     *
     * @return 能够开始返回true, 否则返回false
     */
    protected abstract boolean checkCardRoundStart();

    /**
     * 游戏开始
     */
    protected abstract void onStartGame();

    protected abstract void onPlayerExit(P player);
    
}
