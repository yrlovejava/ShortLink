package com.squirrel.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.project.dao.entity.ShortLinkDO;
import com.squirrel.project.dao.mapper.ShortLinkMapper;
import com.squirrel.project.dto.req.RecycleBinSaveReqDTO;
import com.squirrel.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.squirrel.project.dto.resp.ShortLinkPageRespDTO;
import com.squirrel.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.squirrel.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;

/**
 * 回收站管理接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 保存回收站
     *
     * @param requestParam 请求参数
     */
    @Override
    public void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        // 1.构建查询参数
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.<ShortLinkDO>lambdaUpdate()
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getDelFlag, 0);

        // 2.构建要修改的字段
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .enableStatus(1)
                .build();

        // 3.修改数据库
        baseMapper.update(shortLinkDO, updateWrapper);

        // 4.在redis中删除信息
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, shortLinkDO.getFullShortUrl()));
    }

    /**
     * 分页查询回收站短链接
     * @param requestParam 分页查询短链接参数
     * @return IPage<ShortLinkPageRespDTO>
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        // 1.构建查询条件
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.<ShortLinkDO>lambdaQuery()
                .select(ShortLinkDO::getId,ShortLinkDO::getDomain,ShortLinkDO::getShortUri,
                        ShortLinkDO::getGid, ShortLinkDO::getFullShortUrl,ShortLinkDO::getOriginUrl,
                        ShortLinkDO::getValidDateType,ShortLinkDO::getValidDate,ShortLinkDO::getDescribe,
                        ShortLinkDO::getFavicon,ShortLinkDO::getCreateTime)
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag, 0)
                .in(ShortLinkDO::getGid,requestParam.getGidList())
                .orderByDesc(ShortLinkDO::getUpdateTime);

        // 2.分页查询
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam,queryWrapper);

        // 3.处理查询结果并返回
        return resultPage.convert(e -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(e, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }
}