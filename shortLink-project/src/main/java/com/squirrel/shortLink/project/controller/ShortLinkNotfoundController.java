package com.squirrel.shortLink.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 短链接不存在跳转控制器
 */
@Controller
@Tag(name = "短链接不存在跳转控制器")
public class ShortLinkNotfoundController {

    /**
     * 短链接不存在跳转页面
     * @return 不存在页面
     */
    @Operation(summary = "短链接不存在跳转页面")
    @RequestMapping("/page/notfound")
    public String notfound(){
        return "notfound";
    }

}
