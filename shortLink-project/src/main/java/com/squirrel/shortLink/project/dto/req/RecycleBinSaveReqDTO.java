package com.squirrel.shortLink.project.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 回收站保存参数
 */
@Data
@Schema(description = "回收站保存参数")
public class RecycleBinSaveReqDTO {

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
