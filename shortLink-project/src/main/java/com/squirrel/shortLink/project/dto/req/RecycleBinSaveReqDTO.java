package com.squirrel.shortLink.project.dto.req;

import lombok.Data;

/**
 * 回收站保存参数
 */
@Data
public class RecycleBinSaveReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 全部短链接
     */
    private String fullShortUrl;
}
