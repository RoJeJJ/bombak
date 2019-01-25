package com.roje.bombak.common.mq;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author pc
 */
@Getter@Setter
public class MqMessage implements Serializable {
    private static final long serialVersionUID = 4151991097245480229L;
    private long uid;
    private int action;
    private byte[] data;
    private int senderId;

    public MqMessage(){}

    public MqMessage(long uid, int action, byte[] data){
        this.uid = uid;
        this.data = data;
        this.action = action;
    }

}
