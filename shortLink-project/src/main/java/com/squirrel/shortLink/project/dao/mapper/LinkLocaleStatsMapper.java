package com.squirrel.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.shortLink.project.dao.entity.LinkLocaleStatsDO;
import com.squirrel.shortLink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 地区统计访问持久层
 */
public interface LinkLocaleStatsMapper extends BaseMapper<LinkLocaleStatsDO> {

    /**
     * 记录地区访问监控数据
     * @param linkLocaleStatsDO 监控数据
     */
    void shortLinkLocaleState(@Param("linkLocaleStats") LinkLocaleStatsDO linkLocaleStatsDO);

    /**
     * 根据短连接获取指定日期内地区监控数据
     * @param requestParam 查询参数
     * @return 监控数据
     */
    @Select("select province,sum(cnt) as count " +
            "from t_link_locale_stats " +
            "where full_short_url = #{param.fullShortUrl} " +
            "and date between #{param.startDate} and #{param.endDate} " +
            "group by full_short_url,province")
    List<LinkLocaleStatsDO> listLocaleByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据分组获取指定日期地区内监控数据
     * @param requestParam 分组信息
     * @return 监控数据
     */
    @Select("select province,sum(cnt) as count " +
            "from t_link_locale_stats " +
            "where date between #{param.startDate} and #{param.endDate} " +
            "group by province")
    List<LinkLocaleStatsDO> listLocaleByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
}