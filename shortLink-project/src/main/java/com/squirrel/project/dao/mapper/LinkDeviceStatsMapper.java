package com.squirrel.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.project.dao.entity.LinkDeviceStatsDO;
import org.apache.ibatis.annotations.Param;

/**
 * 访问设备监控持久层
 */
public interface LinkDeviceStatsMapper extends BaseMapper<LinkDeviceStatsDO> {

    void shortLinkDeviceState(@Param("linkDeviceStats")LinkDeviceStatsDO linkDeviceStats);
}
