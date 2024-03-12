package com.poc.stagers.login;

import java.util.Arrays;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ModelAndViewDefiningException;

import com.poc.stagers.jwt.JwtTokenProvider;
import com.poc.stagers.models.User;
import com.poc.stagers.service.UserServiceImpl;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

/**
 * Create a custom interceptor to check for jwt token in the cookie
 */
@Component
public class LoginCheck implements HandlerInterceptor {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserServiceImpl userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws ModelAndViewDefiningException {

        String token = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(JwtTokenProvider.COOKIE_NAME)).findFirst().map(Cookie::getValue)
                .orElse(null);

        try {
            if (token != null && jwtTokenProvider.validateToken(token)) {
                UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken)
                                                                jwtTokenProvider.getAuthentication(token);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                String username  = null;
                if (principal instanceof UserDetails) {
                    username = ((UserDetails)principal).getUsername();
                } else {
                    username = principal.toString();
                }
                User user = userService.findByUsername(username);
                request.setAttribute("login", user);
            }
        } catch (ExpiredJwtException ex) {
            ModelAndView mav = new ModelAndView("login");
            mav.addObject("return_url", request.getRequestURI());
            mav.addObject("errorMsg", ex.getLocalizedMessage());
            throw new ModelAndViewDefiningException(mav);
        } catch (JwtException ex) {
            ModelAndView mav = new ModelAndView("login");
            mav.addObject("return_url", request.getRequestURI());
            mav.addObject("errorMsg", ex.getLocalizedMessage());
            throw new ModelAndViewDefiningException(mav);
        } catch (Exception ex) {
            ModelAndView mav = new ModelAndView("login");
            mav.addObject("return_url", request.getRequestURI());
            mav.addObject("errorMsg", "UserName or Password is Invalid");
        }

        return true;
    }

}
