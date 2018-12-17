package org.mikado.imc.mobility;

import java.io.*;

import java.util.Hashtable;

import org.mikado.imc.log.DefaultMessagePrinter;
import org.mikado.imc.log.MessagePrinter;
import org.mikado.imc.log.MessagePrinters;

/**
 * The classloader that is used for dynamically loading classes from a source
 * different from the local CLASSPATH. Typically it loads classes downloaded
 * from the network, but it can also be used to load classes stored in a file.
 * This classloader has to be pre-configured with the byte code (i.e., byte
 * array) of the classes that will be loaded. Once this table is set, the loader
 * will automatically load dynamically new classes when they are needed.
 * 
 * @author $author$
 * @version $Revision: 1.7 $
 */
public class NodeClassLoader extends ClassLoader {
    /** local cache of already loaded classes */
    private Hashtable<String, Class<?>> classes = new Hashtable<String, Class<?>>();

    /** the actual table of byte code associated to classes */
    private Hashtable<String, byte[]> classData = new Hashtable<String, byte[]>();

    /**
     * whether messages have to be printed (through the associated message
     * printer)
     */
    protected boolean MessagesOn = false;

    /** the message printers for printing messages */
    protected MessagePrinters messagePrinters = new MessagePrinters();

    /** indentation used to print messages */
    protected int indentation = 0;

    /** the string to actually print the indentation */
    protected StringBuffer indent_string = new StringBuffer();

    /**
     * whether to force the loading of a class even if it would be available
     * from the local file system. Classes from the standard libraries and other
     * sensible standard classes will still be loaded with the system class
     * loader in any case
     */
    protected boolean force_load;

    /**
     * Creates a new NodeClassLoader object.
     */
    public NodeClassLoader() {
        this(false);
    }

    /**
     * Creates a new NodeClassLoader object.
     * 
     * @param force
     *            whether the loading has to be forced (i.e., even if a class is
     *            avaiable to the system class loader)
     */
    public NodeClassLoader(boolean force) {
        force_load = force;
    }

    /**
     * Add an element to the table of classes
     * 
     * @param className
     *            the name of the class
     * @param classBytes
     *            the byte code for the class
     */
    synchronized public void addClassBytes(String className, byte[] classBytes) {
        if ((classData.get(className) == null) && (classBytes != null)) {
            classData.put(className, classBytes);
        }
    }

    /**
     * store in the class table the byte code for a class, retrieving it from
     * the local file system. It uses the classpath specified to the virtual
     * machine
     * 
     * @param className
     *            the name of the class
     * 
     * @throws IOException
     *             if the byte code cannot be retrieved
     */
    synchronized public void addClassBytes(String className) throws IOException {
        loadClassBytes(className);
    }

    /**
     * Activate or deactivate the verbosity
     * 
     * @param on
     *            true activates the messages
     */
    public void setMessages(boolean on) {
        MessagesOn = on;

        if (on) {
            messagePrinters.addMessagePrinter(new DefaultMessagePrinter(
                    "NodeClassLoader"));
        }
    }

    /**
     * Set the message printer
     * 
     * @param msg
     *            the message printer
     */
    public void setMessagePrinters(MessagePrinters msg) {
        messagePrinters = msg;
    }

    /**
     * Return the message printer
     * 
     * @return the message printer
     */
    public MessagePrinters getMessagePrinters() {
        return messagePrinters;
    }

    /**
     * Retrieve the byte code for a class from the local file system. It uses
     * the classpath specified to the virtual machine
     * 
     * @param className
     *            the name of the class
     * 
     * @throws IOException
     *             if the byte code cannot be retrieved
     */
    protected void loadClassBytes(String className) throws IOException {
        PrintMessageLoadedClass("loadClassBytes : " + className);

        PrintMessageLoadedClass("Reading data of class "
                + className.replace('.', System.getProperty("file.separator")
                        .charAt(0)) + ".class");

        byte[] result = ClassBytesLoader.loadClassBytes(className);

        addClassBytes(className, result);
    }

    /**
     * Return the byte code for a specific class if it is stored in the class
     * table
     * 
     * @param className
     *            the class name
     * 
     * @return the byte array with the byte code of the class or null if the
     *         class is not in the table of classes
     */
    public byte[] getClassBytes(String className) {
        return (byte[]) classData.get(className);
    }

