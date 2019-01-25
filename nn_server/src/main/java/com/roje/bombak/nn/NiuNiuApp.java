package com.roje.bombak.nn;

import com.roje.bombak.common.eureka.ServiceInfo;
import com.roje.bombak.common.utils.InitUtil;
import com.roje.bombak.nn.config.NnProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
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
@EnableConfigurationProperties(value = NnProperties.class)
@EnableEurekaClient
@EnableRabbit
public class NiuNiuApp {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(NiuNiuApp.class,args);
        InitUtil.initDispatcher(context);
    }

    @Bean
    public Queue queue(ServiceInfo serviceInfo) {
        String name = serviceInfo.getServiceType() + "-" + serviceInfo.getServiceId();
        return new Queue(name);
    }
}
