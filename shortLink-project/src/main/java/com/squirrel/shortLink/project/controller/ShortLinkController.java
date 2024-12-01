package com.squirrel.shortLink.project.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.squirrel.shortLink.common.convention.result.Result;
import com.squirrel.shortLink.common.convention.result.Results;
import com.squirrel.shortLink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkPageReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkUpdateReqDTO;
import com.squirrel.shortLink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.squirrel.shortLink.project.dto.resp.ShortLinkCreateRespDTO;
import com.squirrel.shortLink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.squirrel.shortLink.project.dto.resp.ShortLinkPageRespDTO;
import com.squirrel.shortLink.project.handler.CustomBlockHandler;
import com.squirrel.shortLink.project.service.ShortLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "短链接控制层")
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 短链接跳转
     * @param shortUri 短链接
     * @param request http请求
     * @param response http响应
     */
    @Operation(summary = "短链接跳转")
    @GetMapping("/{short-uri:^(?!doc\\.html$).*}")// 排除/doc.html
    public void restoreUrl(@PathVariable("short-uri") String shortUri, ServletRequest request, ServletResponse response) {
        shortLinkService.restoreUrl(shortUri, request, response);
    }

    /**
     * 创建短链接
     * @param requestParam 创建短链接的参数
     * @return Result<ShortLinkCreateRespDTO>
     */
    @Operation(summary = "创建短链接")
    @PostMapping("/api/short-link/v1/create")
    @SentinelResource(
            value = "create_short-link",
            blockHandler = "createShortLinkBlockHandleMethod",
            blockHandlerClass = CustomBlockHandler.class
    )
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return Results.success(shortLinkService.createShortLink(requestParam));
    }

    /**
     * 通过分布式锁创建短链接
     * @param requestParam 创建短链接请求参数
     * @return Result<ShortLinkCreateRespDTO>
     */
    @Operation(summary = "通过分布式锁创建短链接")
    @PostMapping("/api/short-link/v1/create/by-lock")
    public Result<ShortLinkCreateRespDTO> createShortLinkByLock(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return Results.success(shortLinkService.createShortLinkByLock(requestParam));
    }

    /**
     * 批量创建短链接
     * @param requestParam 批量短链接创建信息
     * @return Result<ShortLinkBatchCreateRespDTO>
     */
    @Operation(summary = "批量创建短链接")
    @PostMapping("/api/short-link/v1/create/batch")
    public Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam) {
        return Results.success(shortLinkService.batchCreateShortLink(requestParam));
    }

    /**
     * 分页查询短链接
     * @param requestParam 分页查询参数
     * @return Result<IPage<ShortLinkPageRespDTO>>
     */
    @Operation(summary = "分页查询短链接")
    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return Results.success(shortLinkService.pageShortLink(requestParam));
    }

    /**
     * 查询短链接分组内数量
     * @param requestParam 查询参数(分组id的集合)
     * @return Result<List<ShortLinkGroupCountQueryRespDTO>>
     */
    @Operation(summary = "查询短链接分组内数量")
    @GetMapping("/api/short-link/v1/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam) {
        return Results.success(shortLinkService.listGroupShortLinkCount(requestParam));
    }

    /**
     * 修改短链接
     * @param requestParam 修改短链接信息
     * @return Result<Void>
     */
    @Operation(summary = "修改短链接")
    @PostMapping("/api/short-link/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkService.updateShortLink(requestParam);
        return Results.success();
    }
}
