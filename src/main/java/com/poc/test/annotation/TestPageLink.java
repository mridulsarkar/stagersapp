package com.poc.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Inherited
public @interface TestPageLink {
    /**
     * This annotation should be used to specify name of the Test Page Link
     */
    String name() default "";
    
    /**
     * This annotation should be used to specify Super Type of the Test Page Link
     */
    String superType() default "";
    
    /**
     * This annotation should be used to specify Category of the Test Page Link
     */
    String typeList() default "";
    
    /**
     * This annotation should be used to provide Description of the Test Page Link
     */
    String description() default "";
    
}