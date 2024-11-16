package com.squirrel.project.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.squirrel.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 短链接实体
 */
@Data
@TableName("t_link")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkDO extends BaseDO {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 域名
     */
    @TableField("domain")
    private String domain;

    /**
     * 短链接
     */
    @TableField("short_uri")
    private String shortUri;

    /**
     * 完整短链接
     */
    @TableField("full_short_url")
    private String fullShortUrl;

    /**
     * 原始链接
     */
    @TableField("origin_url")
    private String originUrl;

    /**
     * 点击量
     */
    @TableField("check_num")
    private Integer clickNum;

    /**
     * 分组标识
     */
    @TableField("gid")
    private String gid;

    /**
     * 启用标识 0：启用 1：未启用
     */
    @TableField("enableStatus")
    private Integer enableStatus;

    /**
     * 创建类型 0：接口创建 1：控制台创建
     */
    @TableField("created_type")
    private Integer createdType;

    /**
     * 有效期类型 0：永久有效 1：自定义
     */
    @TableField("validDateType")
    private Integer validDateType;

    /**
     * 有效期
     */
    @TableField("valid_date")
    private Date validDate;

    /**
     * 描述
     */
    @TableField("`describe`")
    private String describe;

    /**
     * 历史PV
     */
    @TableField("total_pv")
    private Integer totalPV;

    /**
     * 历史UV
     */
    @TableField("total_uv")
    private Integer totalUV;

    /**
     * 历史UIP
     */
    @TableField("total_uip")
    private Integer totalUip;
}
