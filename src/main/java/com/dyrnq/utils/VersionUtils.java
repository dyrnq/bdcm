package com.dyrnq.utils;


import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionUtils {
    public static String getGitRevision() throws Exception {
        String jarPath = VersionUtils.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        jarPath = java.net.URLDecoder.decode(jarPath, "UTF-8");
        try {
            URL url = new URL("jar:file:" + jarPath + "!/build.info");
            InputStream inputStream = url.openStream();
            Properties properties = new Properties();
            properties.load(inputStream);
            String version = properties.getProperty("git.revision");
            return version;
        } catch (Exception e) {
            return "dev";
        }
    }

    public static String getVersion() throws Exception {
        // 查看jar包里面build.info版本号
        String jarPath = VersionUtils.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        jarPath = java.net.URLDecoder.decode(jarPath, "UTF-8");
        try {
            URL url = new URL("jar:file:" + jarPath + "!/build.info");
            InputStream inputStream = url.openStream();
            Properties properties = new Properties();
            properties.load(inputStream);
            String version = properties.getProperty("project.version");
            return version;
        } catch (Exception e) {
            // 开发过程中查看pom.xml版本号
            String xmlContent = new String(Files.readAllBytes(Paths.get("pom.xml")));
            // 使用正则表达式匹配版本号
            Pattern pattern = Pattern.compile("<version>(.*?)<\\/version>");
            Matcher matcher = pattern.matcher(xmlContent);
            String version = "";
            if (matcher.find()) {
                version = matcher.group(1);
            }
            return StringUtils.trim(version);
        }
        //return "1.0.0";
    }
}
