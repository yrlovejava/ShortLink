package com.squirrel.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.squirrel.common.convention.result.Result;
import com.squirrel.common.convention.result.Results;
import com.squirrel.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.squirrel.project.dto.req.ShortLinkStatsReqDTO;
import com.squirrel.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.squirrel.project.dto.resp.ShortLinkStatsRespDTO;
import com.squirrel.project.service.ShortLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkStatsService shortLinkStatsService;

    /**
     * 访问单个短链接指定时间内监控数据
     * @param requestParam 访问短链接参数
     * @return Result<ShortLinkStatsRespDTO>
     */
    @GetMapping("/api/short-link/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return Results.success(shortLinkStatsService.oneShortLinkStats(requestParam));
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     * @param requestParam 短链接信息
     * @return Result<IPage<ShortLinkStatsAccessRecordRespDTO>>
     */
    @GetMapping("/api/short-link/v1/stats/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return Results.success(shortLinkStatsService.shortLinkStatsAccessRecord(requestParam));
    }
}