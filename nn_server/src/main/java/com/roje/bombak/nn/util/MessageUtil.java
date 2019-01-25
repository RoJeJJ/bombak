package com.roje.bombak.nn.util;

import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.message.InnerClientMessage;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/10
 **/
@Component
public class MessageUtil {

    private final ServiceInfo nnInfo;

    private final AmqpTemplate amqpTemplate;

    public MessageUtil(ServiceInfo nnInfo, AmqpTemplate amqpTemplate) {
        this.nnInfo = nnInfo;
        this.amqpTemplate = amqpTemplate;
    }

    public void responseErrorMessage(InnerClientMessage message,int messageId,int errorCode) {

    }
}
