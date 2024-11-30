package com.squirrel.shortLink.admin.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 配置
 */
@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                // 接口文档标题
                .info(new Info().title("SaaS 短链接后台管理 API")
                        // 接口文档描述
                        .description("SaaS 短链接后台管理接口文档")
                        // 接口文档版本
                        .version("v1.0")
                        // 开发者联系方式
                        .contact(new Contact().name("yrlovejava").url("https://github.com/yrlovejava")))
                .externalDocs(new ExternalDocumentation()
                        // 额外补充说明
                        .description("Github 仓库")
                        // 额外补充链接
                        .url("https://github.com/yrlovejava/shortLink"));
    }

}
