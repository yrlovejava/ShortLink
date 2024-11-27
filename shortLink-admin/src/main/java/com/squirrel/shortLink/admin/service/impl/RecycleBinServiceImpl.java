package com.squirrel.shortLink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.squirrel.common.convention.exception.ServiceException;
import com.squirrel.common.convention.result.Result;
import com.squirrel.shortLink.admin.common.biz.user.UserContext;
import com.squirrel.shortLink.admin.dao.entity.GroupDO;
import com.squirrel.shortLink.admin.dao.mapper.GroupMapper;
import com.squirrel.shortLink.admin.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.squirrel.shortLink.admin.remote.ShortLinkActualRemoteService;
import com.squirrel.shortLink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.squirrel.shortLink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * URL 回收站接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final GroupMapper groupMapper;

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 分页查询回收站短链接
     * @param requestParam 请求参数
     * @return 返回参数包装
     */
    @Override
    public Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        // 1.构建查询当前用户的所有短链接
        List<GroupDO> groupDOList = groupMapper.selectList(Wrappers.<GroupDO>lambdaQuery()
                .select(GroupDO::getGid)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0));
        if (CollUtil.isEmpty(groupDOList)) {
            throw new ServiceException("用户无分组信息");
        }
        requestParam.setGidList(groupDOList.stream().map(GroupDO::getGid).toList());
        return shortLinkActualRemoteService.pageRecycleBinShortLink(requestParam.getGidList(),requestParam.getCurrent(),requestParam.getSize());
    }
}
