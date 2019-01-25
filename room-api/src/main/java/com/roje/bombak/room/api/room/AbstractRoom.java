package com.roje.bombak.room.api.room;

import com.google.protobuf.Message;
import com.roje.bombak.common.message.InnerClientMessage;
import com.roje.bombak.common.model.User;
import com.roje.bombak.common.redis.dao.UserRedisDao;
import com.roje.bombak.room.api.constant.Constant;
import com.roje.bombak.room.api.manager.RoomManager;
import com.roje.bombak.room.api.player.Player;
import com.roje.bombak.room.api.proto.RoomMsg;
import com.roje.bombak.room.api.utils.RoomMessageSender;
import io.netty.util.concurrent.EventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/9
 */
@Slf4j
public abstract class AbstractRoom<P extends Player> implements Room<P> {

    private final long id;

    private final long ownerId;

    private final String name;

    private final EventExecutor executor;

    private final RoomMsg.RoomType roomType;

    private final int personSize;

    protected final Map<Long, P> players;

    protected final Map<Integer, P> seatPlayers;

    protected List<P> gamers;

    private boolean gameStart;

    private boolean cardRoundStart;

    private boolean closed;

    private P proposer;

    private List<P> disbandList;

    protected RoomMessageSender sender;

    private final RoomManager roomManager;

    private final UserRedisDao userRedisDao;

    private long startDisbandTime;

    private boolean startDisband;

    protected AbstractRoom(long id, long ownerId, String name, EventExecutor executor, RoomMsg.RoomType type, int personSize,
                           RoomMessageSender sender, RoomManager roomManager, UserRedisDao userRedisDao) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.executor = executor;
        this.roomType = type;
        this.personSize = personSize;
        this.sender = sender;
        this.roomManager = roomManager;
        this.userRedisDao = userRedisDao;
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
    public RoomMsg.RoomType roomType() {
        return roomType;
    }

    public boolean isGameStart() {
        return gameStart;
    }

    public boolean isCardRoundStart() {
        return cardRoundStart;
    }

    @SuppressWarnings("unchecked")
    private void close() {
        closed = true;
        roomManager.removeRoom(this);
    }

