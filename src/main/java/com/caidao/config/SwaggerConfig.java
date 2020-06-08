package com.caidao.config;

import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tom
 * @since 2020-5-21
 */
@Configuration
@EnableSwagger2
@EnableSwaggerBootstrapUI
public class SwaggerConfig {

    /**
     * 全局参数
     * @return List<Parameter>
     */
    @Bean
    public Docket sysApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                //Swagger UI默认显示所有接口
                .apis(RequestHandlerSelectors.basePackage("com.caidao.controller"))
                .paths(PathSelectors.any())
                .build().globalOperationParameters(parameter());
    }

    private List<Parameter> parameter() {
        List<Parameter> params = new ArrayList<>();
        params.add(new ParameterBuilder().name("token")
                .description("认证令牌")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false).build());
        return params;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("江南造船厂接口")
                .description("江南造船厂接口分前端和后端，具体的接口信息可以看文档")
                .termsOfServiceUrl("")
                .contact(new Contact("Tom", "", "245311344@QQ.com"))
                .version("1.0")
                .build();
    }

}
