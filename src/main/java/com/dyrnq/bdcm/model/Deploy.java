package com.dyrnq.bdcm.model;

import org.noear.solon.core.handle.UploadedFile;
import org.noear.wood.annotation.Column;
import org.noear.wood.annotation.Exclude;
import org.noear.wood.annotation.PrimaryKey;
import org.noear.wood.annotation.Table;

import java.util.Date;

@Table("deploy")
public class Deploy {
    @Column("id")
    @PrimaryKey
    private
    String id;

    @Column("insert_time")
    private
    Date insertTime;

    @Column("update_time")
    private
    Date updateTime;

    @Column("title")
    private
    String title;

    @Column("content")
    private
    String content;

    @Column("inst_id")
    private
    String instId;

    @Column("state")
    private
    Integer state;
    @Exclude
    private
    UploadedFile uploadFile;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getInstId() {
        return instId;
    }

    public void setInstId(String instId) {
        this.instId = instId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public UploadedFile getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(UploadedFile uploadFile) {
        this.uploadFile = uploadFile;
    }
}
