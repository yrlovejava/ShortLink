package com.squirrel.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.shortLink.project.dao.entity.LinkAccessStatsDO;
import com.squirrel.shortLink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkStatsReqDTO;
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
    @Select("select tlas.date,sum(tlas.pv) as pv,sum(tlas.uv) as uv,sum(tlas.uip) as uip " +
            "from t_link tl " +
            "inner join t_link_access_stats tlas on tl.full_short_url = tlas.full_short_url " +
            "where tlas.full_short_url = #{param.fullShortUrl} " +
            "and tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = #{param.enableStatus} " +
            "and tlas.date between #{param.startDate} and #{param.endDate} " +
            "group by tlas.full_short_url,tl.gid,tlas.date;")
    List<LinkAccessStatsDO> listStatsByShortLink(@Param("param") ShortLinkStatsReqDTO reqDTO);

    /**
     * 根据分组获取指定日期内基础监控数据
     * @param requestParam 分组短链接信息
     * @return List<LinkAccessStatsDO>
     */
    @Select("select tlas.date,sum(tlas.pv) as pv,sum(tlas.uv) as uv,sum(tlas.uip) as uip " +
            "from t_link tl " +
            "inner join t_link_access_stats tlas on tl.full_short_url = tlas.full_short_url " +
            "where tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = '0' " +
            "and tlas.date between #{param.startDate} and #{param.endDate} " +
            "group by tl.gid,tlas.date;")
    List<LinkAccessStatsDO> listStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内小时基础监控数据
     * @param requestParam 查询参数
     * @return 监控数据
     */
    @Select("select tlas.hour,sum(tlas.pv) as pv " +
            "from t_link tl " +
            "inner join t_link_access_stats tlas on tl.full_short_url = tlas.full_short_url " +
            "where tlas.full_short_url = #{param.fullShortUrl} " +
            "and tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = #{param.enableStatus} " +
            "and tlas.date between #{param.startDate} and #{param.endDate} " +
            "group by tlas.full_short_url,tl.gid,tlas.hour;")
    List<LinkAccessStatsDO> listHourStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据分组获取指定日期小时基础监控数据
     * @param requestParam 分组短链接信息
     * @return List<LinkAccessStatsDO>
     */
    @Select("select tlas.hour,sum(tlas.pv) as pv " +
            "from t_link tl " +
            "inner join t_link_access_stats tlas on tl.full_short_url = tlas.full_short_url " +
            "where tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = '0' " +
            "and tlas.date between #{param.startDate} and #{param.endDate} " +
            "group by tl.gid,tlas.hour;")
    List<LinkAccessStatsDO> listHourStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内星期基础监控数据
     * @param requestParam 查询参数
     * @return 监控数据
     */
    @Select("select tlas.weekday,sum(tlas.pv) as pv " +
            "from t_link tl " +
            "inner join t_link_access_stats tlas on tl.full_short_url = tlas.full_short_url " +
            "where tlas.full_short_url = #{param.fullShortUrl} " +
            "and tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = #{param.enableStatus} " +
            "and tlas.date between #{param.startDate} and #{param.endDate} " +
            "group by tlas.full_short_url,tl.gid,tlas.weekday;")
    List<LinkAccessStatsDO> listWeekdayStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据分组获取指定日期小时基础监控数据
     * @param requestParam 分组短链接信息
     * @return List<LinkAccessStatsDO>
     */
    @Select("select tlas.weekday,sum(tlas.pv) as pv " +
            "from t_link tl " +
            "inner join t_link_access_stats tlas on tl.full_short_url = tlas.full_short_url " +
            "where tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = '0' " +
            "and tlas.date between #{param.startDate} and #{param.endDate} " +
            "group by tl.gid,tlas.weekday;")
    List<LinkAccessStatsDO> listWeekdayStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
}
