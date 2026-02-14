package com.dyrnq.bdcm;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.SystemUtil;
import com.dyrnq.bdcm.model.User;
import com.dyrnq.bdcm.service.BusinessLogic;
import com.dyrnq.utils.JwtUtils;
import com.dyrnq.utils.VersionUtils;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.noear.snack4.ONode;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.exception.StatusException;
import org.noear.solon.core.handle.*;
import org.noear.solon.core.route.RouterInterceptor;
import org.noear.solon.core.route.RouterInterceptorChain;
import org.noear.solon.i18n.I18nUtil;
import org.noear.solon.scheduling.annotation.EnableScheduling;
import org.noear.solon.validation.ValidatorException;
import org.noear.solon.view.freemarker.FreemarkerRender;
import org.noear.wood.WoodConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@EnableScheduling
public class WebApp {
    static Logger logger = LoggerFactory.getLogger(WebApp.class);

    public static void main(String[] args) {
        Solon.start(WebApp.class, args, app -> {
            Set<String> allNodes = Solon.cfg().stringPropertyNames();
            for (String entry : allNodes) {
                String envName1 = StringUtils.upperCase(StringUtils.replace(entry, "-", "").replace(".", "_"));
                String envName2 = StringUtils.upperCase(StringUtils.replace(entry, "-", "_").replace(".", "_"));
                String envName3 = StringUtils.upperCase(StringUtils.replace(entry.replaceAll("(?<!^)(?=[A-Z])", "_"), "-", "_").replace(".", "_"));

                String getValue = SystemUtil.get(envName1, true);
                if (getValue != null) {
                    Solon.cfg().setProperty(entry, getValue);
                }
                getValue = SystemUtil.get(envName2, true);
                if (getValue != null) {
                    Solon.cfg().setProperty(entry, getValue);
                }
                getValue = SystemUtil.get(envName3, true);
                if (getValue != null) {
                    Solon.cfg().setProperty(entry, getValue);
                }
            }

            //LogUtil.globalSet(new LogUtilToSlf4j());
            //app.onError(e -> logger.error(e.getMessage(), e));
            app.context().getBeanAsync(FreemarkerRender.class, e -> {
                freemarker.template.Configuration cfg = e.getProvider();
                try {
                    //cfg.setClassicCompatible(false);
                    //cfg.setStrictSyntaxMode(false);
                    cfg.setSetting(Configuration.NUMBER_FORMAT_KEY, "0.##");
                    cfg.setSetting(Configuration.DEFAULT_ENCODING_KEY, "UTF-8");
                    cfg.setSetting(Configuration.TEMPLATE_UPDATE_DELAY_KEY, "0");
                    cfg.setSetting(Configuration.CACHE_STORAGE_KEY, "strong:20, soft:250");
                    // rethrow,debug,html_debug,ignore;
                    if (Solon.cfg().isDebugMode()) {
                        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
                    } else {
                        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
                    }
                } catch (TemplateException ex) {
                    logger.error(ex.getMessage(), ex);
                }

            });
            app.filter((c, chain) -> {
                String path = c.path();
                while (path.contains("//")) {
                    path = path.replace("//", "/");
                }
                c.pathNew(path);
                chain.doFilter(c);
            });


//            app.onEvent(freemarker.template.Configuration.class, cfg -> {
//                cfg.setSetting("classic_compatible", "true");
//                cfg.setSetting("number_format", "0.##");
//                cfg.setSetting("default_encoding", "UTF-8");
//                cfg.setSetting("template_update_delay", "0");
//                cfg.setSetting("cache_storage", "soft:1");
//                //cfg.setSetting("strict_syntax","false");
//
//            });


            WoodConfig.isUsingValueExpression = false;
            if (Solon.cfg().isDebugMode()) {
                //执行后打印下sql
                WoodConfig.onExecuteAft(cmd -> {
                    System.out.println(cmd.text + "\r\n" + ONode.serialize(cmd.paramMap()));
                });

                WoodConfig.onException((cmd, err) -> {
                    System.out.println(cmd.text + "\r\n" + ONode.serialize(cmd.paramMap()));
                });
            }


        });
    }

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

    @Component
    public static class AppFilter implements Filter {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        @Inject("${solon.app.name}")
        String projectName;

//        @Inject("${solon.app.cfg}")
//        Map<String,Object> map;

//        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
//                .setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
//                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
//                .create();

        @Inject
        CfgExtractor cfgExtractor;

        @Override
        public void doFilter(Context ctx, FilterChain chain) throws Throwable {
            Map<String, String> cookName = new HashMap<>();
            cookName.put("token", cfgExtractor.tokenCookieName());
            cookName.put("instId", CookieName.NAME_INSTID);
            ctx.attrSet("projectName", projectName);
            ctx.attrSet("cookName", JSONUtil.toJsonStr(cookName));
            ctx.attrSet("cfg", "{ \"pageLimit\":10, \"pageLimits\":[10,20,50,100], \"aceMode\": \"yaml\" }");
            ctx.attrSet("ctx", getCtxStr(ctx));
            ctx.attrSet("currentVersion", VersionUtils.getVersion());
            ctx.attrSet("gitRevision", VersionUtils.getGitRevision());
            ctx.attrSet("jsrandom", VersionUtils.getVersion() + "." + System.currentTimeMillis());
            ctx.attrSet("cookieMap", ctx.cookieMap());
            try {
                String ctxDisplayLanguage = I18nUtil.getLocaleResolver().getLocale(ctx).getDisplayLanguage();
                if (StringUtils.equalsIgnoreCase(ctxDisplayLanguage, "Chinese") || StringUtils.equalsIgnoreCase(ctxDisplayLanguage, "中文")) {
                    ctx.attrSet("langType", "简体中文");
                } else {
                    ctx.attrSet("langType", "English");
                }
            } catch (Exception e) {
                ctx.attrSet("langType", "简体中文");
            }
            chain.doFilter(ctx);
        }
    }


