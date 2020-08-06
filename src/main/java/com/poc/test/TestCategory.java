package com.poc.test;

import java.util.Comparator;
import java.util.Collections;
import java.util.List;

import com.poc.util.CollectionsUtil;

public class TestCategory
{
    private String name;
    private List<TestUnit> testUnitList= CollectionsUtil.list();
    private List<TestPair> testUnitPairs = null;
    
    public TestCategory(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void add(TestUnit testUnit) {
        testUnitList.add(testUnit);
        testUnitPairs = null;
    }
    
    public List<TestUnit> getTestUnitList() {
        return testUnitList;
    }
    
    public List<TestPair> getTestUnitPairs() {
        if (testUnitPairs == null) {
            testUnitPairs = CollectionsUtil.list();
            for (int i = 0; i < testUnitList.size(); i += 2) {
                TestUnit leftUnit  = testUnitList.get(i);
                TestUnit rightUnit = null;
                if (i + 1 < testUnitList.size()) {
                    rightUnit = testUnitList.get(i + 1);
                }
                TestPair pair = new TestPair(leftUnit, rightUnit);
                testUnitPairs.add(pair);
            }
        }
        return testUnitPairs;
    }
    
    public void sort ()
    {
        Collections.sort(testUnitList,
                new Comparator() {
                    public int compare (Object object1, Object object2) {
                        TestUnit c1 = (TestUnit)object1;
                        TestUnit c2 = (TestUnit)object2;
                        return c1.getMainName().toLowerCase().compareTo(
                                c2.getMainName().toLowerCase());
                    }
                    public boolean equals (Object o1, Object o2) {
                        return compare(o1, o2) == 0;
                    }
                });
        for (TestUnit testUnit : testUnitList) {
            testUnit.sort();
        }
        testUnitPairs = null;
    }
}