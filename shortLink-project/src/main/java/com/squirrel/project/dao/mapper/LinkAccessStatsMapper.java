package com.squirrel.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.project.dao.entity.LinkAccessStatsDO;
import com.squirrel.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 短链接基础访问监控持久层
 */
public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStatsDO> {

    /**
     * 记录访问数据
     * @param linkAccessStatsDO 访问数据
     */
    void shortLinkStats(@Param("linkAccessStats") LinkAccessStatsDO linkAccessStatsDO);

    /**
     * 根据短链接获取指定日期内基础监控数据
     * @param reqDTO 查询参数
     * @return 监控数据
     */
    @Select("select date,sum(pv) as pv,sum(uv) as uv,sum(uip) as uip " +
            "from t_link_access_stats " +
            "where full_short_url = #{param.fullShortUrl} " +
            "and date between #{param.startDate} and #{param.endDate} " +
            "group by full_short_url,date")
    List<LinkAccessStatsDO> listStatsByShortLink(@Param("param")ShortLinkStatsReqDTO reqDTO);

    /**
     * 根据短链接获取指定日期内小时基础监控数据
     * @param reqDTO 查询参数
     * @return 监控数据
     */
    @Select("select hour,sum(pv) as pv " +
            "from t_link_access_stats " +
            "where full_short_url = #{param.fullShortUrl} " +
            "and date between #{param.startDate} and #{param.endDate} " +
            "group by full_short_url,hour")
    List<LinkAccessStatsDO> listHourStatsByShortLink(@Param("param") ShortLinkStatsReqDTO reqDTO);

    /**
     * 根据短链接获取指定日期内星期基础监控数据
     * @param requestParam 查询参数
     * @return 监控数据
     */
    @Select("select weekday,sum(pv) as pv " +
            "from t_link_access_stats " +
            "where full_short_url = #{param.fullShortUrl} " +
            "and date between #{param.startDate} and #{param.endDate} " +
            "group by full_short_url,weekday")
    List<LinkAccessStatsDO> listWeekdayStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);
}
