package com.squirrel.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.shortLink.project.dao.entity.LinkBrowserStatsDO;
import com.squirrel.shortLink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

/**
 * 浏览器统计访问持久层
 */
public interface LinkBrowserStatsMapper extends BaseMapper<LinkBrowserStatsDO> {

    /**
     * 记录浏览器访问数据
     * @param linkBrowserStatsDO 浏览器访问数据
     */
    void shortLinkBrowserStats(@Param("linkBrowserStats") LinkBrowserStatsDO linkBrowserStatsDO);

    /**
     * 根据短链接获取指定日期内浏览器监控数据
     * @param requestParam 查询参数
     * @return 监控数据
     */
    @Select("select browser,sum(cnt) as count " +
            "from t_link_browser_stats " +
            "where full_short_url = #{param.fullShortUrl} " +
            "and date between #{param.startDate} and #{param.endDate} " +
            "group by full_short_url,browser")
    List<HashMap<String, Object>> listBrowserStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据分组获取指定日期内浏览器监控数据
     * @param requestParam 分组短链接
     * @return List<HashMap<String,Object>>
     */
    @Select("select browser,sum(cnt) as count " +
            "from t_link_browser_stats " +
            "where date between #{param.startDate} and #{param.endDate} " +
            "group by browser")
    List<HashMap<String,Object>> listBrowserStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
}
