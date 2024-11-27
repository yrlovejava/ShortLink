package com.squirrel.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.common.convention.exception.ClientException;
import com.squirrel.common.convention.exception.ServiceException;
import com.squirrel.project.common.enums.VailDateTypeEnum;
import com.squirrel.project.config.GotoDomainWhiteListConfiguration;
import com.squirrel.project.dao.entity.*;
import com.squirrel.project.dao.mapper.*;
import com.squirrel.project.dto.biz.ShortLinkStatsRecordDTO;
import com.squirrel.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.squirrel.project.dto.req.ShortLinkCreateReqDTO;
import com.squirrel.project.dto.req.ShortLinkPageReqDTO;
import com.squirrel.project.dto.req.ShortLinkUpdateReqDTO;
import com.squirrel.project.dto.resp.*;
import com.squirrel.project.mq.producer.ShortLinkStatsSaveProducer;
import com.squirrel.project.service.ShortLinkService;
import com.squirrel.project.toolkit.HashUtil;
import com.squirrel.project.toolkit.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.squirrel.project.common.constant.RedisKeyConstant.*;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortLinkBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final GotoDomainWhiteListConfiguration gotoDomainWhiteListConfiguration;
    private final ShortLinkStatsSaveProducer shortLinkStatsSaveProducer;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    /**
     * 创建短链接
     *
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建信息
     */
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        // 验证白名单
        verificationWhiteList(requestParam.getOriginUrl());
        // 1.获取短链接
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(createShortLinkDefaultDomain)
                .append("/")
                .append(shortLinkSuffix)
                .toString();

        // 2.构建shortLink 实体
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(createShortLinkDefaultDomain)
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .totalPV(0)
                .totalUV(0)
                .totalUip(0)
                .fullShortUrl(fullShortUrl)
                .favicon(getFavicon(requestParam.getOriginUrl()))
                .build();

        // 3.构建goto 实体
        ShortLinkGotoDO linkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid())
                .build();

        // 4.在数据库中保存数据
        try {
            save(shortLinkDO);
            shortLinkGotoMapper.insert(linkGotoDO);
        } catch (DuplicateKeyException ex) {
            throw new ServiceException(String.format("短链接：%s 生成重复", fullShortUrl));
        }

        // 5.在redis中保存短链接，采用的是先写数据库再写redis的策略
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidTime(requestParam.getValidDate()),
                TimeUnit.MILLISECONDS
        );

        // 6.在布隆过滤器中保存短链接
        shortLinkBloomFilter.add(fullShortUrl);

        // 7.返回响应
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    /**
     * 验证白名单
     * @param originUrl 原始链接
     */
    private void verificationWhiteList(String originUrl) {
        // 是否开启白名单
        Boolean enable = gotoDomainWhiteListConfiguration.getEnable();
        // 如果不开启，直接返回
        if (enable == null || Boolean.FALSE.equals(enable)) {
            return;
        }
        // 获取域名
        String domain = LinkUtil.extractDomain(originUrl);
        if (StrUtil.isBlank(domain)) {
            throw new ClientException("跳转链接填写错误");
        }
        // 获取可跳转的原始域名
        List<String> details = gotoDomainWhiteListConfiguration.getDetails();
        if (!details.contains(domain)) {
            // 如果不包含
            throw new ClientException("演示环境为了避免恶意攻击，请生成一下网站跳转链接: " + gotoDomainWhiteListConfiguration.getNames());
        }
    }

    /**
     * 批量创建短链接
     * @param requestParam 批量短链接创建请求
     * @return 批量创建响应
     */
    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        // 1.获取所有的原始链接
        List<String> originUrls = requestParam.getOriginUrls();
        // 2.获取所有的描述
        List<String> describes = requestParam.getDescribes();

        // 3.解析请求参数中的信息，封装为创建单个短链接的DTO
        List<ShortLinkBaseInfoRespDTO> result = new ArrayList<>();
        for (int i = 0; i < originUrls.size(); i++) {
            ShortLinkCreateReqDTO shortLinkCreateReqDTO = BeanUtil.toBean(requestParam, ShortLinkCreateReqDTO.class);
            shortLinkCreateReqDTO.setOriginUrl(originUrls.get(i));
            shortLinkCreateReqDTO.setDescribe(describes.get(i));
            try {
                // 4.调用本地方法，保存到数据库
                ShortLinkCreateRespDTO shortLink = createShortLink(shortLinkCreateReqDTO);
                ShortLinkBaseInfoRespDTO linkBaseInfoRespDTO = ShortLinkBaseInfoRespDTO.builder()
                        .fullShortUrl(shortLink.getFullShortUrl())
                        .originUrl(shortLink.getOriginUrl())
                        .describe(describes.get(i))
                        .build();
                // 5.在响应结果集中添加响应
                result.add(linkBaseInfoRespDTO);
            } catch (Throwable ex) {
                log.error("批量创建短链接失败，原始参数：{}", originUrls.get(i));
            }
        }
        // 6.返回响应数据
        return ShortLinkBatchCreateRespDTO.builder()
                .total(result.size())
                .baseLinkInfos(result)
                .build();
    }

    /**
     * 获取原始网站图标
     * @param url 网站url
     * @return 图标url
     */
    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }

    /**
     * 生成短链接
     *
     * @param dto 短链接创建的参数
     * @return 短链接
     */
    private String generateSuffix(ShortLinkCreateReqDTO dto) {
        // 1.初始化计数器
        int customGenerateCount = 0;
        String shortUri;
        // 2.确保生成的短链接唯一
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接频繁生成，请稍后再试");
            }
            String originUrl = dto.getOriginUrl();
            originUrl += UUID.fastUUID().toString();
            shortUri = HashUtil.hashToBase62(originUrl);
            if (!shortLinkBloomFilter.contains(createShortLinkDefaultDomain + "/" + shortUri)) {
                break;
            }
            customGenerateCount++;
        }
        // 3.返回创建的短链接
        return shortUri;
    }

    /**
     * 分页查询短链接
     *
     * @param requestParam 短链接查询参数
     * @return 短链接分页返回结果
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        // 1.构建查询条件
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.<ShortLinkDO>lambdaQuery()
                .select(ShortLinkDO::getId, ShortLinkDO::getDomain,
                        ShortLinkDO::getShortUri, ShortLinkDO::getFullShortUrl, ShortLinkDO::getOriginUrl,
                        ShortLinkDO::getGid, ShortLinkDO::getValidDateType, ShortLinkDO::getValidDate, ShortLinkDO::getDescribe,
                        ShortLinkDO::getFavicon, ShortLinkDO::getCreateTime)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);

        // 2.分页查询
        IPage<ShortLinkDO> resultPage = getBaseMapper().selectPage(requestParam, queryWrapper);

        // 3.返回结果
        return resultPage.convert(e -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(e, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    /**
     * 查询短链接分组内数量
     *
     * @param requestParam 查询参数(分组id的集合)
     * @return Result<List < ShortLinkGroupCountQueryRespDTO>>
     */
    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        // 1.查询数据
        // 1.1构造查询条件
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid,count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .groupBy("gid");
        // 1.2在数据库中查询数据
        List<Map<String, Object>> shortLinkDOList = baseMapper.selectMaps(queryWrapper);

        // 2.返回数据
        return BeanUtil.copyToList(shortLinkDOList, ShortLinkGroupCountQueryRespDTO.class);
    }

    /**
     * 修改短链接
     *
     * @param requestParam 修改短链接信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        // 验证白名单
        verificationWhiteList(requestParam.getOriginUrl());
        // 1.构造查询条件
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.<ShortLinkDO>lambdaQuery()
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);

        // 2.在数据库中查询数据
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null) {
            throw new ClientException("短链接记录不存在");
        }

        // 3.构造实体类
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .gid(requestParam.getGid())
                .originUrl(requestParam.getOriginUrl())
                .describe(requestParam.getDescribe())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .build();
        // 属性拷贝
        BeanUtil.copyProperties(shortLinkDO, hasShortLinkDO);

        // 4.判断是否修改分组id
        if (Objects.equals(hasShortLinkDO.getGid(), requestParam.getGid())) {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.<ShortLinkDO>lambdaUpdate()
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.<ShortLinkDO>lambdaUpdate()
                    .eq(ShortLinkDO::getGid, hasShortLinkDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            // TODO: 这里需要通过加锁保证原子性
            baseMapper.delete(updateWrapper);
            baseMapper.insert(shortLinkDO);
        }

        // 5.如果需要修改日期和有效时间类型，那么就要同步redis，这里采取的是删缓存
        if (!Objects.equals(hasShortLinkDO.getValidDateType(), requestParam.getValidDateType()) // 这表明需要修改有效日期
                || !Objects.equals(hasShortLinkDO.getValidDate(), requestParam.getValidDate()) // 这表明需要修改日期
        ) {
            // 先删除有效链接缓存
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
            // 如果数据库中短链接有效日期在当前时间之前，即已过期(因为只有过期短链接才会有空对象的缓存)
            if (hasShortLinkDO.getValidDate() != null && hasShortLinkDO.getValidDate().before(new Date())) {
                // 如果已经过期了，但是有效日期类型改成了永久的，或者有效日期改成了当前时间之后
                if (Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()) || requestParam.getValidDate().after(new Date())) {
                    // 那么就需要删除空对象缓存
                    stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
                }
            }
        }
    }

    /**
     * 短链接跳转
     *
     * @param shortUri 短链接后缀
     * @param request  HTTP 请求
     * @param response HTTP 响应
     */
    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        // 1.构建完整短链接
        String serverName = request.getServerName();
        String serverPort = Optional.of(request.getServerPort())
                .filter(e -> !Objects.equals(e,80))
                .map(String::valueOf)
                .map(e -> ":" + e)
                .orElse("");
        String fullShortUrl = serverName + serverPort + "/" + shortUri;

        // 2.在redis中查询跳转表
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            // 记录访问量
            shortLinkStats(fullShortUrl,null,buildLinkStatsRecordAndSetUser(fullShortUrl,request,response));
            // 跳转
            ((HttpServletResponse) response).sendRedirect(originalLink);
            return;
        }

        // 3.查询布隆过滤器，解决缓存穿透问题
        boolean contains = shortLinkBloomFilter.contains(fullShortUrl);
        if (!contains) {
            // 跳转到短链接不存在页面
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        // 查询空值，如有空值，那么证明数据库中也不存在，直接返回
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            // 跳转到短链接不存在页面
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }

        // 4.加锁
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            // 这里需要再次查询是因为之前查询的时候没有加锁，这时候可能有其他线程已经在redis中添加了短链接
            // 这时候去做double check防止对数据库做无用的查询和在redis中做无用的添加
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;
            }
            // 5.查询数据库中路由表的记录
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(Wrappers.<ShortLinkGotoDO>lambdaQuery()
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl)
            );
            if (shortLinkGotoDO == null) {
                // 在redis中缓存空值
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                // 跳转到短链接不存在页面
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }

            // 6.构建真实短链接实体的查询信息
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.<ShortLinkDO>lambdaQuery()
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);

            // 7.查询数据库中短链接实体
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            // 判断是否过期
            if (shortLinkDO == null || (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date()))) {
                // 如果已经过期，在redis中缓存空对象
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                // 跳转到短链接不存在页面
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            // 在redis中保存
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUrl(),
                    LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()),
                    TimeUnit.MILLISECONDS
            );
            // 记录访问量
            shortLinkStats(fullShortUrl,null,buildLinkStatsRecordAndSetUser(fullShortUrl,request,response));
            // 跳转
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 构建短链接统计实体
     * @param fullShortUrl 完整的短链接
     * @param request http 请求
     * @param response http 响应
     * @return ShortLinkStatsRecordDTO
     */
    private ShortLinkStatsRecordDTO buildLinkStatsRecordAndSetUser(String fullShortUrl,ServletRequest request,ServletResponse response) {
        // 用于标识这是否当前用户首次访问网站
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        // 获取所有的cookie
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        AtomicReference<String> uv = new AtomicReference<>();
        // 定义在resp中添加 uv 的cookie的任务
        Runnable addResponseCookieTask = () -> {
            uv.set(UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl,fullShortUrl.indexOf("/"),fullShortUrl.length()));
            ((HttpServletResponse) response).addCookie(uvCookie);
            uvFirstFlag.set(Boolean.TRUE);
            // 在redis的set集合中保存uv，通过uv这个随机字符串的数量来判断独立访客量
            stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl,uv.get());
        };
        if (ArrayUtil.isNotEmpty(cookies)) {
            // 查找 uv 的cookie
            // 如果存在，那么就去redis中查询当前短链接的uv量，如果为0或者null
            Arrays.stream(cookies)
                    .filter(e -> Objects.equals(e.getName(),"uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(e -> {
                        uv.set(e);
                        // 在redis中去添加uv，如果返回null或0 证明添加失败，证明set中有这个uv
                        Long uvAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl,e);
                        // 如果添加成功，那么设置为首次访问
                        uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                    },addResponseCookieTask);
        } else {
            // 直接执行添加uv cookie的任务
            addResponseCookieTask.run();
        }
        // 获取用户真实的ip
        String remoteAddr = LinkUtil.getActualIp((HttpServletRequest) request);
        // 获取操作系统
        String os = LinkUtil.getOs(((HttpServletRequest) request));
        // 获取浏览器
        String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
        // 获取设备
        String device = LinkUtil.getDevice(((HttpServletRequest) request));
        // 获取网络
        String network = LinkUtil.getNetwork(((HttpServletRequest) request));
        // 在redis中去添加uip，如果返回null或0 证明添加失败，证明set中有这个uip
        Long uipAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UIP_KEY + fullShortUrl, remoteAddr);
        // 如果有uip，则证明不是第一次访问
        boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;

        // 返回短链接统计实体
        return ShortLinkStatsRecordDTO.builder()
                .fullShortUrl(fullShortUrl)
                .uv(uv.get())
                .uvFirstFlag(uvFirstFlag.get())
                .uipFirstFlag(uipFirstFlag)
                .remoteAddr(remoteAddr)
                .os(os)
                .browser(browser)
                .device(device)
                .network(network)
                .build();
    }

    /**
     * 统计短链接uv，pv，uip
     * @param fullShortUrl 完整短链接
     * @param gid 分组标识
     * @param statsRecord 短链接统计实体参数
     */
    @Override
    public void shortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecord) {
        Map<String, String> producerMap = new HashMap<>();
        producerMap.put("fullShortUrl", fullShortUrl);
        producerMap.put("gid", gid);
        producerMap.put("statsRecord", JSON.toJSONString(statsRecord));
        // 使用redis的消息队列保存短链接访问记录
        shortLinkStatsSaveProducer.send(producerMap);
    }
}
