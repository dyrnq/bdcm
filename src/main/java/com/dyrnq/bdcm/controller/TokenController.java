package com.dyrnq.bdcm.controller;

import com.dyrnq.bdcm.model.User;
import com.dyrnq.bdcm.service.BusinessLogic;
import com.dyrnq.utils.JwtUtils;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import java.util.HashMap;
import java.util.Map;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mapping("token")
@Controller
public class TokenController extends BaseController {
    static Logger logger = LoggerFactory.getLogger(TokenController.class);

    @Inject
    BusinessLogic businessLogic;

    @Inject("${server.session.state.jwt.secret:${jwt.secret:}}")
    String jwt_secret;

    @Inject("${server.session.state.jwt.prefix:${jwt.prefix:}}")
    String jwt_prefix;

    @Inject("${jwt.expire:864000000}")
    long jwt_expire;

    /**
     * 获取Token
     *
     * @param name 用户名
     * @param pass 密码
     */
    @Mapping("getToken")
    public Result getToken(Context ctx, String name, String pass) {
        try {
            User user = businessLogic.login(name, pass);
            Map<String, Object> map = new HashMap<>();
            map.put(Claims.SUBJECT, user.getName());
            Claims claims = new DefaultClaims(map);
            return Result.succeed(JwtUtils.buildJwt(claims, jwt_expire, jwt_secret, jwt_prefix));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }
    }

    @Mapping("/i18n")
    public Result changeLocale(Context ctx, String l) {
        ctx.cookieSet("SOLON.LOCALE", l);
        return Result.succeed("ok");
    }

    @Mapping("/cap")
    public void getCode(Context ctx) throws Exception {
        ctx.headerAdd("Pragma", "No-cache");
        ctx.headerAdd("Cache-Control", "no-cache");
        ctx.headerAdd("Expires", "0");
        ctx.contentType("image/gif");

        SpecCaptcha specCaptcha = new SpecCaptcha(100, 40, 4);
        specCaptcha.setCharType(Captcha.TYPE_ONLY_NUMBER);
        ctx.sessionSet("captcha", specCaptcha.text().toLowerCase());
        specCaptcha.out(ctx.outputStream());
    }
}
