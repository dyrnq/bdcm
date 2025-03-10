package com.dyrnq.bdcm.controller.api;


import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.StrUtil;
import com.dyrnq.bdcm.controller.ApiController;
import com.dyrnq.bdcm.controller.PageResult;
import com.dyrnq.bdcm.dso.ArtifactMapper;
import com.dyrnq.bdcm.model.Artifact;
import com.dyrnq.utils.IDUtils;
import org.apache.commons.lang3.StringUtils;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.wood.IPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Mapping("api/artifact")
@Controller
public class ArtifactController extends ApiController {
    static Logger logger = LoggerFactory.getLogger(ArtifactController.class);
    @Inject
    ArtifactMapper artifactMapper;

    @Mapping("")
    public PageResult query(Context ctx, int page, int limit) {
        try {
            int start = PageUtil.getStart(page - 1, limit);
            IPage<Artifact> p = artifactMapper.selectPage(start, limit, null);
            return PageResult.succeed(p.getList(), p.getTotal());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return PageResult.failure(e.getMessage());
        }
    }

    @Mapping("add")
    public Result add(Context ctx, Artifact artifact) {
        try {
            String id =IDUtils.getLongIDAsString();
            logger.info("id={}",id);
            if(StrUtil.isBlank(artifact.getId())){
                artifact.setId(id);
            }
            artifactMapper.insert(artifact, true);
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
                artifactMapper.deleteById(i);
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
            Artifact artifact = artifactMapper.selectById(id);
            return Result.succeed(artifact);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    @Mapping("update")
    public Result update(Context ctx, Artifact artifact) {
        try {
            artifactMapper.updateById(artifact, true);
            return Result.succeed("ok");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }


}

