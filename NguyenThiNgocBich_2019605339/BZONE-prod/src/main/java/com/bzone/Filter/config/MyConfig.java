package com.bzone.Filter.config;

import com.bzone.Filter.AuthoFilter;
import com.bzone.Filter.FilterUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyConfig {

    @Autowired
    private FilterUser filterUser;

    @Autowired
    private AuthoFilter authoFilter;
    @Bean
    public FilterRegistrationBean<FilterUser> HomeFilter(){
        FilterRegistrationBean<FilterUser> filterUserFilterRegistrationBean = new FilterRegistrationBean<>();
        filterUserFilterRegistrationBean.setFilter(filterUser);
        filterUserFilterRegistrationBean.addUrlPatterns("/*");
        return filterUserFilterRegistrationBean;
    }
    @Bean
    public FilterRegistrationBean<AuthoFilter> AuthoFilter(){
        FilterRegistrationBean<AuthoFilter> filterUserFilterRegistrationBean = new FilterRegistrationBean<>();
        filterUserFilterRegistrationBean.setFilter(authoFilter);
        filterUserFilterRegistrationBean.addUrlPatterns("/*");
        return filterUserFilterRegistrationBean;
    }
}
