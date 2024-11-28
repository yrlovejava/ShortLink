package com.squirrel.shortLink.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 短链接不存在跳转控制器
 */
@Controller
public class ShortLinkNotfoundController {

    /**
     * 短链接不存在跳转页面
     * @return 不存在页面
     */
    @RequestMapping("/page/notfound")
    public String notfound(){
        return "notfound";
    }


}
