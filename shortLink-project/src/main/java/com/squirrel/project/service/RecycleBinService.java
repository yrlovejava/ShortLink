package com.squirrel.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.squirrel.project.dao.entity.ShortLinkDO;
import com.squirrel.project.dto.req.RecycleBinRecoverReqDTO;
import com.squirrel.project.dto.req.RecycleBinRemoveReqDTO;
import com.squirrel.project.dto.req.RecycleBinSaveReqDTO;
import com.squirrel.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.squirrel.project.dto.resp.ShortLinkPageRespDTO;

/**
 * 回收站管理接口层
 */
public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 保存回收站
     *
     * @param requestParam 请求参数
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);

    /**
     * 分页查询回收站短链接
     * @param requestParam 分页查询短链接参数
     * @return IPage<ShortLinkPageRespDTO>
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam);

    /**
     * 恢复短链接
     *
     * @param requestParam 请求参数
     */
    void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam);

    /**
     * 移除短链接
     * @param requestParam 短链接移除请求参数
     */
    void removeRecycleBin(RecycleBinRemoveReqDTO requestParam);
}
