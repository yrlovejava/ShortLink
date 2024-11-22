package com.squirrel.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.project.dao.entity.LinkDeviceStatsDO;
import com.squirrel.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.squirrel.project.dto.req.ShortLinkStatsReqDTO;
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
    void shortLinkDeviceState(@Param("linkDeviceStats")LinkDeviceStatsDO linkDeviceStats);

    /**
     * 根据短链接获取指定日期内访问设备监控数据
     * @param requestParam 查询参数
     * @return 监控数据
     */
    @Select("select device,sum(cnt) as count " +
            "from t_link_device_stats " +
            "where full_short_url = #{param.fullShortUrl} " +
            "and date between #{param.startDate} and #{param.endDate} " +
            "group by full_short_url,device")
    List<LinkDeviceStatsDO> listDeviceStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据分组获取指定日期内访问设备监控数据
     * @param requestParam 分组信息
     * @return 监控数据
     */
    @Select("select device,sum(cnt) as count " +
            "from t_link_device_stats " +
            "where date between #{param.startDate} and #{param.endDate} " +
            "group by device")
    List<LinkDeviceStatsDO> listDeviceStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
}
