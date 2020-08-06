package com.poc.test;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

public interface TestLinkCallback
{
    public ModelAndView click (HttpServletRequest requestContext,
                                       TestLink link,
                                       Object returnPage);
}