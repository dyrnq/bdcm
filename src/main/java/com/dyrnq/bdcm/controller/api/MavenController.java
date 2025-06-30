package com.dyrnq.bdcm.controller.api;

import cn.hutool.core.util.StrUtil;
import com.dyrnq.bdcm.controller.ApiController;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.annotation.Valid;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapping("api/maven")
@Controller
@Valid
@Slf4j
public class MavenController extends ApiController {
    static final String MAVEN_PATH = "https://repo1.maven.org/maven2/";


    static final Map<String, String> MIRROR = new LinkedHashMap<>();

    static {
        MIRROR.put("huawei", "https://repo.huaweicloud.com/repository/maven/");
        MIRROR.put("aliyun", "https://maven.aliyun.com/repository/public/");
        MIRROR.put("tencent", "http://mirrors.cloud.tencent.com/nexus/repository/maven-public/");
        MIRROR.put("local", "");

    }

    @Mapping("url")
    public Result<Map<String, List<String>>> url(Context ctx, String xml) {
        try {
            log.info(xml);
            Map<String, List<String>> data = new LinkedHashMap<>();
            List<String> list = new ArrayList<>();

            String path = "https://repo1.maven.org/maven2/org/flywaydb/flyway-core/11.9.1/flyway-core-11.9.1.jar";
            path = "https://repo1.maven.org/maven2/${group}/${artifact}/${ver}/${artifact}-${ver}.jar";
            // 1. 解析为 Document，指定解析类型为 XML
            Document doc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser());

            // 2. 找到所有 <dependency> 元素
            Elements deps = doc.select("dependency");
            //
            // 3. 遍历并提取子元素文本
            for (Element dep : deps) {
                String url = path;
                String groupId = dep.selectFirst("groupId").text();
                String artifactId = dep.selectFirst("artifactId").text();
                String version = dep.selectFirst("version") != null ? dep.selectFirst("version").text() : "_NONE";
                String group = StrUtil.replace(groupId, ".", "/");
                log.info("Dependency: {}:{}:{}", groupId, artifactId, version);
                url = StrUtil.replace(url, "${group}", group);
                url = StrUtil.replace(url, "${artifact}", artifactId);
                url = StrUtil.replace(url, "${ver}", version);
                list.add(url);
            }

            data.put("original", list);

            for (Map.Entry<String, String> kv : MIRROR.entrySet()) {
                String key = kv.getKey();
                String value = kv.getValue();
                List<String> mirrorList = new ArrayList<>();
                list.forEach(c -> {
                    mirrorList.add(StrUtil.replace(c, MAVEN_PATH, value));
                });

                data.put(key, mirrorList);
            }

            List<String> cmdList = new ArrayList<>();
            for (Element dep : deps) {
                String groupId = dep.selectFirst("groupId").text();
                String artifactId = dep.selectFirst("artifactId").text();
                String version = dep.selectFirst("version") != null ? dep.selectFirst("version").text() : "_NONE";
                String group = StrUtil.replace(groupId, ".", "/");
                cmdList.add("mvn dependency:get -Dartifact=" + groupId + ":" + artifactId + ":" + version);
            }

            data.put("cmd", cmdList);
            return Result.succeed(data);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }
}
