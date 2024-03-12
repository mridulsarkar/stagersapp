package com.poc.stagers.login;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.poc.stagers.jwt.JwtTokenProvider;
import com.poc.stagers.models.User;
import com.poc.stagers.service.UserServiceImpl;

/**
 * Argument Resolver
 */
@Component
public class LoginUserResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UserServiceImpl userService;

    @Override
    public boolean supportsParameter(MethodParameter param) {
        return param.hasParameterAnnotation(LoginUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter param, ModelAndViewContainer mvc, NativeWebRequest nreq,
            WebDataBinderFactory dbf) throws Exception {
        final Map<String, Object> resolved = new HashMap<>();

        HttpServletRequest req = (HttpServletRequest) nreq.getNativeRequest();

        Arrays.stream(req.getCookies()).filter(cookie -> cookie.getName().equals(JwtTokenProvider.COOKIE_NAME))
                .map(Cookie::getValue).findFirst().ifPresent(token -> {
                    // @LoginUser User user
                    if (param.getParameterType().isAssignableFrom(User.class)) {
                        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                        String username  = null;
                        if (principal instanceof UserDetails) {
                            username = ((UserDetails)principal).getUsername();
                        } else {
                            username = principal.toString();
                        }
                        User user = userService.findByUsername(username);
                        resolved.put("resolved", user);
                    }
                });
        return resolved.get("resolved");
    }
}