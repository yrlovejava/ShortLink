package com.squirrel.shortLink.admin.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 回收站恢复功能
 */
@Data
@Schema(description = "回收站恢复功能远程调用参数")
public class RecycleBinRecoverReqDTO {

    /**
     * 分组标识
     */
    @Schema(description = "分组标识")
    private String gid;

    /**
     * 全部短链接
     */
    @Schema(description = "全部短链接")
    private String fullShortUrl;
}
