package com.squirrel.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.squirrel.common.convention.result.Result;
import com.squirrel.common.convention.result.Results;
import com.squirrel.project.dto.req.RecycleBinSaveReqDTO;
import com.squirrel.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.squirrel.project.dto.resp.ShortLinkPageRespDTO;
import com.squirrel.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站管理控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    /**
     * 保存回收站
     * @param requestParam 回收站保存参数
     * @return Result<Void>
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        recycleBinService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     */
    @GetMapping("/api/short-link/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        return Results.success(recycleBinService.pageShortLink(requestParam));
    }
}
