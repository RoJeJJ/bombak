package com.roje.bombak.lobby;

import com.roje.bombak.common.dispatcher.CommonProcessor;
import com.roje.bombak.common.dispatcher.Dispatcher;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.utils.InitUtil;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * @author pc
 */
@SpringBootApplication
@EnableEurekaClient
@EnableRabbit
public class LobbyApp {
    public static void main(String[] args) {
        ApplicationContext appContext = SpringApplication.run(LobbyApp.class,args);
        InitUtil.initDispatcher(appContext);
    }

    @Bean
    public Queue queue(ServiceInfo serviceInfo) {
        String name = serviceInfo.getServiceType() + "-" + serviceInfo.getServiceId();
        return new Queue(name);
    }
}
