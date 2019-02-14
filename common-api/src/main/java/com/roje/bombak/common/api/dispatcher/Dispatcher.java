package com.roje.bombak.common.api.dispatcher;

import com.roje.bombak.common.api.annotation.Message;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/2
 **/
@Component
public class Dispatcher<P> {
    private final Map<Integer, P> processors = new HashMap<>();

    public void register(P processor) {
        Message message = processor.getClass().getAnnotation(Message.class);
        if (message != null) {
            processors.put(message.id(), processor);
        }
    }

    public P processor(int id) {
        return processors.get(id);
    }
}
