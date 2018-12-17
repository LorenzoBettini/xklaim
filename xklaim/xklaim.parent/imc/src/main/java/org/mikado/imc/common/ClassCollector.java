/*
 * Created on 5-apr-2006
 */
/**
 * 
 */
package org.mikado.imc.common;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/*
 * import javax.jnlp.BasicService; import javax.jnlp.ServiceManager; import
 * javax.jnlp.UnavailableServiceException;
 */

/**
 * Builds a vector of all the class names that are available in the current
 * classpath
 * 
 * @author bettini
 * @version $Revision: 1.9 $
 */
public class ClassCollector {
    protected Vector<ClassEntry> classEntries = new Vector<ClassEntry>();

    protected ClassFilter classFilter;

    protected String classPath = "";

    /**
     * Builds a class collector with an associated ClassFilter
     * 
     * @param classFilter
     */
    public ClassCollector(ClassFilter classFilter) {
        this.classFilter = classFilter;
        build();
    }

    /**
     * Builds a class collector with an associated class name filter
     * 
     * @param classFilter
     * @throws ClassNotFoundException
     */
    public ClassCollector(String classFilter) throws ClassNotFoundException {
        this.classFilter = new ClassFilter(classFilter);
        build();
    }

    /**
     * Builds a class collector with no associated ClassFilter
     */
    public ClassCollector() {
        build();
    }

    /**
     * Actually builds the vector of class entries.
     */
    protected void build() {
        /* get the list of all directories (and jars) in the class path */
        Vector<String> classPathDirectories = FileUtils
                .getClassPathDirectories();

        classPath = classPathDirectories.toString();

        System.out.println("classpath: " + classPath);

        ClassLoader classLoader = getClass().getClassLoader();
        System.out.println("classloader: " + classLoader.getClass().getName());
        System.out.println("parent: " + classLoader.getParent());
        System.out.println("jnlpx.home = " + System.getProperty("jnlpx.home"));
        // System.out.println("webappcontext = " + getWebAppContextUrl());

        if (classLoader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
            System.out.println("URLS: " + urlClassLoader.getURLs());
        }

        Enumeration<String> classPathEntries = classPathDirectories.elements();
        while (classPathEntries.hasMoreElements()) {
            try {
                inspectClassPathEntry(classPathEntries.nextElement());
            } catch (IOException e) {
                e.printStackTrace();
                /* we just go on */
            }
        }
    }

    /*
     * Uses the jnlp API to determine the webapp context. If used outside of
     * webstart, <code>fallBackWebAppContextUrl</code> is returned. For
     * example this could return <code>http://localhost:8080/mywebapp/</code>.
     * 
     * @return the url to the webapp ending with a slash
     * 
     * public String getWebAppContextUrl() { String webAppContextUrl = ""; try {
     * BasicService basicService = (BasicService) ServiceManager
     * .lookup("javax.jnlp.BasicService"); String codeBase =
     * basicService.getCodeBase().toExternalForm(); if (!codeBase.endsWith("/")) {
     * codeBase += "/"; } webAppContextUrl = codeBase; } catch
     * (UnavailableServiceException e) { e.printStackTrace(); } return
     * webAppContextUrl; }
     */

    protected void inspectClassPathEntry(String classPathEntry)
            throws IOException {
        if (classPathEntry.endsWith(".jar")) {
            inspectJarEntry(classPathEntry);
        } else {
            inspectDirEntry(new File(classPathEntry));
        }
    }

    protected void inspectJarEntry(String jarFileName) throws IOException {
        JarFile jarFile = new JarFile(jarFileName, false);

        Enumeration<JarEntry> elements = jarFile.entries();
        while (elements.hasMoreElements()) {
            JarEntry jarEntry = elements.nextElement();
            ClassEntry classEntry = ClassEntry.parseClassFilePath(jarEntry
                    .getName());
            if (classEntry != null
                    && filter(classEntry.getFullyQualifiedClassName())) {
                classEntries.add(classEntry);
            }
        }
    }

    protected void inspectDirEntry(File dir) throws IOException {
        System.out.println("Inspecting dir entry: " + dir);
        String[] files = dir.list();
        for (int i = 0; i < files.length; i++) {
            File file = new File(files[i]);
            inspectDirEntry(dir, new File(""), file);
        }
    }

    protected void inspectDirEntry(File initialDir, File Dir, File file)
            throws IOException {
        String path = Dir.getPath();
        File completePath = new File(initialDir.getCanonicalPath()
                + File.separator + path + File.separator + file.getName());

        if (!completePath.exists())
            return;

        if (completePath.isFile()) {
            // if the path is not empty it starts with a slash
            String className = (path.length() > 0 ? path.substring(1) : "")
                    + File.separator + file;
            ClassEntry classEntry = ClassEntry.parseClassFilePath(className);
            if (classEntry != null
                    && filter(classEntry.getFullyQualifiedClassName())) {
                classEntries.add(classEntry);
            }
        } else {
            String[] files = completePath.list();
            for (int i = 0; i < files.length; i++) {
                inspectDirEntry(initialDir, new File(path + File.separator
                        + file.getName()), new File(files[i]));
            }
        }
    }

    protected boolean filter(String className) {
        if (classFilter != null)
            return classFilter.filter(className);

        return true;
    }

    /**
     * @return Returns the classEntries.
     */
    public Vector<ClassEntry> getClassEntries() {
        return classEntries;
    }

    /**
     * @return Returns the classPath.
     */
    public String getClassPath() {
        return classPath;
    }
}
