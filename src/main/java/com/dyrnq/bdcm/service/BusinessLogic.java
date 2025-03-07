package com.dyrnq.bdcm.service;

import cn.hutool.core.codec.Base64;
import com.dyrnq.bdcm.dso.UserMapper;
import com.dyrnq.bdcm.model.User;
import com.dyrnq.utils.BCryptPasswordEncoder;
import org.noear.solon.annotation.Component;
import org.noear.solon.i18n.I18nUtil;
import org.noear.wood.MapperWhereQ;
import org.noear.wood.annotation.Db;
import org.noear.wood.ext.Act1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class BusinessLogic {
    static Logger logger = LoggerFactory.getLogger(BusinessLogic.class);
    @Db
    UserMapper userMapper;


    /**
     * 使用base64传入用户名和密码进行登录,传入的参数base64Name和base64Pass都被base64过两次，因此这里也要解开两次
     *
     * @param base64Name
     * @param base64Pass
     * @return
     */
    public User login(String base64Name, String base64Pass) {
        String name = Base64.decodeStr(Base64.decodeStr(base64Name));
        String pass = Base64.decodeStr(Base64.decodeStr(base64Pass));
        Act1<MapperWhereQ> condition = mapperWhereQ -> {
            mapperWhereQ.whereEq("name", name);
        };

        List<User> list = userMapper.selectList(condition);
        if (list != null && list.size() > 0) {
            User user = list.get(0);
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
            if (encoder.matches(pass, user.getPass())) {
                return user;
            } else {
                throw new RuntimeException(I18nUtil.getMessage("loginStr.backError2"));
            }
        } else {
            throw new RuntimeException(I18nUtil.getMessage("loginStr.backError5"));
        }

        //return null;
    }

    /**
     * 为某个用户更改密码,传入的密码参数(base64Pass)base64过两次，因此这里也要解开两次
     *
     * @param id
     * @param base64Pass
     */
    public void changePass(String id, String base64Pass) {
        String pass = Base64.decodeStr(Base64.decodeStr(base64Pass));
        User user = new User();
        user.setId(id);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        user.setPass(encoder.encode(pass));
        userMapper.updateById(user, true);
    }

    public User findByName(String name) {
        Act1<MapperWhereQ> condition = mapperWhereQ -> {
            mapperWhereQ.whereEq("name", name);
        };

        List<User> list = userMapper.selectList(condition);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }


}
