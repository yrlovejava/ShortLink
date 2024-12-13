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
 * 短链接实体
 */
@Data
@TableName("t_link")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkDO extends BaseDO {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 域名
     */
    @TableField("domain")
    private String domain;

    /**
     * 短链接
     */
    @TableField("short_uri")
    private String shortUri;

    /**
     * 完整短链接
     */
    @TableField("full_short_url")
    private String fullShortUrl;

    /**
     * 原始链接
     */
    @TableField("origin_url")
    private String originUrl;

    /**
     * 点击量
     */
    @TableField("click_num")
    private Integer clickNum;

    /**
     * 分组标识
     */
    @TableField("gid")
    private String gid;

    /**
     * 启用标识 0：启用 1：未启用
     */
    @TableField("enable_status")
    private Integer enableStatus;

    /**
     * 创建类型 0：接口创建 1：控制台创建
     */
    @TableField("created_type")
    private Integer createdType;

    /**
     * 有效期类型 0：永久有效 1：自定义
     */
    @TableField("valid_date_type")
    private Integer validDateType;

    /**
     * 有效期
     */
    @TableField("valid_date")
    private Date validDate;

    /**
     * 描述
     */
    @TableField("`describe`")
    private String describe;

    /**
     * 网站标识
     */
    @TableField("favicon")
    private String favicon;

    /**
     * 历史PV
     */
    @TableField("total_pv")
    private Integer totalPV;

    /**
     * 历史UV
     */
    @TableField("total_uv")
    private Integer totalUV;

    /**
     * 历史UIP
     */
    @TableField("total_uip")
    private Integer totalUip;

    /**
     * 删除时间
     */
    @TableField("del_time")
    private Long delTime;

    /**
     * 今日PV
     */
    @TableField(exist = false)
    private Integer todayPv;

    /**
     * 今日UV
     */
    @TableField(exist = false)
    private Integer todayUv;

    /**
     * 今日UIP
     */
    @TableField(exist = false)
    private Integer todayUip;
}
