package com.squirrel.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.shortLink.project.dao.entity.LinkNetworkStatsDO;
import com.squirrel.shortLink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 访问网络监控持久层
 */
public interface LinkNetworkStatsMapper extends BaseMapper<LinkNetworkStatsDO> {

    /**
     * 记录网络监控数据
     * @param linkNetworkStatsDO 网络监控数据
     */
    void shortLinkNetworkState(@Param("linkNetworkStats") LinkNetworkStatsDO linkNetworkStatsDO);

    /**
     * 根据短链接获取指定日期内访问网络监控数据
     * @param requestParam 查询参数
     * @return 监控数据
     */
    @Select("select tlns.network,sum(tlns.cnt) as count " +
            "from t_link tl " +
            "inner join t_link_network_stats tlns on tl.full_short_url = tlns.full_short_url " +
            "where tlns.full_short_url = #{param.fullShortUrl} " +
            "and tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = #{param.enableStatus} " +
            "and tlns.date between #{param.startDate} and #{param.endDate} " +
            "group by tlns.full_short_url,tl.gid,tlns.network")
    List<LinkNetworkStatsDO> listNetworkStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据分组获取指定日期内访问网络监控数据
     * @param requestParam 分组信息
     * @return 网络监控数据
     */
    @Select("select tlns.network,sum(tlns.cnt) as count " +
            "from t_link tl " +
            "inner join t_link_network_stats tlns on tl.full_short_url = tlns.full_short_url " +
            "where tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = '0' " +
            "and tlns.date between #{param.startDate} and #{param.endDate} " +
            "group by tl.gid,tlns.network")
    List<LinkNetworkStatsDO> listNetworkStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
}
