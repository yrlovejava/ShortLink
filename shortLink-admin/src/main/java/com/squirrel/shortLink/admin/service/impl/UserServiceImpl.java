package com.squirrel.shortLink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.shortLink.admin.common.convention.exception.ClientException;
import com.squirrel.shortLink.admin.dao.entity.UserDO;
import com.squirrel.shortLink.admin.dao.mapper.UserMapper;
import com.squirrel.shortLink.admin.dto.req.UserRegisterReqDTO;
import com.squirrel.shortLink.admin.dto.resp.UserRespDTO;
import com.squirrel.shortLink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import static com.squirrel.shortLink.admin.common.enums.UserErrorCodeEnum.*;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户返回实体
     */
    @Override
    public UserRespDTO getUserByUsername(String username) {
        // 1.查询数据库
        UserDO userDO = getBaseMapper().selectOne(Wrappers.<UserDO>lambdaQuery()
                .eq(UserDO::getUsername, username));
        if (userDO == null) {
            throw new ClientException(USER_NULL);
        }
        // 2.返回信息
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO, result);
        return result;
    }

    /**
     * 查询用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    @Override
    public Boolean hasUsername(String username) {
        return !userRegisterCachePenetrationBloomFilter.contains(username);
    }

    /**
     * 用户注册
     * @param requestParam 注册信息
     */
    @Override
    public void register(UserRegisterReqDTO requestParam) {
        // 1.查询用户名是否存在
        if (!hasUsername(requestParam.getUsername())){
            throw new ClientException(USER_NAME_EXIST);
        }
        // 2.向数据库中插入数据
        int inserted = getBaseMapper().insert(BeanUtil.toBean(requestParam, UserDO.class));
        if (inserted < 1){
            throw new ClientException(USER_SAVE_ERROR);
        }
        // 3.在布隆过滤器中添加新用户名信息
        userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
    }
}
