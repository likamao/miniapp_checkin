package com.checkin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.checkin.config.PermissionInterceptor;
import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootApplication
public class CheckinApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CheckinApplication.class, args);
        
        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("正在关闭应用...");
            // 获取数据源并关闭
            DataSource dataSource = context.getBean(DataSource.class);
            try {
                dataSource.getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            context.close();
            System.out.println("应用已关闭");
        }));
    }
    
    @Autowired
    private PermissionInterceptor permissionInterceptor;

    // 添加 CORS 配置和权限拦截器
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                                "http://localhost:8080",
                                "http://localhost:30100",
                                "https://summer.likamao.top"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization")
                        .allowCredentials(true)
                        .maxAge(3600);
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                // 添加权限拦截器，拦截所有 API 请求
                registry.addInterceptor(permissionInterceptor)
                        .addPathPatterns("/api/**")
                        .excludePathPatterns(
                            "/api/auth/login",  // 登录接口不需要权限验证
                            "/api/test/**"      // 测试接口不需要权限验证
                        );
            }
        };
    }
}