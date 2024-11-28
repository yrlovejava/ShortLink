package com.squirrel.shortLink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.squirrel.shortLink.project.dao.entity.*;
import com.squirrel.shortLink.project.dao.mapper.*;
import com.squirrel.shortLink.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.squirrel.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import com.squirrel.shortLink.project.dto.resp.*;
import com.squirrel.shortLink.project.service.ShortLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 短链接监控接口实现层
 */
@Service
@RequiredArgsConstructor
public class ShortLinkStatsServiceImpl implements ShortLinkStatsService {

    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    /**
     * 获取单个短链接监控数据
     *
     * @param requestParam 查询参数
     * @return 监控数据
     */
    @Override
    public ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam) {
        // 1.基础访问详情 uv pv uip
        // 1.1查询数据库
        List<LinkAccessStatsDO> linkStatsByShortLink = linkAccessStatsMapper.listStatsByShortLink(requestParam);
        if (CollUtil.isEmpty(linkStatsByShortLink)) {
            return null;
        }
        // 基础访问数据
        LinkAccessStatsDO pvUvUipStatsByShortLink = linkAccessLogsMapper.findPvUvUipStatsByShortLink(requestParam);
        // 1.2转换为要返回的数据
        List<ShortLinkStatsAccessDailyRespDTO> daily = new ArrayList<>();
        List<String> rangeDates = DateUtil.rangeToList(DateUtil.parse(requestParam.getStartDate()), DateUtil.parse(requestParam.getEndDate()), DateField.DAY_OF_MONTH).stream()
                .map(DateUtil::formatDate)
                .toList();
        // 需要处理有的日期没有访问记录的情况
        rangeDates.forEach(e -> {
            linkStatsByShortLink.stream()
                    .filter(item -> Objects.equals(e, DateUtil.formatDate(item.getDate())))
                    .findFirst()
                    .ifPresentOrElse(item -> {
                        ShortLinkStatsAccessDailyRespDTO accessDailyRespDTO = ShortLinkStatsAccessDailyRespDTO.builder()
                                .date(e)
                                .pv(item.getPv())
                                .uv(item.getUv())
                                .uip(item.getUip())
                                .build();
                        daily.add(accessDailyRespDTO);
                    }, () -> {
                        ShortLinkStatsAccessDailyRespDTO accessDailyRespDTO = ShortLinkStatsAccessDailyRespDTO.builder()
                                .date(e)
                                .pv(0)
                                .uv(0)
                                .uip(0)
                                .build();
                        daily.add(accessDailyRespDTO);
                    });
        });

        // 2.地区访问详情（仅国内）
        List<ShortLinkStatsLocaleCNRespDTO> localeCnStats = new ArrayList<>();
        // 2.1查询数据库
        List<LinkLocaleStatsDO> listedLocaleByShortLink = linkLocaleStatsMapper.listLocaleByShortLink(requestParam);
        // 2.2求和数量
        int localCnSum = listedLocaleByShortLink.stream()
                .mapToInt(LinkLocaleStatsDO::getCnt)
                .sum();
        // 2.3转换为要返回的数据
        listedLocaleByShortLink.forEach(e -> {
            double ratio = (double) e.getCnt() / localCnSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsLocaleCNRespDTO localeCNRespDTO = ShortLinkStatsLocaleCNRespDTO.builder()
                    .cnt(e.getCnt())
                    .locale(e.getProvince())
                    .ratio(actualRatio)
                    .build();
            localeCnStats.add(localeCNRespDTO);
        });

        // 3.小时访问详情
        List<Integer> hourStats = new ArrayList<>();
        // 3.1查询数据库
        List<LinkAccessStatsDO> listHourStatsByShortLink = linkAccessStatsMapper.listHourStatsByShortLink(requestParam);
        // 3.2遍历添加每个小时的数据
        for (int i = 0; i < 24; i++) {
            AtomicInteger hour = new AtomicInteger(i);
            Integer hourCnt = listHourStatsByShortLink.stream()
                    .filter(e -> Objects.equals(e.getHour(), hour.get()))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            hourStats.add(hourCnt);
        }

        // 4.高频访问Ip详情
        List<ShortLinkStatsTopIpRespDTO> topIpStats = new ArrayList<>();
        // 4.1查询数据库
        List<HashMap<String, Object>> listTopIpByShortLink = linkAccessLogsMapper.listTopIpByShortLink(requestParam);
        // 4.2转换为需要返回的类型
        listTopIpByShortLink.forEach(e -> {
            ShortLinkStatsTopIpRespDTO statsTopIpRespDTO = ShortLinkStatsTopIpRespDTO.builder()
                    .ip(e.get("ip").toString())
                    .cnt(Integer.parseInt(e.get("count").toString()))
                    .build();
            topIpStats.add(statsTopIpRespDTO);
        });

        // 5.一周内访问详情
        List<Integer> weekdayStats = new ArrayList<>();
        // 5.1查询数据库
        List<LinkAccessStatsDO> listWeekdayStatsByShortLink = linkAccessStatsMapper.listWeekdayStatsByShortLink(requestParam);
        // 5.2遍历添加一周内每一天的访问数据
        for (int i = 0; i < 8; i++) {
            AtomicInteger weekday = new AtomicInteger(i);
            Integer weekdayCnt = listWeekdayStatsByShortLink.stream()
                    .filter(e -> Objects.equals(e.getWeekday(), weekday.get()))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            weekdayStats.add(weekdayCnt);
        }

        // 6.浏览器访问详情
        List<ShortLinkStatsBrowserRespDTO> browserStats = new ArrayList<>();
        // 6.1查询数据库
        List<HashMap<String, Object>> listBrowserStatsByShortLink = linkBrowserStatsMapper.listBrowserStatsByShortLink(requestParam);
        // 6.2统计所有访问数
        int browserSum = listBrowserStatsByShortLink.stream()
                .mapToInt(each -> Integer.parseInt(each.get("count").toString()))
                .sum();
        listBrowserStatsByShortLink.forEach(each -> {
            // 6.3计算占比
            double ratio = (double) Integer.parseInt(each.get("count").toString()) / browserSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            // 6.4封装成要返回的数据
            ShortLinkStatsBrowserRespDTO browserRespDTO = ShortLinkStatsBrowserRespDTO.builder()
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .browser(each.get("browser").toString())
                    .ratio(actualRatio)
                    .build();
            browserStats.add(browserRespDTO);
        });

        // 7.操作系统访问详情
        List<ShortLinkStatsOsRespDTO> osStats = new ArrayList<>();
        // 7.1查询数据库
        List<HashMap<String, Object>> listOsStatsByShortLink = linkOsStatsMapper.listOsStatsByShortLink(requestParam);
        // 7.2计算总访问量
        int osSum = listOsStatsByShortLink.stream()
                .mapToInt(each -> Integer.parseInt(each.get("count").toString()))
                .sum();
        listOsStatsByShortLink.forEach(each -> {
            // 7.3计算占比
            double ratio = (double) Integer.parseInt(each.get("count").toString()) / osSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            // 7.4封装成要返回的数据
            ShortLinkStatsOsRespDTO osRespDTO = ShortLinkStatsOsRespDTO.builder()
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .os(each.get("os").toString())
                    .ratio(actualRatio)
                    .build();
            osStats.add(osRespDTO);
        });

        // 8.访客访问类型详情
        List<ShortLinkStatsUvRespDTO> uvTypeStats = new ArrayList<>();
        // 8.1查询数据库
        HashMap<String, Object> findUvTypeByShortLink = linkAccessLogsMapper.findUvTypeCntByShortLink(requestParam);
        // 8.2获取新旧访客数量
        int oldUserCnt = Integer.parseInt(
                Optional.ofNullable(findUvTypeByShortLink)
                        .map(e -> e.get("oldUserCnt"))
                        .map(Object::toString)
                        .orElse("0")
        );
        int newUserCnt = Integer.parseInt(
                Optional.ofNullable(findUvTypeByShortLink)
                        .map(e -> e.get("newUserCnt"))
                        .map(Object::toString)
                        .orElse("0"));
        int uvSum = oldUserCnt + newUserCnt;
        // 8.3计算新旧访客数量占比
        double oldRatio = (double) oldUserCnt / uvSum;
        double actualOldRatio = Math.round(oldRatio * 100.0) / 100.0;
        double newRatio = (double) newUserCnt / uvSum;
        double actualNewRatio = Math.round(newRatio * 100.0) / 100.0;
        // 8.4封装新访客返回数据
        ShortLinkStatsUvRespDTO newUvRespDTO = ShortLinkStatsUvRespDTO.builder()
                .uvType("newUser")
                .cnt(newUserCnt)
                .ratio(actualNewRatio)
                .build();
        uvTypeStats.add(newUvRespDTO);
        // 8.5封装旧访客返回数据
        ShortLinkStatsUvRespDTO oldUvRespDTO = ShortLinkStatsUvRespDTO.builder()
                .uvType("oldUser")
                .cnt(oldUserCnt)
                .ratio(actualOldRatio)
                .build();
        uvTypeStats.add(oldUvRespDTO);

        // 9.访问设备类型详情
        List<ShortLinkStatsDeviceRespDTO> deviceStats = new ArrayList<>();
        // 9.1查询数据库
        List<LinkDeviceStatsDO> listDeviceStatsByShortLink = linkDeviceStatsMapper.listDeviceStatsByShortLink(requestParam);
        // 9.2统计所有数量
        int deviceSum = listDeviceStatsByShortLink.stream()
                .mapToInt(LinkDeviceStatsDO::getCnt)
                .sum();
        listDeviceStatsByShortLink.forEach(each -> {
            // 9.3计算占比
            double ratio = (double) each.getCnt() / deviceSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            // 9.4封装为要返回的数据
            ShortLinkStatsDeviceRespDTO deviceRespDTO = ShortLinkStatsDeviceRespDTO.builder()
                    .cnt(each.getCnt())
                    .device(each.getDevice())
                    .ratio(actualRatio)
                    .build();
            deviceStats.add(deviceRespDTO);
        });

        // 10.访问网络类型详情
        List<ShortLinkStatsNetworkRespDTO> networkStats = new ArrayList<>();
        // 10.1查询数据库
        List<LinkNetworkStatsDO> listNetworkStatsByShortLink = linkNetworkStatsMapper.listNetworkStatsByShortLink(requestParam);
        // 10.2统计所有的数量
        int networkSum = listNetworkStatsByShortLink.stream()
                .mapToInt(LinkNetworkStatsDO::getCnt)
                .sum();
        listNetworkStatsByShortLink.forEach(each -> {
            // 10.3计算占比
            double ratio = (double) each.getCnt() / networkSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            // 10.4封装为要返回的数据
            ShortLinkStatsNetworkRespDTO networkRespDTO = ShortLinkStatsNetworkRespDTO.builder()
                    .cnt(each.getCnt())
                    .network(each.getNetwork())
                    .ratio(actualRatio)
                    .build();
            networkStats.add(networkRespDTO);
        });

        // 11.封装为最终要返回的数据
        return ShortLinkStatsRespDTO.builder()
                .pv(pvUvUipStatsByShortLink.getPv())
                .uv(pvUvUipStatsByShortLink.getUv())
                .uip(pvUvUipStatsByShortLink.getUip())
                .daily(daily)
                .localeCnStats(localeCnStats)
                .hourStats(hourStats)
                .topIpStats(topIpStats)
                .weekdayStats(weekdayStats)
                .browserStats(browserStats)
                .osStats(osStats)
                .uvTypeStats(uvTypeStats)
                .deviceStats(deviceStats)
                .networkStats(networkStats)
                .build();
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     * @param requestParam 获取短链接监控访问记录数据入参
     * @return 访问记录监控数据
     */
    @Override
    public IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        // 1.构造查询条件
        LambdaQueryWrapper<LinkAccessLogsDO> queryWrapper = Wrappers.<LinkAccessLogsDO>lambdaQuery()
                .eq(LinkAccessLogsDO::getFullShortUrl, requestParam.getFullShortUrl())
                .between(LinkAccessLogsDO::getCreateTime, requestParam.getStartDate(), requestParam.getEndDate())
                .eq(LinkAccessLogsDO::getDelFlag, 0)
                .orderByDesc(LinkAccessLogsDO::getCreateTime);

        // 2.分页查询数据库
        IPage<LinkAccessLogsDO> linkAccessLogsDOIPage = linkAccessLogsMapper.selectPage(requestParam, queryWrapper);

        // 3.转换为需要返回的数据
        IPage<ShortLinkStatsAccessRecordRespDTO> actualResult = linkAccessLogsDOIPage.convert(each -> BeanUtil.toBean(each, ShortLinkStatsAccessRecordRespDTO.class));

        // 4.遍历记录获取用户名
        List<String> userAccessLogsList = actualResult.getRecords().stream()
                .map(ShortLinkStatsAccessRecordRespDTO::getUser)
                .toList();

        // 5.查询用户记录和是否为新旧用户
        List<Map<String, Object>> ubTypeList = linkAccessLogsMapper.selectUvTypeByUsers(
                requestParam.getFullShortUrl(),
                requestParam.getStartDate(),
                requestParam.getEndDate(),
                userAccessLogsList
        );

        // 6.处理数据
        actualResult.getRecords().forEach(e -> {
            String uvType = ubTypeList.stream()
                    .filter(item -> Objects.equals(e.getUser(),item.get("user")))
                    .findFirst()
                    .map(item -> item.get("uvType"))
                    .map(Objects::toString)
                    .orElse("旧访客");
            e.setUvType(uvType);
        });

        // 7.返回处理后的数据
        return actualResult;
    }

    /**
     * 访问分组短链接指定时间内监控数据
     * @param requestParam 分组短链接
     * @return 监控数据
     */
    @Override
    public ShortLinkStatsRespDTO groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        List<LinkAccessStatsDO> listStatsByGroup = linkAccessStatsMapper.listStatsByGroup(requestParam);
        if (CollUtil.isEmpty(listStatsByGroup)) {
            return null;
        }
        // 基础访问数据
        LinkAccessStatsDO pvUvUidStatsByGroup = linkAccessLogsMapper.findPvUvUipStatsByGroup(requestParam);
        // 基础访问详情
        List<ShortLinkStatsAccessDailyRespDTO> daily = new ArrayList<>();
        List<String> rangeDates = DateUtil.rangeToList(DateUtil.parse(requestParam.getStartDate()), DateUtil.parse(requestParam.getEndDate()), DateField.DAY_OF_MONTH).stream()
                .map(DateUtil::formatDate)
                .toList();
        rangeDates.forEach(each -> listStatsByGroup.stream()
                .filter(item -> Objects.equals(each, DateUtil.formatDate(item.getDate())))
                .findFirst()
                .ifPresentOrElse(item -> {
                    ShortLinkStatsAccessDailyRespDTO accessDailyRespDTO = ShortLinkStatsAccessDailyRespDTO.builder()
                            .date(each)
                            .pv(item.getPv())
                            .uv(item.getUv())
                            .uip(item.getUip())
                            .build();
                    daily.add(accessDailyRespDTO);
                }, () -> {
                    ShortLinkStatsAccessDailyRespDTO accessDailyRespDTO = ShortLinkStatsAccessDailyRespDTO.builder()
                            .date(each)
                            .pv(0)
                            .uv(0)
                            .uip(0)
                            .build();
                    daily.add(accessDailyRespDTO);
                }));
        // 地区访问详情（仅国内）
        List<ShortLinkStatsLocaleCNRespDTO> localeCnStats = new ArrayList<>();
        List<LinkLocaleStatsDO> listedLocaleByGroup = linkLocaleStatsMapper.listLocaleByGroup(requestParam);
        int localeCnSum = listedLocaleByGroup.stream()
                .mapToInt(LinkLocaleStatsDO::getCnt)
                .sum();
        listedLocaleByGroup.forEach(each -> {
            double ratio = (double) each.getCnt() / localeCnSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsLocaleCNRespDTO localeCNRespDTO = ShortLinkStatsLocaleCNRespDTO.builder()
                    .cnt(each.getCnt())
                    .locale(each.getProvince())
                    .ratio(actualRatio)
                    .build();
            localeCnStats.add(localeCNRespDTO);
        });
        // 小时访问详情
        List<Integer> hourStats = new ArrayList<>();
        List<LinkAccessStatsDO> listHourStatsByGroup = linkAccessStatsMapper.listHourStatsByGroup(requestParam);
        for (int i = 0; i < 24; i++) {
            AtomicInteger hour = new AtomicInteger(i);
            int hourCnt = listHourStatsByGroup.stream()
                    .filter(each -> Objects.equals(each.getHour(), hour.get()))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            hourStats.add(hourCnt);
        }
        // 高频访问IP详情
        List<ShortLinkStatsTopIpRespDTO> topIpStats = new ArrayList<>();
        List<HashMap<String, Object>> listTopIpByGroup = linkAccessLogsMapper.listTopIpByGroup(requestParam);
        listTopIpByGroup.forEach(each -> {
            ShortLinkStatsTopIpRespDTO statsTopIpRespDTO = ShortLinkStatsTopIpRespDTO.builder()
                    .ip(each.get("ip").toString())
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .build();
            topIpStats.add(statsTopIpRespDTO);
        });
        // 一周访问详情
        List<Integer> weekdayStats = new ArrayList<>();
        List<LinkAccessStatsDO> listWeekdayStatsByGroup = linkAccessStatsMapper.listWeekdayStatsByGroup(requestParam);
        for (int i = 1; i < 8; i++) {
            AtomicInteger weekday = new AtomicInteger(i);
            int weekdayCnt = listWeekdayStatsByGroup.stream()
                    .filter(each -> Objects.equals(each.getWeekday(), weekday.get()))
                    .findFirst()
                    .map(LinkAccessStatsDO::getPv)
                    .orElse(0);
            weekdayStats.add(weekdayCnt);
        }
        // 浏览器访问详情
        List<ShortLinkStatsBrowserRespDTO> browserStats = new ArrayList<>();
        List<HashMap<String, Object>> listBrowserStatsByGroup = linkBrowserStatsMapper.listBrowserStatsByGroup(requestParam);
        int browserSum = listBrowserStatsByGroup.stream()
                .mapToInt(each -> Integer.parseInt(each.get("count").toString()))
                .sum();
        listBrowserStatsByGroup.forEach(each -> {
            double ratio = (double) Integer.parseInt(each.get("count").toString()) / browserSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsBrowserRespDTO browserRespDTO = ShortLinkStatsBrowserRespDTO.builder()
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .browser(each.get("browser").toString())
                    .ratio(actualRatio)
                    .build();
            browserStats.add(browserRespDTO);
        });
        // 操作系统访问详情
        List<ShortLinkStatsOsRespDTO> osStats = new ArrayList<>();
        List<HashMap<String, Object>> listOsStatsByGroup = linkOsStatsMapper.listOsStatsByGroup(requestParam);
        int osSum = listOsStatsByGroup.stream()
                .mapToInt(each -> Integer.parseInt(each.get("count").toString()))
                .sum();
        listOsStatsByGroup.forEach(each -> {
            double ratio = (double) Integer.parseInt(each.get("count").toString()) / osSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsOsRespDTO osRespDTO = ShortLinkStatsOsRespDTO.builder()
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .os(each.get("os").toString())
                    .ratio(actualRatio)
                    .build();
            osStats.add(osRespDTO);
        });
        // 访问设备类型详情
        List<ShortLinkStatsDeviceRespDTO> deviceStats = new ArrayList<>();
        List<LinkDeviceStatsDO> listDeviceStatsByGroup = linkDeviceStatsMapper.listDeviceStatsByGroup(requestParam);
        int deviceSum = listDeviceStatsByGroup.stream()
                .mapToInt(LinkDeviceStatsDO::getCnt)
                .sum();
        listDeviceStatsByGroup.forEach(each -> {
            double ratio = (double) each.getCnt() / deviceSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsDeviceRespDTO deviceRespDTO = ShortLinkStatsDeviceRespDTO.builder()
                    .cnt(each.getCnt())
                    .device(each.getDevice())
                    .ratio(actualRatio)
                    .build();
            deviceStats.add(deviceRespDTO);
        });
        // 访问网络类型详情
        List<ShortLinkStatsNetworkRespDTO> networkStats = new ArrayList<>();
        List<LinkNetworkStatsDO> listNetworkStatsByGroup = linkNetworkStatsMapper.listNetworkStatsByGroup(requestParam);
        int networkSum = listNetworkStatsByGroup.stream()
                .mapToInt(LinkNetworkStatsDO::getCnt)
                .sum();
        listNetworkStatsByGroup.forEach(each -> {
            double ratio = (double) each.getCnt() / networkSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsNetworkRespDTO networkRespDTO = ShortLinkStatsNetworkRespDTO.builder()
                    .cnt(each.getCnt())
                    .network(each.getNetwork())
                    .ratio(actualRatio)
                    .build();
            networkStats.add(networkRespDTO);
        });
        return ShortLinkStatsRespDTO.builder()
                .pv(pvUvUidStatsByGroup.getPv())
                .uv(pvUvUidStatsByGroup.getUv())
                .uip(pvUvUidStatsByGroup.getUip())
                .daily(daily)
                .localeCnStats(localeCnStats)
                .hourStats(hourStats)
                .topIpStats(topIpStats)
                .weekdayStats(weekdayStats)
                .browserStats(browserStats)
                .osStats(osStats)
                .deviceStats(deviceStats)
                .networkStats(networkStats)
                .build();
    }

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     * @param requestParam 分组短链接信息
     * @return Result<IPage<ShortLinkStatsAccessRecordRespDTO>>
     */
    @Override
    public IPage<ShortLinkStatsAccessRecordRespDTO> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
        // 1.构造查询表条件
        LambdaQueryWrapper<LinkAccessLogsDO> queryWrapper = Wrappers.lambdaQuery(LinkAccessLogsDO.class)
                .between(LinkAccessLogsDO::getCreateTime, requestParam.getStartDate(), requestParam.getEndDate())
                .eq(LinkAccessLogsDO::getDelFlag, 0)
                .orderByDesc(LinkAccessLogsDO::getCreateTime);

        // 2.分页查询短链接访问记录
        IPage<LinkAccessLogsDO> linkAccessLogsDOIPage = linkAccessLogsMapper.selectPage(requestParam, queryWrapper);

        // 3.处理查询到的访问记录
        IPage<ShortLinkStatsAccessRecordRespDTO> actualResult = linkAccessLogsDOIPage.convert(each -> BeanUtil.toBean(each, ShortLinkStatsAccessRecordRespDTO.class));
        List<String> userAccessLogsList = actualResult.getRecords().stream()
                .map(ShortLinkStatsAccessRecordRespDTO::getUser)
                .toList();

        // 4.按照用户名查询uv和是否为新老用户
        List<Map<String, Object>> uvTypeList = linkAccessLogsMapper.selectGroupUvTypeByUsers(
                requestParam.getGid(),
                requestParam.getStartDate(),
                requestParam.getEndDate(),
                userAccessLogsList
        );

        // 5.处理查询到的结果集
        actualResult.getRecords().forEach(each -> {
            String uvType = uvTypeList.stream()
                    .filter(item -> Objects.equals(each.getUser(), item.get("user")))
                    .findFirst()
                    .map(item -> item.get("uvType"))
                    .map(Object::toString)
                    .orElse("旧访客");
            each.setUvType(uvType);
        });

        // 6.返回结果
        return actualResult;
    }
}
