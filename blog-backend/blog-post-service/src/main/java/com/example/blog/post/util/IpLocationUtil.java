package com.example.blog.post.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 根据 IP 解析地理位置（使用 ip-api.com 免费接口）
 */
public final class IpLocationUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private IpLocationUtil() {}

    public static String resolveAddress(String ip) {
        if (ip == null || ip.isBlank()) return "未知地址";
        String trimmed = ip.trim();
        if (isPrivateIp(trimmed)) return "本地";
        try {
            String url = "http://ip-api.com/json/" + trimmed + "?fields=status,country,regionName,city&lang=zh-CN";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return "未知地址";
            JsonNode root = MAPPER.readTree(resp.body());
            if (root.path("status").asText().equals("fail")) return "未知地址";
            String country = root.has("country") ? root.get("country").asText() : "";
            String region = root.has("regionName") ? root.get("regionName").asText() : "";
            String city = root.has("city") ? root.get("city").asText() : "";
            StringBuilder sb = new StringBuilder();
            if (!country.isEmpty()) sb.append(country);
            if (!region.isEmpty() && !region.equals(city)) sb.append(" ").append(region);
            if (!city.isEmpty()) sb.append(" ").append(city);
            return sb.length() > 0 ? sb.toString().trim() : "未知地址";
        } catch (Exception e) {
            return "未知地址";
        }
    }

    private static boolean isPrivateIp(String ip) {
        if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("localhost")) return true;
        if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.16.")) return true;
        return false;
    }
}
