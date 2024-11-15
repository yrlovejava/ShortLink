package com.squirrel.shortLink.admin.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.shortLink.admin.dao.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户持久层
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
}
