package com.roje.bombak.gate.config;

import com.roje.bombak.common.constant.Constant;
import com.roje.bombak.common.eureka.ServiceInfo;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author pc
 * @version 1.0
 * @date 2018/12/11
 **/
@Configuration
public class GateRabbitConfig {
    @Bean
    public Queue fanoutQueue() {
        return new Queue(Constant.BROADCAST_QUEUE_NAME);
    }

    @Bean
    public FanoutExchange exchange() {
        return new FanoutExchange(Constant.BROADCAST_EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(@Qualifier("fanoutQueue") Queue queue, FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    @Bean
    public Queue queue(ServiceInfo serviceInfo) {
        String name = serviceInfo.getServiceType() + "-" + serviceInfo.getServiceId();
        return new Queue(name);
    }
}
