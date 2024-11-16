package com.squirrel.shortLink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.shortLink.admin.common.biz.user.UserContext;
import com.squirrel.shortLink.admin.dao.entity.GroupDO;
import com.squirrel.shortLink.admin.dao.mapper.GroupMapper;
import com.squirrel.shortLink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.squirrel.shortLink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.squirrel.shortLink.admin.service.GroupService;
import com.squirrel.shortLink.admin.toolkit.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.List;

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
                .sortOrder(0)
                .username(UserContext.getUsername())
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
                .eq(GroupDO::getUsername, UserContext.getUsername())
        ) != null;
    }

    /**
     * 查询短链接分组信息
     * @return 分组信息
     */
    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        // 1.构造查询条件
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.<GroupDO>lambdaQuery()
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder)
                .orderByDesc(GroupDO::getUpdateTime);

        // 2.从数据库中查询数据
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);

        // 3.返回结果
        return BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);
    }

    /**
     * 修改短链接分组信息
     * @param requestParam 修改分组信息
     */
    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
        // 1.构造查询方法
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.<GroupDO>lambdaUpdate()
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);

        // 2.修改数据库
        GroupDO groupDO = new GroupDO();
        groupDO.setName(requestParam.getName());
        getBaseMapper().update(groupDO, updateWrapper);
    }

    /**
     * 删除分组id
     * @param gid 分组id
     */
    @Override
    public void deleteGroup(String gid) {
        // 1.构造删除条件
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.<GroupDO>lambdaUpdate()
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getDelFlag, 0);

        // 2.删除数据(实际上是修改 delFlag)
        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);
        getBaseMapper().update(groupDO,updateWrapper);
    }
}
