package com.dyrnq.bdcm.model;

import lombok.Data;
import org.noear.wood.annotation.Column;
import org.noear.wood.annotation.PrimaryKey;
import org.noear.wood.annotation.Table;

@Table("user")
@Data
public class User {
    @Column("id")
    @PrimaryKey
    private String id;

    @Column("name")
    private String name;

    @Column("pass")
    private String pass;

    @Column("email")
    private String email;

    @Column("phone")
    private String phone;
}
