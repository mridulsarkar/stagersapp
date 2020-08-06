package com.poc.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.poc.test.annotation.TestPageLink;
import com.poc.test.annotation.TestParam;
import com.poc.test.annotation.TestStager;

public class TestAnnotationUtil
{
    public static String getAnnotationTypeList(Annotation annotation) {
        String typeList = null;
        if (TestPageLink.class.isAssignableFrom(annotation.annotationType())) {
            TestPageLink testLink = (TestPageLink)annotation;
            typeList = testLink.typeList();
        } else if (TestStager.class.isAssignableFrom(annotation.annotationType())) {
            TestStager testLink2 = (TestStager)annotation;
            typeList = testLink2.typeList();
        }
        return typeList;
    }
    
    public static String getAnnotationSuperType(Annotation annotation) {
        String superType = null;
        if (TestPageLink.class.isAssignableFrom(annotation.annotationType())) {
            TestPageLink testLink = (TestPageLink)annotation;
            superType = testLink.superType();
        } else if (TestStager.class.isAssignableFrom(annotation.annotationType())) {
            TestStager testLink2 = (TestStager)annotation;
            superType = testLink2.superType();
        }
        return superType;
    }
    
    public static String getAnnotationName(Annotation annotation) {
        String name = null;
        if (TestPageLink.class.isAssignableFrom(annotation.annotationType())) {
            TestPageLink testLink = (TestPageLink)annotation;
            name = testLink.name();
        }
        else if (TestStager.class.isAssignableFrom(annotation.annotationType())) {
            TestStager testLink2 = (TestStager)annotation;
            name = testLink2.name();
        }
        return name;
    }
    
    public static String getDescription(Annotation annotation) {
        String name = null;
        if (TestPageLink.class.isAssignableFrom(annotation.annotationType())) {
            TestPageLink testLink = (TestPageLink)annotation;
            name = testLink.description();
        } else if (TestStager.class.isAssignableFrom(annotation.annotationType())) {
            TestStager testLink2 = (TestStager)annotation;
            name = testLink2.description();
        }
        return name;
    }
    
    public static Object getObjectToInvoke( HttpServletRequest requestContext, 
                                            Method method) {
        Object obj = null;
        if (!Modifier.isStatic(method.getModifiers())) {
            obj = createObject(requestContext, method.getDeclaringClass());
        }
        return obj;
    }
    
    public static Object invokeMethod(  HttpServletRequest requestContext, 
                                        TestContext testContext,  
                                        Method method, 
                                        Object onObject) {
        return invokeMethod(requestContext, testContext, method, onObject, null);
    }
    
    public static Object invokeMethod(  HttpServletRequest requestContext, 
                                        TestContext testContext, 
                                        Method method, 
                                        Object onObject, 
                                        Object objectForValidation) {
        Class<?>[] methodTypes = method.getParameterTypes();
        Object[] args = new Object[methodTypes.length];
        for (int i = 0; i < methodTypes.length; ++i) {
            if (HttpServletRequest.class.equals(methodTypes[i])) {
                args[i] = requestContext;
            } else if (TestContext.class.equals(methodTypes[i])) {
                args[i] = testContext;
            } else if (objectForValidation != null && 
                            methodTypes[i].isInstance(objectForValidation)) {
                args[i] = objectForValidation;
            } else {
                args[i] = testContext.get((Class<?>)methodTypes[i]);
            }
        }
        return invokeMethod(method, onObject, args);
    }
    
    public static Object invokeMethod(  Method method, 
                                        Object onObject, 
                                        Object[] args) {
        try {
            return method.invoke(onObject, args);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static Object createObject(HttpServletRequest requestContext, Class<?> clazz) {
        try {
            final Object obj = clazz.newInstance();
            return obj;
        } catch (Exception exp) {
            return null;
        }
    }
    
    public static void initializePageTestParams(HttpServletRequest requestContext, Object page) {
        TestContext testContext = TestContext.getTestContext(requestContext);
        Map<Annotation, AnnotatedElement> annotations = (Map<Annotation, AnnotatedElement>)
                                    TestLinkManager.getInstance().annotationsForClass(page.getClass().getName());
        Set<Annotation> keys = annotations.keySet();
        for (Annotation key : keys) {
            if (TestParam.class.isAssignableFrom(key.annotationType())) {
                final Object ref = annotations.get(key);
                if (ref.getClass() != Method.class) {
                    continue;
                }
                invokeMethod(requestContext, testContext, (Method)ref, page);
            }
        }
    }
}