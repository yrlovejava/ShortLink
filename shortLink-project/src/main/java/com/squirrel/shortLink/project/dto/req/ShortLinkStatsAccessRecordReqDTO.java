package com.squirrel.shortLink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 短链接监控访问记录请求参数
 */
@Data
@Schema(description = "短链接监控访问记录请求参数")
public class ShortLinkStatsAccessRecordReqDTO extends Page {

    /**
     * 完整短链接
     */
    @Schema(description = "完整短链接")
    private String fullShortUrl;

    /**
     * 分组标识
     */
    @Schema(description = "分组标识")
    private String gid;

    /**
     * 开始日期
     */
    @Schema(description = "开始日期")
    private String startDate;

    /**
     * 结束日期
     */
    @Schema(description = "结束日期")
    private String endDate;

    /**
     * 启用标识 0: 启用 1: 未启用
     */
    @Schema(description = "启用标识 0: 启用 1: 未启用")
    private Integer enableStatus;
}
