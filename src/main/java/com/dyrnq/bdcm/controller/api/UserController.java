package com.dyrnq.bdcm.controller.api;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.PageUtil;
import com.dyrnq.bdcm.controller.BaseController;
import com.dyrnq.bdcm.controller.PageResult;
import com.dyrnq.bdcm.dso.UserMapper;
import com.dyrnq.bdcm.model.User;
import com.dyrnq.bdcm.service.BusinessLogic;
import com.dyrnq.utils.BCryptPasswordEncoder;
import org.apache.commons.lang3.StringUtils;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.solon.i18n.I18nUtil;
import org.noear.wood.IPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Mapping("api/user")
@Controller
public class UserController extends BaseController {
    static Logger logger = LoggerFactory.getLogger(UserController.class);
    @Inject
    UserMapper userMapper;

    @Inject
    BusinessLogic businessLogic;

    @Mapping("")
    public PageResult query(Context ctx, int page, int limit) {
        try {
            int start = PageUtil.getStart(page - 1, limit);
            IPage<User> p = userMapper.selectPage(start, limit, null);
            List<User> userList = p.getList();
            userList.forEach(user -> user.setPass("**********************"));
            return PageResult.succeed(userList, p.getTotal());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return PageResult.failure(e.getMessage());
        }
    }

    @Mapping("add")
    public Result add(Context ctx, User user) {
        try {
            String base64Pass = user.getPass();
            String pass = Base64.decodeStr(Base64.decodeStr(base64Pass));
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
            user.setPass(encoder.encode(pass));
            userMapper.insert(user, true);
            return Result.succeed("ok");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    @Mapping("del")
    public Result del(Context ctx, String... id) {
        try {
            for (String i : id) {
                userMapper.deleteById(i);
            }
            return Result.succeed("ok");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    @Mapping("get")
    public Result get(Context ctx, String id) {
        try {
            User user = userMapper.selectById(id);
            user.setPass("**********************");
            return Result.succeed(user);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    @Mapping("update")
    public Result update(Context ctx, User user) {
        //throw new RuntimeException("not support");
        try {
            userMapper.updateById(user, true);
            return Result.succeed("ok");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    @Mapping("changePass")
    public Result changePass(Context ctx, String id, String newPass) {
        try {
            if (StringUtils.isBlank(newPass)) {
                throw new RuntimeException(I18nUtil.getMessage("loginStr.error2"));
            }
            businessLogic.changePass(id, newPass);
            return Result.succeed("ok");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }
}
