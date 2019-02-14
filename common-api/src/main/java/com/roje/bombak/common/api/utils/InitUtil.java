package com.roje.bombak.common.api.utils;

import com.roje.bombak.common.api.annotation.Message;
import com.roje.bombak.common.api.dispatcher.Dispatcher;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/14
 **/
public class InitUtil {
    public static  void initDispatcher(ApplicationContext context) {
        Dispatcher dispatcher = context.getBean(Dispatcher.class);
        Map<String,Object> processors = context.getBeansWithAnnotation(Message.class);
        for (Object object:processors.values()) {
//            if (object instanceof P) {
                //noinspection unchecked
                dispatcher.register(object);
//            }
        }
    }
}
