package com.dyrnq.bdcm.dso;

import com.dyrnq.bdcm.model.Deploy;
import org.noear.wood.BaseMapper;
import org.noear.wood.annotation.Db;

@Db("db1")
public interface DeployMapper extends BaseMapper<Deploy> {
}
