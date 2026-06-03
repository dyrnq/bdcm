package com.dyrnq.bdcm.controller.api;

import cn.hutool.core.util.PageUtil;
import com.dyrnq.bdcm.controller.ApiController;
import com.dyrnq.bdcm.controller.PageResult;
import com.dyrnq.bdcm.dso.ArtJobLogMapper;
import com.dyrnq.bdcm.dso.ArtJobMapper;
import com.dyrnq.bdcm.model.ArtJob;
import com.dyrnq.bdcm.model.ArtJobLog;
import com.dyrnq.bdcm.service.ArtJobService;
import com.dyrnq.bdcm.service.dto.ArtJobQuery;
import com.dyrnq.bdcm.service.dto.ArtJobView;
import java.util.List;
import java.util.Map;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Path;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.annotation.Valid;
import org.noear.wood.IPage;
import org.noear.wood.MapperWhereQ;
import org.noear.wood.ext.Act1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mapping("api/artJob")
@Controller
@Valid
public class ArtJobController extends ApiController {
    static Logger logger = LoggerFactory.getLogger(ArtJobController.class);

    @Inject
    ArtJobMapper artJobMapper;

    @Inject
    ArtJobLogMapper artJobLogMapper;

    @Inject
    ArtJobService artJobService;

    @Mapping("")
    public PageResult query(Context ctx, int page, int limit, ArtJobQuery query) {
        try {
            int start = PageUtil.getStart(page - 1, limit);
            IPage<ArtJobView> p = artJobService.query(start, limit, query);
            return PageResult.succeed(p.getList(), p.getTotal());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return PageResult.failure(e.getMessage());
        }
    }

    @Mapping("del")
    public Result del(Context ctx, long... id) {
        try {
            for (long i : id) {
                artJobMapper.deleteById(i);
                artJobLogMapper.delete(mapperWhereQ -> {
                    mapperWhereQ.whereEq("art_job_id", i);
                });
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
            ArtJob artifact = artJobMapper.selectById(id);
            return Result.succeed(artifact);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    @Mapping("log/{id}")
    public Result log(Context ctx, @Path("id") long id) {
        try {
            Act1<MapperWhereQ> condition = mapperWhereQ -> {
                mapperWhereQ.whereEq("art_job_id", id);
            };
            List<ArtJobLog> list = artJobLogMapper.selectList(condition);
            return Result.succeed(list);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    @Mapping("report")
    public Result report(Context ctx) {
        try {
            Map<String, Long> report = artJobService.report();
            StringBuilder html = new StringBuilder();
            for (Map.Entry<String, Long> entry : report.entrySet()) {
                html.append(entry.getKey())
                        .append(": <b>")
                        .append(entry.getValue())
                        .append("</b> ");
            }
            return Result.succeed(html.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }

    @Mapping("threadPool")
    public Result threadPool(Context ctx) {
        try {
            Map<String, Object> report = artJobService.threadPool();
            StringBuilder html = new StringBuilder();
            for (Map.Entry<String, Object> entry : report.entrySet()) {
                html.append(entry.getKey())
                        .append(": <b>")
                        .append(entry.getValue())
                        .append("</b> ");
            }
            return Result.succeed(html.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.failure(e.getMessage());
        }
    }
}
