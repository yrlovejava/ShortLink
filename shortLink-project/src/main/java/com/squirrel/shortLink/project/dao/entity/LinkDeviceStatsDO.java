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
 * 访问设备统计访问实体
 */
@Data
@TableName("t_link_device_stats")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkDeviceStatsDO extends BaseDO {

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
     * 访问设备
     */
    @TableField("device")
    private String device;
}