    public int getPersonSize() {
        return personSize;
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
        if (roomType == RoomMsg.RoomType.card) {
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
        if (roomType == RoomMsg.RoomType.gold) {
            //金币房,满足人数就可以开始游戏
            start = seatPlayers.size() >= startPersonCount();
        } else if (roomType == RoomMsg.RoomType.card){
            start = checkCardRoundStart();
        }
        if (start) {
            startGame();
        }
    }

    private boolean isInGame(P p) {
        return gamers.contains(p);
    }

    public void applyDisband(P player) {
        if (roomType != RoomMsg.RoomType.card) {
            log.info("只有房卡房才能申请解散");
            return;
        }
        if (!cardRoundStart) {
            //牌局还没开始,可以解散房间
            if (player.uid() == ownerId) {
                //验证身份,只有房主可以解散房间
                close();
                RoomMsg.RoomClosed.Builder builder = RoomMsg.RoomClosed.newBuilder();
                builder.setReason("房间被房主解散");
                sender.send(players.values(), Constant.Indicate.ROOM_CLOSE, builder.build().toByteArray());
            }
        } else {
            log.info("牌局开始了申请解散");
            if (proposer != null) {
                log.info("已经有人申请过解散了,不能再申请");
            } else {
                if (isInGame(player)) {
                    proposer = player;
                    initDisbandVote(proposer);
                    indicateDisbandVote(null);
                }
            }

        }
    }

    private void initDisbandVote(P proposer) {
        disbandList.clear();
        for (P p:gamers) {
            if (p != proposer) {
                p.setDisbandStatus(RoomMsg.DisbandStatus.Wait);
            } else {
                p.setDisbandStatus(RoomMsg.DisbandStatus.Agree);
            }
            disbandList.add(p);
        }
        startDisband = true;
        startDisbandTime = System.currentTimeMillis();
    }

    private void endDisbandVote() {
        proposer = null;
        startDisband = false;
        for (P p:disbandList) {
            p.setDisbandStatus(RoomMsg.DisbandStatus.Def);
        }
        disbandList.clear();
    }

    private void indicateDisbandVote(P player) {
        RoomMsg.DisCardRoomRes.Builder builder = RoomMsg.DisCardRoomRes.newBuilder();
        for (P p:disbandList) {
            RoomMsg.DisbandData.Builder b = RoomMsg.DisbandData.newBuilder();
            b.setUid(p.uid());
            b.setStatus(p.getDisbandStatus());
            builder.addDisbandData(b);
        }
        if (player == null) {
            sender.send(players.values(),Constant.Cmd.DISBAND_CARD_ROOM_RES,builder.build().toByteArray());
        } else {
            sender.send(player,Constant.Cmd.DISBAND_CARD_ROOM_RES,builder.build().toByteArray());
        }
    }

    public void disbandVote(P p, RoomMsg.Vote vote) {
        if (roomType != RoomMsg.RoomType.card) {
            log.info("不是房卡房,没有投票请求");
            return;
        }
        if (proposer == null) {
            log.info("并没有人申请过解散房间");
            return;
        }
        if (p.getDisbandStatus() == RoomMsg.DisbandStatus.Wait) {
            if (vote == RoomMsg.Vote.ConsentVote) {
                p.setDisbandStatus(RoomMsg.DisbandStatus.Agree);
            } else if (vote == RoomMsg.Vote.AgainstVote) {
                p.setDisbandStatus(RoomMsg.DisbandStatus.Refuse);
            } else {
                log.warn("玩家{}投票失败vote{}",p.uid(),vote);
                return;
            }
            RoomMsg.DisbandData.Builder builder = RoomMsg.DisbandData.newBuilder();
            builder.setUid(p.uid());
            builder.setStatus(p.getDisbandStatus());
            sender.send(players.values(), Constant.Cmd.DISBAND_CARD_ROOM_VOTE_RES,builder.build().toByteArray());

            checkVoteResult();
        }
    }

    /**
     * 检查投票结果
     */
    private void checkVoteResult() {
        int agree = 0;
        int refuse = 0;
        int def = 0;
        int total = disbandList.size();
        for (P p:disbandList) {
            if (p.getDisbandStatus() == RoomMsg.DisbandStatus.Agree) {
                agree++;
            } else if (p.getDisbandStatus() == RoomMsg.DisbandStatus.Refuse) {
                refuse++;
            } else {
                def++;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void exit(P p) {
        //玩家在游戏中或者房卡房玩家在牌局中,退出房间不移除玩家数据
        List<P> ps = new ArrayList<>(players.values());
        boolean inGame = p.isInGame() || (roomType == RoomMsg.RoomType.card && isInGame(p));
        if (inGame) {
            p.setExit(true);
            RoomMsg.PlayerExit.Builder builder = RoomMsg.PlayerExit.newBuilder();
            builder.setUid(p.uid());
            sender.send(ps, Constant.Indicate.PLAYER_EXIT,builder.build().toByteArray());
        } else {
            players.remove(p.uid());
            roomManager.removeRoomPlayer(this,p);
            RoomMsg.ExitRoomRes.Builder builder = RoomMsg.ExitRoomRes.newBuilder();
            builder.setUid(p.uid());
            if (p.seat() != 0) {
                //在座位上
                seatPlayers.remove(p.seat());
                sender.send(ps,Constant.Cmd.EXIT_ROOM_RES,builder.build().toByteArray());
            } else {
                //不在座位上,通知本人就可以了
                sender.send(p,Constant.Cmd.EXIT_ROOM_RES,builder.build().toByteArray());
            }
        }
    }

    public void getUp(P p) {
        //此判断跟退出房间类似,玩家在游戏中或者房卡房玩家在牌局中,不能离开座位
        boolean up = p.isInGame() || (roomType == RoomMsg.RoomType.card && isInGame(p));
        if (up) {
            RoomMsg.GetUpRes.Builder builder = RoomMsg.GetUpRes.newBuilder();
            builder.setUid(p.uid());
            sender.send(players.values(), Constant.Cmd.GET_UP_RES,builder.build().toByteArray());
        } else {
            log.info("现在不能离开座位");
        }
    }

    public void sitDown(P p, int seat) {
        if (seat < 1 || seat > personSize) {
            log.info("座位号错误");
            return;
        }
        if (p.seat() != 0) {
            if (p.seat() == seat) {
                log.info("已经坐下了");
            } else {
                log.info("请先离开座位");
            }
            return;
        }
        p.sit(seat);
        seatPlayers.put(seat, p);
        List<P> others = new ArrayList<>(players.values());
        others.remove(p);
        RoomMsg.SitDownRes.Builder builder = RoomMsg.SitDownRes.newBuilder();
        builder.setPlayerData(p.playerDataToSelf().toByteString());
        sender.send(p,Constant.Cmd.SIT_DOWN_RES,builder.build().toByteArray());
        builder.setPlayerData(p.playerDataToOthers().toByteString());
        sender.send(others,Constant.Cmd.SIT_DOWN_RES, builder.build().toByteArray());

        checkStart();
    }

    /**
     * 初始化玩家加入房间
     * @param player 玩家
     */
    protected abstract void initializeJoin(P player);


    private void playerJoin(P player){
        if (player.getDisbandStatus() == RoomMsg.DisbandStatus.Wait) {
            indicateDisbandVote(player);
        }
        onPlayerJoin(player);
    }


    /**
     * 房间数据
     * @param player 接收房间数据的玩家
     * @return protobuf 序列化的数据
     */
    protected abstract Message roomData(P player);

    @Override
    public boolean join(InnerClientMessage message) {
        P player = players.get(message.getUid());
        boolean reconnect;
        if (player != null) {
            log.info("玩家{}加入房间,并且玩家之前已经在房间中");
            if (!player.isExit() && !player.isOffline()) {
                log.info("重复进入房间了");
                sender.sendError(message, Constant.ErrorCode.PLAYER_ALREADY_IN_ROOM);
                return false;
            }
            reconnect = true;
            log.info("玩家{}重新连接到房间", player.uid());
            if (player.isOffline()) {
                player.setOffline(false);
            }
            if (player.isExit()) {
                player.setExit(false);
            }
            if (player.seat() != 0) {
                //之前就坐在座位上
                List<P> players = new ArrayList<>(this.players.values());
                players.remove(player);
                RoomMsg.PlayerOnline.Builder builder = RoomMsg.PlayerOnline.newBuilder();
                builder.setUid(player.uid());
                sender.send(players,Constant.Indicate.PLAYER_ONLINE,builder.build().toByteArray());
            }
        } else {
            if (personSize >= roomManager.getRoomMaxPlayer()) {
                log.info("房间满了");
                sender.sendError(message,Constant.ErrorCode.ROOM_FULL);
                return false;
            }
            reconnect = false;
            player = newPlayer(message.getUid());
            players.put(player.uid(),player);
            initializeJoin(player);
            log.info("玩家{}新加入房间{}", player.uid(), id);
        }
        updateUserInfo(player,reconnect);
        updatePlayerServiceInfo(player, message);

        RoomMsg.JoinRoomRes.Builder builder = RoomMsg.JoinRoomRes.newBuilder();
        builder.setGameName(roomManager.getGameName());
        builder.setRoomData(roomData(player).toByteString());
        sender.send(player,Constant.Cmd.JOIN_ROOM_RES,builder.build().toByteArray());
        playerJoin(player);
        return !reconnect;
    }

    /**
     * 玩家加入房间
     * @param player 玩家
     */
    protected abstract void onPlayerJoin(P player);

    private void updateUserInfo(P p,boolean rec) {
        User user = userRedisDao.getUser(p.uid());
        p.setNickname(user.getNickname());
        p.setHeadImg(user.getHeadImg());
        if (!rec) {
            p.setRoomCard(user.getRoomCard());
            p.setGold(user.getGold());
        }
    }

    private void updatePlayerServiceInfo(P player, InnerClientMessage message) {
        player.setServiceType(message.getSenderServiceType());
        player.setServiceId(message.getSenderServiceId());
    }

}
