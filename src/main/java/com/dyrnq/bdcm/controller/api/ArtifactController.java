package com.dyrnq.bdcm.controller.api;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.StrUtil;
import com.dyrnq.bdcm.controller.ApiController;
import com.dyrnq.bdcm.controller.PageResult;
import com.dyrnq.bdcm.dso.ArtifactMapper;
import com.dyrnq.bdcm.model.Artifact;
import com.dyrnq.bdcm.service.ArtifactService;
import com.dyrnq.bdcm.service.dto.ArtQuery;
import com.dyrnq.utils.IDUtils;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.annotation.Numeric;
import org.noear.solon.validation.annotation.Valid;
import org.noear.wood.IPage;
import org.noear.wood.MapperWhereQ;
import org.noear.wood.ext.Act1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

@Mapping("api/artifact")
@Controller
@Valid
public class ArtifactController extends ApiController {
    static Logger logger = LoggerFactory.getLogger(ArtifactController.class);
    @Inject
    ArtifactMapper artifactMapper;
    @Inject
    ArtifactService artifactService;

    @Mapping("")
    public PageResult query(Context ctx, int page, int limit, ArtQuery query) {
        try {

            Act1<MapperWhereQ> condition = mapperWhereQ -> {
                mapperWhereQ.whereTrue();

                if (StrUtil.isNotBlank(query.getArtName())) {
                    mapperWhereQ.and().beginLk("name", "%" + query.getArtName() + "%").end();
                }
                if (StrUtil.isNotBlank(query.getArtUrl())) {
                    mapperWhereQ.and().beginLk("url", "%" + query.getArtUrl() + "%").end();
                }
                if (ObjectUtil.isNotEmpty(query.getArtId())) {
                    mapperWhereQ.and().beginLk("id", "%" + query.getArtId() + "%").end();
                }
            };

            int start = PageUtil.getStart(page - 1, limit);
            IPage<Artifact> p = artifactMapper.selectPage(start, limit, condition);
            return PageResult.succeed(p.getList(), p.getTotal());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return PageResult.failure(e.getMessage());
        }
    }

    @Mapping("add")
    @Numeric("id")
    public Result add(Context ctx, Artifact artifact) {
        try {
            Long id =IDUtils.getLongID();
            logger.info("id={}",id);
            if(ObjectUtil.isNull(artifact.getId())){
                artifact.setId(id);
            }
            artifact.setUpdateTime(new Date());
            artifact.setInsertTime(new Date());
            artifactMapper.insert(artifact, true);
            return Result.succeed("ok");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    @Mapping("del")
    public Result del(Context ctx, long... id) {
        try {
            for (long i : id) {
                artifactMapper.deleteById(i);
            }
            return Result.succeed("ok");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    @Mapping("get")
    public Result get(Context ctx, long id) {
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
            artifact.setUpdateTime(new Date());
            artifactMapper.updateById(artifact, true);
            return Result.succeed("ok");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    @Mapping("/download")
    public Result download(Context ctx, long... id) {
        try {
            List<Long> list = CollectionUtil.newArrayList();
            for (long i : id) {
                list.add(i);
            }
            artifactService.download(list);
            return Result.succeed("ok");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    @Mapping("/enable")
    public Result enable(Context ctx, long... id) {
        try {
            List<Long> list = CollectionUtil.newArrayList();
            for (long i : id) {
                list.add(i);
            }
            artifactMapper.db().table("artifact").set("auto_job", 1).whereTrue().and().beginIn("id", list).end().update();
            return Result.succeed("ok");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    @Mapping("/disable")
    public Result disable(Context ctx, long... id) {
        try {
            List<Long> list = CollectionUtil.newArrayList();
            for (long i : id) {
                list.add(i);
            }
            artifactMapper.db().table("artifact").set("auto_job", 0).whereTrue().and().beginIn("id", list).end().update();
            return Result.succeed("ok");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    @Mapping("/reset")
    public Result reset(Context ctx, long... id) {
        try {
            List<Long> list = CollectionUtil.newArrayList();
            for (long i : id) {
                list.add(i);
            }
            artifactMapper.db().table("artifact").usingNull(true)
                    .set("current_job_id", null)
                    .set("final_status", null)
                    .set("_lock", null).whereTrue().and().beginIn("id", list).end().update();
            return Result.succeed("ok");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

}

