package com.squirrel.shortLink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.squirrel.common.convention.result.Result;
import com.squirrel.shortLink.admin.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.squirrel.shortLink.admin.dto.req.ShortLinkStatsReqDTO;
import com.squirrel.shortLink.admin.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.squirrel.shortLink.admin.dto.resp.ShortLinkStatsRespDTO;
import com.squirrel.shortLink.admin.remote.ShortLinkRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    /**
     * 后续重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 访问单个短链接指定时间内监控数据
     * @param requestParam 查询参数
     * @return Result<ShortLinkStatsRespDTO>
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return shortLinkRemoteService.oneShortLinkStats(requestParam);
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return shortLinkRemoteService.shortLinkStatsAccessRecord(requestParam);
    }
}
