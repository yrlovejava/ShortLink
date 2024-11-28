package com.squirrel.shortLink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.squirrel.common.convention.result.Result;
import com.squirrel.common.convention.result.Results;
import com.squirrel.shortLink.admin.dto.req.RecycleBinRecoverReqDTO;
import com.squirrel.shortLink.admin.dto.req.RecycleBinRemoveReqDTO;
import com.squirrel.shortLink.admin.dto.req.RecycleBinSaveReqDTO;
import com.squirrel.shortLink.admin.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.squirrel.shortLink.admin.remote.ShortLinkActualRemoteService;
import com.squirrel.shortLink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.squirrel.shortLink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站管理控制层
 */
@RestController(value = "recycleBinControllerByAdmin")
@RequiredArgsConstructor
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 保存回收站
     * @param requestParam 保存回收站短链接信息
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        shortLinkActualRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     * @param requestParam 分页查询信息
     */
    @GetMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        return recycleBinService.pageRecycleBinShortLink(requestParam);
    }

    /**
     * 恢复短链接
     * @param requestParam 恢复短链接参数
     * @return Result<Void>
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam) {
        shortLinkActualRemoteService.recoverRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 移除短链接
     * @param requestParam 移除短链接信息
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/remove")
    public Result<Void> removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam) {
        shortLinkActualRemoteService.removeRecycleBin(requestParam);
        return Results.success();
    }
}
