package top.openfbi.mdnote.user.controller;


import top.openfbi.mdnote.common.exception.ResultException;
import top.openfbi.mdnote.config.ResponseResultBody;
import top.openfbi.mdnote.user.model.User;
import top.openfbi.mdnote.user.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.openfbi.mdnote.user.util.Session;

/**
 * 登录操作接口
 */
@RequestMapping("/passport")
@ResponseResultBody
@RestController
public class PassportController {
    @Autowired
    private UserService userService;
    private static final Logger logger
            = LoggerFactory.getLogger(UserController.class);

    @Data
    @AllArgsConstructor
    static class RegistResponse {
        private String userName;
    }

    // 注册账户
    @PostMapping("/register")
    public RegistResponse regist(@RequestBody User newUser) throws ResultException {
        return new RegistResponse(userService.registService(newUser));
    }

    @Data
    @AllArgsConstructor
    static class LoginResponse {
        private String userName;
    }

    // 登录账户
    @PostMapping(value = "/login")
    public LoginResponse login(@RequestBody User user) throws ResultException {
        LoginResponse loginResponse = new LoginResponse(userService.login(user));
        return loginResponse;

    }

    // IsInfoResponse方法专门的返回值
    @Data
    @AllArgsConstructor
    static class IsLoginResponse {
        private boolean loggedIn;
    }

    // 检测用户是否已经登录
    @GetMapping("/isLogin")
    public void isLogin() {
        return;
    }

    // 退出登录
    @GetMapping("/logOut")
    public void logOut() {
        userService.logOut(Session.getUser().getId());
    }
}
