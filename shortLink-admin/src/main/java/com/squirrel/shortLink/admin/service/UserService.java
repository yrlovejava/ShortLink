/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
