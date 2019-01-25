package com.roje.bombak.common.message;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author pc
 * @version 1.0
 * @date 2018/12/29
 **/
@Getter@Setter
public class InnerClientMessage implements Serializable {

    private static final long serialVersionUID = 219333225378927807L;

    private int serial;

    private String senderServiceType;

    private String senderServiceId;

    private long timestamp;

    private long uid;

    private int messageId;

    private int errorCode;

    private byte[] content = new byte[0];

    @Override
    public String toString() {
        return "InnerClientMessage{" +
                "serial=" + serial +
                ", senderServiceType='" + senderServiceType + '\'' +
                ", senderServiceId='" + senderServiceId + '\'' +
                ", timestamp=" + timestamp +
                ", uid=" + uid +
                ", messageId=" + messageId +
                ", errorCode=" + errorCode +
                '}';
    }
}
