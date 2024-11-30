package com.squirrel.shortLink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.squirrel.shortLink.common.convention.result.Result;
import com.squirrel.shortLink.common.convention.result.Results;
import com.squirrel.shortLink.admin.dto.req.UserLoginReqDTO;
import com.squirrel.shortLink.admin.dto.req.UserRegisterReqDTO;
import com.squirrel.shortLink.admin.dto.req.UserUpdateReqDTO;
import com.squirrel.shortLink.admin.dto.resp.UserActualRespDTO;
import com.squirrel.shortLink.admin.dto.resp.UserLoginRespDTO;
import com.squirrel.shortLink.admin.dto.resp.UserRespDTO;
import com.squirrel.shortLink.admin.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin/v1")
@Tag(name = "用户管理控制层")
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return Result<UserRespDTO> 脱敏用户信息
     */
    @Operation(summary = "根据用户名查询用户信息")
    @GetMapping("/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {
        return Results.success(userService.getUserByUsername(username));
    }

    /**
     * 根据用户名查询无脱敏用户信息
     * @param username 用户名
     * @return Result<UserActualRespDTO> 无脱敏用户信息
     */
    @Operation(summary = "根据用户名查询无脱敏用户信息")
    @GetMapping("/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable("username") String username) {
        return Results.success(BeanUtil.toBean(
            userService.getUserByUsername(username), UserActualRespDTO.class
        ));
    }

    /**
     * 查询用户名是否存在
     * @param username 用户名
     * @return Result<Boolean> 是否存在
     */
    @Operation(summary = "查询用户名是否存在")
    @GetMapping("/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username) {
        return Results.success(userService.hasUsername(username));
    }

    /**
     * 注册用户
     * @param requestParam 注册信息
     */
    @Operation(summary = "注册用户")
    @PostMapping("/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParam) {
        userService.register(requestParam);
        return Results.success();
    }

    /**
     * 修改用户
     * @param requestParam 修改的用户信息
     * @return Result<Void>
     */
    @Operation(summary = "修改用户")
    @PutMapping("/user")
    public Result<Void> update(@RequestBody UserUpdateReqDTO requestParam) {
        userService.update(requestParam);
        return Results.success();
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam) {
        return Results.success(userService.login(requestParam));
    }

    /**
     * 检查用户是否登录
     */
    @Operation(summary = "检查用户是否登录")
    @GetMapping("/user/check-login")
    public Result<Boolean> checkLogin(@RequestParam("username") String username, @RequestParam("token") String token) {
        return Results.success(userService.checkLogin(username, token));
    }

    /**
     * 用户退出登录
     */
    @Operation(summary = "用户退出登录")
    @DeleteMapping("/user/logout")
    public Result<Void> logout(@RequestParam("username") String username, @RequestParam("token") String token) {
        userService.logout(username, token);
        return Results.success();
    }
}
