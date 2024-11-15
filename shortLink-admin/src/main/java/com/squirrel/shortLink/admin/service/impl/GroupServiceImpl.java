package com.squirrel.shortLink.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.shortLink.admin.dao.entity.GroupDO;
import com.squirrel.shortLink.admin.dao.mapper.GroupMapper;
import com.squirrel.shortLink.admin.service.GroupService;
import com.squirrel.shortLink.admin.toolkit.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短链接分组接口实现层
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    /**
     * 新增分组
     * @param name 分组名
     */
    @Override
    public void saveGroup(String name) {
        // 1.获取分组标识
        String gid;
        do {
            gid = RandomGenerator.generateRandom();
        }while (!hasGid(gid));
        GroupDO groupDO = GroupDO.builder()
                .name(name)
                .gid(gid)
                .build();
        getBaseMapper().insert(groupDO);
    }

    /**
     * 查找是否存在分组ID
     * @param gid 分组ID
     * @return 是否存在
     */
    private boolean hasGid(String gid) {
        return getBaseMapper().selectOne(Wrappers.<GroupDO>lambdaQuery()
                .eq(GroupDO::getGid, gid)
                // TODO 设置用户名
                .eq(GroupDO::getUsername, null)
        ) != null;
    }
}
