package com.poc.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.poc.test.annotation.TestPageLink;
import com.poc.test.annotation.TestParam;
import com.poc.test.annotation.TestStager;
import com.poc.util.ClassUtil;
import com.poc.util.CollectionsUtil;
import com.poc.util.StringsUtil;

public class TestLink
{
    private static String defaultFirstLevelCategory = "Stagers";
    private static Map<String, String> badCategoryNames = CollectionsUtil.map();
    
    Annotation annotation;
    Object annotatedItem;
    String type;
    String displayName;
    String secondaryName;
    String linkText;
    String firstLevelCategory;
    String secondLevelCategory;
    boolean hidden = false;
    Boolean requiresParams = null;

    static {
        badCategoryNames.put("dummy","dummy");
        badCategoryNames.put("test","test");
    }
    
    public TestLink() {
        hidden = true;
    }
    
    public TestLink(Annotation annotation, Object annotatedItem) {
        init(annotation, annotatedItem, null);
    }
    
    public TestLink(Annotation annotation, Object annotatedItem, String type) {
        init(annotation, annotatedItem, type);
    }
    
    private void init(Annotation annotation, Object annotatedItem, String type) {
        this.annotation = annotation;
        this.annotatedItem = annotatedItem;
        this.type = type;
        
        displayName = computeDisplayName();
        linkText = StringsUtil.deCamelize(displayName);
        secondaryName = computeSecondaryName();

        if (!StringsUtil.nullOrEmptyOrBlankString(type)) {
            firstLevelCategory = categoryForClassName(type, TestLink.defaultFirstLevelCategory);
            secondLevelCategory = type;
        } else if (annotatedItem.getClass() == Method.class) {
            Method m = (Method)annotatedItem;
            firstLevelCategory = categoryForClassName(
                                        m.getDeclaringClass().getName(), TestLink.defaultFirstLevelCategory);
            secondLevelCategory = m.getDeclaringClass().getName();
        } else if (annotatedItem.getClass() == Class.class) {
            firstLevelCategory = categoryForClassName(
                                        ((Class<?>)annotatedItem).getName(), TestLink.defaultFirstLevelCategory);
            secondLevelCategory = ((Class<?>)annotatedItem).getName();
        }
    }
    
    public Annotation getAnnotation() {
        return annotation;
    }
    
    public Object getAnnotatedItem() {
        return annotatedItem;
    }
    
    public String getType() {
        return type;
    }
    
    private String computeSecondaryName() {
        String name = null;
        if (StringsUtil.nullOrEmptyOrBlankString(type)) {
            if (annotatedItem.getClass() == Method.class) {
                Method m = (Method)annotatedItem;
                name = m.getDeclaringClass().getName();
            }
        } else {
            name = type;
        }
        String description = getDescription();
        if (!StringsUtil.nullOrEmptyOrBlankString(description)) {
            name = name + ": " + description;
        }
        return name;
    }
    
    private String computeDisplayName() {
        String displayName = null;
        displayName = TestAnnotationUtil.getAnnotationName(annotation);
        if (StringsUtil.nullOrEmptyOrBlankString(displayName)) {
            if (annotatedItem.getClass() == Method.class) {
                Method m = (Method)annotatedItem;
                displayName = m.getName();
            } else if (annotatedItem.getClass() == Class.class) {
                displayName = ClassUtil.stripPackageFromClassName(((Class<?>)annotatedItem).getName());
            }
        }
        return displayName;
    }
    
    public String getSecondLevelCategoryName() {
        return secondLevelCategory;
    }
    
