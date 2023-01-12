package com.poc.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.AnnotatedElement;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.scannotation.ClasspathUrlFinder;
import org.scannotation.archiveiterator.DirectoryIteratorFactory;
import org.scannotation.archiveiterator.Filter;
import org.scannotation.archiveiterator.JarIterator;
import org.scannotation.archiveiterator.StreamIterator;
import org.scannotation.WarUrlFinder;

import javax.servlet.ServletContextEvent;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.Attribute;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.FieldVisitor;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;


public class JarScanner
{
    private static final ConcurrentHashMap<String, DirectoryIteratorFactory> registry = 
                                        new ConcurrentHashMap<String, DirectoryIteratorFactory>();
    static Map<String, List<AnnotationListener>> annotationListeners = 
                                        CollectionsUtil.map();
    
    public static URL[] findWebInfLibClasspaths(ServletContextEvent servletContextEvent) {
        return WarUrlFinder.findWebInfLibClasspaths(servletContextEvent);
    }

    public static URL[] findClassPaths() {
        return ClasspathUrlFinder.findClassPaths();
    }

    public static StreamIterator create(URL url, Filter filter) throws IOException {
        String urlString = url.toString();
        if (urlString.endsWith("!/")) {
            urlString = urlString.substring(4);
            urlString = urlString.substring(0, urlString.length() - 2);
            url = new URL(urlString);
        }

        if (!urlString.endsWith("/")) {
            return new JarIterator(new File(url.getPath()), filter); 
        } else {
            DirectoryIteratorFactory factory = registry.get(url.getProtocol());
            if (factory == null)
                throw new IOException("Unable to scan directory of protocol: " + url.toString() + " " + url.getProtocol());
            return factory.create(url, filter);
        }
    }

    public interface UrlFilter {
        boolean accepts(URL url);
    }

    public interface AnnotationListener {
        void annotationDiscovered (String className, String annotationType);
    }

    public static Map<Annotation, AnnotatedElement> annotationsForClasses (Collection<String> classNames, Class<?>[] types) {
        Map<Annotation, AnnotatedElement> annotationMap = new IdentityHashMap<Annotation, AnnotatedElement>();
        for (String className : classNames) {
            annotationsForClassName(className, types, annotationMap);
        }
        return annotationMap;
    }
    
    /**
     * Lookup all annotations on the given class.  Should be called late/lazily
     * to avoid class instantiation until absolutely necessary
     * @param className
     * @param types
     * @return
     */
    public static Map<Annotation, AnnotatedElement> annotationsForClassName (String className, Class<?>[] types) {
        Map<Annotation, AnnotatedElement> annotationMap = new IdentityHashMap<Annotation, AnnotatedElement>();
        return annotationsForClassName(className, types, annotationMap);
    }
    
    public static Map<Annotation, AnnotatedElement> annotationsForClassName(String className, 
                                Class<?>[] types, Map<Annotation, AnnotatedElement> annotationMap) {
        Class<?> cls = ClassUtil.classForName(className);
        if (cls == null) {
            System.err.println("Unable to find class: " + className);
            return annotationMap;
        }
        for (Method method : cls.getDeclaredMethods()) {
            addToAnnotationMap(annotationMap, method.getAnnotations(), (AnnotatedElement)method, (Class[])types);
        }
        for (Field field : cls.getDeclaredFields()) {
            addToAnnotationMap(annotationMap, field.getAnnotations(), (AnnotatedElement)field, (Class[])types);
        }
        addToAnnotationMap(annotationMap, cls.getAnnotations(), (AnnotatedElement)cls, (Class[])types);
        return annotationMap;
    }
    
    static void addToAnnotationMap(Map<Annotation, AnnotatedElement> map, Annotation[] annotations, 
                                AnnotatedElement ref, Class<?>[] types) {
        if (annotations == null) {
            return;
        }
        for (Annotation annotation : annotations) {
            for (Class<?> type : types) {
                if (type.isAssignableFrom(annotation.annotationType())) {
                    map.put(annotation, ref);
                    break;
                }
            }
        }
    }
    
