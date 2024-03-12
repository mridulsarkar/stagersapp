package com.poc.stagers.config;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.poc.stagers.jwt.JwtTokenFilterConfigurer;
import com.poc.stagers.jwt.JwtTokenProvider;
import com.poc.stagers.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableMongoRepositories(basePackages = { "com.poc.stagers.repositories" })
public class SecurityConfig
{
    @Autowired
    private UserDetailsService userDetailsService;
        
    @Autowired
    CustomAuthSuccessHandler customizeAuthenticationSuccessHandler;
    
    @Bean
    public UserDetailsService userDetailsService() {
        return (UserDetailsService)new UserDetailsServiceImpl();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder;
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return (JwtTokenProvider)new JwtTokenProvider();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Lambda DSL configuration of spring HTTP security 
        http
            .csrf(csrf -> // Disable CSRF
                csrf.disable()
            ).sessionManagement((sessionManagement) -> 
                // Configure stateless session management. For JWT based auth, all the user
                // authentication info is self contained in the token itself, so we don't
                // need to store any additional session information
                sessionManagement.sessionCreationPolicy( SessionCreationPolicy.STATELESS )
            ).authorizeHttpRequests((authorize) -> // Public endpoints
                authorize.antMatchers(new String[] { "/" }).permitAll()
                    .antMatchers(new String[] { "/home" }).permitAll()
                    .antMatchers(new String[] { "/login" }).permitAll()
                    .antMatchers(new String[] { "/signup" }).permitAll()
                    .antMatchers(new String[] { "/stagers/**" }).permitAll()  
                    .anyRequest().authenticated()  // Disallow everything else
            ).formLogin((form) -> // Login handler
                form.successHandler((AuthenticationSuccessHandler)customizeAuthenticationSuccessHandler)
                    .loginPage("/loginAuthentication").permitAll()
                    .loginProcessingUrl("/loginAuthentication")
                    .failureUrl("/login?error=true")
                    .defaultSuccessUrl("/dashboard")
                    .usernameParameter("username")
                    .passwordParameter("password")
            ).logout((logout) -> // Logout handler
                logout.logoutUrl("/logout")
                    .logoutSuccessUrl("/login")
                    .invalidateHttpSession(true)
                    .deleteCookies(JwtTokenProvider.COOKIE_NAME)
            ).exceptionHandling((expHandle) -> // Set unauthorized requests exception handler
                expHandle.authenticationEntryPoint(
                            (request, response, ex) -> {
                                response.sendError (
                                    HttpServletResponse.SC_UNAUTHORIZED,
                                    ex.getMessage()
                                );
                            }
                )
            );
        
        http.authenticationProvider(authenticationProvider());
        // Apply JWT
        http.apply(new JwtTokenFilterConfigurer(jwtTokenProvider()));
            
        return http.build();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder((PasswordEncoder)this.passwordEncoder());

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