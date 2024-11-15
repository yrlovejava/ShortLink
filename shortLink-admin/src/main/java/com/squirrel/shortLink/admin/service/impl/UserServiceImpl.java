package com.squirrel.shortLink.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.shortLink.admin.common.enums.UserErrorCodeEnum;
import com.squirrel.shortLink.admin.dao.entity.UserDO;
import com.squirrel.shortLink.admin.dao.mapper.UserMapper;
import com.squirrel.shortLink.admin.dto.resp.UserRespDTO;
import com.squirrel.shortLink.admin.service.UserService;
import jodd.cli.CliException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户接口实现层
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户返回实体
     */
    @Override
    public UserRespDTO getUserByUsername(String username) {
        UserDO userDO = getBaseMapper().selectOne(Wrappers.<UserDO>lambdaQuery()
                .eq(UserDO::getUsername, username));
        if (userDO == null) {
            throw new CliException(UserErrorCodeEnum.USER_NULL.message());
        }
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO, result);
        return result;
    }
}
