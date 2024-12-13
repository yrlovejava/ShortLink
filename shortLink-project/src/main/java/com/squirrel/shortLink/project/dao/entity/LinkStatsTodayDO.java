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
package com.squirrel.shortLink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.squirrel.shortLink.project.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 短链接今日统计实体
 */
@Data
@TableName("t_link_stats_today")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkStatsTodayDO extends BaseDO {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 短链接
     */
    @TableField("fullShortUrl")
    private String fullShortUrl;

    /**
     * 日期
     */
    @TableField("date")
    private Date date;

    /**
     * 今日pv
     */
    @TableField("today_pv")
    private Integer todayPv;

    /**
     * 今日uv
     */
    @TableField("today_uv")
    private Integer todayUv;

    /**
     * 今日ip数
     */
    @TableField("today_uip")
    private Integer todayUip;
}
