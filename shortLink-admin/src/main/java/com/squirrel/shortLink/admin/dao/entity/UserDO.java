package com.squirrel.shortLink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.squirrel.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户持久层实体
 */
@Data
@TableName("t_user")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDO extends BaseDO {

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
}
