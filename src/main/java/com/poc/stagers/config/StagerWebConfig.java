package com.poc.stagers.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.CacheControl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import com.poc.stagers.login.LoginCheck;
import com.poc.stagers.login.LoginUserResolver;

@Configuration
@EnableWebMvc
@EnableMongoRepositories
public class StagerWebConfig implements WebMvcConfigurer
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
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
            .build();
    }

    @Bean
    public LoginCheck loginCheck() {
        return (LoginCheck)new LoginCheck();
    }

    @Bean
    public LoginUserResolver loginUserResolver() {
        return (LoginUserResolver) new LoginUserResolver();
    }
    
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor((HandlerInterceptor)this.localeInterceptor());
        registry.addInterceptor(loginCheck())
                                        .excludePathPatterns("/", "/home", "/login", 
                                                        "/stagers", "/signup", "/resources/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserResolver());
    }
    
    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/home").setViewName("home");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/dashboard").setViewName("dashboard");
        registry.addViewController("/charts").setViewName("charts");
        registry.addViewController("/tables").setViewName("tables");
        registry.addViewController("/createevent").setViewName("createevent");
        registry.addViewController("/stagers").setViewName("stagers");
        registry.addViewController("/stagers/createuserstager").setViewName("createuserstager");
        registry.addViewController("/stagers/createeventstager").setViewName("createeventstager");
    }
    
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler(new String[] { "/**" })
                .addResourceLocations(StagerWebConfig.CLASSPATH_RESOURCE_LOCATIONS)
                .setCacheControl(CacheControl.maxAge(2L, TimeUnit.HOURS)
                .cachePublic());
        registry.addResourceHandler(new String[] { "/images/**" })
                .addResourceLocations(new String[] { "/images/" })
                .setCacheControl(CacheControl.maxAge(2L, TimeUnit.HOURS)
                .cachePublic());
    }
}