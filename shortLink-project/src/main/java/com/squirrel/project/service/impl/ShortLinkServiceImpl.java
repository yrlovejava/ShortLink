package com.squirrel.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.squirrel.common.convention.exception.ClientException;
import com.squirrel.common.convention.exception.ServiceException;
import com.squirrel.project.dao.entity.*;
import com.squirrel.project.dao.mapper.*;
import com.squirrel.project.dto.req.ShortLinkCreateReqDTO;
import com.squirrel.project.dto.req.ShortLinkPageReqDTO;
import com.squirrel.project.dto.req.ShortLinkUpdateReqDTO;
import com.squirrel.project.dto.resp.ShortLinkCreateRespDTO;
import com.squirrel.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.squirrel.project.dto.resp.ShortLinkPageRespDTO;
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
import org.apache.calcite.prepare.Prepare;
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
import static com.squirrel.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;

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
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAmapKey;

    /**
     * 创建短链接
     *
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
            ShortLinkDO hasShortLinkDO = getBaseMapper().selectOne(
                    Wrappers.<ShortLinkDO>lambdaQuery()
                            .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
            );
            if (hasShortLinkDO != null) {
                log.warn("短链接: {} 重复入库", fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
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
            originUrl += System.currentTimeMillis();
            shortUri = HashUtil.hashToBase62(originUrl);
            if (!shortLinkBloomFilter.contains(shortUri)) {
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
        String fullShortUrl = serverName + "/" + shortUri;

        // 2.在redis中查询跳转表
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            // 记录返回量
            shortLinkStats(fullShortUrl,null,request,response);
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
            if (shortLinkDO == null || shortLinkDO.getValidDate().before(new Date())) {
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
            shortLinkStats(fullShortUrl,shortLinkDO.getGid(),request,response);
            // 跳转
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 统计短链接uv，pv，uip
     * @param fullShortUrl 完全的短链接
     * @param gid 分组标识
     * @param request http请求
     * @param response http响应
     */
    private void shortLinkStats(String fullShortUrl,String gid,ServletRequest request,ServletResponse response) {
        // TODO: 频繁插入数据库，对数据库压力很大，这里需要优化
        // 1.UV计算
        // 用于标识这是否当前用户首次访问网站
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        // 获取所有的cookie
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        try {
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
                stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl,uv.get());
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
                            Long uvAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uv" + fullShortUrl,e);
                            // 如果添加成功，那么设置为首次访问
                            uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                        },addResponseCookieTask);
            } else {
                // 直接执行添加uv cookie的任务
                addResponseCookieTask.run();
            }

            // 2.UIP计算
            // 获取用户真实的ip
            String remoteAddr = LinkUtil.getActualIp((HttpServletRequest) request);
            // 在redis中尝试添加
            Long uipAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, remoteAddr);
            boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;

            // 3.PV计算
            if(StrUtil.isBlank(gid)){
                // 通过路由表查询分组id
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(Wrappers.<ShortLinkGotoDO>lambdaQuery()
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl)
                );
                gid = shortLinkGotoDO.getGid();
            }

            // 获取当前的时间，日期
            int hour = DateUtil.hour(new Date(),true);
            Week week = DateUtil.dayOfWeekEnum(new Date());
            int weekValue = week.getIso8601Value();

            // 构造访问监控实体
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFirstFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();

            // 插入数据库
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);

            // 4.统计地方访问数据
            // 封装请求参数
            Map<String,Object> localeParamMap = new HashMap<>();
            localeParamMap.put("key",statsLocaleAmapKey);
            localeParamMap.put("ip",remoteAddr);
            // 调用高德API
            String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
            JSONObject localeResultObj = JSON.parseObject(localeResultStr);
            String infocode = localeResultObj.getString("infocode");
            String actualProvince;
            String actualCity;
            if (StrUtil.isNotBlank(infocode) && StrUtil.equals(infocode,"10000")) {
                String province = localeResultObj.getString("province");
                boolean unknownFlag = StrUtil.equals(province, "[]");
                // 构造地区数据的实体
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .province(actualProvince = unknownFlag ? "未知" : province)
                        .city(actualCity = unknownFlag ? "未知" : localeResultObj.getString("city"))
                        .adcode(unknownFlag ? "未知" : localeResultObj.getString("adcode"))
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .country("中国")
                        .gid(gid)
                        .date(new Date())
                        .build();
                // 插入数据库
                linkLocaleStatsMapper.shortLinkLocaleState(linkLocaleStatsDO);

                // 5.统计操作系统访问数据
                String os = LinkUtil.getOs((HttpServletRequest) request);
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .os(os)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkOsStatsMapper.shortLinkOsState(linkOsStatsDO);

                // 5.统计浏览器访问数据
                String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .browser(browser)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkBrowserStatsMapper.shortLinkBrowserStats(linkBrowserStatsDO);

                // 6.访问设备数据
                String device = LinkUtil.getDevice(((HttpServletRequest) request));
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .device(device)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);

                // 7.访问网络数据
                String network = LinkUtil.getNetwork(((HttpServletRequest) request));
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .network(network)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);

                // 8.访问日志记录
                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                        .user(uv.get())
                        .ip(remoteAddr)
                        .browser(browser)
                        .os(os)
                        .network(network)
                        .device(device)
                        .locale(StrUtil.join("-","中国",actualProvince,actualCity))
                        .fullShortUrl(fullShortUrl)
                        .build();
                linkAccessLogsMapper.insert(linkAccessLogsDO);
            }
        }catch (Throwable ex) {
            log.error("短链接访问量统计异常",ex);
        }
    }
}
