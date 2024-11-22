package com.squirrel.project.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.squirrel.common.database.BaseDO;
import lombok.Data;

import java.util.Date;

/**
 * 短链接今日统计实体
 */
@Data
@TableName("t_link_stats_today")
public class LinkStatsTodayDO extends BaseDO {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分组标识
     */
    @TableField(exist = false)
    private String gid;

    /**
     * 短链接
     */
    @TableField("fullShortUrl")
    private String fullShortUrl;

    /**
     * 日期
     */
    @TableField("date")
    private Date date;

    /**
     * 今日pv
     */
    @TableField("today_pv")
    private Integer todayPv;

    /**
     * 今日uv
     */
    @TableField("today_uv")
    private Integer todayUv;

    /**
     * 今日ip数
     */
    @TableField("today_uip")
    private Integer todayIpCount;
}