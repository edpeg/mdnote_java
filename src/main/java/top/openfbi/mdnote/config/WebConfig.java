package top.openfbi.mdnote.config;

import jakarta.annotation.Resource;
import jakarta.servlet.MultipartConfigElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@SpringBootConfiguration
public class WebConfig implements WebMvcConfigurer {
    @Value(value = "${file-upload.single-file-size}")
    private int singleFileSize;
    @Value(value = "${file-upload.total-file-size}")
    private int totalFileSize;
    @Value(value = "${session.cookie-max-age}")
    private int cookieMaxAge;
    @Resource
    private LoginInterceptor loginInterceptor;
    @Resource
    private InternalApiInterceptor internalApiInterceptor;
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    static String API_PREFIX = "/api";

//    // 跨域问题
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        System.out.println("addCorsMappings  生效");
//        registry.addMapping("/**")
//                .allowCredentials(true)
//                .allowedOrigins("http://localhost:8080")
//                .allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE")
//                .allowedHeaders("*")
//                .maxAge(3600);
////        registration.allowedOrigins("http://127.0.0.1:8080");
////        registration.allowedOrigins("http://localhost:8080");
//
//    }
//
//    /**
//     *  放行跨域请求
//     */
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/**")
//                .addResourceLocations("classpath:static/images/");
//    }

    /**
     * 文件上传配置
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //文件最大
        factory.setMaxFileSize(DataSize.ofMegabytes(singleFileSize)); //KB,MB
        /// 设置总上传数据总大小
        factory.setMaxRequestSize(DataSize.ofMegabytes(totalFileSize));
        return factory.createMultipartConfig();
    }

    /**
     * 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //intercept拦截器
        registry.addInterceptor(internalApiInterceptor).addPathPatterns("/api/internal/**");

        //登录拦截器
        registry.addInterceptor(loginInterceptor).
                // 放行指定URL
                        excludePathPatterns("/api/passport/login").
                excludePathPatterns("/api/passport/register").
                excludePathPatterns("/api/note/open").
                addPathPatterns("/**");    // 拦截其余 url
    }

    /**
     * 统一请求头为api
     * 例：
     * 原URL：user/update
     * 修改后:api/user/update
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        logger.debug("configurePathMatch add API_PREFIX: {}", API_PREFIX);
        configurer.addPathPrefix(API_PREFIX, c -> c.isAnnotationPresent(RequestMapping.class));
    }

    /**
     * 设置客户端Cookie 生命周期为一个月
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        // 设置cookie名称
        serializer.setCookieName("openfbi.top_session");
        //路径
        serializer.setCookiePath("/");
        //有效时间
        serializer.setCookieMaxAge(cookieMaxAge);
        // 设置仅https协议使用此cookie
        serializer.setUseSecureCookie(true);
        // 设置cookie无法被js获取
        serializer.setUseHttpOnlyCookie(true);
        return serializer;
    }


}


