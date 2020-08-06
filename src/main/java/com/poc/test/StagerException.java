package com.poc.test;

public class StagerException extends TestException
{
    private static final long serialVersionUID = 1L;
    
    public StagerException(final String message) {
        super(message);
    }
    
    public StagerException(final Throwable cause) {
        super((cause != null) ? cause.getMessage() : "", cause);
    }
    
    public StagerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}