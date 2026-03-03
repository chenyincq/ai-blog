package com.example.blog.post.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@ConfigurationProperties(prefix = "blog.upload")
public class UploadProperties {

    /** 上传文件存储目录，支持相对路径（相对工作目录）或绝对路径 */
    private String path = "uploads";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path != null ? path.trim() : "uploads";
    }

    /** 解析为绝对路径 */
    public Path resolveAbsolutePath() {
        Path p = Paths.get(path);
        return p.isAbsolute() ? p : Paths.get(System.getProperty("user.dir", ".")).resolve(p).toAbsolutePath().normalize();
    }

    /** 用于 Spring 静态资源映射的 file: URL 形式，需以 / 结尾 */
    public String toFileLocation() {
        Path abs = resolveAbsolutePath();
        String loc = abs.toUri().toString().replace('\\', '/');
        return loc.endsWith("/") ? loc : loc + "/";
    }
}
