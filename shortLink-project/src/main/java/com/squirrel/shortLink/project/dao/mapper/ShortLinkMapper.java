package com.squirrel.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.squirrel.shortLink.project.dao.entity.ShortLinkDO;
import com.squirrel.shortLink.project.dto.req.ShortLinkPageReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 短链接持久层
 */
public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {

    /**
     * 短链接访问统计自增
     * @param gid 分组id
     * @param fullShortUrl 完整的短链接
     * @param totalPv 这里是增加的pv
     * @param totalUv 这里是增加的uv
     * @param totalUip 这里是增加的uip
     */
    @Update("update t_link " +
            "set total_pv = total_pv + #{totalPv}, " +
            "total_uv = total_uv + #{totalUv}, " +
            "total_uip = total_uip + #{totalUip} " +
            "where gid = #{gid} and full_short_url = #{fullShortUrl}")
    void incrementStats(
            @Param("gid") String gid,
            @Param("fullShortUrl") String fullShortUrl,
            @Param("totalPv") Integer totalPv,
            @Param("totalUv") Integer totalUv,
            @Param("totalUip") Integer totalUip
    );

    /**
     * 分页统计短链接
     * @param requestParam 分页参数
     * @return 短链接
     */
    IPage<ShortLinkDO> pageLink(ShortLinkPageReqDTO requestParam);
}
