/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    @Select("select tlls.province,sum(tlls.cnt) as count " +
            "from t_link tl " +
            "inner join t_link_locale_stats tlls on tl.full_short_url = tlls.full_short_url " +
            "where tlls.full_short_url = #{param.fullShortUrl} " +
            "and tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = #{param.enableStatus} " +
            "and tlls.date between #{param.startDate} and #{param.endDate} " +
            "group by tlls.full_short_url,tl.gid,tlls.province")
    List<LinkLocaleStatsDO> listLocaleByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据分组获取指定日期地区内监控数据
     * @param requestParam 分组信息
     * @return 监控数据
     */
    @Select("select tlls.province,sum(tlls.cnt) as count " +
            "from t_link tl " +
            "inner join t_link_locale_stats tlls on tl.full_short_url = tlls.full_short_url " +
            "where tl.gid = #{param.gid} " +
            "and tl.del_flag = '0' " +
            "and tl.enable_status = '0' " +
            "and tlls.date between #{param.startDate} and #{param.endDate} " +
            "group by tl.gid,tlls.province")
    List<LinkLocaleStatsDO> listLocaleByGroup(@Param("param") ShortLinkGroupStatsReqDTO requestParam);
}
