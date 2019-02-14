package com.roje.bombak.gate.process;

import com.roje.bombak.common.api.annotation.Message;
import com.roje.bombak.gate.constant.GateConstant;
import com.roje.bombak.gate.processor.GateProcessor;
import com.roje.bombak.gate.proto.Gate;
import com.roje.bombak.gate.session.GateSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author pc
 */
@Slf4j
@Component
@Message(id = GateConstant.HEART_BEAT_RES)
public class HeartBeatResProcessor implements GateProcessor {
    @Override
    public void process(GateSession session, Gate.ClientMessage message) {
        //心跳回复,默认不作处理
    }
}
