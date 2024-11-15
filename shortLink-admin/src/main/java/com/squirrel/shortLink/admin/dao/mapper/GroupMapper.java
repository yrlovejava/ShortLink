package com.squirrel.shortLink.admin.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.shortLink.admin.dao.entity.GroupDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短链接分组持久层
 */
@Mapper
public interface GroupMapper extends BaseMapper<GroupDO> {

}
