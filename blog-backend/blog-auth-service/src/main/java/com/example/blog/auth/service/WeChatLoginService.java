package com.example.blog.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信开放平台「网站应用」扫码登录。
 * 文档: https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Wechat_Login.html
 */
@Service
public class WeChatLoginService {

    private static final String AUTHORIZE_URL = "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect";
    private static final String TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
    private static final String USERINFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${wechat.app-id}")
    private String appId;

    @Value("${wechat.app-secret}")
    private String appSecret;

    @Value("${wechat.redirect-uri}")
    private String redirectUri;

    /** 生成微信授权登录 URL，前端跳转此 URL 让用户扫码 */
    public String getAuthorizeUrl(String state) {
        try {
            String encoded = java.net.URLEncoder.encode(redirectUri, "UTF-8");
            return String.format(AUTHORIZE_URL, appId, encoded, state != null ? state : "");
        } catch (Exception e) {
            throw new RuntimeException("Build wechat authorize url failed", e);
        }
    }

    /** 用 code 换取 access_token 和 openid */
    public WeChatTokenResult exchangeToken(String code) {
        String url = String.format(TOKEN_URL, appId, appSecret, code);
        String body = restTemplate.getForObject(URI.create(url), String.class);
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.has("errcode") && node.get("errcode").asInt() != 0) {
                throw new RuntimeException("WeChat token error: " + body);
            }
            WeChatTokenResult r = new WeChatTokenResult();
            r.setAccessToken(node.has("access_token") ? node.get("access_token").asText() : null);
            r.setOpenid(node.has("openid") ? node.get("openid").asText() : null);
            r.setUnionid(node.has("unionid") ? node.get("unionid").asText() : null);
            return r;
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("Parse wechat token failed: " + body, e);
        }
    }

    /** 用 access_token 和 openid 拉取用户信息 */
    public WeChatUserInfo getUserInfo(String accessToken, String openid) {
        String url = String.format(USERINFO_URL, accessToken, openid);
        String body = restTemplate.getForObject(URI.create(url), String.class);
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.has("errcode") && node.get("errcode").asInt() != 0) {
                throw new RuntimeException("WeChat userinfo error: " + body);
            }
            WeChatUserInfo u = new WeChatUserInfo();
            u.setOpenid(node.has("openid") ? node.get("openid").asText() : null);
            u.setUnionid(node.has("unionid") ? node.get("unionid").asText() : null);
            u.setNickname(node.has("nickname") ? node.get("nickname").asText() : null);
            u.setHeadimgurl(node.has("headimgurl") ? node.get("headimgurl").asText() : null);
            return u;
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("Parse wechat userinfo failed: " + body, e);
        }
    }

    public static class WeChatTokenResult {
        private String accessToken;
        private String openid;
        private String unionid;
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }
        public String getUnionid() { return unionid; }
        public void setUnionid(String unionid) { this.unionid = unionid; }
    }

    public static class WeChatUserInfo {
        private String openid;
        private String unionid;
        private String nickname;
        private String headimgurl;
        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }
        public String getUnionid() { return unionid; }
        public void setUnionid(String unionid) { this.unionid = unionid; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getHeadimgurl() { return headimgurl; }
        public void setHeadimgurl(String headimgurl) { this.headimgurl = headimgurl; }
    }
}
