package com.poc.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface TestParam {

    /**
     * This annotation should be used to indicate a required parameter
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD,ElementType.METHOD})
    @Inherited
    public @interface Required {}

    /**
     * This annotation should be used to specify a control for a valid value for a parameter
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD,ElementType.METHOD})
    @Inherited
    public @interface Valid
    {
        public abstract String value();
    }

    /**
     * This annotation should be used to add a set of properties to a field
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE,ElementType.FIELD,ElementType.METHOD})
    @Inherited
    public @interface Properties
    {
        public abstract String value();
    }

}