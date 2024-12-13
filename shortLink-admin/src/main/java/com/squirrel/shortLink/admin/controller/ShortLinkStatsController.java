/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squirrel.shortLink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.squirrel.shortLink.admin.common.convention.result.Result;
import com.squirrel.shortLink.admin.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.squirrel.shortLink.admin.dto.req.ShortLinkGroupStatsReqDTO;
import com.squirrel.shortLink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.squirrel.shortLink.admin.remote.dto.req.ShortLinkStatsReqDTO;
import com.squirrel.shortLink.admin.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.squirrel.shortLink.admin.dto.resp.ShortLinkStatsRespDTO;
import com.squirrel.shortLink.admin.remote.ShortLinkActualRemoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "短链接监控控制层")
public class ShortLinkStatsController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 访问单个短链接指定时间内监控数据
     * @param requestParam 查询参数
     * @return Result<ShortLinkStatsRespDTO>
     */
    @Operation(summary = "访问单个短链接指定时间内监控数据")
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
    @Operation(summary = "访问单个短链接指定时间内访问记录监控数据")
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
    @Operation(summary = "访问分组短链接指定时间内监控数据")
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
    @Operation(summary = "访问分组短链接指定时间内访问记录监控数据")
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
