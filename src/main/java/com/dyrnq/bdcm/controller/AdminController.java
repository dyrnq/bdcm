package com.dyrnq.bdcm.controller;


import com.dyrnq.bdcm.CfgExtractor;
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


    @Mapping("inst")
    public Object inst() {
        ModelAndView model = new ModelAndView("admin/inst.html");
        return model;
    }

    @Mapping("manifest")
    public Object manifest() {
        ModelAndView model = new ModelAndView("admin/manifest.html");
        return model;
    }

    @Mapping("deploy")
    public Object deploy(Context ctx) {
        ModelAndView model = new ModelAndView("admin/deploy.html");
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

    @Mapping("route")
    public Object route() {
        ModelAndView model = new ModelAndView("admin/route.html");
        return model;
    }

    @Mapping("plugin")
    public Object plugin() {
        ModelAndView model = new ModelAndView("admin/plugin.html");
        return model;
    }

    @Mapping("streamRoute")
    public Object streamRoute() {
        ModelAndView model = new ModelAndView("admin/streamRoute.html");
        return model;
    }

    @Mapping("upstream")
    public Object upstream() {
        ModelAndView model = new ModelAndView("admin/upstream.html");
        return model;
    }

    @Mapping("service")
    public Object service() {
        ModelAndView model = new ModelAndView("admin/service.html");
        return model;
    }

    @Mapping("secret")
    public Object secret() {
        ModelAndView model = new ModelAndView("admin/secret.html");
        return model;
    }

    @Mapping("ssl")
    public Object ssl() {
        ModelAndView model = new ModelAndView("admin/ssl.html");
        return model;
    }

    @Mapping("consumer")
    public Object consumer() {
        ModelAndView model = new ModelAndView("admin/consumer.html");
        return model;
    }

    @Mapping("login")
    public Object login() {
        ModelAndView model = new ModelAndView("admin/login.html");
        return model;
    }

    @Mapping("globalRule")
    public Object globalRule() {
        ModelAndView model = new ModelAndView("admin/globalRule.html");
        return model;
    }

    @Mapping("pluginConfig")
    public Object pluginConfig() {
        ModelAndView model = new ModelAndView("admin/pluginConfig.html");
        return model;
    }

    @Mapping("consumerGroup")
    public Object consumerGroup() {
        ModelAndView model = new ModelAndView("admin/consumerGroup.html");
        return model;
    }

    @Mapping("editor")
    public Object editor(Context ctx, String cls, String id) {
        ModelAndView model = new ModelAndView("admin/editor.html");
        model.put("id", id);
        model.put("cls", cls);
        return model;
    }

    @Mapping("proto")
    public Object proto() {
        ModelAndView model = new ModelAndView("admin/proto.html");
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

    @Mapping("ca")
    public Object ca(Context ctx) {
        ModelAndView model = new ModelAndView("admin/ca.html");
        return model;
    }

}
