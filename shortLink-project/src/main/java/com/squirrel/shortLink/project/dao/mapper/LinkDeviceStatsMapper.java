package com.squirrel.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.shortLink.project.dao.entity.LinkDeviceStatsDO;
import com.squirrel.shortLink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 访问设备监控持久层
 */
public interface LinkDeviceStatsMapper extends BaseMapper<LinkDeviceStatsDO> {

    /**
     * 记录设备访问数据
     * @param linkDeviceStats 设备访问数据
     */
    void shortLinkDeviceState(@Param("linkDeviceStats") LinkDeviceStatsDO linkDeviceStats);

    /**
     * 根据短链接获取指定日期内访问设备监控数据
     * @param requestParam 查询参数
     * @return 监控数据
     */
    @Select("select tlds.device,sum(tlds.cnt) as count " +
            "from t_link tl " +
            "inner join t_link_device_stats tlds on tl.full_short_url = tlds.full_short_url " +
            "where tlds.full_short_url = #{param.fullShortUrl} " +
            "and tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = #{param.enableStatus} " +
            "and tlds.date between #{param.startDate} and #{param.endDate} " +
            "group by tlds.full_short_url,tl.gid,tlds.device")
    List<LinkDeviceStatsDO> listDeviceStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据分组获取指定日期内访问设备监控数据
     * @param requestParam 分组信息
     * @return 监控数据
     */
    @Select("select tlds.device,sum(tlds.cnt) as count " +
            "from t_link tl " +
            "inner join t_link_device_stats tlds on tl.full_short_url = tlds.full_short_url " +
            "where tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = '0' " +
            "and tlds.date between #{param.startDate} and #{param.endDate} " +
            "group by tl.gid,tlds.device")
    List<LinkDeviceStatsDO> listDeviceStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
}
