package com.dyrnq.bdcm.dso;

import com.dyrnq.bdcm.model.Artifact;
import org.noear.wood.BaseMapper;
import org.noear.wood.annotation.Db;

@Db("db1")
public interface ArtifactMapper extends BaseMapper<Artifact> {
}
