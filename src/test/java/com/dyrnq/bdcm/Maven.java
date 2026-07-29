package com.dyrnq.bdcm;

import cn.hutool.core.util.StrUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Maven {
    static final String MAVEN_PATH = "https://repo1.maven.org/maven2/";
    static final String HUAWEI_PATH = "https://repo.huaweicloud.com/repository/maven/";
    static final String TENCENT_PATH = "http://mirrors.cloud.tencent.com/nexus/repository/maven-public/";
    static final String ALIYUN_PATH = "https://maven.aliyun.com/repository/public/";

    public static void main(String[] args) {

        String xml = """
                <dependency>
                    <groupId>org.apache.doris</groupId>
                    <artifactId>flink-doris-connector-1.20</artifactId>
                    <version>25.1.0</version>
                </dependency>
                <dependency>
                    <groupId>org.apache.doris</groupId>
                    <artifactId>flink-doris-connector-1.20</artifactId>
                    <version>25.1.0</version>
                </dependency>
                """;

        String path = "https://repo1.maven.org/maven2/org/flywaydb/flyway-core/11.9.1/flyway-core-11.9.1.jar";
        path = "https://repo1.maven.org/maven2/${group}/${artifact}/${ver}/${artifact}-${ver}.jar";
        // 1. 解析为 Document，指定解析类型为 XML
        Document doc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser());

        // 2. 找到所有 <dependency> 元素
        Elements deps = doc.select("dependency");

        // 3. 遍历并提取子元素文本
        for (Element dep : deps) {
            String groupId = dep.selectFirst("groupId").text();
            String artifactId = dep.selectFirst("artifactId").text();
            String version = dep.selectFirst("version").text();
            String group = StrUtil.replace(groupId, ".", "/");
            System.out.printf("Dependency: %s:%s:%s%n", groupId, artifactId, version);

            path = StrUtil.replace(path, "${group}", group);
            path = StrUtil.replace(path, "${artifact}", artifactId);
            path = StrUtil.replace(path, "${ver}", version);

            System.out.println(path);

            System.out.println(StrUtil.replace(path, MAVEN_PATH, HUAWEI_PATH));
            System.out.println(StrUtil.replace(path, MAVEN_PATH, TENCENT_PATH));
            System.out.println(StrUtil.replace(path, MAVEN_PATH, ALIYUN_PATH));
        }
    }
}
