package com.poc.test;

import java.util.Base64;
import java.util.Set;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import com.poc.util.CollectionsUtil;
import java.util.Map;

public class TestContext
{
    public static final String Name = TestContext.class.getName();
    public static final String ID = "testContextId";

    private Map<String, Object> internalContext = CollectionsUtil.map();
    private Map<Object, Object> context = CollectionsUtil.map();
    private String username;
    private String id;
    private String[] returnUrl;
    private static Map<String, TestContext> savedTestContext;

    static {
        TestContext.savedTestContext = CollectionsUtil.map();
    }
    
    public TestContext() {
        id = String.valueOf(System.currentTimeMillis());
    }
    
    public String getId() {
        return id;
    }
    
    public static TestContext getSavedTestContext(HttpServletRequest requestContext) {
        TestContext testContext = null;
        String testContextId = null;
        if (null != requestContext) {
            testContextId = requestContext.getParameter(TestContext.ID);
        }
        if (testContextId != null) {
            testContext = TestContext.savedTestContext.get(testContextId);
        }
        return testContext;
    }
    
    public static void removeSavedTestContext(HttpServletRequest requestContext) {
        String testContextId = null;
        if (null != requestContext) {
            testContextId = requestContext.getParameter(TestContext.ID);
            TestContext.savedTestContext.remove(testContextId);
        }
    }
    
    public void saveTestContext() {
        TestContext.savedTestContext.put(id, this);
    }
    
    public void addInternalParam(String key, Object value) {
        if (value != null) {
            internalContext.put(key, value);
        }
    }
    
    public Object getInternalParam(String key) {
        return internalContext.get(key);
    }
    
    public static TestContext getTestContext(HttpServletRequest requestContext) {
        return getTestContext(requestContext.getSession());
    }
    
    public static TestContext getTestContext(HttpSession session) {
        return (session != null) ? ((TestContext)session.getAttribute(TestContext.Name)) : null;
    }
    
    public static void setTestContext(HttpSession session, TestContext testcontext) {
        session.setAttribute(TestContext.Name, (Object)testcontext);
    }
    
    public Set<?> keys() {
        return context.keySet();
    }
    
    public Object get(Class<?> type) {
        return context.get(type);
    }
    
    public void put(Object object) {
        put(object.getClass(), object);
    }
    
    public void put(Object key, Object value) {
        context.put(key, value);
    }
    
    public static void put(HttpServletRequest requestContext, Object value) {
        TestContext tc = getTestContext(requestContext);
        if (tc != null && value != null) {
            tc.put(value);
        }
    }
    
    public static void put(HttpServletRequest requestContext, String key, Object value) {
        TestContext tc = getTestContext(requestContext);
        if (tc != null && key != null && value != null) {
            tc.put((Object)key, value);
        }
    }
    
    public static void put(HttpSession session, Object value) {
        TestContext tc = getTestContext(session);
        if (tc != null && value != null) {
            tc.put(value);
        }
    }
    
    public static void put(HttpSession session, String key, Object value) {
        TestContext tc = getTestContext(session);
        if (tc != null && key != null && value != null) {
            tc.put((Object)key, value);
        }
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void clear() {
        context = CollectionsUtil.map();
    }
    
    public static String getEncodedReturnUrl(String displayName, String url) {
        StringBuilder buf = new StringBuilder();
        buf.append(displayName);
        buf.append(';');
        buf.append(url);
        return new String(Base64.getEncoder().encode(buf.toString().getBytes()));
    }
    
    public String getEncodedReturnUrl() {
        return getEncodedReturnUrl(getReturnUrlName(), getReturnUrl());
    }
    
    public String getReturnUrlName() {
        return (returnUrl != null) ? returnUrl[0] : null;
    }
    
    public String getReturnUrl() {
        return (returnUrl != null) ? returnUrl[1] : null;
    }
    
}