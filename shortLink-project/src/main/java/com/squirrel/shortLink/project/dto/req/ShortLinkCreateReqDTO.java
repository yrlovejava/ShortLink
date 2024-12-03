package com.squirrel.shortLink.project.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 创建短链接参数
 */
@Data
@Schema(description = "创建短链接参数")
public class ShortLinkCreateReqDTO {

    /**
     * 域名
     */
    @Schema(description = "域名")
    private String domain;

    /**
     * 原始链接
     */
    @Schema(description = "原始链接")
    private String originUrl;

    /**
     * 分组标识
     */
    @Schema(description = "分组标识")
    private String gid;

    /**
     * 创建类型 0：接口创建 1：控制台创建
     */
    @Schema(description = "创建类型 0：接口创建 1：控制台创建")
    private Integer createdType;

    /**
     * 有效期类型 0：永久有效 1：自定义
     */
    @Schema(description = "有效期类型 0：永久有效 1：自定义")
    private Integer validDateType;

    /**
     * 有效期
     */
    @Schema(description = "有效期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date validDate;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String describe;
}
