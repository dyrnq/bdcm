package com.dyrnq.bdcm.service.job;

import com.dyrnq.bdcm.dso.ArtifactMapper;
import com.dyrnq.bdcm.model.Artifact;
import com.dyrnq.bdcm.service.ArtifactService;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.annotation.Scheduled;
import org.noear.wood.MapperWhereQ;
import org.noear.wood.ext.Act1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Scheduled(fixedRate = 1000 * 3)
public class ArtifactAutoDownloadJob implements Runnable {
    static Logger logger = LoggerFactory.getLogger(ArtifactAutoDownloadJob.class);
    @Inject
    ArtifactService artifactService;
    @Inject
    ArtifactMapper artifactMapper;

    @Override
    public void run() {
        logger.debug("开始ArtifactAutoDownloadJob..................");

        Act1<MapperWhereQ> condition = mapperWhereQ -> {
            //mapperWhereQ.whereEq("auto_job", 1).andNeq("_lock", 1).limit(1);
            mapperWhereQ.whereEq("auto_job", 1)
                    .and()
                    .begin("_lock is null or _lock !=1")
                    .end()
                    .and()
                    .begin("final_status is null or final_status !=1")
                    .end()
                    .limit(0, 1);
        };

        List<Artifact> list = artifactMapper.selectList(condition);
        if (list != null && !list.isEmpty()) {
            Artifact artifact = list.get(0);
            List<Long> ids = new ArrayList<>();
            ids.add(artifact.getId());
            artifactService.download(ids);
        } else {
            logger.debug("没有需要下载的");
        }
    }
}
