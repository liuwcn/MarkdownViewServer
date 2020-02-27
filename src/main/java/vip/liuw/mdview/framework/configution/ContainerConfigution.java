package vip.liuw.mdview.framework.configution;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.RegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Configuration
public class ContainerConfigution implements WebMvcConfigurer {
    @Value("${myconfig.root-dir}")
    private String rootDir;

    @Value("${myconfig.token}")
    private String systemToken;


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //把图片资源映射到根文件夹
        registry.addResourceHandler("/**/*.jpg", "/**/*.jpeg", "/**/*.png", "/**/*.bmp", "/**/*.webp").addResourceLocations(
                "file:" + rootDir + "/");
    }

    /**
     * 权限验证
     */
    @Bean
    public RegistrationBean regLiftFilter() {
        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new Filter() {
            @Override
            public void init(FilterConfig filterConfig) throws ServletException {

            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                HttpServletRequest req = (HttpServletRequest) request;
                for (Cookie cookie : req.getCookies()) {
                    if ("token".equals(cookie.getName()) && systemToken.equals(cookie.getValue())) {
                        chain.doFilter(request, response);
                    }
                }
            }

            @Override
            public void destroy() {

            }
        });
        bean.addUrlPatterns("/mdfile/*");
        return bean;
    }
}
