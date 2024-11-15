package com.squirrel.shortLink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.squirrel.shortLink.admin.common.convention.result.Result;
import com.squirrel.shortLink.admin.common.convention.result.Results;
import com.squirrel.shortLink.admin.dto.resp.UserActualRespDTO;
import com.squirrel.shortLink.admin.dto.resp.UserRespDTO;
import com.squirrel.shortLink.admin.service.UserService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return Result
     */
    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {
        return Results.success(userService.getUserByUsername(username));
    }

    /**
     * 根据用户名查询无脱敏用户信息
     * @param username 用户名
     * @return
     */
    @GetMapping("/api/shortlink/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable("username") String username) {
        return Results.success(BeanUtil.toBean(
            userService.getUserByUsername(username), UserActualRespDTO.class
        ));
    }
}
