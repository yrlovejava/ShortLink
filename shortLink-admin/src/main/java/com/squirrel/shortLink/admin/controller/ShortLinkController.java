package com.squirrel.shortLink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.squirrel.common.convention.result.Result;
import com.squirrel.common.convention.result.Results;
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
public class ShortLinkController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 创建短链接
     * @param requestParam 短链接创建信息
     * @return Result<ShortLinkCreateRespDTO>
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return shortLinkActualRemoteService.createShortLink(requestParam);
    }

    /**
     * 批量创建短链接
     * @param requestParam 批量短链接创建信息
     * @param response http响应
     */
    @SneakyThrows
    @PostMapping("/api/short-link/admin/v1/create/batch")
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
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return shortLinkActualRemoteService.pageShortLink(requestParam.getGid(), requestParam.getOrderTag(), requestParam.getCurrent(), requestParam.getSize());
    }

    /**
     * 修改短链接
     * @param requestParam 修改短链接信息
     * @return Result<Void>
     */
    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkActualRemoteService.updateShortLink(requestParam);
        return Results.success();
    }
}
