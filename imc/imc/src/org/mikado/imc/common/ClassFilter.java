/*
 * Created on 5-apr-2006
 */
/**
 * 
 */
package org.mikado.imc.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * Given a specific class name filters other classes: only those that are
 * subclasses of the specified filter class name.
 * 
 * @author bettini
 * @version $Revision: 1.4 $
 */
public class ClassFilter {
    /**
     * The class representing the superclass to filter against
     */
    public Class<?> classFilter;

    /**
     * @param classFilter
     *            The name of the class to use as filter
     * @throws ClassNotFoundException
     */
    public ClassFilter(String classFilter) throws ClassNotFoundException {
        this.classFilter = Class.forName(classFilter);
    }

    /**
     * Check whether the passed string represents a class that is a subclass of
     * the filter class.
     * 
     * @param className
     * @return true if the passed string represents a class that is a subclass
     *         of the filter class.
     */
    public boolean filter(String className) {
        try {
            Class<?> specifiedClass = Class.forName(className);
            return classFilter.isAssignableFrom(specifiedClass);
        } catch (ClassNotFoundException e) {
            /* simply return false since we couldn't instance it anyway */
            return false;
        } catch (Error e) {
            /* simply return false since we couldn't instance it anyway */
            return false;
        }
    }

    /**
     * Filters all the passed entries and return a Vector with only those that
     * passed the filtering.
     * 
     * @param entries
     *            The entries to filter.
     * @return a Vector with only those that passed the filtering.
     */
    public Vector<ClassEntry> filter(Collection<ClassEntry> entries) {
        Vector<ClassEntry> filtered = new Vector<ClassEntry>();
        Iterator<ClassEntry> iterator = entries.iterator();

        while (iterator.hasNext()) {
            ClassEntry classEntry = iterator.next();
            filterEntry(filtered, classEntry);
        }

        return filtered;
    }

    /**
     * @param filtered
     * @param classEntry
     */
    protected void filterEntry(Vector<ClassEntry> filtered, ClassEntry classEntry) {
        if (filter(classEntry.getFullyQualifiedClassName()))
            filtered.add(classEntry);
    }
}