package com.squirrel.shortLink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.squirrel.shortLink.common.convention.result.Result;
import com.squirrel.shortLink.admin.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.squirrel.shortLink.admin.dto.req.ShortLinkGroupStatsReqDTO;
import com.squirrel.shortLink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.squirrel.shortLink.admin.remote.dto.req.ShortLinkStatsReqDTO;
import com.squirrel.shortLink.admin.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.squirrel.shortLink.admin.dto.resp.ShortLinkStatsRespDTO;
import com.squirrel.shortLink.admin.remote.ShortLinkActualRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController(value = "shortLinkStatsControllerByAdmin")
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin/v1/stats")
public class ShortLinkStatsController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 访问单个短链接指定时间内监控数据
     * @param requestParam 查询参数
     * @return Result<ShortLinkStatsRespDTO>
     */
    @GetMapping()
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return shortLinkActualRemoteService.oneShortLinkStats(
                requestParam.getFullShortUrl(),
                requestParam.getGid(),
                requestParam.getEnableStatus(),
                requestParam.getStartDate(),
                requestParam.getEndDate()
        );
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     * @param requestParam 单个短链接信息
     * @return Result<Page<ShortLinkStatsAccessRecordRespDTO>>
     */
    @GetMapping("/access-record")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return shortLinkActualRemoteService.shortLinkStatsAccessRecord(
                requestParam.getFullShortUrl(),
                requestParam.getGid(),
                requestParam.getStartDate(),
                requestParam.getEndDate(),
                requestParam.getEnableStatus(),
                requestParam.getCurrent(),
                requestParam.getSize()
        );
    }

    /**
     * 访问分组短链接指定时间内监控数据
     * @param requestParam 分组短链接信息
     * @return Result<ShortLinkStatsRespDTO>
     */
    @GetMapping("/group")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        return shortLinkActualRemoteService.groupShortLinkStats(
                requestParam.getGid(),
                requestParam.getStartDate(),
                requestParam.getEndDate()
        );
    }

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     * @param requestParam 分组短链接信息
     * @return Result<Page<ShortLinkStatsAccessRecordRespDTO>>
     */
    @GetMapping("/access-record/group")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
        return shortLinkActualRemoteService.groupShortLinkStatsAccessRecord(
                requestParam.getGid(),
                requestParam.getStartDate(),
                requestParam.getEndDate(),
                requestParam.getCurrent(),
                requestParam.getSize()
        );
    }
}
