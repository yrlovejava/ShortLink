package com.squirrel.shortLink.admin.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 回收站短链接分页请求参数
 */
@Data
@Schema(description = "回收站短链接分页请求参数")
public class ShortLinkRecycleBinPageReqDTO extends Page {

    /**
     * 分组标识
     */
    @Schema(description = "分组标识集合")
    private List<String> gidList;
}
