package com.poc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.AnnotatedElement;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


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
            return new JarIterator(url, filter);
        } else {
            DirectoryIteratorFactory factory = registry.get(url.getProtocol());
            if (factory == null)
                throw new IOException("Unable to scan directory of protocol: " + url.getProtocol());
            return factory.create(url, filter);
        }
    }

    public interface StreamIterator {
        /**
         * @return true until past last item
         */
        boolean next();

        /**
         * User is resposible for closing the InputStream returned
         *
         * @return null if no more streams left to iterate on
         */
        InputStream getInputStream ();

        /**
         * @return file name (relative path)
         */
        String getFilename ();

        /**
         * @return URL for current item
         */
        String getURLString ();

        /**
         * Cleanup any open resources of the iterator
         */
        void close();
    }

    public interface Filter {
        boolean accepts(String filename);
    }

    public interface UrlFilter {
        boolean accepts(URL url);
    }

    public interface DirectoryIteratorFactory {
        StreamIterator create(URL url, Filter filter) throws IOException;
    }

    public static class InputStreamWrapper extends InputStream {
        private InputStream delegate;

        public InputStreamWrapper(InputStream delegate) {
            this.delegate = delegate;
        }

        public int read()
                throws IOException {
            return delegate.read();
        }

        public int read(byte[] bytes)
                throws IOException {
            return delegate.read(bytes);
        }

        public int read(byte[] bytes, int i, int i1)
                throws IOException {
            return delegate.read(bytes, i, i1);
        }

        public long skip(long l)
                throws IOException {
            return delegate.skip(l);
        }

        public int available()
                throws IOException {
            return delegate.available();
        }

        public void close()
                throws IOException {
            // ignored
        }

        public void mark(int i) {
            delegate.mark(i);
        }

        public void reset()
                throws IOException {
            delegate.reset();
        }

        public boolean markSupported() {
            return delegate.markSupported();
        }
    }

    public static class JarIterator implements StreamIterator {
        JarInputStream jar;
        JarEntry next;
        Filter filter;
        boolean initial = true;
        boolean closed = false;
        String urlPrefix;

        public JarIterator(URL url, Filter filter) throws IOException {
            urlPrefix = "jar:".concat(url.toExternalForm()).concat("!/");
            this.filter = filter;
            jar = new JarInputStream(url.openStream());
        }

        private void setNext() {
            initial = true;
            try {
                if (next != null) jar.closeEntry();
                next = null;
                do {
                    next = jar.getNextJarEntry();
                } while (next != null && (next.isDirectory() || (filter == null || !filter.accepts(next.getName()))));
                if (next == null) {
                    close();
                }
            } catch (IOException e) {
                throw new RuntimeException("failed to browse jar", e);
            }
        }

        public boolean next() {
            if (closed || (next == null && !initial)) return false;
            setNext();
            return (next != null);
        }

        public InputStream getInputStream () {
            return (next == null) ? null : new InputStreamWrapper(jar);
        }

        public String getFilename() {
            return next.getName();
        }

        public String getURLString() {
            return urlPrefix.concat(next.getName());
        }

        public void close() {
            try {
                closed = true;
                jar.close();
            } catch (IOException ignored) {

            }
        }
    }

    public static class FileIterator implements StreamIterator {
        private ArrayList<File> files;
        private int index = 0;

        public FileIterator(File file, Filter filter) {
            files = new ArrayList<File>();
            try {
                create(files, file, filter);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected static void create(List list, File dir, Filter filter) throws Exception {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    create(list, files[i], filter);
                } else {
                    if (filter == null || filter.accepts(files[i].getAbsolutePath())) {
                        list.add(files[i]);
                    }
                }
            }
        }

        public boolean next() {
            return (index++ < files.size());
        }

        public InputStream getInputStream () {
            if (index >= files.size()) return null;
            File fp = (File) files.get(index);
            try {
                return new FileInputStream(fp);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public String getFilename() {
            return (index >= files.size()) ? null : files.get(index).getAbsolutePath();
        }

        public String getURLString() {
            try {
                return (index >= files.size()) ? null
                        : ((File)files.get(index)).toURI().toURL().toExternalForm();
            } catch (MalformedURLException e) {
                System.err.print(e.getMessage());
                return null;
            }
        }

        public void close()
        {
        }
    }

    public static class FileProtocolIteratorFactory implements DirectoryIteratorFactory
    {

        public StreamIterator create(URL url, Filter filter) throws IOException
        {
            File f = new File(url.getPath());
            if (f.isDirectory()) {
                return new FileIterator(f, filter);
            } else {
                return new JarIterator(url, filter);
            }
        }
    }


    public static class ClasspathUrlFinder {
        /**
         * Find the classpath URLs for a specific classpath resource.  The classpath URL is extracted
         * from loader.getResources() using the baseResource.
         * @param baseResource
         */
        public static URL[] findResourceBases(String baseResource, ClassLoader loader, UrlFilter filter) {
            ArrayList<URL> list = new ArrayList<URL>();
            try {
                Enumeration<URL> urls = loader.getResources(baseResource);
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    if (filter == null || filter.accepts(url)) {
                        list.add(findResourceBase(url, baseResource));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return list.toArray(new URL[list.size()]);
        }

        /**
         * Find the classpath URLs for a specific classpath resource.  The classpath URL is extracted
         * from loader.getResources() using the baseResource.
         * @param baseResource
         */
        public static URL[] findResourceBases(String baseResource) {
            return findResourceBases(baseResource, Thread.currentThread().getContextClassLoader(), null);
        }

        public static URL[] findResourceBasesContainingManifestKey (String manifestKey) {
            final Attributes.Name Key = new Attributes.Name(manifestKey);

            return findResourceBases("META-INF/MANIFEST.MF",
                    Thread.currentThread().getContextClassLoader(),
                    new UrlFilter() {
                        public boolean accepts(URL url)
                        {
                            try {
                                return new Manifest(url.openStream()).getMainAttributes().get(Key) != null;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
        }

        public static URL findResourceBase(URL url, String baseResource) {
            String urlString = url.toString();
            int idx = urlString.lastIndexOf(baseResource);
            urlString = urlString.substring(0, idx);
            URL deployUrl = null;
            try {
                deployUrl = new URL(urlString);
            }
            catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            return deployUrl;
        }

        /**
         * Find the classpath URL for a specific classpath resource.  The classpath URL is extracted
         * from Thread.currentThread().getContextClassLoader().getResource() using the baseResource.
         * @param baseResource
         */
        public static URL findResourceBase(String baseResource) {
            return findResourceBase(baseResource, Thread.currentThread().getContextClassLoader());
        }

        /**
         * Find the classpath URL for a specific classpath resource.  The classpath URL is extracted
         * from loader.getResource() using the baseResource.
         * @param baseResource
         * @param loader
         */
        public static URL findResourceBase(String baseResource, ClassLoader loader) {
            URL url = loader.getResource(baseResource);
            return findResourceBase(url, baseResource);
        }

        /**
         * Find the classpath for the particular class
         * @param clazz
         */
        public static URL findClassBase(Class<?> clazz) {
            String resource = clazz.getName().replace('.', '/') + ".class";
            return findResourceBase(resource, clazz.getClassLoader());
        }

        /**
         * Uses the java.class.path system property to obtain a list of URLs that represent the CLASSPATH
         */
        public static URL[] findClassPaths() {
            List<URL> list = new ArrayList<URL>();
            String classpath = System.getProperty("java.class.path");
            StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);

            while (tokenizer.hasMoreTokens()) {
                String path = tokenizer.nextToken();
                File fp = new File(path);
                if (!fp.exists()) continue; // throw new RuntimeException("File in java.class.path does not exist: " + fp);
                try {
                    list.add(fp.toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            return list.toArray(new URL[list.size()]);
        }

        /**
         * Uses the java.class.path system property to obtain a list of URLs that represent the CLASSPATH
         * <p/>
         * paths is used as a filter to only include paths that have the specific relative file within it
         * @param paths list of files that should exist in a particular path
         */
        public static URL[] findClassPaths(String... paths) {
            ArrayList<URL> list = new ArrayList<URL>();

            String classpath = System.getProperty("java.class.path");
            StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
            for (int i = 0; i < paths.length; i++) {
                paths[i] = paths[i].trim();
            }

            while (tokenizer.hasMoreTokens()) {
                String path = tokenizer.nextToken().trim();
                boolean found = false;
                for (String wantedPath : paths) {
                    if (path.endsWith(File.separator + wantedPath)) {
                        found = true;
                        break;
                    }
                }
                if (!found) continue;
                File fp = new File(path);
                if (!fp.exists()) {
                    throw new RuntimeException("File in java.class.path does not exists: " + fp);
                }
                try {
                    list.add(fp.toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            return list.toArray(new URL[list.size()]);
        }
    }

    public static class WarUrlFinder {
        public static URL[] findWebInfLibClasspaths(ServletContextEvent servletContextEvent) {
            ServletContext servletContext = servletContextEvent.getServletContext();
            return findWebInfLibClasspaths(servletContext);
        }

        public static URL[] findWebInfLibClasspaths(ServletContext servletContext) {
            ArrayList<URL> list = new ArrayList<URL>();
            Set<?> libJars = servletContext.getResourcePaths("/WEB-INF/lib");
            for (Object jar : libJars) {
                try {
                    list.add(servletContext.getResource((String) jar));
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            return list.toArray(new URL[list.size()]);
        }

        public static URL findWebInfClassesPath(ServletContextEvent servletContextEvent) {
            ServletContext servletContext = servletContextEvent.getServletContext();
            return findWebInfClassesPath(servletContext);
        }

        /**
         * Find the URL pointing to "/WEB-INF/classes"  This method may not work in conjunction with IteratorFactory
         * if your servlet container does not extract the /WEB-INF/classes into a real file-based directory
         * @param servletContext
         * @return null if cannot determin /WEB-INF/classes
         */
        public static URL findWebInfClassesPath(ServletContext servletContext) {
            String path = servletContext.getRealPath("/WEB-INF/classes");
            if (path == null) return null;
            File fp = new File(path);
            if (fp.exists() == false) return null;
            try {
                return fp.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
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
            _addToMap(annotationMap, method.getAnnotations(), (AnnotatedElement)method, (Class[])types);
        }
        for (Field field : cls.getDeclaredFields()) {
            _addToMap(annotationMap, field.getAnnotations(), (AnnotatedElement)field, (Class[])types);
        }
        _addToMap(annotationMap, cls.getAnnotations(), (AnnotatedElement)cls, (Class[])types);
        return annotationMap;
    }
    
    static void _addToMap(Map<Annotation, AnnotatedElement> map, Annotation[] annotations, 
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
        visitBytecode(iter, filename, new JarScanner.ClassAnnotationScanner());
    }
    
    static void visitBytecode(StreamIterator iter, String filename, 
                                    ClassAnnotationScanner visitor) {
        try {
            InputStream is = iter.getInputStream();
            ClassReader cr = new ClassReader(is);
            cr.accept(visitor, 0);
        } catch (IOException ex) {}
    }
    
    public static void scanClasses(Filter jarfilter, Filter jarEntryFilter, 
                                        ClassAnnotationScanner visitor) throws IOException {
        URL[] urls = JarScanner.findClassPaths();
        for (int i = 0; i < urls.length; ++i) {
            URL jarURL = urls[i];
            if (jarfilter.accepts(jarURL.toString())) {
                JarScanner.StreamIterator iter = JarScanner.create(jarURL, jarEntryFilter);
                while (iter.next()) {
                    String filename = iter.getFilename();
                    visitBytecode(iter, filename, visitor);
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