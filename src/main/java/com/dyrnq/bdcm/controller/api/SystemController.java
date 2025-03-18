package com.dyrnq.bdcm.controller.api;

import com.dyrnq.bdcm.controller.ApiController;
import org.apache.commons.lang3.StringUtils;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mapping("api/system")
@Controller
public class SystemController extends ApiController {
    static Logger logger = LoggerFactory.getLogger(SystemController.class);
    static final String DEFAULT_JWT_SECRET = "IDP32XTulsVIUZU+srFEUC9Lhu1wV+nd8iCJPoPA2zSFVAtWhCgpMEymxy5wFAZKMB9yROX31UjDzjwL66r1RA==";
    @Inject("${server.session.state.jwt.secret:${jwt.secret:}}")
    String jwt_secret;

    @Mapping("getVersion")  //获取版本号
    public String getVersion() {
        return "1.0.0";
    }

    @Mapping("jwtSecret")
    public Result jwtSecret(Context ctx) {
        try {
            if (StringUtils.equals(jwt_secret, DEFAULT_JWT_SECRET)) {
                return Result.succeed("警告：JWT密钥为默认值！请及时修改！");
            } else {
                return Result.failure("");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

}
