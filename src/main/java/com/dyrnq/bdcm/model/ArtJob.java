package com.dyrnq.bdcm.model;

import java.util.Date;
import lombok.Data;
import org.noear.wood.annotation.Column;
import org.noear.wood.annotation.PrimaryKey;
import org.noear.wood.annotation.Table;

@Table("art_job")
@Data
public class ArtJob {
    @Column("id")
    @PrimaryKey
    private Long id;

    @Column("art_id")
    private Long artId;

    @Column("user_id")
    private String userId;

    @Column("status")
    private Integer status;

    @Column("begin_time")
    private Date beginTime;

    @Column("end_time")
    private Date endTime;

    @Column("progress")
    private String progress;
}
