package com.squirrel.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.squirrel.shortLink.project.dao.entity.LinkAccessLogsDO;
import com.squirrel.shortLink.project.dao.entity.LinkAccessStatsDO;
import com.squirrel.shortLink.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkStatsReqDTO;
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
    @Select("select ip,count(tlal.ip) as count " +
            "from t_link tl " +
            "inner join t_link_access_logs tlal on tl.full_short_url = tlal.full_short_url " +
            "where tlal.full_short_url = #{param.fullShortUrl} " +
            "and tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = #{param.enableStatus} " +
            "and tlal.create_time between #{param.startDate} and #{param.endDate} " +
            "group by tlal.full_short_url,tl.gid,tlal.ip " +
            "order by count desc " +
            "limit 5;")
    List<HashMap<String,Object>> listTopIpByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

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
            @Param("gid") String gid,
            @Param("fullShortUrl") String fullShortUrl,
            @Param("enableStatus") Integer enableStatus,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("userAccessLogsList") List<String> userAccessLogsList
    );

    /**
     * 根据短链接获取指定日期内PV，UV，UIP数据
     * @param requestParam 短链接
     * @return pv，uv，uip
     */
    @Select("select " +
            "count(tlal.user) as pv," +
            "count(distinct tlal.user) as uv," +
            "count(distinct tlal.ip) as uip " +
            "from t_link tl " +
            "inner join t_link_access_logs tlal on tl.full_short_url = tlal.full_short_url " +
            "where tlal.full_short_url = #{param.fullShortUrl} " +
            "and tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = #{param.enableStatus} " +
            "and tlal.create_time between #{param.startDate} and #{param.endDate} " +
            "group by tlal.full_short_url, tl.gid;")
    LinkAccessStatsDO findPvUvUipStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据分组获取指定日期内高频访问IP数据
     * @param requestParam 分组短链接信息
     * @return 高频IP数据
     */
    @Select("select ip,count(tlal.ip) as count " +
            "from t_link tl " +
            "inner join t_link_access_logs tlal on tl.full_short_url = tlal.full_short_url " +
            "where tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = '0' " +
            "and tlal.create_time between #{param.startDate} and #{param.endDate} " +
            "group by tl.gid,tlal.ip " +
            "order by count desc " +
            "limit 5;")
    List<HashMap<String,Object>> listTopIpByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);

    /**
     * 根据分组获取指定日期内PV,UV,UIP数据
     * @param requestParam 分组短链接信息
     * @return PV,UV,UIP
     */
    @Select("select " +
            "count(tlal.user) as pv," +
            "count(distinct tlal.user) as uv," +
            "count(distinct tlal.ip) as uip " +
            "from t_link tl " +
            "inner join t_link_access_logs tlal on tl.full_short_url = tlal.full_short_url " +
            "where tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = '0' " +
            "and tlal.create_time between #{param.startDate} and #{param.endDate} " +
            "group by tl.gid;")
    LinkAccessStatsDO findPvUvUipStatsByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);

    /**
     * 获取分组用户信息和是否为新老访客
     * @param gid 分组id
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param userAccessLogsList 用户名集合
     * @return 分组用户信息和是否为新老访客
     */
    @MapKey("user")
    List<Map<String, Object>> selectGroupUvTypeByUsers(
            @Param("gid") String gid,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("userAccessLogsList") List<String> userAccessLogsList
    );

    /**
     * 分页查询指定分组的访问日志
     * @param requestParam 分页查询参数
     * @return 访问日志
     */
    @Select("select tlal.* " +
            "from t_link tl " +
            "inner join t_link_access_logs tlal on tl.full_short_url = tlal.full_short_url " +
            "where tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = '0' " +
            "and tlal.create_time between #{param.startDate} and #{param.endDate} " +
            "order by tlal.create_time desc")
    IPage<LinkAccessLogsDO> selectGroupPage(@Param("param")ShortLinkGroupStatsAccessRecordReqDTO requestParam);
}
