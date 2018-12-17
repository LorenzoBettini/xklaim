package org.mikado.imc.mobility;

/**
 * Utility class for loading the contents of a .class file
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for loading the contents of a .class file.<br>
 * <br>
 * 
 */

public class ClassBytesLoader {

    /**
     * Load the contents of .class file and store such contents on a byte array.
     * 
     * @param className
     *            the name of the class to load. This must be a Java name, not a
     *            file system path: such path will be constructed by the method
     *            itself, according to the property of the underlying operating
     *            system.
     * @return the byte array with the byte code of the specified class
     * @throws IOException
     */
    public static byte[] loadClassBytes(String className) throws IOException {
        int size;
        byte[] classBytes;
        InputStream is;
        String fileSeparator = System.getProperty("file.separator");

        className = className.replace('.', fileSeparator.charAt(0));
        className = className + ".class";

        // Search for the class in the CLASSPATH
        is = ClassLoader.getSystemResourceAsStream(className);

        if (is == null)
            throw new FileNotFoundException(className);

        size = is.available();

        classBytes = new byte[size];

        is.read(classBytes);
        is.close();

        return classBytes;
    }
}
