package com.squirrel.shortLink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接跳转实体
 */
@Data
@Builder
@TableName("t_link_goto")
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkGotoDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分组标识
     */
    @TableField("gid")
    private String gid;

    /**
     * 完整短链接
     */
    @TableField("full_short_url")
    private String fullShortUrl;
}
