package com.squirrel.shortLink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.squirrel.shortLink.common.convention.result.Result;
import com.squirrel.shortLink.common.convention.result.Results;
import com.squirrel.shortLink.project.dto.req.RecycleBinRecoverReqDTO;
import com.squirrel.shortLink.project.dto.req.RecycleBinRemoveReqDTO;
import com.squirrel.shortLink.project.dto.req.RecycleBinSaveReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.squirrel.shortLink.project.dto.resp.ShortLinkPageRespDTO;
import com.squirrel.shortLink.project.service.RecycleBinService;
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
     * @param requestParam 分页查询短链接参数
     * @return Result<IPage<ShortLinkPageRespDTO>>
     */
    @GetMapping("/api/short-link/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        return Results.success(recycleBinService.pageShortLink(requestParam));
    }

    /**
     * 恢复短链接
     * @param requestParam 恢复短链接参数
     * @return  Result<Void>
     */
    @PostMapping("/api/short-link/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam) {
        recycleBinService.recoverRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 移除短链接
     * @param requestParam 移除短链接的参数
     * @return Result<Void>
     */
    @PostMapping("/api/short-link/v1/recycle-bin/remove")
    public Result<Void> removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam) {
        recycleBinService.removeRecycleBin(requestParam);
        return Results.success();
    }
}
