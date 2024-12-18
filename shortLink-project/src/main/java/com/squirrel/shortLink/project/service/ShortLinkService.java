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
package com.squirrel.shortLink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.squirrel.shortLink.project.dao.entity.ShortLinkDO;
import com.squirrel.shortLink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkPageReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkUpdateReqDTO;
import com.squirrel.shortLink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.squirrel.shortLink.project.dto.resp.ShortLinkCreateRespDTO;
import com.squirrel.shortLink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.squirrel.shortLink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.util.List;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDO> {

    /**
     * 创建短链接
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建信息
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);

    /**
     * 分页查询短链接
     * @param requestParam 短链接查询参数
     * @return 短链接分页返回结果
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    /**
     * 查询短链接分组内数量
     * @param requestParam 查询参数(分组id的集合)
     * @return Result<List<ShortLinkGroupCountQueryRespDTO>>
     */
    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);

    /**
     * 修改短链接
     * @param requestParam 修改短链接信息
     */
    void updateShortLink(ShortLinkUpdateReqDTO requestParam);

    /**
     * 短链接跳转
     * @param shortUri 短链接后缀
     * @param request HTTP 请求
     * @param response HTTP 响应
     */
    void restoreUrl(String shortUri, ServletRequest request, ServletResponse response);

    /**
     * 批量创建短链接
     * @param requestParam 批量短链接创建请求
     * @return 批量创建响应
     */
    ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam);

    /**
     * 短链接统计
     * @param shortLinkStatsRecordDTO 短链接统计实体参数
     */
    void shortLinkStats(ShortLinkStatsRecordDTO shortLinkStatsRecordDTO);

    /**
     * 根据分布式锁创建短链接
     * @param requestParam 创建短链接请求参数
     * @return 创建短链接响应
     */
    ShortLinkCreateRespDTO createShortLinkByLock(ShortLinkCreateReqDTO requestParam);
}
