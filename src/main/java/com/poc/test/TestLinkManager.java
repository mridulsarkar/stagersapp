package com.poc.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.poc.test.annotation.TestPageLink;
import com.poc.test.annotation.TestParam;
import com.poc.test.annotation.TestStager;
import com.poc.util.CollectionsUtil;
import com.poc.util.JarScanner;
import com.poc.util.StringsUtil;

public class TestLinkManager {
    static final Class<?> annotationClasses[] = {   TestPageLink.class,
                                                    TestStager.class};
    static final Class<?> allTestAnnotationClasses[] = {TestPageLink.class,
                                                        TestStager.class,
                                                        TestParam.class};
    
    static TestLinkManager instance = new TestLinkManager();
    protected static Set<String> classesWithAnnotations = CollectionsUtil.set();
    protected static Map<String, Map<Annotation, AnnotatedElement>> annotationMapByClassName = CollectionsUtil.map();
    private List<TestLink> allTestLinks = CollectionsUtil.list();
    private List<TestCategory> categoryList = CollectionsUtil.list();
    private TestLinkCallback testLinkClickCallback = null;
    
    private TestLinkManager() {
        this.init();
    }
    
    private void init() {
        if (StringsUtil.isTestAutomationMode()) {
            for (Class<?> annotationClass : annotationClasses) {
                JarScanner.registerAnnotationListener(annotationClass, 
                        new JarScanner.AnnotationListener() {
                            public void annotationDiscovered(String className, String annotationType) {
                                classesWithAnnotations.add(className);
                            }
                        });
            }
        }
    }
    
    public static final TestLinkManager getInstance() {
        return instance;
    }

    public void registerTestLinkClickCallback (TestLinkCallback testLinkClickCallback) {
        this.testLinkClickCallback = testLinkClickCallback;
    }

    public TestLinkCallback getTestLinkClickCallback() {
        return testLinkClickCallback;
    }
    
    public void registerAnnotationClass(String className) {
        classesWithAnnotations.add(className);
    }
    
    public Map<Annotation, AnnotatedElement> annotationsForClass(String className) {
        Map<Annotation, AnnotatedElement> annotationMap = annotationMapByClassName.get(className);
        if (annotationMap == null) {
            annotationMap = (Map<Annotation, AnnotatedElement>)JarScanner.annotationsForClassName(className, 
                        allTestAnnotationClasses);
            annotationMapByClassName.put(className, annotationMap);
        }
        return annotationMap;
    }
    
    synchronized void initializeTestLinks() {
        if (null == allTestLinks || allTestLinks.isEmpty()) {
            buildAllLinks();
            Map<String, List<TestLink>> firstCategoryMap = 
                                        CollectionsUtil.groupBy(allTestLinks,
                                            new CollectionsUtil.ValueMapper() {
                                                public Object valueForObject (Object o) {
                                                    return ((TestLink)o).getFirstLevelCategoryName();
                                                }
                                            });
            
            Collection<String> firstLevelCategoryNames;
            firstLevelCategoryNames = firstCategoryMap.keySet();
            categoryList = CollectionsUtil.list();
            for (String key : firstLevelCategoryNames) {
                Map<String, List<TestLink>> secondCategoryMap =
                                        CollectionsUtil.groupBy(firstCategoryMap.get(key),
                                            new CollectionsUtil.ValueMapper() {
                                                public Object valueForObject (Object o) {
                                                    return ((TestLink)o).getSecondLevelCategoryName();
                                                }
                                            });
                
                TestCategory category = new TestCategory(key);
                categoryList.add(category);
                Set<String> testUnitKeys = secondCategoryMap.keySet();
                for (String testUnitKey : testUnitKeys) {
                    TestUnit testUnit = new TestUnit(testUnitKey, secondCategoryMap.get(testUnitKey));
                    category.add(testUnit);
                }
                category.sort();
            }
        }
        Collections.sort(categoryList,
            new Comparator() {
                public int compare (Object object1, Object object2) {
                    TestCategory c1 = (TestCategory)object1;
                    TestCategory c2 = (TestCategory)object2;
                    return c1.getName().compareTo(c2.getName());
                }
                public boolean equals (Object o1, Object o2) {
                    return compare(o1, o2) == 0;
                }
            });
    }
    
    public List<TestCategory> getTestCategoryList() {
        if (categoryList == null || categoryList.isEmpty()) {
            initializeTestLinks();
        }
        return categoryList;
    }
    
    private void buildAllLinks() {
        allTestLinks = CollectionsUtil.list();
        for (String className : classesWithAnnotations) {
            Map<Annotation, AnnotatedElement> annotations = annotationsForClass(className);
            Set<?> keys = annotations.keySet();
            for (Object key : keys) {
                Annotation annotation = (Annotation)key;
                if (annotation.annotationType().isAssignableFrom(TestPageLink.class) || 
                            annotation.annotationType().isAssignableFrom(TestStager.class)) {
                    Object annotationRef = annotations.get(annotation);
                    if (shouldExpandTestLink(annotation)) {
                        List<TestLink> testLinks = expandTestLink(annotation, annotationRef);
                        allTestLinks.addAll(testLinks);
                    } else {
                        allTestLinks.add(new TestLink(annotation, annotationRef));
                    }
                }
            }
        }
    }
    
    private boolean shouldExpandTestLink(Annotation annotation) {
        String typeList = TestAnnotationUtil.getAnnotationTypeList(annotation);
        String superType = TestAnnotationUtil.getAnnotationSuperType(annotation);
        return (!StringsUtil.nullOrEmptyOrBlankString(typeList) || 
                    !StringsUtil.nullOrEmptyOrBlankString(superType))
                    ? true : false;
    }

    private List<TestLink> expandTestLink (Annotation annotation, Object ref)
    {
        List<TestLink> testLinks = CollectionsUtil.list();
        String typeList = TestAnnotationUtil.getAnnotationTypeList(annotation) ;
        if (!StringsUtil.nullOrEmptyOrBlankString(typeList)) {
            String[] types = typeList.split(";");
            for (String type : types) {
                testLinks.add(new TestLink(annotation, ref, type));
            }
        }
        return testLinks;
    }
    
}