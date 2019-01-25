package com.roje.bombak.login.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

/**
 * @author pc
 * @version 1.0
 * @date 2018/12/10
 **/
public enum ResponseData {
    /**
     * 成功
     */
    success(0, "成功"),
    /**
     * 账号不能为空
     */
    account_is_blank(101, "账号不能为空"),
    /**
     * 密码不能为空
     */
    password_is_blank(102, "密码不能为空"),
    /**
     * 账号或密码错误
     */
    account_or_password_error(103,"账号或密码错误"),
    /**
     * 账号被禁止登录
     */
    login_banned(104,"账号禁止登录,请联系管理员"),
    /**
     *网关服务器不可用
     */
    gate_not_found(105,"网关服务器不可用"),
    /**
     * 注册密码不够长度
     */
    register_password_len_not_enough(106,"注册密码不够长度"),
    /**
     * 用户名已经存在
     */
    register_account_exists(107,"用户名已经存在"),
    /**
     *
     */
    register_error(108,"注册失败");

    @Getter
    private int code;
    @Getter
    private String msg;

    @Setter@Getter
    private long uid;

    @Setter@Getter
    private String token;

    @Getter@Setter
    private String ip;

    @Getter@Setter
    private int port;

    ResponseData(int i, String msg) {
        code = i;
        this.msg = msg;
    }

    public String buildJsonString() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode resp = mapper.createObjectNode();
        resp.put("code",code);
        resp.put("msg",msg);
        if (code == 0) {
            if (uid != 0) {
                resp.put("uid",uid);
            }
            if (!StringUtils.isBlank(token)) {
                resp.put("token",token);
            }
            if (!StringUtils.isBlank(ip) && port > 0) {
                resp.put("ip",ip);
                resp.put("port",port);
            }
        }
        return resp.toString();
    }
}
