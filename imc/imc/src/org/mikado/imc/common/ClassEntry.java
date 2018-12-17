/*
 * Created on 5-apr-2006
 */
/**
 * 
 */
package org.mikado.imc.common;

import java.io.File;

/**
 * Represents information about a class (class name and package name)
 * 
 * @author bettini
 * @version $Revision: 1.2 $
 */
public class ClassEntry {
    /**
     * The class name
     */
    public String className;

    /**
     * The package name (null if the class is in the default package)
     */
    public String packageName;

    /**
     * @param className
     * @param packageName
     */
    public ClassEntry(String className, String packageName) {
        this.className = className;
        this.packageName = packageName;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return className + (packageName != null ? " - " + packageName : "");
    }

    /**
     * @return The fully qualified class name
     */
    public String getFullyQualifiedClassName() {
        return (packageName != null ? packageName + "." : "") + className;
    }

    /**
     * Given the path to a class file builds the corresponding ClassEntry
     * 
     * @param classRepr
     *            the path to a class file (e.g., "org/mypackage/MyClass.class")
     * @return null if the passed representation cannot be parsed appropriately
     */
    public static ClassEntry parseClassFilePath(String classRepr) {
        File file = new File(classRepr);

        String packageName = file.getParent();
        if (packageName != null)
            packageName = packageName.replace(File.separatorChar, '.');

        String fileName = file.getName();

        if (!fileName.endsWith(".class"))
            return null;

        String className = fileName
                .substring(0, fileName.lastIndexOf(".class"));

        return new ClassEntry(className, packageName);
    }

    /**
     * Given a fully qualified class name builds the corresponding ClassEntry
     * 
     * @param classRepr
     *            the fully qualified class name (e.g., "org.mypackage.MyClass")
     * @return null if the passed representation cannot be parsed appropriately
     */
    public static ClassEntry parseClassName(String classRepr) {
        int classNameIndex = classRepr.lastIndexOf('.');

        if (classNameIndex < 0)
            return new ClassEntry(classRepr, null); // default package

        String packageName = classRepr.substring(0, classNameIndex);
        String className = classRepr.substring(classNameIndex+1);

        return new ClassEntry(className, packageName);
    }

    /**
     * true if the fully qualified names are the same
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (obj instanceof ClassEntry) {
            ClassEntry classEntry = (ClassEntry) obj;

            return getFullyQualifiedClassName().equals(
                    classEntry.getFullyQualifiedClassName());
        }

        return getFullyQualifiedClassName().equals(obj.toString());
    }

    /**
     * Based on the fully qualified name
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getFullyQualifiedClassName().hashCode();
    }
}