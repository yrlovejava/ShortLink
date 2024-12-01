package com.squirrel.shortLink.project.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 短链接修改请求对象
 */
@Data
@Schema(description = "短链接修改请求参数")
public class ShortLinkUpdateReqDTO {

    /**
     * 原始链接
     */
    @Schema(description = "原始链接")
    private String originUrl;

    /**
     * 完整短链接
     */
    @Schema(description = "完整短链接")
    private String fullShortUrl;

    /**
     * 原始分组标识
     */
    @Schema(description = "原始分组标识")
    private String originGid;

    /**
     * 分组标识
     */
    @Schema(description = "分组标识")
    private String gid;

    /**
     * 有效期类型 0：永久有效 1：自定义
     */
    @Schema(description = "有效期类型 0：永久有效 1：自定义")
    private Integer validDateType;

    /**
     * 有效期
     */
    @Schema(description = "有效期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date validDate;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String describe;
}
