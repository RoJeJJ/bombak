package com.roje.bombak.room.common.manager;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/8
 **/
public interface RoomIdGenerator {

    long getId();

    void removeId(long id);
}
