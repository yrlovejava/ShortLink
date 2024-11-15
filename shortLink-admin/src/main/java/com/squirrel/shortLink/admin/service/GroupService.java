package com.squirrel.shortLink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.squirrel.shortLink.admin.dao.entity.GroupDO;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增分组
     * @param name 分组名
     */
    void saveGroup(String name);
}
