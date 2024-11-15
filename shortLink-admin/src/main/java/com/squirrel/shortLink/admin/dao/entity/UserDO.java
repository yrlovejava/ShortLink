package com.squirrel.shortLink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 用户持久层实体
 */
@Data
@TableName("t_user")
public class UserDO {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField("mail")
    private String mail;

    /**
     * 注销时间戳
     */
    @TableField("deletion_time")
    private Long deletionTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 删除标识:
     * 0: 未删除
     * 1: 已删除
     */
    @TableField(value = "del_flag",fill = FieldFill.INSERT)
    private Integer delFlag;
}
