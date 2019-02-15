package com.roje.bombak.gate.process;

import com.roje.bombak.common.annotation.Message;
import com.roje.bombak.common.proto.ServerMsg;
import com.roje.bombak.gate.constant.GateConstant;
import com.roje.bombak.gate.processor.GateProcessor;
import com.roje.bombak.gate.session.GateSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author pc
 */
@Slf4j
@Component
@Message(id = GateConstant.Cmd.HEART_BEAT_RES)
public class HeartBeatResProcessor implements GateProcessor {
    @Override
    public void process(GateSession session, ServerMsg.C2SMessage message) {
        //心跳回复,默认不作处理
    }
}
