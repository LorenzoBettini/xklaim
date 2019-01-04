package org.mikado.imc.mobility;

import java.io.*;

import java.lang.reflect.*;

import java.util.*;

import org.mikado.imc.log.MessagePrinter;
import org.mikado.imc.log.MessagePrinters;

/**
 * The base class for Java "mobile" (possibly active) objects
 * 
 * @author $author$
 * @version $Revision: 1.10 $
 */
public class JavaMigratingCode extends Thread implements Serializable,
        MigratingCode {
    private static final long serialVersionUID = 3257008752333894450L;

    /**
     * Whether the byte code of the class of the object has to be collected upon
     * "migration"
     */
    protected boolean deliverCode = true;

    /**
     * he table with all the classes used by the object serialized key = the
     * class name value = the byte array with the byte code
     */
    protected Hashtable<String, byte[]> usedClasses;

    /** Classes that must not be collected */
    protected HashSet<String> excludeClasses;

    /** Packages whose classes must not be collected */
    protected Vector<String> excludePackages;

    /** The message printers */
    transient protected MessagePrinters messagePrinters = new MessagePrinters();

    /**
     * Create a new JavaMigratingCode object.
     */
    public JavaMigratingCode() {
    }

    /**
     * Creates a new JavaMigratingCode object.
     * 
     * @param s
     *            the name of the new thread
     */
    public JavaMigratingCode(String s) {
        super(s);
    }

    /**
     * State whether the classes of this code have to migrate.
     * 
     * @param b
     *            true if the classes of this code have to migrate with it
     */
    final public void setDeliverCode(boolean b) {
        deliverCode = b;
    }

    /**
     * Whether the classes of this code have to migrate.
     * 
     * @return true if the classes of this code have to migrate with it
     */
    final public boolean hasToDeliverCode() {
        return deliverCode;
    }

    /**
     * Set the message printer for this object.
     * 
     * @param msg
     *            the message printer associated to this object
     */
    public void setMessagePrinters(MessagePrinters msg) {
        messagePrinters = msg;
    }

    /**
     * Update the set of classes that have to be excluded during the class
     * collection process
     * 
     * @param table
     *            the set used to update the set of classes that have to be
     *            excluded
     */
    public void setExcludeClasses(Set<String> table) {
        if (excludeClasses == null) {
            excludeClasses = new HashSet<String>();
        }

        excludeClasses.addAll(table);
    }

    /**
     * Update the set of classes that have to be excluded during the class
     * collection process
     * 
     * @param c
     *            the name of the class to be excluded
     */
    public void setExcludeClasses(String c) {
        if (excludeClasses == null) {
            excludeClasses = new HashSet<String>();
        }

        excludeClasses.add(c);
    }

    /**
     * Update the set of packages whose classes have to be excluded during the
     * class collection process
     * 
     * @param pack
     *            the name of the class to be excluded
     */
    public final void addExcludePackage(String pack) {
        if (excludePackages == null) {
            excludePackages = new Vector<String>();
        }

        excludePackages.addElement(pack);
    }

    /**
     * retrieve the contents of the .class file for this class
     * 
     * @return the byte array with the contents of .class file
     */
    final public byte[] getClassBytes() {
        if (!deliverCode) {
            return null;
        }

        return getClassBytes(getClass().getName());
    }

    /**
     * retrieve the contents of the .class file for a specific class
     * 
     * @param className
     *            the name of the class whose contents are retrieved
     * 
     * @return the byte array with the contents of .class file
     */
    synchronized public byte[] getClassBytes(String className) {
        byte[] ClassBytes = null;

        if (usedClasses != null) {
            ClassBytes = (byte[]) usedClasses.get(className);
        }

        if (ClassBytes != null) {
            return ClassBytes;
        } else {
            ClassBytes = getClassBytesFromClassLoader(className);
        }

        return ClassBytes;
    }

    /**
     * Retrieve the code of a class from the class loader table
     * 
     * @param className
     *            the name of the class whose contents are retrieved
     * 
     * @return the contents of the class
     */
    public byte[] getClassBytesFromClassLoader(String className) {
        ClassLoader classLoader = getClass().getClassLoader();

        /*
        System.out.println("ClassLoader: "
                + (classLoader != null ? classLoader.getClass().getName()
                        : "null"));
                        */

        if (classLoader instanceof NodeClassLoader) {
            byte[] ClassBytes = ((NodeClassLoader) classLoader)
                    .getClassBytes(className);

            if (ClassBytes != null) {
                return ClassBytes;
            }
        }

        try {
            /*
            System.out.println("retrieving class contents from "
                    + FileUtils.getClassPathDirectories());
            System.out.println("system class loader "
                    + ClassLoader.getSystemClassLoader());
                    */
            byte[] result = ClassBytesLoader.loadClassBytes(className);

            return result;
        } catch (FileNotFoundException nof) {
            nof.printStackTrace();

            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Retrieve all the classes used by this agent class.
     * 
     * @return an Hashtable where the key is a class name, and the value is the
     *         byte code of that class.
     */
    synchronized protected Hashtable<String, byte[]> getUsedClasses() {
        if ((usedClasses == null) && deliverCode) {
            createUsedClassesTable();
        }

        return usedClasses;
    }

    /**
     * Assign the table of used classes
     * 
     * @param cl
     *            the table used for the update
     */
    final public void setUsedClasses(Hashtable<String, byte[]> cl) {
        usedClasses = cl;
    }

    /**
     * Initialize the table of used classes.
     */
    synchronized protected void createUsedClassesTable() {
        if (usedClasses != null) {
            return;
        }

        usedClasses = new Hashtable<String, byte[]>();

        // retrieve all the classes used by this class
        getUsedClasses(getClass());
    }

    /**
     * Retrieve all the classes used by this agent. This procedure is recursive
     * and it examins, through Reflection API all the instance variables, all
     * the method signatures (including constructors), the base class and
     * implemented interfaces.
     * 
     * @param c
     *            the class to examine.
     */
    protected void getUsedClasses(Class<?> c) {
        if ((c == null) || !addUsedClass(c)) {
            return;
        }

        Field[] fields = c.getDeclaredFields();
        Constructor<?>[] constructors = c.getDeclaredConstructors();
        Method[] methods = c.getDeclaredMethods();
        int i;

        for (i = 0; i < fields.length; i++) {
            getUsedClasses(fields[i].getType());
        }

        for (i = 0; i < constructors.length; i++) {
            getUsedClasses(constructors[i].getParameterTypes());
            getUsedClasses(constructors[i].getExceptionTypes());
        }

        for (i = 0; i < methods.length; i++) {
            getUsedClasses(methods[i].getReturnType());
            getUsedClasses(methods[i].getParameterTypes());
            getUsedClasses(methods[i].getExceptionTypes());
        }

        getUsedClasses(c.getDeclaredClasses());
        getUsedClasses(c.getSuperclass());
        getUsedClasses(c.getInterfaces());

        getUsedClasses(c.getEnclosingClass());
    }

    /**
     * Overloaded version that calls getUsedClasses on every class of the array
     * passed as parameter.
     * 
     * @param classes
     *            a Class array
     */
    protected void getUsedClasses(Class<?>[] classes) {
        for (int i = 0; i < classes.length; ++i)
            getUsedClasses(classes[i]);
    }

    /**
     * Add the class to the table of used classes if it is not already there and
     * if it is not a reserved class (i.e. a class of Java library or a class of
     * this package.
     * 
     * @param classVar
     *            the class to add to the table of used classes
     * 
     * @return <tt>true</tt> if the class has been added, <tt>false</tt>
     *         otherwise
     */
    protected boolean addUsedClass(Class<?> classVar) {
        String className = filter(classVar.getName());

        if (isUsefulClass(className) && !usedClasses.containsKey(className)
                && !isToExclude(className)) {
            PrintMessage("Recorded " + className);
            usedClasses.put(className, getClassBytes(className));

            return true;
        }

        return false;
    }

    /**
     * Check whether this class is among the ones to be excluded from the
     * collecting.
     * 
     * @param classname
     *            the name of the class check
     * 
     * @return <tt>true</tt> if the class is to be excluded, <tt>false</tt>
     *         otherwise
     */
    protected boolean isToExclude(String classname) {
        if ((excludeClasses != null) && excludeClasses.contains(classname)) {
            return true;
        }

        if (excludePackages == null) {
            return false;
        }

        Enumeration<String> expackages = excludePackages.elements();

        while (expackages.hasMoreElements()) {
            if (classname.startsWith(expackages.nextElement().toString())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Filter out undesired characters (e.g. if it is an array, remove) the
     * character <tt>"[L"</tt>.
     * 
     * @param className
     *            the name of the class
     * 
     * @return the new string without such characters
     */
    protected String filter(String className) {
        if (className.startsWith("[L")) {
            return className.substring(2);
        }

        return className;
    }

    /**
     * Check whether a class has to be add to the table of used classes.
     * 
     * @param className
     *            the name of the class to examine
     * 
     * @return <tt>true</tt> if the class must be added, <tt>false</tt>
     *         otherwise
     */
    final static public boolean isUsefulClass(String className) {
        boolean result;
        result = (className.equals("void") || className.equals("int")
                || className.equals("char") || className.equals("double")
                || className.equals("float") || className.equals("long")
                || className.equals("short") || className.equals("boolean")
                || className.equals("byte") || className.startsWith("java.")
                || className.startsWith("org.mikado.imc.mobility.") || className
                .startsWith("["));

        /* TODO I actually don't know what [C and [B stand for... */

        return (!result);
    }

    /**
     * Print a message through the associated message printer
     * 
     * @param s
     *            the string to print
     */
    protected void PrintMessage(String s) {
        if (messagePrinters != null)
            messagePrinters.Print(s);
    }

    /**
     * Default implementation for Thread.run. This implementation does nothing.
     * We chose to inherit from Thread since existing classes could have already
     * be thought as derived from Thread. This way, switching to this package
     * will be smooth. Otherwise, the Thread features will be basically ignored.
     */
    public void run() {
        // do nothing
    }

    /**
     * Create a JavaMigratingPacket from this code
     * 
     * @return The JavaMigratingPacket created
     * 
     * @throws IOException
     */
    public JavaMigratingPacket make_packet() throws IOException {
        return new JavaMigratingPacket(this);
    }

    /**
     * @see org.mikado.imc.log.MessagePrinters#addMessagePrinter(org.mikado.imc.log.MessagePrinter)
     */
    public void addMessagePrinter(MessagePrinter messagePrinter) {
        messagePrinters.addMessagePrinter(messagePrinter);
    }
}
