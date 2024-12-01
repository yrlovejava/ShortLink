package com.squirrel.shortLink.admin.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 短链接分组创建参数
 */
@Data
@Schema(description = "短链接分组创建远程调用参数")
public class ShortLinkGroupSaveReqDTO {

    /**
     * 分组名
     */
    @Schema(description = "分组名")
    private String name;
}
