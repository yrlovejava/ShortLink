package com.squirrel.shortLink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.shortLink.admin.dao.entity.GroupUniqueDO;
import com.squirrel.shortLink.admin.dao.mapper.GroupUniqueMapper;
import com.squirrel.shortLink.common.convention.exception.ClientException;
import com.squirrel.shortLink.common.convention.exception.ServiceException;
import com.squirrel.shortLink.common.convention.result.Result;
import com.squirrel.shortLink.admin.common.biz.user.UserContext;
import com.squirrel.shortLink.admin.dao.entity.GroupDO;
import com.squirrel.shortLink.admin.dao.mapper.GroupMapper;
import com.squirrel.shortLink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.squirrel.shortLink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.squirrel.shortLink.admin.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.squirrel.shortLink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.squirrel.shortLink.admin.remote.ShortLinkActualRemoteService;
import com.squirrel.shortLink.admin.service.GroupService;
import com.squirrel.shortLink.admin.toolkit.RandomGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.sql.rowset.serial.SerialException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.squirrel.shortLink.admin.common.constant.RedisCacheConstant.LOCK_GROUP_CREATE_KEY;

/**
 * 短链接分组接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    private final RedissonClient redissonClient;

    @Value("${short-link.group.max-num}")
    private Integer groupMaxNum;

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;
    private final RBloomFilter<String> gidRegisterCachePenetrationBloomFilter;
    private final GroupUniqueMapper groupUniqueMapper;

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
        // 1.加锁
        RLock lock = redissonClient.getLock(String.format(LOCK_GROUP_CREATE_KEY, username));
        lock.lock();
        try {
            // 2.查询数据库中分组数量
            List<GroupDO> groupDOList = baseMapper.selectList(Wrappers.<GroupDO>lambdaQuery()
                    .eq(GroupDO::getUsername, username)
                    .eq(GroupDO::getDelFlag, 0));
            // 如果数量超过，就报错
            if(CollUtil.isNotEmpty(groupDOList) && groupDOList.size() >= groupMaxNum) {
                throw new ClientException(String.format("已超出最大分组数: %d",groupMaxNum));
            }

            // 3.获取分组标识
            int retryCount = 0;
            int maxRetries = 10;
            String gid = null;
            while (retryCount < maxRetries) {
                gid = saveGroupUniqueReturnGid();
                // 如果分组标识不为空，也就是生成成功
                if (StrUtil.isNotEmpty(gid)) {
                    // 4.插入数据库
                    GroupDO groupDO = GroupDO.builder()
                            .name(groupName)
                            .gid(gid)
                            .sortOrder(0)
                            .username(username)
                            .build();
                    getBaseMapper().insert(groupDO);
                    // 在布隆过滤器添加
                    gidRegisterCachePenetrationBloomFilter.add(gid);
                    break;
                }
                retryCount++;
            }
            if (StrUtil.isEmpty(gid)) {
                throw new ServiceException("生成分组标识频繁");
            }
        }finally {
            lock.unlock();
        }
    }

    /**
     * 生成唯一的分组标识并保存在数据库中
     * @return 分组标识
     */
    private String saveGroupUniqueReturnGid() {
        String gid = RandomGenerator.generateRandom();
        // 先查询布隆过滤器，如果没有就生成,如果有就返回null
        if (gidRegisterCachePenetrationBloomFilter.contains(gid)){
            return null;
        }
        GroupUniqueDO groupUniqueDO = GroupUniqueDO.builder()
                .gid(gid)
                .build();
        try {
            // 就算布隆过滤器有误判，但是数据库的唯一索引会导致插入失败，如果插入失败返回null即可
            groupUniqueMapper.insert(groupUniqueDO);
        } catch (DuplicateKeyException e) {
            return null;
        }
        return gid;
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
        Result<List<ShortLinkGroupCountQueryRespDTO>> listResult = shortLinkActualRemoteService.
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
