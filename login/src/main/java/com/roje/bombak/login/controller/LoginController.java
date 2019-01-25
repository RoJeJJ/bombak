package com.roje.bombak.login.controller;

import com.roje.bombak.login.response.ResponseData;
import com.roje.bombak.login.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * @author pc
 */
@Slf4j
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class LoginController {

    private final UserService<ResponseData> userService;

    public LoginController(UserService<ResponseData> userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/login")
    public String login(String account,String password){
        ResponseData response = userService.login(account,password);
        return response.buildJsonString();
    }

    @PostMapping(value = "/register")
    public String register(String account,String password) {
        ResponseData response = userService.register(account,password);
        return response.buildJsonString();
    }

}
