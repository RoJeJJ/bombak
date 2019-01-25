package com.roje.bombak.gate.controller;

import com.roje.bombak.gate.manager.GateSessionManager;
import com.roje.bombak.gate.session.GateSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author pc
 */
@Slf4j
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class GateController {

    private final GateSessionManager sessionManager;


    @Autowired
    public GateController(GateSessionManager serverManager) {
        this.sessionManager = serverManager;
    }


    @GetMapping(value = "/kickOut")
    public String kickOut(@RequestParam("uid") long uid) {
        GateSession session = sessionManager.getSession(uid);
        if (session != null) {
            session.close();
        }
        return "ok";
    }
}
