package com.dyrnq.bdcm.model;

import lombok.Data;
import org.noear.wood.annotation.Column;
import org.noear.wood.annotation.PrimaryKey;
import org.noear.wood.annotation.Table;

import java.util.Date;

@Table("artifact")
@Data
public class Artifact {
    @Column("id")
    @PrimaryKey
    private Long id;

    @Column("url")
    private String url;

    @Column("insert_time")
    private Date insertTime;

    @Column("update_time")
    private Date updateTime;

    @Column("final_status")
    private Integer finalStatus;

    @Column("name")
    private String name;

    @Column("_lock")
    private Integer lock;

    @Column("begin_lock")
    private Date beginLock;

    @Column("auto_job")
    private Integer autoJob;

    @Column("current_job_id")
    private Long currentJobId;

    @Column("etag")
    private String etag;

    @Column("file_size")
    private Long fileSize;
}
