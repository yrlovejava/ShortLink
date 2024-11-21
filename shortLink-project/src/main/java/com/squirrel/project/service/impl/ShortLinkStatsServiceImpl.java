package com.squirrel.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.squirrel.project.dao.entity.LinkAccessStatsDO;
import com.squirrel.project.dao.entity.LinkDeviceStatsDO;
import com.squirrel.project.dao.entity.LinkLocaleStatsDO;
import com.squirrel.project.dao.entity.LinkNetworkStatsDO;
import com.squirrel.project.dao.mapper.*;
import com.squirrel.project.dto.req.ShortLinkStatsReqDTO;
import com.squirrel.project.dto.resp.*;
import com.squirrel.project.service.ShortLinkStatsService;
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
                    .filter(e -> Objects.equals(e.getHour(), weekday.get()))
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
}
