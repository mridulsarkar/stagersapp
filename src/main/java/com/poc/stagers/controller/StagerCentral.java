package com.poc.stagers.controller;

import com.poc.test.TestPair;
import com.poc.test.TestLinkManager;
import com.poc.test.TestLink;
import com.poc.test.TestUnit;
import com.poc.test.TestCategory;
import com.poc.test.TestContext;

import java.util.List;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

public class StagerCentral
{
    public static String Name = StagerCentral.class.getName();
    private TestContext testContext;
    private TestCategory currentCategory;
    private TestUnit testUnit;
    private TestLink testLinkHolder;
    private HttpServletRequest requestContext;
    private HttpSession session;
    
    public StagerCentral() {
        this.init(true);
    }
    
    public StagerCentral(HttpServletRequest requestContext) {
        this.requestContext = requestContext;
        this.session = requestContext.getSession();
        if (null == this.session) {
            this.session = this.requestContext.getSession(true);
        }
        this.init();
    }
    
    public void init() {
        this.init(false);
    }
    
    public void init(boolean forceInit) {
        // If there is a valid http session?
        // During app startup there will be no valid http session though,
        // so this check is mandatory
        if (null != session) {
            testContext = (TestContext)session.getAttribute(TestContext.Name);
        }

        if (null == testContext || forceInit) {
            // First check in the Http request context else
            // fall back on the Test Context static map
            if (null == testContext) {
                testContext = TestContext.getSavedTestContext(requestContext);
            }
            // Create a new Test Context
            if (null == testContext) {
                testContext = new TestContext();
            } else {
                // Remove Test Context from static map
                TestContext.removeSavedTestContext(requestContext);
            }
            // Set in the session, if there is one
            // During app startup there will be no valid session though
            // so this check is mandatory
            if (null != session) {
                TestContext.setTestContext(session, testContext);
            }
        }
    }
    
    public HttpServletRequest getRequestContext() {
        return requestContext;
    }
    
    public void setRequestContext(HttpServletRequest requestContext) {
        this.requestContext = requestContext;
    }
    
    public HttpSession getSession() {
        return session;
    }
    
    public void setSession(HttpSession session) {
        this.session = session;
    }
    
    public static Object getAttributeFromSession(HttpSession session, String name) {
        return session.getAttribute(name);
    }
    
    public static void setAttributeInSession(HttpSession session, String name, Object value) {
        session.setAttribute(name, value);
    }
    
    public List<TestCategory> testCategoryList() {
        return TestLinkManager.getInstance().getTestCategoryList();
    }
    
    public void setCurrentCategory(TestCategory category) {
        this.currentCategory = category;
    }
    
    public TestCategory getCurrentCategory() {
        return currentCategory;
    }
    
    public List<TestUnit> categoryTestUnits() {
        return currentCategory.getTestUnitList();
    }
    
    public List<TestPair> categoryTestUnitPairs() {
        return currentCategory.getTestUnitPairs();
    }
    
    public void setCurrentTestUnit(TestUnit testUnit) {
        this.testUnit = testUnit;
    }
    
    public TestUnit getCurrentTestUnit() {
        return testUnit;
    }
    
    public void setCurrentTestUnitLink(TestLink testLinkHolder) {
        this.testLinkHolder = testLinkHolder;
    }
    
    public TestLink getCurrentTestUnitLink() {
        return testLinkHolder;
    }
    
    public TestLink testUnitLink(String categoryName, String linkName) {
        List<TestCategory> testCategoryList = (List<TestCategory>)this.testCategoryList();
        TestCategory category = null;
        for (TestCategory cat : testCategoryList) {
            if (cat.getName().equalsIgnoreCase(categoryName)) {
                category = cat;
                break;
            }
        }

        List<TestUnit> testUnits = category.getTestUnitList();
        for (TestUnit testUnit : testUnits) {
            if (testUnit.hasStagers()) {
                for (TestLink stager : testUnit.stagers()) {
                    if (stager.getLinkText().equalsIgnoreCase(linkName)) {
                        return stager;
                    }
                }
            }
            if (testUnit.hasPageAccessLinks()) {
                for (TestLink pageAccessLink : testUnit.pageAccessLinks()) {
                    if (pageAccessLink.getLinkText().equalsIgnoreCase(linkName)) {
                        return pageAccessLink;
                    }
                }
            }
        }
        
        return null;
    }
    
}