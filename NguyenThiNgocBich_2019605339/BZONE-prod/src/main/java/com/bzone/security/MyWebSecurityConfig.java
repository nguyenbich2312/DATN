package com.bzone.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyWebSecurityConfig {
    @Autowired
    private UserDetailsService userDetailsService;
    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/admin/**")
                .hasAuthority("Admin")
                .antMatchers("/user/**")
                .hasAuthority("Customer")
                .antMatchers("/shipper/**")
                .hasAuthority("Shipper")
                .antMatchers("/seller/**")
                .hasAuthority("Seller")
                .antMatchers("/manager/**")
                .hasAuthority("Manager")
                .antMatchers("/", "/product/**", "/loadProduct/**","/loadMenu/**", "/search/**", "/css/**", "/JS/**", "/image/**","/category")
                .permitAll()
                .antMatchers("/login*", "/signup/**", "/forgot-password/**", "/reset-password/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .usernameParameter("email")
                .loginProcessingUrl("/login")
                .successHandler(new MyLoginSuccesHandler())
                .failureForwardUrl("/login-failed")
                .and()
                .exceptionHandling()
                .accessDeniedHandler(new MyAccessDeniedHandler())
                .and()
                .logout()
                .logoutUrl("/perform_logout")
                .logoutSuccessHandler(new MyLogoutSuccessHandler())
         ;
        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }
}
