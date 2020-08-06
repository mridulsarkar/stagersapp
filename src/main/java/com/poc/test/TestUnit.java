package com.poc.test;

import java.util.Comparator;
import java.util.Collections;
import java.util.List;

import com.poc.util.ClassUtil;
import com.poc.util.CollectionsUtil;

public class TestUnit
{
    private static TestLinkComparator comparator = new TestLinkComparator();

    private String name;
    private String mainName;
    private String secondName;
    private List<TestLink> uiWithParamLinks = CollectionsUtil.list();
    private List<TestLink> uiNoParamLinks = CollectionsUtil.list();
    private List<TestLink> noUIWithParamLinks = CollectionsUtil.list();
    private List<TestLink> noUiNoParamLinks = CollectionsUtil.list();

    private List<TestLink> stagers = CollectionsUtil.list();
    private List<TestLink> pageAccessLinks = CollectionsUtil.list();
    private boolean displayTestContextValue = false;
    
    public TestUnit(String name, List<TestLink> links) {
        this.name = name;
        this.mainName = ClassUtil.stripPackageFromClassName(name);
        this.secondName = ClassUtil.stripClassFromClassName(name);
        
        for (TestLink link : links) {
            if (link.isInteractive()) {
                if (link.requiresParam()) {
                    uiWithParamLinks.add(link);
                } else {
                    uiNoParamLinks.add(link);
                }
                pageAccessLinks.add(link);
            } else {
                if (link.requiresParam()) {
                    noUIWithParamLinks.add(link);
                } else {
                    noUiNoParamLinks.add(link);
                }
                if (!link.isTestStager()) {
                    continue;
                }
                stagers.add(link);
            }
        }
        if (links.size() > 0) {
            String type = links.get(0).getType();
            if (type != null && type.equals(name)) {
                displayTestContextValue = true;
            }
        }
    }
    
    public boolean displayTestContextValue() {
        return displayTestContextValue;
    }
    
    public String getFullName() {
        return name;
    }
    
    public String getMainName() {
        return mainName;
    }
    
    public String getSecondaryName() {
        return secondName;
    }
    
    public List<TestLink> uiParamLinks() {
        return uiWithParamLinks;
    }
    
    public List<TestLink> uiNoParamLinks() {
        return uiNoParamLinks;
    }
    
    public List<TestLink> noUiParamLinks() {
        return noUIWithParamLinks;
    }
    
    public List<TestLink> noUiNoParamLinks() {
        return noUiNoParamLinks;
    }
    
    public List<TestLink> stagers() {
        return stagers;
    }
    
    public List<TestLink> pageAccessLinks() {
        return pageAccessLinks;
    }
    
    public boolean hasUiParamLinks() {
        return uiWithParamLinks.size() > 0;
    }
    
    public boolean hasUiNoParamLinks() {
        return uiNoParamLinks.size() > 0;
    }
    
    public boolean hasNoUiParamLinks() {
        return noUIWithParamLinks.size() > 0;
    }
    
    public boolean hasNoUiNoParamLinks() {
        return noUiNoParamLinks.size() > 0;
    }
    
    public boolean hasStagers() {
        return stagers.size() > 0;
    }
    
    public boolean hasPageAccessLinks() {
        return pageAccessLinks.size() > 0;
    }
    
    public void sort() {
        sort(uiWithParamLinks);
        sort(uiNoParamLinks);
        sort(noUIWithParamLinks);
        sort(noUiNoParamLinks);
        sort(pageAccessLinks);
        sort(stagers);
    }
    
    private static void sort(List<TestLink> list) {
        Collections.sort(list, TestUnit.comparator);
    }

    private static class TestLinkComparator implements Comparator<TestLink> {
        public int compare (TestLink t1, TestLink t2) {
            return t1.getDisplayName().toLowerCase().compareTo(
                                t2.getDisplayName().toLowerCase());
        }
    }
}