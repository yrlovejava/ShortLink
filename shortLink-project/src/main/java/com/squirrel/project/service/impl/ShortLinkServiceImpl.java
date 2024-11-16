package com.squirrel.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.common.convention.exception.ServiceException;
import com.squirrel.project.dao.entity.ShortLinkDO;
import com.squirrel.project.dao.mapper.ShortLinkMapper;
import com.squirrel.project.dto.req.ShortLinkCreateReqDTO;
import com.squirrel.project.dto.resp.ShortLinkCreateRespDTO;
import com.squirrel.project.service.ShortLinkService;
import com.squirrel.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortLinkBloomFilter;

    /**
     * 创建短链接
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建信息
     */
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        // 1.获取短链接
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(requestParam.getDomain())
                .append("/")
                .append(shortLinkSuffix)
                .toString();

        // 2.构建shortLink 实体
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(requestParam.getDomain())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .fullShortUrl(fullShortUrl)
                .build();

        // 4.在数据库中保存数据
        try {
            save(shortLinkDO);
        }catch (DuplicateKeyException ex){
            ShortLinkDO hasShortLinkDO = getBaseMapper().selectOne(
                    Wrappers.<ShortLinkDO>lambdaQuery()
                            .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
            );
            if (hasShortLinkDO != null) {
                log.warn("短链接: {} 重复入库",fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
        }

        // 5.在布隆过滤器中保存短链接
        shortLinkBloomFilter.add(shortLinkSuffix);

        // 6.返回响应
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
        // 1.初始化计数器
        int customGenerateCount = 0;
        String shortUri;
        // 2.确保生成的短链接唯一
        while (true){
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接频繁生成，请稍后再试");
            }
            String originUrl = dto.getOriginUrl();
            originUrl += System.currentTimeMillis();
            shortUri = HashUtil.hashToBase62(originUrl);
            if (!shortLinkBloomFilter.contains(shortUri)) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }
}
