package com.example.blog.auth.web;

import com.example.blog.auth.model.User;
import com.example.blog.auth.repository.UserRepository;
import com.example.blog.auth.security.JwtService;
import com.example.blog.auth.service.WeChatLoginService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://127.0.0.1:5175"}, allowCredentials = "true")
public class AuthController {

    private final UserRepository userRepository;
    private final WeChatLoginService weChatLoginService;

    @Value("${blog.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public AuthController(UserRepository userRepository, WeChatLoginService weChatLoginService) {
        this.userRepository = userRepository;
        this.weChatLoginService = weChatLoginService;
    }

    /** 账号密码登录 */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        return userRepository.findByUsername(req.getUsername())
                .filter(u -> u.getPassword() != null && u.getPassword().equals(req.getPassword()))
                .map(u -> {
                    String token = JwtService.generateToken(u.getUsername(), u.getRole(), u.getId());
                    return ResponseEntity.ok(new LoginResponse(token, u.getRole(), u.getNickname(), u.getAvatar()));
                })
                .orElseGet(() -> ResponseEntity.status(401).build());
    }

    /** 获取微信授权登录 URL，前端跳转此地址让用户扫码 */
    @GetMapping("/wechat/authorize")
    public Map<String, String> wechatAuthorize(@RequestParam(name = "state", required = false) String state) {
        String s = state != null && !state.isEmpty() ? state : UUID.randomUUID().toString();
        String url = weChatLoginService.getAuthorizeUrl(s);
        Map<String, String> body = new HashMap<>();
        body.put("url", url);
        return body;
    }

    /** 微信回调：用 code 换 token 和用户信息，创建/更新用户并签发 JWT，再重定向回前端 */
    @GetMapping("/wechat/callback")
    public RedirectView wechatCallback(
            @RequestParam("code") String code,
            @RequestParam(name = "state", required = false) String state) {
        WeChatLoginService.WeChatTokenResult tokenResult = weChatLoginService.exchangeToken(code);
        WeChatLoginService.WeChatUserInfo userInfo = weChatLoginService.getUserInfo(
                tokenResult.getAccessToken(), tokenResult.getOpenid());

        User user = userRepository.findByWechatOpenid(userInfo.getOpenid())
                .orElseGet(() -> {
                    User u = new User();
                    u.setWechatOpenid(userInfo.getOpenid());
                    u.setWechatUnionid(userInfo.getUnionid());
                    u.setRole("USER");
                    return u;
                });
        user.setNickname(userInfo.getNickname());
        user.setAvatar(userInfo.getHeadimgurl());
        userRepository.save(user);

        String jwt = JwtService.generateToken(
                user.getWechatOpenid(),
                user.getRole(),
                user.getId());

        String redirect = frontendUrl + "/#/admin/callback?token=" + jwt + "&nickname=" + (user.getNickname() != null ? java.net.URLEncoder.encode(user.getNickname(), java.nio.charset.StandardCharsets.UTF_8) : "");
        return new RedirectView(redirect);
    }

    public static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginResponse {
        private String token;
        private String role;
        private String nickname;
        private String avatar;
        public LoginResponse(String token, String role, String nickname, String avatar) {
            this.token = token;
            this.role = role;
            this.nickname = nickname;
            this.avatar = avatar;
        }
        public String getToken() { return token; }
        public String getRole() { return role; }
        public String getNickname() { return nickname; }
        public String getAvatar() { return avatar; }
    }
}
