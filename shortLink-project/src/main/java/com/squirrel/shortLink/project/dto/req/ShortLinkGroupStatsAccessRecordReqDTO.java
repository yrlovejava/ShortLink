package com.squirrel.shortLink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.squirrel.shortLink.project.dao.entity.LinkAccessLogsDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分组短链接监控访问记录请求参数
 */
@Data
@Schema(description = "分组短链接监控访问记录请求参数")
public class ShortLinkGroupStatsAccessRecordReqDTO extends Page<LinkAccessLogsDO> {

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
