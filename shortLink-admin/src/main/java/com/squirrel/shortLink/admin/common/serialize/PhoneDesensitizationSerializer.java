package com.squirrel.shortLink.admin.common.serialize;

import cn.hutool.core.util.DesensitizedUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 手机号脱敏反序列化
 */
public class PhoneDesensitizationSerializer extends JsonSerializer<String> {

    /**
     * 序列化电话号码
     * @param phone 要序列化的电话号码
     * @param jsonGenerator Jackson 提供的 JSON 数据生成器，用于写入 JSON 数据
     * @param serializerProvider 序列化器提供器，用于获取序列化配置或其他序列化器
     * @throws IOException 抛出的异常
     */
    @Override
    public void serialize(String phone, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        // 1.电话号码脱敏
        String phoneDesensitization = DesensitizedUtil.mobilePhone(phone);
        // 2.写入 JSON 数据
        jsonGenerator.writeString(phoneDesensitization);
    }
}
