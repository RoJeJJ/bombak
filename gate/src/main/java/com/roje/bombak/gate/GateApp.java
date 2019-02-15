package com.roje.bombak.gate;

import com.roje.bombak.common.service.ServerService;
import com.roje.bombak.common.utils.DispatcherUtil;
import com.roje.bombak.gate.config.GateProperties;
import com.roje.bombak.gate.service.GateNettyTcpServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationContext;

/**
 * @author pc
 */
@SpringBootApplication
@EnableConfigurationProperties(value = GateProperties.class)
@EnableEurekaClient
public class GateApp {
    public static void main(String[] args) {
        ApplicationContext appContext = SpringApplication.run(GateApp.class, args);
        DispatcherUtil.init(appContext);

        ServerService nettyService = appContext.getBean(GateNettyTcpServiceImpl.class);
        nettyService.start();

        Runtime.getRuntime().addShutdownHook(new Thread(nettyService::stop));
    }
}
