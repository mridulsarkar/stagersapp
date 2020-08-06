package com.poc.test;

public class TestException extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public TestException(final String message) {
        super(message);
    }
    
    public TestException(final String message, final Throwable cause) {
        super(message, cause);
    }
}