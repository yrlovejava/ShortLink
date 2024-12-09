package com.squirrel.shortLink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.squirrel.shortLink.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 短链接今日统计实体
 */
@Data
@TableName("t_link_stats_today")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkStatsTodayDO extends BaseDO {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
    private Integer todayUip;
}
