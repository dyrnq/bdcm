package com.dyrnq.bdcm.model;

import lombok.Data;
import org.noear.wood.annotation.Column;
import org.noear.wood.annotation.PrimaryKey;
import org.noear.wood.annotation.Table;

import java.util.Date;

@Table("art_job_log")
@Data
public class ArtJobLog {
    @Column("id")
    @PrimaryKey
    private Long id;

    @Column("art_job_id")
    private Long artJobId;

    @Column("log")
    private String log;
}
