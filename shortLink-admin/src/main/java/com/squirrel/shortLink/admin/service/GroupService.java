package com.squirrel.shortLink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.squirrel.shortLink.admin.dao.entity.GroupDO;
import com.squirrel.shortLink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.squirrel.shortLink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.squirrel.shortLink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增分组
     * @param groupName 分组名
     */
    void saveGroup(String groupName);

    /**
     * 新增短链接分组
     * @param username 用户名
     * @param groupName 短链接分组名
     */
    void saveGroup(String username,String groupName);

    /**
     * 查询短链接分组信息
     * @return 分组信息
     */
    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 修改短链接分组信息
     * @param requestParam 修改分组信息
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO requestParam);

    /**
     * 删除分组信息
     * @param gid 分组id
     */
    void deleteGroup(String gid);

    /**
     * 短链接分组排序
     * @param requestParam 短链接分组排序参数
     */
    void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam);
}
