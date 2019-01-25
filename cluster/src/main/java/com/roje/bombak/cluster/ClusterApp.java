package com.roje.bombak.cluster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author pc
 */
@SpringBootApplication
@EnableEurekaServer
public class ClusterApp {
    public static void main(String[] args) {
        SpringApplication.run(ClusterApp.class,args);
    }
}
