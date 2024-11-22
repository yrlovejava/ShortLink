package com.squirrel.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.project.dao.entity.LinkAccessLogsDO;
import com.squirrel.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 访问日志监控持久层
 */
public interface LinkAccessLogsMapper extends BaseMapper<LinkAccessLogsDO> {

    /**
     * 根据短链接获取指定日期内高频访问IP数据
     * @param requestParam 查询参数
     * @return ip数据
     */
    @Select("select ip,count(ip) as count " +
            "from t_link_access_logs " +
            "where full_short_url = #{param.fullShortUrl} " +
            "and create_time between #{param.startDate} and #{param.endDate} " +
            "group by full_short_url,ip " +
            "order by count desc " +
            "limit 5")
    List<HashMap<String,Object>> listTopIpByShortLink(@Param("param")ShortLinkStatsReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内新旧访客数据
     * @param requestParam 查询参数
     * @return 访客数据
     */
    HashMap<String,Object> findUvTypeCntByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 获取用户信息 和 是否新老访客
     * @param fullShortUrl 完整的短连接
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param userAccessLogsList 用户信息
     * @return 访客信息和类型
     */
    @MapKey("user")
    List<Map<String,Object>> selectUvTypeByUsers(
            @Param("fullShortUrl") String fullShortUrl,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("userAccessLogsList") List<String> userAccessLogsList
    );
}