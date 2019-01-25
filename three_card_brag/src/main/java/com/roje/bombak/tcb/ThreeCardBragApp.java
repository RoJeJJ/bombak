package com.roje.bombak.tcb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/21
 **/
@SpringBootApplication
public class ThreeCardBragApp {
  public static void main(String[] args) {
    ApplicationContext appContext = SpringApplication.run(ThreeCardBragApp.class,args);
  }
}
