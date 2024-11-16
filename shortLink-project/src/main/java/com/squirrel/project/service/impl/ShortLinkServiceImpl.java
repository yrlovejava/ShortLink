package com.squirrel.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.project.dao.entity.ShortLinkDO;
import com.squirrel.project.dao.mapper.ShortLinkMapper;
import com.squirrel.project.dto.req.ShortLinkCreateReqDTO;
import com.squirrel.project.dto.resp.ShortLinkCreateRespDTO;
import com.squirrel.project.service.ShortLinkService;
import com.squirrel.project.toolkit.HashUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    /**
     * 创建短链接
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建信息
     */
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        // 1.获取短链接
        String shortLinkSuffix = generateSuffix(requestParam);

        // 2.属性拷贝
        ShortLinkDO shortLinkDO = BeanUtil.toBean(requestParam, ShortLinkDO.class);

        // 3.设置其他属性
        shortLinkDO.setShortUri(shortLinkSuffix);
        shortLinkDO.setEnableStatus(0);
        shortLinkDO.setFullShortUrl(requestParam.getDomain() + "/" + shortLinkSuffix);

        // 4.在数据库中保存数据
        save(shortLinkDO);

        // 5.返回响应
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    /**
     * 生成短链接
     * @param dto 短链接创建的参数
     * @return 短链接
     */
    private String generateSuffix(ShortLinkCreateReqDTO dto){
        String originUrl = dto.getOriginUrl();
        return HashUtil.hashToBase62(originUrl);
    }
}
