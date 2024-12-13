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
package com.squirrel.shortLink.admin.toolkit;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 封装 EasyExcel 操作 Web 工具方法
 */
public class EasyExcelWebUtil {

    /**
     * 向浏览器写入 Excel 响应，直接返回用户下载数据
     * @param response http响应
     * @param fileName 文件名
     * @param clazz 类型
     * @param data 数据
     */
    @SneakyThrows
    public static void write(HttpServletResponse response, String fileName, Class<?> clazz, List<?> data) {
        // 1.设置响应头文本类型 Excel的 MIME 类型
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        // 2.设置编码
        response.setCharacterEncoding("UTF-8");
        // 3.对文件名进行编码，以便在 HTTP 响应头中传输时避免特殊字符导致的问题
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+","%20");
        // 4.设置响应头
        // attachment 触发下载
        response.setHeader("Content-Disposition", "attachment; filename*=utf-8''" + fileName + ".xlsx");
        // 5.写入数据
        EasyExcel.write(response.getOutputStream(),clazz).sheet("Sheet").doWrite(data);
    }
}
