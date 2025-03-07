package com.dyrnq.bdcm.dso;

import com.dyrnq.bdcm.model.Cert;
import org.noear.wood.BaseMapper;
import org.noear.wood.annotation.Db;

@Db("db1")
public interface CertMapper extends BaseMapper<Cert> {
}