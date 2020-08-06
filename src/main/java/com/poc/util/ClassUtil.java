package com.poc.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.scannotation.archiveiterator.Filter;

public final class ClassUtil
{
    public static String NativeInteger = "int";
    public static String NativeBoolean = "boolean";
    public static String NativeDouble = "double";
    public static String NativeFloat = "float";
    public static String NativeLong = "long";
    public static String NativeByte = "byte";
    public static String NativeShort = "short";
    public static String NativeChar = "char";
    
    private ClassUtil() {
    }
    
    public static void classTouch(final String name) {
        classForName(name, (Class<?>)Object.class, false);
    }
    
    public static Class<?> classForNativeType(final String typeName) {
        if ("int".equals(typeName)) {
            return Integer.TYPE;
        }
        if (ClassUtil.NativeBoolean.equals(typeName)) {
            return Boolean.TYPE;
        }
        if (ClassUtil.NativeDouble.equals(typeName)) {
            return Double.TYPE;
        }
        if (ClassUtil.NativeFloat.equals(typeName)) {
            return Float.TYPE;
        }
        if (ClassUtil.NativeLong.equals(typeName)) {
            return Long.TYPE;
        }
        if (ClassUtil.NativeByte.equals(typeName)) {
            return Byte.TYPE;
        }
        if (ClassUtil.NativeShort.equals(typeName)) {
            return Short.TYPE;
        }
        if (ClassUtil.NativeChar.equals(typeName)) {
            return Character.TYPE;
        }
        return null;
    }
    
    public static Class<?> classForName(final String className) {
        return (Class<?>)classForName(className, (Class<?>)Object.class, true);
    }
    
    public static Class<?> classForName(final String className, final Class<?> supposedSuperclass) {
        return (Class<?>)classForName(className, (Class<?>)supposedSuperclass, true);
    }
    
    public static Class<?> classForName(final String className, final boolean warning) {
        return (Class<?>)classForName(className, (Class<?>)Object.class, warning);
    }
    
    public static Class<?> classForName(final String className, final Class<?> supposedSuperclass, final boolean warning) {
        try {
            final Class<?> classObj = Class.forName(className);
            return (Class<?>)checkInstanceOf((Class<?>)classObj, (Class<?>)supposedSuperclass, warning);
        } catch (ClassNotFoundException | NoClassDefFoundError | SecurityException exp) {
            System.err.println("classForName: " + exp.getMessage());
            return null;
        }
    }
    
    public static boolean instanceOf(final Object object, final String className) {
        return object != null && instanceOf((Class<?>)object.getClass(), classForName(className));
    }
    
    public static boolean instanceOf(final Class<?> instance, final Class<?> target) {
        return target != null && target.isAssignableFrom(instance);
    }
    
    public static String getClassNameOfObject(final Object o) {
        if (o == null) {
            return "null";
        }
        return o.getClass().getName();
    }
    
    public static Object newInstance(final String className) {
        return newInstance(className, true);
    }
    
    public static Object newInstance(final String className, final boolean warning) {
        return newInstance(classForName(className, warning));
    }
    
    public static Object newInstance(final String className, final String supposedSuperclassName) {
        return newInstance(className, supposedSuperclassName, true);
    }
    
    public static Object newInstance(final String className, final String supposedSuperclassName, final boolean warning) {
        return newInstance(className, classForName(supposedSuperclassName, warning), warning);
    }
    
    public static Object newInstance(final String className, final Class<?> supposedSuperclass, final boolean warning) {
        return newInstance(classForName(className, warning), (Class<?>)supposedSuperclass, warning);
    }
    
    public static Object newInstance(final Class<?> classObj, final Class<?> supposedSuperclass, final boolean warning) {
        if (classObj == null) {
            return null;
        }
        if (supposedSuperclass == null) {
            return null;
        }
        final Class<?> clazz = (Class<?>)checkInstanceOf((Class<?>)classObj, (Class<?>)supposedSuperclass, warning);
        if (clazz == null) {
            return null;
        }
        return newInstance((Class<?>)clazz);
    }
    
    public static Object newInstance(final Class<?> theClass) {
        if (theClass == null) {
            return null;
        }
        try {
            return theClass.newInstance();
        } catch (InstantiationException | IllegalAccessException exp) {
            System.err.println(theClass.getName() + exp.getMessage());
            return null;
        }
    }
    
    public static String stripPackageFromClassName(final String className) {
        final int pos = className.lastIndexOf(46);
        if (pos > 0) {
            return className.substring(pos + 1);
        }
        return className;
    }
    
    public static String stripClassFromClassName(final String className) {
        final int pos = className.lastIndexOf(46);
        if (pos > 0) {
            return className.substring(0, pos);
        }
        return "";
    }
    
    public static Object invokeStaticMethod(final String className, final String methodName) {
        try {
            final Class<?> c = (Class<?>)classForName(className);
            if (c != null) {
                final Method m = c.getMethod(methodName, (Class<?>[])new Class[0]);
                return m.invoke(null, new Object[0]);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | SecurityException exp) {
            System.err.println(exp.getMessage());
        }
        return null;
    }
    
    public static Object invokeStaticMethod(final String className, final String methodName, final Class<?>[] paramTypes, final Object[] args) {
        try {
            final Class<?> c = (Class<?>)classForName(className);
            if (c != null) {
                final Method m = c.getMethod(methodName, paramTypes);
                return m.invoke(null, args);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | SecurityException exp) {
            System.err.println(exp.getMessage());
        }
        return null;
    }
    
    public static Field[] getDeclaredFields(final Class<?> clazz) {
        int fieldCount = 0;
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            fieldCount += c.getDeclaredFields().length;
        }
        final Field[] fields = new Field[fieldCount];
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            final Field[] declaredFields = c.getDeclaredFields();
            final int length = declaredFields.length;
            fieldCount -= length;
            System.arraycopy(declaredFields, 0, fields, fieldCount, length);
        }
        return fields;
    }
    
    private static Class<?> checkInstanceOf(final Class<?> classObj, final Class<?> supposedSuperclass, final boolean warning) {
        if (instanceOf((Class<?>)classObj, (Class<?>)supposedSuperclass)) {
            return classObj;
        }
        if (warning) {
            System.err.println(classObj.getName() + supposedSuperclass.getName());
        }
        return null;
    }
    
    public static void scanClasses() {
        final String classPath = System.getProperty("java.class.path");
        final String pathSeparator = System.getProperty("path.separator");
        
        System.setProperty("java.class.path", 
                            classPath + pathSeparator 
                            + "/Users/mridulsarkar/.m2/repository/com/poc/stagersapp/0.0.1-SNAPSHOT/stagersapp-0.0.1-SNAPSHOT.jar");
        
        try {
            JarScanner.scanClasses(
                            new Filter() {
                                public boolean accepts(String filename) {
                                    return true;
                                }
                            },
                            // Filter to accept only Stager PoC Classes 
                            new Filter() {
                                public boolean accepts(String filename) {
                                    return filename.contains("com/poc");
                                 }
                            }, 
                            new JarScanner.ClassAnnotationScanner());
        
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Exception while scanning jars: " + e.getMessage());
        }
    }
    
}