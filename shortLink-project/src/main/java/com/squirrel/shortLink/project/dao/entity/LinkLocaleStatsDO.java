package com.squirrel.shortLink.project.dao.entity;

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
 * 地区统计访问实体
 */
@Data
@TableName("t_link_locale_stats")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkLocaleStatsDO extends BaseDO {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 完整短链接
     */
    @TableField("full_short_url")
    private String fullShortUrl;

    /**
     * 分组标识
     */
    @TableField(exist = false)
    private String gid;

    /**
     * 日期
     */
    @TableField("date")
    private Date date;

    /**
     * 访问量
     */
    @TableField("cnt")
    private Integer cnt;

    /**
     * 省份名称
     */
    @TableField("province")
    private String province;

    /**
     * 市名称
     */
    @TableField("city")
    private String city;

    /**
     * 城市编码
     */
    @TableField("adcode")
    private String adcode;

    /**
     * 国家标识
     */
    @TableField("country")
    private String country;
}
