package com.roje.bombak.common.utils;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.common.processor.Dispatcher;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/14
 **/
public class DispatcherUtil {
    public static  void init(ApplicationContext context) {
        Dispatcher dispatcher = context.getBean(Dispatcher.class);
        Map<String,Object> processors = context.getBeansWithAnnotation(Message.class);
        for (Object object:processors.values()) {
            dispatcher.register(object);
        }
    }
}
