package com.squirrel.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.squirrel.project.dao.entity.LinkStatsTodayDO;
import org.apache.ibatis.annotations.Param;

/**
 * 短链接今天统计持久层
 */
public interface LinkStatsTodayMapper extends BaseMapper<LinkStatsTodayDO> {

    /**
     * 记录今日统计监控数据
     * @param linkStatsTodayDO 今日监控数据
     */
    void shortLinkTodayState(@Param("linkTodayStats") LinkStatsTodayDO linkStatsTodayDO);
}