    /**
     * Actually load a class
     * 
     * @param className
     *            the name of the class
     * 
     * @return the Class object for the loaded class
     * 
     * @throws ClassNotFoundException
     *             whether the class cannot be loaded either with the system
     *             class loader or from the table of classes
     */
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return (loadClass(className, true));
    }

    /**
     * Actually load a class
     * 
     * @param className
     *            the name of the class
     * @param resolveIt
     *            whether the class has to be resolved (see the documentation
     *            for the class ClassLoader)
     * 
     * @return the Class object for the loaded class
     * 
     * @throws ClassNotFoundException
     *             whether the class cannot be loaded either with the system
     *             class loader or from the table of classes
     * @throws SecurityException
     * @throws ClassFormatError
     */
    public synchronized Class<?> loadClass(String className, boolean resolveIt)
            throws ClassNotFoundException {
        if (force_load) {
            return forceLoadClass(className);
        }

        Class<?> result;
        byte[] classData;

        PrintMessage(">>>>>> Load class : " + className + " for "
                + Thread.currentThread());

        /* Check our local cache of classes */
        result = (Class<?>) classes.get(className);

        if (result != null) {
            PrintMessage(">>>>>> returning cached result.");

            return result;
        }

        try {
            result = findSystemClass(className);
            PrintMessage(">>>>>> returning system class (in CLASSPATH).");

            return result;
        } catch (ClassNotFoundException e) {
            PrintMessage(">>>>>> Not a system class");
        }

        if (!isUsefulClass(className)) {
            // this is dangerous
            throw new SecurityException(className);
        }

        inc_indent();

        /* Try to load it from our repository */
        classData = getClassBytes(className);

        if (classData != null) {
            PrintMessageLoadedClass(">>>>>> Fetching the bytes of " + className);
        } else {
            ClassNotFoundException e = new ClassNotFoundException(className);
            e.printStackTrace();
            throw e;
        }

        /* Define it (parse the class file) */
        result = defineClass(className, classData, 0, classData.length);

        if (result == null) {
            throw new ClassFormatError();
        }

        if (resolveIt) {
            resolveClass(result);
        }

        classes.put(className, result);
        PrintMessageLoadedClass(">>>>>> Returning newly loaded class.");

        dec_indent();

        return result;
    }

    /**
     * force the loading of a class even if it would be available from the local
     * file system. Classes from the standard libraries and other sensible
     * standard classes will still be loaded with the system class loader in any
     * case
     * 
     * @param className
     *            the name of the class
     * @param retrieve_class_bytes
     *            whether the bytes of the class have to be retrieved (e.g.,
     *            from the local file system)
     * 
     * @return the Class object for the loaded class
     * 
     * @throws ClassNotFoundException
     *             whether the class cannot be loaded either with the system
     *             class loader or from the table of classes
     * @throws IOException
     */
    public synchronized Class<?> forceLoadClass(String className,
            boolean retrieve_class_bytes) throws ClassNotFoundException,
            IOException {
        if (retrieve_class_bytes) {
            addClassBytes(className);
        }

        return forceLoadClass(className);
    }

    /**
     * force the loading of a class even if it would be available from the local
     * file system. Classes from the standard libraries and other sensible
     * standard classes will still be loaded with the system class loader in any
     * case
     * 
     * @param className
     *            the name of the class
     * 
     * @return the Class object for the loaded class
     * 
     * @throws ClassNotFoundException
     *             whether the class cannot be loaded either with the system
     *             class loader or from the table of classes
     * @throws ClassFormatError
     */
    public synchronized Class<?> forceLoadClass(String className)
            throws ClassNotFoundException {
        Class<?> result;
        byte[] classData;

        PrintMessage(">>>>>> Load class : " + className + " for "
                + Thread.currentThread());

        /* Check our local cache of classes */
        result = (Class<?>) classes.get(className);

        if (result != null) {
            PrintMessage("        >>>>>> returning cached result.");

            return result;
        }

        inc_indent();

        /* Try to load it from our repository */
        classData = getClassBytes(className);

        if (classData != null) {
            PrintMessageLoadedClass(">>>>>> Fetching the bytes of " + className);
        } else {
            try {
                result = findSystemClass(className);
                PrintMessage(">>>>>> returning system class (in CLASSPATH).");
                dec_indent();
                return result;
            } catch (ClassNotFoundException e) {
                PrintMessage(">>>>>> Not a system class");
            }

            throw new ClassNotFoundException(className);
        }

        /* Define it (parse the class file) */
        result = defineClass(className, classData, 0, classData.length);

        if (result == null) {
            throw new ClassFormatError();
        }

        resolveClass(result);

        classes.put(className, result);
        PrintMessageLoadedClass(">>>>>> Returning newly loaded class.");

        dec_indent();

        return result;
    }

    /**
     * increment the indentation
     */
    protected void inc_indent() {
        ++indentation;
        indent_string.append(' ');
    }

    /**
     * decrement the indentation
     */
    protected void dec_indent() {
        indent_string.deleteCharAt(--indentation);
    }

    /**
     * print a message through the associated message printer, if messages have
     * been activated.
     * 
     * @param s
     *            the string to print
     */
    protected void PrintMessage(String s) {
        if (MessagesOn) {
            messagePrinters.Print(indent_string + s);
        }
    }

    /**
     * print a message related to the loading of a class through the associated
     * message printer
     * 
     * @param s
     *            the string to print
     */
    protected void PrintMessageLoadedClass(String s) {
        messagePrinters.Print(indent_string + s);
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
    final static protected boolean isUsefulClass(String className) {
        boolean result;
        result = (className.equals("void") || className.equals("int")
                || className.equals("char") || className.equals("double")
                || className.equals("float") || className.equals("long")
                || className.equals("short") || className.equals("boolean")
                || className.equals("byte") || className.startsWith("java.") || className
                .startsWith("org."));

        return (!result);
    }

    /**
     * @see org.mikado.imc.log.MessagePrinters#addMessagePrinter(org.mikado.imc.log.MessagePrinter)
     */
    public void addMessagePrinter(MessagePrinter messagePrinter) {
        messagePrinters.addMessagePrinter(messagePrinter);
    }
}
