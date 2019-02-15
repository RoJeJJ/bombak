package com.roje.bombak.common.processor;

import com.roje.bombak.common.annotation.Message;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/2
 **/
@Component
public class Dispatcher {
    private final Map<Integer, Object> processors = new HashMap<>();

    public void register(Object processor) {
        Message message = processor.getClass().getAnnotation(Message.class);
        if (message != null) {
            processors.put(message.id(), processor);
        }
    }

    @SuppressWarnings("unchecked")
    public <P>P processor(int id) {
        return (P)processors.get(id);
    }
}
