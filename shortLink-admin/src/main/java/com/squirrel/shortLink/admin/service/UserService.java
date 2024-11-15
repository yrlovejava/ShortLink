package com.squirrel.shortLink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.squirrel.shortLink.admin.dao.entity.UserDO;
import com.squirrel.shortLink.admin.dto.req.UserLoginReqDTO;
import com.squirrel.shortLink.admin.dto.req.UserRegisterReqDTO;
import com.squirrel.shortLink.admin.dto.req.UserUpdateReqDTO;
import com.squirrel.shortLink.admin.dto.resp.UserLoginRespDTO;
import com.squirrel.shortLink.admin.dto.resp.UserRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户返回实体
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
     Boolean hasUsername(String username);

    /**
     * 用户注册
     * @param requestParam 注册信息
     */
    void register(UserRegisterReqDTO requestParam);

    /**
     * 修改用户
     * @param requestParam 修改的用户信息
     */
    void update(UserUpdateReqDTO requestParam);

    /**
     * 用户登录
     * @param requestParam 用户登录数据
     * @return UserLoginRespDTO
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 检查用户是否登录
     * @param username 用户名
     * @param token token
     * @return 是否登录
     */
    Boolean checkLogin(String username, String token);

    /**
     * 用户退出
     * @param username 用户名
     * @param token token
     */
    void logout(String username, String token);
}
