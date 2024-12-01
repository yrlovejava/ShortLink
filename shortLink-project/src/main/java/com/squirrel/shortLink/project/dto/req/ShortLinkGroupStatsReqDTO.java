package com.squirrel.shortLink.project.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分组短链接监控请求参数
 */
@Data
@Schema(description = "分组短链接监控请求参数")
public class ShortLinkGroupStatsReqDTO {

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
}
