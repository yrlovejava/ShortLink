package com.squirrel.shortLink.admin.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 短链接分组修改参数
 */
@Data
@Schema(description = "短链接分组修改请求参数")
public class ShortLinkGroupUpdateReqDTO {

    /**
     * 分组标识
     */
    @Schema(description = "分组标识")
    private String gid;

    /**
     * 分组名
     */
    @Schema(description = "分组名")
    private String name;
}
