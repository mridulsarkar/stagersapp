package com.poc.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.FIELD,ElementType.METHOD})
@Inherited
public @interface TestStager
{
    /**
     * This annotation should be used to provide Name of the Test Stager
     */
    public String name() default "";

    /**
     * This annotation should be used to provide Super Type of the Test Stager
     */
    public String superType() default "";

    /**
     * This annotation should be used to provide Category of the Test Stager
     */
    public String typeList () default "";

    /**
     * This annotation should be used to provide Description of the Test Stager
     */
    public String description () default "";
}