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
    private String id;

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
}
