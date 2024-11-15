package com.squirrel.shortLink.admin.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MyBatis-Plus 原数据自动填充类
 */
@Component(value = "myMetaObjectHandlerByAdmin")
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入的时候填充属性
     * @param metaObject 元数据
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        strictInsertFill(metaObject,"createTime", Date::new, Date.class);
        strictInsertFill(metaObject,"updateTime", Date::new, Date.class);
        strictInsertFill(metaObject,"defFlag",() -> 0,Integer.class);
    }

    /**
     * 更新的时候填充
     * @param metaObject 元数据
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        strictInsertFill(metaObject,"updateTime", Date::new, Date.class);
    }
}
