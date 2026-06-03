package com.dyrnq.bdcm.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.dyrnq.bdcm.CfgExtractor;
import com.dyrnq.bdcm.CookieName;
import com.dyrnq.utils.VersionUtils;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;
import org.noear.solon.i18n.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Slf4j
public class AppFilter implements Filter {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject("${solon.app.name}")
    String projectName;

    @Inject
    CfgExtractor cfgExtractor;

    @Inject("${spring.database.type:}")
    String dbType;

    @Inject("${spring.datasource.url:}")
    String dbUrl;

    static String getCtxStr(Context context) {
        String httpHost = context.header("X-Forwarded-Host");
        String realPort = context.header("X-Forwarded-Port");
        String host = context.header("Host");

        String ctx = "//";
        if (StrUtil.isNotEmpty(httpHost)) {
            ctx += httpHost;
        } else if (StrUtil.isNotEmpty(host)) {
            ctx += host;
            if (!host.contains(":") && StrUtil.isNotEmpty(realPort)) {
                ctx += ":" + realPort;
            }
        } else {
            host = context.url().split("/")[2];
            ctx += host;
            if (!host.contains(":") && StrUtil.isNotEmpty(realPort)) {
                ctx += ":" + realPort;
            }
        }
        return ctx;
    }

    @Override
    public void doFilter(Context ctx, FilterChain chain) throws Throwable {
        Map<String, String> cookName = new HashMap<>();
        cookName.put("token", cfgExtractor.tokenCookieName());
        cookName.put("instId", CookieName.NAME_INSTID);
        ctx.attrSet("dbType", resolveDbLabel());
        ctx.attrSet("projectName", projectName);
        ctx.attrSet("cookName", JSONUtil.toJsonStr(cookName));
        ctx.attrSet("cfg", "{ \"pageLimit\":10, \"pageLimits\":[10,20,50,100,1000], \"aceMode\": \"yaml\" }");
        ctx.attrSet("ctx", getCtxStr(ctx));
        ctx.attrSet("currentVersion", VersionUtils.getVersion());
        ctx.attrSet("gitRevision", VersionUtils.getGitRevision());
        ctx.attrSet("jsrandom", VersionUtils.getVersion() + "." + System.currentTimeMillis());
        ctx.attrSet("cookieMap", ctx.cookieMap());
        try {
            String ctxDisplayLanguage =
                    I18nUtil.getLocaleResolver().getLocale(ctx).getDisplayLanguage();
            if (Strings.CI.equals(ctxDisplayLanguage, "Chinese") || Strings.CI.equals(ctxDisplayLanguage, "中文")) {
                ctx.attrSet("langType", "简体中文");
            } else {
                ctx.attrSet("langType", "English");
            }
        } catch (Exception e) {
            ctx.attrSet("langType", "简体中文");
        }
        chain.doFilter(ctx);
    }

    private String resolveDbLabel() {
        // 优先从 datasource url 推断
        if (StrUtil.isNotBlank(dbUrl)) {
            if (dbUrl.startsWith("jdbc:h2:")) return "H2";
            if (dbUrl.startsWith("jdbc:mysql:")) return "MySQL";
            if (dbUrl.startsWith("jdbc:postgresql:") || dbUrl.startsWith("jdbc:pgsql:")) return "PostgreSQL";
            if (dbUrl.startsWith("jdbc:sqlite:")) return "SQLite";
            return dbUrl.split(":")[1]; // fallback: 取 jdbc:xxx 中间段
        }
        // 其次从 database.type 取值
        if (StrUtil.isNotBlank(dbType)) return dbType;
        // 默认
        return "h2";
    }
}
