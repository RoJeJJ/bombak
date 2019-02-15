package com.roje.bombak.common.error;

import lombok.Getter;

/**
 * @author pc
 */

public enum ErrorMsg {
    /**
     * 成功
     */
    success(0,"成功"),
    /**
     * 数据异常
     */
    protobuf_error(101,"数据异常"),
    /**
     * 无效账号
     */
    invalid_account(102,"无效的账号"),
    /**
     * 用户不存在
     */
    account_not_exist(103,"用户不存在"),
    /**
     * 无效的token
     */
    invalid_token(104,"无效的token"),
    /**
     * 大厅服务器不可用
     */
    lobby_not_found(104,"大厅服务器不可用"),
    /**
     * 没有找到这个游戏
     */
    game_not_found(105,"未找到这个游戏"),
    /**
     * 未知错误,登录失败
     */
    login_failed(106,"登录失败"),
    /**
     * 同一时间只能登录一个网关服务器
     */
    login_multi_gate(107,"同一时间只能登录一个网关服务器"),
    /**
     * 没有登录
     */
    not_login(109,"没有登录"),
    /**
     * 已经在其他地方登录了
     */
    login_elsewhere(110,"账号已经在游戏中了"),

    enter_room_not_found(111,"房间不存在"),

    already_enter_other_room(112,"已经加入其它房间了"),

    enter_room_full(112,"房间满了"),

    enter_room_closed(113,"房间关闭了");

    @Getter
    private int code;

    @Getter
    private String msg;

    ErrorMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
