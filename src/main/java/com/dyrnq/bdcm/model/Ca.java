package com.dyrnq.bdcm.model;

import org.noear.wood.annotation.Column;
import org.noear.wood.annotation.PrimaryKey;
import org.noear.wood.annotation.Table;

@Table("ca")
public class Ca {
    @Column("id")
    @PrimaryKey
    private
    String id;

    @Column("cert")
    private
    String cert;

    //在关系型数据库中，字段名不应该使用数据库的保留字作为名称。_r 表示raw(data)
    @Column("key_r")
    private
    String key;

    @Column("not_after")
    private
    Long notAfter;

    @Column("not_before")
    private
    Long notBefore;


    @Column("subject")
    private
    String subject;

    @Column("title")
    private
    String title;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCert() {
        return cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(Long notAfter) {
        this.notAfter = notAfter;
    }

    public Long getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Long notBefore) {
        this.notBefore = notBefore;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
