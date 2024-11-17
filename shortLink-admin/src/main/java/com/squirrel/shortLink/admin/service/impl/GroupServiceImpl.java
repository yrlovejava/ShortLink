package com.squirrel.shortLink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.common.convention.result.Result;
import com.squirrel.shortLink.admin.common.biz.user.UserContext;
import com.squirrel.shortLink.admin.dao.entity.GroupDO;
import com.squirrel.shortLink.admin.dao.mapper.GroupMapper;
import com.squirrel.shortLink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.squirrel.shortLink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.squirrel.shortLink.admin.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.squirrel.shortLink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.squirrel.shortLink.admin.remote.ShortLinkRemoteService;
import com.squirrel.shortLink.admin.service.GroupService;
import com.squirrel.shortLink.admin.toolkit.RandomGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 短链接分组接口实现层
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    /**
     * 后续重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 新增分组
     * @param groupName 分组名
     */
    @Override
    public void saveGroup(String groupName) {
        saveGroup(UserContext.getUsername(), groupName);
    }

    /**
     * 新增短链接分组
     * @param username 用户名
     * @param groupName 短链接分组名
     */
    @Override
    public void saveGroup(String username, String groupName) {
        // 1.获取分组标识
        String gid;
        do {
            gid = RandomGenerator.generateRandom();
        }while (!hasGid(username,gid));

        // 2.插入数据库
        GroupDO groupDO = GroupDO.builder()
                .name(groupName)
                .gid(gid)
                .sortOrder(0)
                .username(username)
                .build();
        getBaseMapper().insert(groupDO);
    }

    /**
     * 查找是否存在分组ID
     * @param username 用户名
     * @param gid 分组ID
     * @return 是否存在
     */
    private boolean hasGid(String username,String gid) {
        return getBaseMapper().selectOne(Wrappers.<GroupDO>lambdaQuery()
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, Optional.ofNullable(username).orElse(UserContext.getUsername()))
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
                .select(GroupDO::getGid, GroupDO::getName,GroupDO::getSortOrder)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder)
                .orderByDesc(GroupDO::getUpdateTime);

        // 2.从数据库中查询数据
        List<GroupDO> groupDOList = baseMapper.selectList(queryWrapper);

        // 3.调用远程接口查询短链接分组数量
        Result<List<ShortLinkGroupCountQueryRespDTO>> listResult = shortLinkRemoteService.
                listGroupShortLinkCount(
                        groupDOList.stream().map(GroupDO::getGid).toList()
                );

        // 4.构造返回值
        // 4.1由数据库中的实体进行属性拷贝
        List<ShortLinkGroupRespDTO> shortLinkGroupRespDTOList = BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);
        // 4.2通过stream流设置短链接分组中的数量
        shortLinkGroupRespDTOList.forEach(s -> {
            Optional<ShortLinkGroupCountQueryRespDTO> first = listResult.getData().stream()
                    .filter(item -> Objects.equals(item.getGid(),s.getGid()))
                    .findFirst();
            first.ifPresent(item -> s.setShortLinkCount(first.get().getShortLinkCount()));
        });

        // 5.返回
        return shortLinkGroupRespDTOList;
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

    /**
     * 短链接分组排序
     * @param requestParam 短链接分组排序参数
     */
    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
        requestParam.forEach(elem -> {
            GroupDO groupDO = GroupDO.builder()
                    .sortOrder(elem.getSortOrder())
                    .build();
            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getUsername, UserContext.getUsername())
                    .eq(GroupDO::getGid, elem.getGid())
                    .eq(GroupDO::getDelFlag, 0);
            baseMapper.update(groupDO, updateWrapper);
        });
    }
}
