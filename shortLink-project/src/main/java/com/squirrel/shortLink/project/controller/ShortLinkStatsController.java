package com.squirrel.shortLink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.squirrel.shortLink.common.convention.result.Result;
import com.squirrel.shortLink.common.convention.result.Results;
import com.squirrel.shortLink.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import com.squirrel.shortLink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.squirrel.shortLink.project.dto.resp.ShortLinkStatsRespDTO;
import com.squirrel.shortLink.project.service.ShortLinkStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "短链接监控控制层")
public class ShortLinkStatsController {

    private final ShortLinkStatsService shortLinkStatsService;

    /**
     * 访问单个短链接指定时间内监控数据
     * @param requestParam 访问短链接参数
     * @return Result<ShortLinkStatsRespDTO>
     */
    @Operation(summary = "访问单个短链接指定时间内监控数据")
    @GetMapping("/api/short-link/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return Results.success(shortLinkStatsService.oneShortLinkStats(requestParam));
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     * @param requestParam 短链接信息
     * @return Result<IPage<ShortLinkStatsAccessRecordRespDTO>>
     */
    @Operation(summary = "访问单个短链接指定时间内访问记录监控数据")
    @GetMapping("/api/short-link/v1/stats/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return Results.success(shortLinkStatsService.shortLinkStatsAccessRecord(requestParam));
    }

    /**
     * 访问分组短链接指定时间内监控数据
     * @param requestParam 分组短链接信息
     * @return Result<ShortLinkStatsRespDTO>
     */
    @Operation(summary = "访问分组短链接指定时间内监控数据")
    @GetMapping("/api/short-link/v1/stats/group")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        return Results.success(shortLinkStatsService.groupShortLinkStats(requestParam));
    }

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     * @param requestParam 分组短链接信息
     * @return Result<IPage<ShortLinkStatsAccessRecordRespDTO>>
     */
    @Operation(summary = "访问分组短链接指定时间内监控数据")
    @GetMapping("/api/short-link/v1/stats/access-record/group")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
        return Results.success(shortLinkStatsService.groupShortLinkStatsAccessRecord(requestParam));
    }
}
