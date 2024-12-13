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
package com.squirrel.shortLink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.squirrel.shortLink.project.common.convention.result.Result;
import com.squirrel.shortLink.project.common.convention.result.Results;
import com.squirrel.shortLink.project.dto.req.RecycleBinRecoverReqDTO;
import com.squirrel.shortLink.project.dto.req.RecycleBinRemoveReqDTO;
import com.squirrel.shortLink.project.dto.req.RecycleBinSaveReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.squirrel.shortLink.project.dto.resp.ShortLinkPageRespDTO;
import com.squirrel.shortLink.project.service.RecycleBinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 回收站管理控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/v1/recycle-bin")
@Tag(name = "回收站管理控制层")
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    /**
     * 保存回收站
     * @param requestParam 回收站保存参数
     * @return Result<Void>
     */
    @Operation(summary = "保存回收站")
    @PostMapping("/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        recycleBinService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     * @param requestParam 分页查询短链接参数
     * @return Result<IPage<ShortLinkPageRespDTO>>
     */
    @Operation(summary = "分页查询回收站短链接")
    @GetMapping("/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        return Results.success(recycleBinService.pageShortLink(requestParam));
    }

    /**
     * 恢复短链接
     * @param requestParam 恢复短链接参数
     * @return  Result<Void>
     */
    @Operation(summary = "恢复短链接")
    @PostMapping("/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam) {
        recycleBinService.recoverRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 移除短链接
     * @param requestParam 移除短链接的参数
     * @return Result<Void>
     */
    @Operation(summary = "移除短链接")
    @PostMapping("/remove")
    public Result<Void> removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam) {
        recycleBinService.removeRecycleBin(requestParam);
        return Results.success();
    }
}
