package com.roje.bombak.gate.manager;

import com.roje.bombak.gate.session.GateSession;


/**
 * @author pc
 */
public interface GateSessionManager {


    /**
     * session登录
     * @param session 登录的session
     * @param uid 登录的uid
     * @return 返回旧session
     */
    void login(GateSession session,long uid);

    /**
     * 根据提供的ID获取session
     * @param id id
     * @return session
     */
    GateSession getSession(long id);

    /**
     * 关闭session
     * @param session session
     */
    void closeSession(GateSession session);
}