    public String getFirstLevelCategoryName() {
        return firstLevelCategory;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getLinkText() {
        return linkText;
    }
    
    public String getSecondaryName() {
        return secondaryName;
    }
    
    private static String categoryForClassName(String className, String defaultValue) {
        String categoryName = null;
        String[] strings = className.split("\\.");
        for (String name : strings) {
            if (badCategoryNames.get(name) == null) {
                categoryName = name;
                break;
            }
        }
        if (StringsUtil.nullOrEmptyOrBlankString(categoryName)) {
            categoryName = defaultValue;
        }
        return categoryName;
    }
    
    public boolean isTestLink() {
        return TestPageLink.class.isAssignableFrom(annotation.annotationType());
    }
    
    public boolean isTestStager() {
        return TestStager.class.isAssignableFrom(annotation.annotationType());
    }
    
    public boolean isTestLinkParam() {
        return TestParam.class.isAssignableFrom(annotation.annotationType());
    }
    
    public String getDescription() {
        return TestAnnotationUtil.getDescription(annotation);
    }
    
    public boolean isActive(HttpServletRequest requestContext) {
        TestContext testContext = TestContext.getTestContext(requestContext);
        boolean active = true;
        if (requiresParam() && annotatedItem.getClass() == Method.class) {
            Method method = (Method)annotatedItem;
            if (useAnnotationType(requestContext, method)) {
                Class<?> typeOnAnnotation = (Class<?>)ClassUtil.classForName(type);
                if (testContext.get((Class<?>)typeOnAnnotation) == null) {
                    active = false;
                }
            } else {
                active = canInvokeMethod(method, testContext);
            }
        }
        if (active) {
            active = checkTestLinkParams(testContext);
        }
        return active;
    }
    
    public List<Class<?>> getRequiredContextItems(HttpServletRequest requestContext) {
        List<Class<?>> classesRequired = CollectionsUtil.list();
        if (requiresParam() && annotatedItem.getClass() == Method.class) {
            Method method = (Method)annotatedItem;
            Class<?>[] methodTypes = method.getParameterTypes();
            for (int i = 0; i < methodTypes.length; ++i) {
                classesRequired.add(methodTypes[i]);
            }
            if (useAnnotationType(requestContext, method)) {
                Class<?> typeOnAnnotation = (Class<?>)ClassUtil.classForName(type);
                classesRequired.add(typeOnAnnotation);
            }
        }
        return classesRequired;
    }
    
    public boolean hasDynamicArgument() {
        return (null != getStagerDynamicArgumentClass());
    }
    
    public Class<?> getStagerDynamicArgumentClass() {
        Class<?> dynamicClass = null;
        if (annotatedItem.getClass() == Method.class) {
            Method method = (Method)annotatedItem;
            Class<?>[] types = method.getParameterTypes();
            for (Class<?> type : types) {
                if (StagerArgs.class.isAssignableFrom(type)) {
                    dynamicClass = type;
                    break;
                }
            }
        }
        return dynamicClass;
    }
    
    private boolean canInvokeMethod(Method method, TestContext testContext) {
        boolean canInvoke = true;
        Class<?>[] methodTypes = method.getParameterTypes();
        for (int i = 0; i < methodTypes.length; ++i) {
            if (!isInternalParameter((Class<?>)methodTypes[i]) && 
                            testContext.get((Class<?>)methodTypes[i]) == null) {
                canInvoke = false;
                break;
            }
        }
        return canInvoke;
    }
    
    protected static boolean isInternalParameter(Class<?> parameter) {
        if (parameter == HttpServletRequest.class ||
                    parameter == TestContext.class ||
                        StagerArgs.class.isAssignableFrom(parameter)) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean checkTestLinkParams(TestContext testContext) {
        boolean canInvoke = true;
        Class<?> testClass;
        if (annotatedItem.getClass() == Method.class) {
            Method m = (Method)annotatedItem;
            testClass = m.getDeclaringClass();
        }
        else {
            testClass = annotatedItem.getClass();
        }
        Map<Annotation, AnnotatedElement> annotations = (Map<Annotation, AnnotatedElement>)
                                    TestLinkManager.getInstance().annotationsForClass(testClass.getName());
        for (Object key : annotations.keySet()) {
            Annotation annotation = (Annotation)key;
            if (TestParam.class.isAssignableFrom(annotation.annotationType())) {
                Object ref = annotations.get(key);
                if (ref.getClass() == Method.class && !canInvokeMethod((Method)ref, testContext)) {
                    canInvoke = false;
                    break;
                }
                continue;
            }
        }
        return canInvoke;
    }
    
    public boolean isHidden() {
        return hidden;
    }
    
    public boolean isInteractive() {
        boolean isInteractive = false;
        if (annotatedItem.getClass() == Method.class) {
            Method m = (Method)annotatedItem;
            Annotation hasPageLink = m.getDeclaredAnnotation(TestPageLink.class);
            if (null != hasPageLink) {
                isInteractive = true;
            }
        }
        return isInteractive;
    }
    
    public boolean requiresParam() {
        if (requiresParams == null) {
            boolean requiresParam = false;
            if (annotatedItem.getClass() == Method.class) {
                Method method = (Method)annotatedItem;
                Class<?>[] methodTypes = method.getParameterTypes();
                requiresParam = !allInternalParameters(methodTypes);
            }
            requiresParams = requiresParam;
        }
        return requiresParams;
    }
    
    private boolean allInternalParameters(Class<?>[] params) {
        boolean allInternal = true;
        for (Class<?> c : params) {
            if (!isInternalParameter((Class<?>)c)) {
                allInternal = false;
            }
        }
        return allInternal;
    }
    
    protected static Class<?>[] filterInternalParameters(Class<?>[] parameters) {
        List<Class<?>> list = new ArrayList<Class<?>>();
        for (Class<?> param : parameters) {
            if (!isInternalParameter((Class<?>)param)) {
                list.add(param);
            }
        }
        return list.toArray(new Class[list.size()]);
    }
    
    private boolean useAnnotationType(HttpServletRequest requestContext, Method method) {
        boolean useAnnotationType = false;
        TestContext testContext = TestContext.getTestContext(requestContext);
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (type != null && parameterTypes.length == 1) {
            Class<?> typeOnAnnotation = (Class<?>)ClassUtil.classForName(type);
            Class<?> methodType = parameterTypes[0];
            if (methodType != null && typeOnAnnotation != null && 
                        methodType.isAssignableFrom(typeOnAnnotation) ) {
                if (testContext.get(typeOnAnnotation) != null) {
                    useAnnotationType = true;
                }
            }
        }
        return useAnnotationType;
    }
    
    public Object click(HttpServletRequest requestContext, Object returnPage) {
        Object page = null;
        if (isTestLink()) {
            page = testLinkClick(requestContext);
        } else if (isTestStager()) {
            page = testStagerClick(requestContext, returnPage);
        }
        return page;
    }
    
    private Object testStagerClick(HttpServletRequest requestContext, Object returnPage) {
        Object page = null;
        if (annotatedItem.getClass() == Method.class) {
            Method method = (Method)annotatedItem;

            TestLinkCallback testLinkClickCallback =
                            TestLinkManager.getInstance().getTestLinkClickCallback();
            if (testLinkClickCallback != null) {
                Object nextPage = testLinkClickCallback.click(
                                                requestContext, this, returnPage);
                if (nextPage != null) {
                    page = nextPage;
                }
            } else {
                page = testStagerClick(requestContext, method);
            }
        }
        
        return page;
    }
    
    public static Object testStagerClick(HttpServletRequest requestContext, Method method) {
        TestContext testContext = TestContext.getTestContext(requestContext);
        Object obj = TestAnnotationUtil.getObjectToInvoke(requestContext, method);
        Object res = TestAnnotationUtil.invokeMethod(requestContext, testContext, method, obj);
        if (res != null) {
            testContext.put(res);
        }
        return res;
    }
    
    private Object testLinkClick(HttpServletRequest requestContext) {
        Object page = null;

        TestContext testContext = TestContext.getTestContext(requestContext);

        if (annotatedItem.getClass() == Method.class) {
            Method method = (Method)annotatedItem;
            Object object = TestAnnotationUtil.getObjectToInvoke(requestContext, method);
            try {
                if (useAnnotationType(requestContext, method)) {
                    Class<?> typeOnAnnotation = (Class<?>)ClassUtil.classForName(type);
                    Object[] args = { testContext.get((Class<?>)typeOnAnnotation) };
                    page = (Object)TestAnnotationUtil.invokeMethod(method, object, args);
                } else {
                    page = (Object)TestAnnotationUtil.invokeMethod(requestContext, testContext, method, object);
                }
            } finally {
                
            }
        }
        return page;
    }
    
}