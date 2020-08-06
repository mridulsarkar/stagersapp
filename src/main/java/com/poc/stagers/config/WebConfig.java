package com.poc.stagers.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.CacheControl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
@EnableWebMvc
@EnableMongoRepositories
public class WebConfig implements WebMvcConfigurer
{
    private static final String[] CLASSPATH_RESOURCE_LOCATIONS;

    static {
        CLASSPATH_RESOURCE_LOCATIONS = new String[] {   "classpath:/resources/", 
                                                        "classpath:/static/" };
    }
    
    @Bean
    public LocaleResolver localeResolver() {
        return (LocaleResolver)new CookieLocaleResolver();
    }
    
    @Bean
    public LocaleChangeInterceptor localeInterceptor() {
        final LocaleChangeInterceptor localeInterceptor = new LocaleChangeInterceptor();
        localeInterceptor.setParamName("lang");
        return localeInterceptor;
    }
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder;
    }
    
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor((HandlerInterceptor)this.localeInterceptor());
    }
    
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/home").setViewName("home");
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/dashboard").setViewName("dashboard");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/stagers").setViewName("stagers");
        registry.addViewController("/stagers/createuser").setViewName("createuserstager");
        registry.addViewController("/stagers/createevent").setViewName("createeventstager");
    }
    
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler(new String[] { "/**" })
                .addResourceLocations(WebConfig.CLASSPATH_RESOURCE_LOCATIONS)
                .setCacheControl(CacheControl.maxAge(2L, TimeUnit.HOURS)
                .cachePublic());
        registry.addResourceHandler(new String[] { "/images/**" })
                .addResourceLocations(new String[] { "/images/" })
                .setCacheControl(CacheControl.maxAge(2L, TimeUnit.HOURS)
                .cachePublic());
    }
    
}