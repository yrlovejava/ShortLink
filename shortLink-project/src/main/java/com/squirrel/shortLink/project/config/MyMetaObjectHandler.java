/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squirrel.shortLink.project.config;

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
