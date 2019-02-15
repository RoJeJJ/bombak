package com.roje.bombak.room.common.room;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.roje.bombak.room.common.constant.RoomConstant;
import com.roje.bombak.room.common.player.Player;
import com.roje.bombak.room.common.player.VoteStatus;
import com.roje.bombak.room.common.proto.RoomMsg;
import com.roje.bombak.room.common.utils.RoomMessageSender;
import io.netty.util.concurrent.EventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/9
 */
@Slf4j
public abstract class BaseRoom<P extends Player> implements Room<P> {

    private static final int VOTE_TIME = 60;

    private final long id;

    private final long ownerId;

    private final String name;

    private final EventExecutor executor;

    private final RoomType roomType;

    private final int seatCount;

    protected final Map<Long, P> players;

    protected final Map<Integer, P> seatPlayers;

    protected List<P> gamers;

    private boolean gameStart;

    private boolean cardRoundStart;

    private boolean closed;

    private P proposer;

    private List<P> disbandList;

    protected RoomMessageSender sender;

    private final RoomListener<P,Room<P>> listener;

    private long startDisbandTime;

    private final int maxPerson;

    private final String gameType;

    private Future disbandTask;

    protected BaseRoom(long id, long ownerId, String name, String gameType, RoomType type, int seatCount,
                       int maxPerson, EventExecutor executor, RoomMessageSender sender, RoomListener<P,Room<P>> listener) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.gameType = gameType;
        this.executor = executor;
        this.roomType = type;
        this.seatCount = seatCount;
        this.maxPerson = maxPerson;
        this.sender = sender;
        this.listener = listener;
        players = new HashMap<>();
        seatPlayers = new HashMap<>();
        gamers = new ArrayList<>();
        disbandList = new ArrayList<>();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public long id() {
        return id;
    }

