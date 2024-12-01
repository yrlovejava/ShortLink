package com.squirrel.shortLink.admin.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 短链接分组排序参数
 */
@Data
@Schema(description = "短链接分组排序请求参数")
public class ShortLinkGroupSortReqDTO {

    /**
     * 分组ID
     */
    @Schema(description = "分组标识")
    private String gid;

    /**
     * 排序
     */
    @Schema(description = "排序号")
    private Integer sortOrder;
}