    public static void registerAnnotationListener(Class<?> annotationClass, 
                                                    AnnotationListener listener) {
        String key = "L" + annotationClass.getCanonicalName().replace(".", "/") + ";";
        List<AnnotationListener> listeners = annotationListeners.get(key);
        if (listeners == null) {
            listeners = new ArrayList<AnnotationListener>();
            annotationListeners.put(key, listeners);
        }
        listeners.add(listener);
    }
    
    static void notifyAnnotationListeners(String className, String annotationType) {
        List<AnnotationListener> listeners = annotationListeners.get(annotationType);
        if (listeners != null) {
            for (AnnotationListener l : listeners) {
                l.annotationDiscovered(className, annotationType);
            }
        }
    }
    
    static void processBytecode(StreamIterator iter, String filename) {
        if (annotationListeners == null) {
            return;
        }
        visitBytecode(iter.next(), new JarScanner.ClassAnnotationScanner());
    }
    
    static void visitBytecode(InputStream inputStream, 
                                    ClassAnnotationScanner visitor) {
        try {
            if ( null != inputStream ) {
                ClassReader cr = new ClassReader(inputStream);
                cr.accept(visitor, 0);
            }
        } catch (IOException ex) {}
    }
    
    public static void scanClasses(Filter jarfilter, Filter jarEntryFilter, 
                                        ClassAnnotationScanner visitor) throws IOException {
        URL[] urls = JarScanner.findClassPaths();
        for (int i = 0; i < urls.length; ++i) {
            URL jarURL = urls[i];
            if (jarfilter.accepts(jarURL.toString())) {
                StreamIterator iter = JarScanner.create(jarURL, jarEntryFilter);
                InputStream inputStream = iter.next();
                while (null != inputStream) {
                    visitBytecode(inputStream, visitor);
                    inputStream = iter.next();
                }
            }
        }
    }

    public static class ClassAnnotationScanner extends ClassVisitor {
        private String classNamePath;
        private AnnotationVisitorScanner annotationScanner;
        private FieldAnnotationScanner fieldScanner;
        private MethodAnnotationScanner methodScanner;

        public ClassAnnotationScanner() {
            super(Opcodes.ASM8);
            annotationScanner = new AnnotationVisitorScanner();
            fieldScanner = new FieldAnnotationScanner();
            methodScanner = new MethodAnnotationScanner();
        }

        class AnnotationVisitorScanner extends AnnotationVisitor {
            public AnnotationVisitorScanner() {
                super(Opcodes.ASM8);
            }

            public void visit(String name, Object value) {
                System.out.println("annotation: " + name + " = " + value);
                super.visit(name, value);
            }

            public AnnotationVisitor visitArray(String name) {
                return this;
            }
        }

        class FieldAnnotationScanner extends FieldVisitor {
            public FieldAnnotationScanner() {
                super(Opcodes.ASM8);
            }

            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                return annotationScanner;
            }
        }

        class MethodAnnotationScanner extends MethodVisitor {
            public MethodAnnotationScanner() {
                super(Opcodes.ASM8);
            }

            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                return annotationScanner;
            }

            public AnnotationVisitor visitAnnotationDefault() {
                return annotationScanner;
            }

            public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor,
                    boolean visible) {
                return annotationScanner;
            }
        }

        public void visit(int version, int access, String name, String signature, String superName,
                String[] interfaces) {
            this.classNamePath = name;
        }

        public void visitSource(String source, String debug) {
            super.visitSource(source, debug);
        }

        public void visitOuterClass(String owner, String name, String desc) {
            super.visitOuterClass(owner, name, desc);
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            String key = "L" + classNamePath + ";";
            String className = classNamePath.replace("/", ".");
            notifyAnnotationListeners(className, key);
            return annotationScanner;
        }

        public void visitAttribute(Attribute attribute) {
            super.visitAttribute(attribute);
        }

        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            super.visitInnerClass(name, outerName, innerName, access);
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            return fieldScanner;
        }

        public MethodVisitor visitMethod(final int access, final String name, final String desc, 
                                            final String signature, final String[] exceptions) {
            return methodScanner;
        }

        public void visitEnd() {
            super.visitEnd();
        }
    }

}