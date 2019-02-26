package com.roje.bombak.gate.process;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.common.constant.Constant;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.common.redis.dao.UserRedisDao;
import com.roje.bombak.common.utils.MessageSender;
import com.roje.bombak.gate.constant.GateConstant;
import com.roje.bombak.gate.manager.GateSessionManager;
import com.roje.bombak.gate.processor.GateProcessor;
import com.roje.bombak.gate.proto.GateMsg;
import com.roje.bombak.gate.session.GateSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/7
 **/
@Slf4j
@Component
@Message(id = GateConstant.Cmd.LOGIN_REQ)
public class LoginReqProcessor implements GateProcessor {

    private final RedissonClient redissonClient;

    private final UserRedisDao userRedisDao;

    private final ServiceInfo gateInfo;

    private final RestTemplate restTemplate;

    private final GateSessionManager sessionManager;

    private final MessageSender sender;

    public LoginReqProcessor(@Qualifier("redissonSingle") RedissonClient redissonClient,
                             UserRedisDao userRedisDao, ServiceInfo gateInfo,
                             GateSessionManager sessionManager, MessageSender sender) {
        this.redissonClient = redissonClient;
        this.userRedisDao = userRedisDao;
        this.gateInfo = gateInfo;
        this.sessionManager = sessionManager;
        this.sender = sender;
        restTemplate = new RestTemplate();
    }

    @Override
    public void process(GateSession session, ServerMsg.ClientToGateMessage message) throws Exception {
        GateMsg.LoginRequest request = GateMsg.LoginRequest.parseFrom(message.getData().toByteArray());
        long uid = request.getUid();
        String token = request.getToken();

        if (StringUtils.isBlank(token)) {
            log.info("token不能为空");
            session.send(sender.scErrMsg(GateConstant.ErrorCode.EMPTY_TOKEN));
            return;
        }
        String accToken = userRedisDao.getToken(uid);
        if (StringUtils.isBlank(accToken)) {
            log.info("用户不存在或者未登录");
            session.send(sender.scErrMsg(GateConstant.ErrorCode.USER_NOT_FOUND));
            return;
        }
        if (!StringUtils.equals(token, accToken)) {
            log.info("token不一致");
            session.send(sender.scErrMsg(GateConstant.ErrorCode.INVALID_TOKEN));
            return;
        }

        RLock lock = redissonClient.getLock("gate-lock-" + uid);
        boolean locked = lock.tryLock();
        if (!locked) {
            log.info("正在登录其他服务器");
            session.send(sender.scErrMsg(GateConstant.ErrorCode.LOGIN_ANOTHER_GATE));
            return;
        }
        try {
            ServiceInfo otherGateInfo = userRedisDao.getGateInfo(uid);
            if (otherGateInfo != null && !otherGateInfo.equals(gateInfo)) {
                log.info("已经登录其他服务器了,通知其他服务器下线该账号");
                Map<String, Object> var = new HashMap<>(1);
                var.put("uid", uid);
                String resp = restTemplate.postForObject("http://" + gateInfo.getIp() + ":" + gateInfo.getPort() + "/kickOut?uid={uid}",
                        null, String.class, var);
                log.info(resp);
            }
            sessionManager.login(session,uid);
            session.send(sender.scMsg(GateConstant.GATE_MSG,GateConstant.Cmd.LOGIN_RES));
            GateMsg.LoginResponse.Builder builder = GateMsg.LoginResponse.newBuilder();
            builder.setUid(uid);
            sender.allServerMsg(GateConstant.GATE_MSG, Constant.Cmd.LOGIN_BROADCAST,builder.build());
        } finally {
            lock.unlock();
        }
    }
}
