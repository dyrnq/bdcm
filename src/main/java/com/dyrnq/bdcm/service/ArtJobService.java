package com.dyrnq.bdcm.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.dyrnq.bdcm.dso.ArtJobMapper;
import com.dyrnq.bdcm.service.dto.ArtJobQuery;
import com.dyrnq.bdcm.service.dto.ArtJobView;
import org.noear.solon.annotation.Component;
import org.noear.wood.DbContext;
import org.noear.wood.DbTableQuery;
import org.noear.wood.IPage;
import org.noear.wood.MapperWhereQ;
import org.noear.wood.annotation.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

@Component
public class ArtJobService {
    static Logger logger = LoggerFactory.getLogger(ArtJobService.class);
    @Db
    ArtJobMapper artJobMapper;
    @Db
    DbContext db;

    public IPage<ArtJobView> query(int start, int size, ArtJobQuery query) throws SQLException {

        //关联+分页查询
        DbTableQuery tableQuery = db.table("art_job a")
                .leftJoin("artifact b")
                .onEq("a.art_id", "b.id")
                .whereTrue();

        MapperWhereQ whereQ = new MapperWhereQ(tableQuery);
        // 根据 query 中的 artName 动态拼接 SQL
        if (StrUtil.isNotBlank(query.getArtName())) {
            whereQ.and().beginLk("b.name", "%" + query.getArtName() + "%").end();
        }
        if (StrUtil.isNotBlank(query.getArtUrl())) {
            whereQ.and().beginLk("b.url", "%" + query.getArtUrl() + "%").end();
        }
        if (ObjectUtil.isNotEmpty(query.getArtId())) {
            whereQ.and().beginLk("b.id", "%" + query.getArtId() + "%").end();
        }

        IPage<ArtJobView> page = tableQuery.selectPage("a.*, b.name as art_name, b.url as art_url", ArtJobView.class);
        return page;

    }

}
