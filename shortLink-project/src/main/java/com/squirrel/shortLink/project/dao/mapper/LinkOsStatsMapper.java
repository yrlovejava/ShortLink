package com.squirrel.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.shortLink.project.dao.entity.LinkOsStatsDO;
import com.squirrel.shortLink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

/**
 * 操作系统统计访问持久层
 */
public interface LinkOsStatsMapper extends BaseMapper<LinkOsStatsDO> {

    /**
     * 记录操作系统监控数据
     * @param linkOsStatsDO 操作系统监控数据
     */
    void shortLinkOsState(@Param("linkOsStats") LinkOsStatsDO linkOsStatsDO);

    /**
     * 根据短链接获取指定日期内操作系统监控数据
     * @param requestParam 查询参数
     * @return 监控数据
     */
    @Select("select os,sum(cnt) as count " +
            "from t_link_os_stats " +
            "where full_short_url = #{param.fullShortUrl} " +
            "and date between #{param.startDate} and #{param.endDate} " +
            "group by full_short_url,os")
    List<HashMap<String,Object>> listOsStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据分组获取指定日期内操作系统监控数据
     * @param requestParam 分组信息
     * @return 操作系统监控数据
     */
    @Select("select os,sum(cnt) as count " +
            "from t_link_os_stats " +
            "where date between #{param.startDate} and #{param.endDate} " +
            "group by os")
    List<HashMap<String, Object>> listOsStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
}
