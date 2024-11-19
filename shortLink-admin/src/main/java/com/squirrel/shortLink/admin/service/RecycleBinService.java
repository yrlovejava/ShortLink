package com.squirrel.shortLink.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.squirrel.common.convention.result.Result;
import com.squirrel.shortLink.admin.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.squirrel.shortLink.admin.remote.dto.resp.ShortLinkPageRespDTO;

/**
 * URL 回收站接口层
 */
public interface RecycleBinService {

    /**
     * 分页查询回收站短链接
     * @param requestParam 请求参数
     * @return 返回参数包装
     */
    Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam);
}