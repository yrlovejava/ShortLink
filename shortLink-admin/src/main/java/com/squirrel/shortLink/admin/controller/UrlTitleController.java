package com.squirrel.shortLink.admin.controller;

import com.squirrel.shortLink.common.convention.result.Result;
import com.squirrel.shortLink.admin.remote.ShortLinkActualRemoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * URL 标题控制层
 */
@RestController(value = "urlTitleControllerByAdmin")
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin/v1/title")
@Tag(name = "URL 标题控制层")
public class UrlTitleController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 根据 URL 获取对应网站的标题
     * @param url 网站url
     * @return Result<String>
     */
    @Operation(summary = "根据 URL 获取对应网站的标题")
    @GetMapping()
    public Result<String> getTitleByUrl(@RequestParam("url") String url) {
        return shortLinkActualRemoteService.getTitleByUrl(url);
    }
}
