package com.poc.stagers.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.poc.stagers.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableMongoRepositories(basePackages = { "com.poc.stagers.repositories" })
public class SecurityConfig
{
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    
    @Autowired
    CustomAuthSuccessHandler customizeAuthenticationSuccessHandler;
    
    @Bean
    public UserDetailsService userDetailsService() {
        return (UserDetailsService)new UserDetailsServiceImpl();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests().antMatchers(new String[] { "/" })
                .permitAll().antMatchers(new String[] { "/home" })
                .permitAll().antMatchers(new String[] { "/login" })
                .permitAll().antMatchers(new String[] { "/signup" })
                .permitAll().antMatchers(new String[] { "/stagers/**" })
                .permitAll().antMatchers(new String[] { "/dashboard/**" })
                .hasAuthority("ADMIN").anyRequest().authenticated()
            .and()
                .csrf().disable()
                .formLogin().successHandler((AuthenticationSuccessHandler)customizeAuthenticationSuccessHandler)
                .loginPage("/login")
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
            .and()
                .logout().logoutRequestMatcher((RequestMatcher)new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")
            .and()
                .exceptionHandling();
            
        return http.build();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder((PasswordEncoder)this.bCryptPasswordEncoder);

        return authProvider;
    }
    
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers(new String[] 
                                    { "/resources/**", "/static/**", "/css/**", 
                                            "/js/**", "/images/**", "/layer/**", 
                                            "/icons/**", "/include/**" });
    }
}