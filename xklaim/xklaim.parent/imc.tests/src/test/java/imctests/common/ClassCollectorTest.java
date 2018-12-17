/*
 * Created on Apr 6, 2006
 */
package imctests.common;

import java.util.HashSet;
import java.util.Vector;

import org.mikado.imc.common.ClassCollector;
import org.mikado.imc.common.ClassEntry;
import org.mikado.imc.common.ClassFilter;

import junit.framework.TestCase;

/**
 * Tests for class ClassCollector
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class ClassCollectorTest extends TestCase {
    static class MyProcessBase {

    }

    static class MyProcessDerived extends MyProcessBase {

    }

    static class MyProcessDerived2 extends MyProcessDerived {

    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testClassNameParse() {
        String className = "org.foo.bar.MyClass";
        
        ClassEntry classEntry = ClassEntry.parseClassName(className);
        assertTrue(classEntry != null);
        assertTrue(classEntry.packageName != null);
        assertEquals("org.foo.bar", classEntry.packageName);
        assertEquals("MyClass", classEntry.className);
    }
    
    public void testClassPathParse() {
        String className = "org/foo/bar/MyClass.class";
        
        ClassEntry classEntry = ClassEntry.parseClassFilePath(className);
        assertTrue(classEntry != null);
        assertTrue(classEntry.packageName != null);
        assertEquals("org.foo.bar", classEntry.packageName);
        assertEquals("MyClass", classEntry.className);
    }

    public void testClassCollectorWithFilter() throws ClassNotFoundException {
        String className = MyProcessBase.class.getName();
        System.out.println("filtering: " + MyProcessBase.class.getName());
        ClassCollector classCollector = new ClassCollector(className);
        Vector<ClassEntry> entries = classCollector.getClassEntries();
        System.out.println("found classes: " + entries);

        HashSet<ClassEntry> entrySet = new HashSet<ClassEntry>(entries);

        /* we must have found only the above three classes */
        assertTrue(entrySet.size() == 3);
        
        assertTrue(entries.elementAt(0).className != null);
        assertTrue(entries.elementAt(0).packageName != null);

        ClassEntry classEntry = ClassEntry.parseClassName(className);
        assertEquals(classEntry.hashCode(), className.hashCode());

        assertTrue(entrySet.contains(ClassEntry.parseClassName(className)));
        assertTrue(entrySet.contains(ClassEntry
                .parseClassName(MyProcessDerived.class.getName())));
        assertTrue(entrySet.contains(ClassEntry
                .parseClassName(MyProcessDerived2.class.getName())));
    }
    
    public void testClassFilter() throws ClassNotFoundException {
        String className = MyProcessBase.class.getName();
        /* apply no filter at first */
        ClassCollector classCollector = new ClassCollector();
        Vector<ClassEntry> entries = classCollector.getClassEntries();
        
        /* there will be more than three classes :-) */
        assertTrue(entries.size() > 3);
        
        ClassFilter classFilter = new ClassFilter(className);
        entries = classFilter.filter(entries);
        
        HashSet<ClassEntry> entrySet = new HashSet<ClassEntry>(entries);

        /* we must have found only the above three classes */
        assertTrue(entrySet.size() == 3);

        ClassEntry classEntry = ClassEntry.parseClassName(className);
        assertEquals(classEntry.hashCode(), className.hashCode());

        assertTrue(entrySet.contains(ClassEntry.parseClassName(className)));
        assertTrue(entrySet.contains(ClassEntry
                .parseClassName(MyProcessDerived.class.getName())));
        assertTrue(entrySet.contains(ClassEntry
                .parseClassName(MyProcessDerived2.class.getName())));
    }
}
