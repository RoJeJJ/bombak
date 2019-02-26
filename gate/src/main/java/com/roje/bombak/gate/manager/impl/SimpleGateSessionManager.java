package com.roje.bombak.gate.manager.impl;

import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.redis.dao.UserRedisDao;
import com.roje.bombak.gate.manager.GateSessionManager;
import com.roje.bombak.gate.session.GateSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/2
 **/
@Slf4j
@Component
public class SimpleGateSessionManager implements GateSessionManager {

    private final Map<Long,GateSession> sessions = new ConcurrentHashMap<>();

    private final Map<String,GateSession> sessionIds = new ConcurrentHashMap<>();

    private final UserRedisDao userRedisDao;

    private final ServiceInfo gateInfo;

    public SimpleGateSessionManager(UserRedisDao userRedisDao, ServiceInfo gateInfo) {
        this.userRedisDao = userRedisDao;
        this.gateInfo = gateInfo;
    }

    @Override
    public void login(GateSession session,long uid) {
        session.login(uid);
        GateSession old = sessions.put(uid,session);
        if (old != null) {
            old.close();
        } else {
            sessionIds.put(session.id(),session);
            userRedisDao.setGateInfo(uid, gateInfo);
        }
    }

    @Override
    public GateSession getSession(String id) {
        return sessionIds.get(id);
    }

    @Override
    public GateSession getSession(long uid) {
        return sessions.get(uid);
    }

    @Override
    public void closeSession(GateSession session) {
        sessions.remove(session.uid());
        sessionIds.remove(session.id());
        userRedisDao.removeGateInfo(session.uid());
        session.close();
    }
}
