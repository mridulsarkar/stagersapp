package com.poc.test;

public class TestPair
{
    private TestUnit leftUnit;
    private TestUnit rightUnit;
    
    TestPair(TestUnit leftUnit, TestUnit rightUnit) {
        this.leftUnit = leftUnit;
        this.rightUnit = rightUnit;
    }
    
    public TestUnit getLeftUnit() {
        return leftUnit;
    }
    
    public TestUnit getRightUnit() {
        return rightUnit;
    }
    
    public boolean hasRightUnit() {
        return rightUnit != null;
    }
}