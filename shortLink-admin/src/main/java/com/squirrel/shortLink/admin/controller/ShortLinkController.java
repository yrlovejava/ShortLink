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
import com.squirrel.shortLink.admin.common.convention.result.Results;
import com.squirrel.shortLink.admin.remote.ShortLinkActualRemoteService;
import com.squirrel.shortLink.admin.remote.dto.req.ShortLinkBatchCreateReqDTO;
import com.squirrel.shortLink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.squirrel.shortLink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.squirrel.shortLink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.squirrel.shortLink.admin.remote.dto.resp.ShortLinkBaseInfoRespDTO;
import com.squirrel.shortLink.admin.remote.dto.resp.ShortLinkBatchCreateRespDTO;
import com.squirrel.shortLink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.squirrel.shortLink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.squirrel.shortLink.admin.toolkit.EasyExcelWebUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接后管控制层
 */
@RestController(value = "shortLinkControllerByAdmin")
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin/v1")
@Tag(name = "短链接后管控制层")
public class ShortLinkController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 创建短链接
     * @param requestParam 短链接创建信息
     * @return Result<ShortLinkCreateRespDTO>
     */
    @Operation(summary = "创建短链接")
    @PostMapping("/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return shortLinkActualRemoteService.createShortLink(requestParam);
    }

    /**
     * 批量创建短链接
     * @param requestParam 批量短链接创建信息
     * @param response http响应
     */
    @Operation(summary = "批量创建短链接")
    @SneakyThrows
    @PostMapping("/create/batch")
    public void batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam, HttpServletResponse response) {
        Result<ShortLinkBatchCreateRespDTO> shortLinkBatchCreateRespDTOResult = shortLinkActualRemoteService.batchCreateShortLink(requestParam);
        if (shortLinkBatchCreateRespDTOResult.isSuccess()) {
            List<ShortLinkBaseInfoRespDTO> baseLinkInfos = shortLinkBatchCreateRespDTOResult.getData().getBaseLinkInfos();
            EasyExcelWebUtil.write(response, "批量创建短链接-SaaS短链接系统", ShortLinkBaseInfoRespDTO.class, baseLinkInfos);
        }
    }

    /**
     * 分页查询短链接
     * @param requestParam 分页信息
     * @return Result<Page<ShortLinkPageRespDTO>>
     */
    @Operation(summary = "分页查询短链接")
    @GetMapping("/page")
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return shortLinkActualRemoteService.pageShortLink(requestParam.getGid(), requestParam.getOrderTag(), requestParam.getCurrent(), requestParam.getSize());
    }

    /**
     * 修改短链接
     * @param requestParam 修改短链接信息
     * @return Result<Void>
     */
    @Operation(summary = "修改短链接")
    @PostMapping("/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkActualRemoteService.updateShortLink(requestParam);
        return Results.success();
    }
}
