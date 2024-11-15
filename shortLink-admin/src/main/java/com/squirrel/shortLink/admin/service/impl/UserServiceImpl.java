package com.squirrel.shortLink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.shortLink.admin.common.convention.exception.ClientException;
import com.squirrel.shortLink.admin.dao.entity.UserDO;
import com.squirrel.shortLink.admin.dao.mapper.UserMapper;
import com.squirrel.shortLink.admin.dto.req.UserLoginReqDTO;
import com.squirrel.shortLink.admin.dto.req.UserRegisterReqDTO;
import com.squirrel.shortLink.admin.dto.req.UserUpdateReqDTO;
import com.squirrel.shortLink.admin.dto.resp.UserLoginRespDTO;
import com.squirrel.shortLink.admin.dto.resp.UserRespDTO;
import com.squirrel.shortLink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.squirrel.shortLink.admin.common.enums.UserErrorCodeEnum.*;
import static com.squirrel.shortLink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

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
        // 2.加分布式锁，向数据库中插入数据
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());
        try {
            if (lock.tryLock()) {
                int inserted = getBaseMapper().insert(BeanUtil.toBean(requestParam, UserDO.class));
                if (inserted < 1){
                    throw new ClientException(USER_SAVE_ERROR);
                }
                // 3.在布隆过滤器中保存新的用户名
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
                return;
            }
            throw new ClientException(USER_NAME_EXIST);
        }finally {
            lock.unlock();
        }
    }

    /**
     * 修改用户
     * @param requestParam 修改的用户信息
     */
    @Override
    public void update(UserUpdateReqDTO requestParam) {
        // TODO 验证当前用户名是否为登录用户
        // 1.修改数据库中数据
        getBaseMapper().update(
                BeanUtil.toBean(requestParam,UserDO.class),
                Wrappers.<UserDO>lambdaUpdate()
                .eq(UserDO::getUsername, requestParam.getUsername())
        );
    }

    /**
     * 用户登录
     * @param requestParam 用户登录数据
     * @return UserLoginRespDTO
     */
    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        // 1.构造查询条件
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.<UserDO>lambdaQuery()
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getPassword, requestParam.getPassword())
                .eq(UserDO::getDelFlag, 0);

        // 2.查询数据库
        UserDO userDO = getBaseMapper().selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(USER_NULL);
        }

        // 3.查询redis中是否存在key，验证是否登录
        Boolean hasLogin = stringRedisTemplate.hasKey("login_" + requestParam.getUsername());
        if (hasLogin != null && hasLogin) {
            throw new ClientException("用户已登录");
        }

        // 4.将用户信息存入redis
        /**
         * Hash
         * Key: login_用户名
         * Value:
         *  Key: token 标识
         *  Value: JSON 字符串（用户信息）
         */
        // uuid 作为 Token
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put("login_" + requestParam.getUsername(), uuid, JSON.toJSONString(userDO));
        stringRedisTemplate.expire("login_" + requestParam.getUsername(),30L, TimeUnit.DAYS);

        // 5.返回Token
        return new UserLoginRespDTO(uuid);
    }

    /**
     * 检查用户是否登录
     * @param username 用户名
     * @param token token
     * @return 是否登录
     */
    @Override
    public Boolean checkLogin(String username, String token) {
        return stringRedisTemplate.opsForHash().get("login_" + username, token) != null;
    }

    /**
     * 用户退出
     * @param username 用户名
     * @param token token
     */
    @Override
    public void logout(String username, String token) {
        if (checkLogin(username,token)) {
            stringRedisTemplate.delete("login_" + username);
            return;
        }
        throw new ClientException("用户Token不存在或用户未登录");
    }
}
