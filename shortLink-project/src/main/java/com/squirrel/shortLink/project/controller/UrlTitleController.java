package com.squirrel.shortLink.project.controller;

import com.squirrel.shortLink.common.convention.result.Result;
import com.squirrel.shortLink.common.convention.result.Results;
import com.squirrel.shortLink.project.service.UrlTitleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * URL 标题控制层
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "URL 标题控制层")
public class UrlTitleController {

    private final UrlTitleService urlTitleService;

    /**
     * 根据 URL 获取对应网站的标题
     * @param url 网站url
     * @return Result<String>
     */
    @Operation(summary = "根据 URL 获取对应网站的标题")
    @GetMapping("/api/short-link/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url) {
        return Results.success(urlTitleService.getTitleByUrl(url));
    }
}
