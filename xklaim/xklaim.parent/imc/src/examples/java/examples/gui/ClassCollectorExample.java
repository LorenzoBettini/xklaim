/*
 * Created on 5-apr-2006
 */
/**
 * 
 */
package examples.gui;

import java.util.Enumeration;
import java.util.Vector;

import org.mikado.imc.common.ClassCollector;
import org.mikado.imc.common.ClassEntry;

/**
 * Example of use of ClassCollector
 * 
 * @author bettini
 * @version $Revision: 1.1 $
 */
public class ClassCollectorExample {

    /**
     * @param args
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws ClassNotFoundException {
        System.out.println("All classes in the current class path:");
        printEntries(new ClassCollector());

        System.out.println("\nsubclasses of Node:");
        printEntries(new ClassCollector("org.mikado.imc.topology.Node"));

        System.out.println("\nsubclasses of NodeProcess:");
        printEntries(new ClassCollector("org.mikado.imc.topology.NodeProcess"));

        System.out.println("\nsubclasses of NodeCoordinator:");
        printEntries(new ClassCollector(
                "org.mikado.imc.topology.NodeCoordinator"));
    }

    private static void printEntries(ClassCollector classCollector) {
        Vector<ClassEntry> classEntries = classCollector.getClassEntries();

        Enumeration<ClassEntry> entries = classEntries.elements();
        while (entries.hasMoreElements()) {
            System.out.println("" + entries.nextElement());
        }
    }

}
