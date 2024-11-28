package com.squirrel.shortLink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.squirrel.shortLink.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import com.squirrel.shortLink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.squirrel.shortLink.project.dto.resp.ShortLinkStatsRespDTO;

/**
 * 短链接监控接口层
 */
public interface ShortLinkStatsService {

    /**
     * 获取单个短链接监控数据
     * @param requestParam 查询参数
     * @return 监控数据
     */
    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam);

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     * @param requestParam 获取短链接监控访问记录数据入参
     * @return 访问记录监控数据
     */
    IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam);

    /**
     * 访问分组短链接指定时间内监控数据
     * @param requestParam 分组短链接
     * @return 监控数据
     */
    ShortLinkStatsRespDTO groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam);

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     * @param requestParam 分组短链接信息
     * @return 监控数据
     */
    IPage<ShortLinkStatsAccessRecordRespDTO> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam);
}