    public long ownerId() {
        return ownerId;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public EventExecutor executor() {
        return executor;
    }

    @Override
    public P getPlayer(long uid) {
        return players.get(uid);
    }

    @Override
    public RoomType roomType() {
        return roomType;
    }

    public boolean isGameStart() {
        return gameStart;
    }

    public boolean isCardRoundStart() {
        return cardRoundStart;
    }

    private void close(String reason) {
        closed = true;
        if (disbandTask != null) {
            disbandTask.cancel(true);
        }
        onClosed();
        listener.roomClosed(this);
        RoomMsg.RoomClosed.Builder builder = RoomMsg.RoomClosed.newBuilder();
        builder.setReason(reason);
        sender.sendMsg(players.values(), RoomConstant.Notice.ROOM_CLOSE, builder.build());
    }

    public P getSeat(int seat) {
        return seatPlayers.get(seat);
    }

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
     * 开始游戏
     */
    protected abstract void startGame0();

    /**
     * 开始游戏
     */
    private void startGame() {
        gameStart = true;
        if (roomType == RoomType.card) {
            if (!cardRoundStart) {
                cardRoundStart = true;
            }
        }
        gamers.clear();
        gamers = new ArrayList<>(seatPlayers.values());
        log.info("开始游戏");
        startGame0();
    }

    public void checkStart() {
        if (gameStart) {
            return;
        }
        boolean start = false;
        if (roomType == RoomType.gold) {
            //金币房,满足人数就可以开始游戏
            start = seatPlayers.size() >= startPersonCount();
        } else if (roomType == RoomType.card){
            start = checkCardRoundStart();
        }
        if (start) {
            startGame();
        }
    }


    public void applyDisband(P player) {
        if (roomType != RoomType.card) {
            log.info("只有房卡房才能申请解散");
            return;
        }
        if (!cardRoundStart) {
            //牌局还没开始,可以解散房间
            if (player.uid() == ownerId) {
                //验证身份,只有房主可以解散房间
                close("房间被房主解散");
            }
        } else {
            log.info("牌局开始了,申请解散");
            if (proposer != null) {
                log.info("已经有人申请过解散了,不能再申请");
            } else {
                if (player.isInGame()) {
                    initDisbandVote(proposer);
                    indicateDisbandVote(null);
                    disbandTask = executor.schedule(this::disbandTimeout,VOTE_TIME, TimeUnit.SECONDS);
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
        for (P p:disbandList) {
            if (p.getVoteStatus() == VoteStatus.wait) {
                p.setVoteStatus(VoteStatus.refuse);
            }
        }
    }

    private void initDisbandVote(P proposer) {
        this.proposer = proposer;
        disbandList.clear();
        for (P p:gamers) {
            if (p != proposer) {
                p.setVoteStatus(VoteStatus.wait);
            } else {
                p.setVoteStatus(VoteStatus.agree);
            }
            disbandList.add(p);
        }
        startDisbandTime = System.currentTimeMillis();
    }

    private void endDisbandVote() {
        proposer = null;
        for (P p:disbandList) {
            p.setVoteStatus(VoteStatus.def);
        }
        disbandList.clear();
        if (disbandTask != null) {
            disbandTask.cancel(false);
        }
    }

    private void indicateDisbandVote(P player) {
        int reTime = VOTE_TIME - (int)(System.currentTimeMillis() - startDisbandTime) / 1000;
        RoomMsg.DisCardRoomRes.Builder builder = RoomMsg.DisCardRoomRes.newBuilder();
        builder.setTime(reTime);
        for (P p:disbandList) {
            RoomMsg.DisbandData.Builder b = RoomMsg.DisbandData.newBuilder();
            b.setUid(p.uid());
            b.setStatus(p.getVoteStatus().getCode());
            builder.addDisbandData(b);
        }
        if (player == null) {
            sender.sendMsg(players.values(), RoomConstant.Cmd.DISBAND_CARD_ROOM_RES,builder.build());
        } else {
            sender.sendMsg(player, RoomConstant.Cmd.DISBAND_CARD_ROOM_RES,builder.build());
        }
    }

    public void disbandVote(P p, boolean vote) {
        if (roomType != RoomType.card) {
            log.info("不是房卡房,没有投票请求");
            return;
        }
        if (proposer == null) {
            log.info("并没有人申请过解散房间");
            return;
        }
        if (p.getVoteStatus() == VoteStatus.wait) {
            if (vote) {
                p.setVoteStatus(VoteStatus.agree);
            } else {
                p.setVoteStatus(VoteStatus.refuse);
            }
            RoomMsg.DisbandData.Builder builder = RoomMsg.DisbandData.newBuilder();
            builder.setUid(p.uid());
            builder.setStatus(p.getVoteStatus().getCode());
            sender.sendMsg(players.values(), RoomConstant.Cmd.DISBAND_CARD_ROOM_VOTE_RES,builder.build());

            checkVoteResult();
        }
    }

    /**
     * 检查投票结果,只要有人拒绝,就取消解散
     */
    private void checkVoteResult() {
        //未投票人数
        int def = 0;
        for (P p:disbandList) {
            if (p.getVoteStatus() == VoteStatus.refuse) {
                endDisbandVote();
                return;
            } else if (p.getVoteStatus() == VoteStatus.wait) {
                def++;
            }
        }
        if (def == 0) {
            close("房间已被解散");
        }
    }

    public void exit(P p) {
        //玩家在游戏中或者房卡房玩家在牌局中,退出房间不移除玩家数据
        List<P> ps = new ArrayList<>(players.values());
        if (p.isInGame()) {
            p.setExit(true);
            RoomMsg.PlayerExit.Builder builder = RoomMsg.PlayerExit.newBuilder();
            builder.setUid(p.uid());
            sender.sendMsg(ps, RoomConstant.Notice.PLAYER_EXIT,builder.build());
        } else {
            players.remove(p.uid());
            listener.leaveRoom(p,this);
            RoomMsg.ExitRoomRes.Builder builder = RoomMsg.ExitRoomRes.newBuilder();
            builder.setUid(p.uid());
            if (p.getSeat() != 0) {
                //在座位上
                seatPlayers.remove(p.getSeat());
                sender.sendMsg(ps, RoomConstant.Cmd.EXIT_ROOM_RES,builder.build());
            } else {
                //不在座位上,通知本人就可以了
                sender.sendMsg(p, RoomConstant.Cmd.EXIT_ROOM_RES,builder.build());
            }
        }
    }

    public void getUp(P p) {
        //此判断跟退出房间类似,玩家在游戏中或者房卡房玩家在牌局中,不能离开座位
        if (p.isInGame()) {
            RoomMsg.GetUpRes.Builder builder = RoomMsg.GetUpRes.newBuilder();
            builder.setUid(p.uid());
            sender.sendMsg(players.values(), RoomConstant.Cmd.GET_UP_RES,builder.build());
        } else {
            log.info("现在不能离开座位");
        }
    }

    public void sitDown(P p,int seat) {
        if (seat < 1 || seat > seatCount) {
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
        sender.sendMsg(p, RoomConstant.Cmd.SIT_DOWN_RES,builder.build());
        builder.setPlayerData(Any.pack(p.playerData(null)));
        sender.sendMsg(others, RoomConstant.Cmd.SIT_DOWN_RES, builder.build());

        checkStart();
    }

    /**
     * 房间关闭
     */
    protected abstract void onClosed();

    /**
     * 初始化玩家加入房间
     * @param player 玩家
     */
    protected abstract void initJoin(P player);

    /**
     * 玩家加入房间
     * @param player 玩家
     */
    protected abstract void onPlayerJoin(P player);


    /**
     * 房间数据
     * @param player 接收房间数据的玩家
     * @return protobuf 序列化的数据
     */
    protected abstract Message roomData(P player);

    @Override
    public boolean requestJoin(P player) {
        if (players.containsValue(player)) {
            if (player.isOffline()) {
                log.info("玩家{}在房间{}中上线了",player.uid(),id);
                player.setOffline(false);
            }
            if (player.isExit()) {
                log.info("玩家{}从新进入了房间{}",player.uid(),id);
                player.setExit(false);
            }
            if (player.getSeat() != 0) {
                //已经坐下
                List<P> senders = new ArrayList<>(players.values());
                senders.remove(player);
                RoomMsg.ReJoin.Builder builder = RoomMsg.ReJoin.newBuilder();
                builder.setUid(player.uid());
                sender.sendMsg(senders, RoomConstant.Notice.REJOIN,builder.build());
            }
        } else {
            if (players.size() >= maxPerson) {
                log.info("房间满了");
                sender.sendErrMsg(player,RoomConstant.ErrorCode.ROOM_FULL);
                return false;
            }
            players.put(player.uid(),player);
            initJoin(player);
        }
        RoomMsg.JoinRoomRes.Builder builder = RoomMsg.JoinRoomRes.newBuilder();
        builder.setGameType(gameType);
        builder.setRoomData(Any.pack(roomData(player)));
        sender.sendMsg(player,RoomConstant.Cmd.JOIN_ROOM_RES,builder.build());

        if (proposer != null) {
            indicateDisbandVote(player);
        }
        onPlayerJoin(player);
        return true;
    }


}
