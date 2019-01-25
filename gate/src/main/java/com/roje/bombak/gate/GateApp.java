package com.roje.bombak.gate;

import com.roje.bombak.common.dispatcher.Dispatcher;
import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.service.ServerService;
import com.roje.bombak.common.utils.InitUtil;
import com.roje.bombak.gate.config.GateProperties;
import com.roje.bombak.gate.processor.GateProcessor;
import com.roje.bombak.gate.service.GateNettyTcpServiceImpl;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * @author pc
 */
@SpringBootApplication
@EnableConfigurationProperties(value = GateProperties.class)
@EnableEurekaClient
public class GateApp {
    public static void main(String[] args) {
        ApplicationContext appContext = SpringApplication.run(GateApp.class, args);
        InitUtil.initDispatcher(appContext);

        ServerService nettyService = appContext.getBean(GateNettyTcpServiceImpl.class);
        nettyService.start();

        Runtime.getRuntime().addShutdownHook(new Thread(nettyService::stop));
    }

    @Bean
    Dispatcher<GateProcessor> dispatcher() {
        return new Dispatcher<>();
    }
}