    public static class Message {
        String key;
        String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Component
    public static class I18nFilter implements Filter {

//        @Inject
//        MessageUtil m;

        @Override
        public void doFilter(Context ctx, FilterChain chain) throws Throwable {

//        Properties properties = null;
//        String l = ctx.param("l");
//		  if (StrUtil.isNotEmpty(l) && l.equals("en_US") || settingService.get("lang") != null && settingService.get("lang").equals("en_US")) {
//            settingService.set("lang", "en_US");
//            properties = m.getPropertiesEN();
//        } else {
//            settingService.set("lang", "");
//            properties = m.getProperties();
//        }
//            properties=m.getProperties();

            Properties properties = I18nUtil.getMessageBundle().toProps();

            // js国际化
            Set<String> messageHeaders = new HashSet<>();
            List<Message> messages = new ArrayList<>();
            for (String key : properties.stringPropertyNames()) {
                Message message = new WebApp.Message();
                message.setKey(key);
                message.setValue(properties.getProperty(key));
                messages.add(message);

                messageHeaders.add(key.split("\\.")[0]);
            }

            ctx.attrSet("messageHeaders", messageHeaders);
            ctx.attrSet("messages", messages);

            // html国际化
//            for (String key : messageHeaders) {
//                Map<String, String> map = new HashMap<>();
//                for (Message message : messages) {
//                    if (message.getKey().split("\\.")[0].equals(key)) {
//                        map.put(message.getKey().split("\\.")[1], message.getValue());
//                    }
//                }
//                ctx.attrSet(key, map);
//            }
            chain.doFilter(ctx);
        }

    }

    @Component
    public static class JwtInterceptor implements RouterInterceptor {


//        private Claims getClaimsFromToken(String token) {
//            return JwtUtils.parseJwt(token);
//        }

//        public String getUserIdFromToken(String token) throws TokenExpiredException {
//            String userId = null;
//            try {
//                Claims claims = getClaimsFromToken(token);
//                userId = claims.getId();
//            } catch (ExpiredJwtException e) {
//                throw new TokenExpiredException("令牌过期");
//            }
//            return userId;
//        }

//        public String getUsernameFromToken(String token) throws TokenExpiredException {
//            String username = null;
//            try {
//                Claims claims = getClaimsFromToken(token);
//                username = claims.getSubject();
//            } catch (NullPointerException e) {
//                logger.error(e.getMessage());
//                throw new TokenExpiredException("令牌过期");
//            } catch (ExpiredJwtException e) {
//                logger.error(e.getMessage());
//                throw new TokenExpiredException("令牌过期");
//            }
//            return username;
//        }

        @Inject
        BusinessLogic businessLogic;


        @Inject
        CfgExtractor cfgExtractor;

        @Inject("${server.session.state.jwt.secret:${jwt.secret:}}")
        String jwt_secret;
        @Inject("${server.session.state.jwt.prefix:${jwt.prefix:}}")
        String jwt_prefix;

        private Boolean validateToken(Context ctx, String token, String name) {
            if (StringUtils.isBlank(token)) return false;

            Claims claims = JwtUtils.parseJwt(token, jwt_secret, jwt_prefix);
            if (claims == null) return false;
            String username = claims.getSubject();
//            logger.info("username=" + username);
            User user = businessLogic.findByName(username);
            if (user == null) return false;
            ctx.attrSet("admin", user);


            Date expiration = claims.getExpiration();
//            return expiration.before(new Date());
            return (username.equals(user.getName()) && !expiration.before(new Date()));
//            return (username.equals(name) && !isTokenExpired(token));
        }

        @Override
        public void doIntercept(Context ctx, Handler mainHandler, RouterInterceptorChain chain) throws Throwable {
            //如果是登录页则不处理
//            logger.info("ctx.path()=" + ctx.path());
            if ((ctx.path().startsWith("/admin") && !ctx.path().startsWith("/admin/login")) || (ctx.path().startsWith("/api"))) {
                String token = ctx.cookie(cfgExtractor.tokenCookieName());
//                logger.info("TOKEN=" + token);
//                String session_username = ctx.session("user_name", "");
//                logger.info("session_username="+session_username);
                boolean validateToken = false;
                try {
                    validateToken = validateToken(ctx, token, null);
                } finally {
                    if (!validateToken) {
                        if (ctx.path().startsWith("/api")) {
                            ctx.status(401);
                        } else {
                            ctx.redirect("/admin/login", 302);
                        }
                        return;
                    }
                }

            }

            chain.doIntercept(ctx, mainHandler);
        }
    }

    @Component(index = 0) //index 为顺序位（不加，则默认为0）
    public static class AppExceptionFilter implements Filter {
        @Override
        public void doFilter(Context ctx, FilterChain chain) throws Throwable {
            try {
                chain.doFilter(ctx);
            } catch (ValidatorException e) {
                ctx.render(Result.failure(e.getCode(), e.getMessage())); //e.getResult().getDescription()
            } catch (StatusException e) {
                logger.error(e.getMessage(), e);
                if (e.getCode() == 404) {
                    ctx.status(e.getCode());
                } else {
                    String msg = e.getMessage();
                    if (ExceptionUtil.getRootCause(e) != null && ExceptionUtil.getRootCause(e).getMessage() != null) {
                        msg = ExceptionUtil.getRootCause(e).getMessage();
                    }
                    ctx.render(Result.failure(e.getCode(), msg));
                }
            } catch (Throwable e) {
                ctx.render(Result.failure(500, "服务端运行出错"));
            }
        }
    }
}
