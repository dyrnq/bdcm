package com.dyrnq.bdcm.controller;

import com.dyrnq.bdcm.CfgExtractor;
import com.dyrnq.bdcm.RepoConfig;
import org.noear.solon.Utils;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ModelAndView;
import org.noear.solon.i18n.annotation.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mapping("admin")
@Controller
@I18n
public class AdminController extends BaseController {
    static Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Inject
    CfgExtractor cfgExtractor;

    @Inject
    RepoConfig repoConfig;

    @Mapping("artifact")
    public Object artifact() {
        ModelAndView model = new ModelAndView("admin/artifact.html");
        model.put("externalUrl", repoConfig.guessExternalUrl());
        return model;
    }

    @Mapping("artJob")
    public Object artJob() {
        ModelAndView model = new ModelAndView("admin/artJob.html");
        return model;
    }

    @Mapping("user")
    public Object user() {
        ModelAndView model = new ModelAndView("admin/user.html");
        return model;
    }

    @Mapping("about")
    public Object about() {
        ModelAndView model = new ModelAndView("admin/about.html");
        return model;
    }

    @Mapping("maven")
    public Object maven() {
        ModelAndView model = new ModelAndView("admin/maven.html");
        return model;
    }

    @Mapping("login")
    public Object login() {
        ModelAndView model = new ModelAndView("admin/login.html");
        return model;
    }

    @Mapping("")
    public Object index(Context ctx) {
        String token = ctx.cookie(cfgExtractor.tokenCookieName());

        if (Utils.isEmpty(token)) {
            ModelAndView model = new ModelAndView("admin/index-noauth.html");
            return model;
        } else {
            ModelAndView model = new ModelAndView("admin/index-auth.html");
            return model;
        }
    }
}
