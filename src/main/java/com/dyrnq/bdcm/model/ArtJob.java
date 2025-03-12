package com.dyrnq.bdcm.model;

import lombok.Data;
import org.noear.wood.annotation.Column;
import org.noear.wood.annotation.PrimaryKey;
import org.noear.wood.annotation.Table;

import java.util.Date;

@Table("art_job")
@Data
public class ArtJob {
    @Column("id")
    @PrimaryKey
    private Long id;

    @Column("art_id")
    private Integer artId;

    @Column("user_id")
    private Integer userId;

    @Column("status")
    private Integer status;

    @Column("begin_time")
    private Date beginTime;

    @Column("end_time")
    private Date endTime;
}
