package org.mikado.imc.common;

import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.io.File;

/**
 * Some utility functions dealing with files.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.3 $
 */
public class FileUtils {
    /**
     * @return A vector of directories in the class path
     */
    public static Vector<String> getClassPathDirectories() {
        StringTokenizer tok = new StringTokenizer(System
                .getProperty("java.class.path"), System
                .getProperty("path.separator"));

        Vector<String> v = new Vector<String>();
        while (tok.hasMoreElements()) {
            try {
                v.addElement((new File(tok.nextElement().toString()))
                        .getCanonicalPath());
            } catch (java.io.IOException ioe) {
                ioe.printStackTrace();
            }
        }

        return v;
    }

    /**
     * true if the file is in the specified path
     * 
     * @param path
     * @param file
     * @return true if the file is in the specified path
     */
    public static boolean isInPath(String path, String file) {
        return file.startsWith(path);
    }

    /**
     * return the file path in the CLASSPATH of the given class file cl (already
     * in path format, not in '.' notation)
     * 
     * @param cl
     * @param class_path
     * @return the file path
     */
    public static File getClassPathFile(String cl, Vector<?> class_path) {
        Enumeration<?> en = class_path.elements();
        String path;
        File file;

        while (en.hasMoreElements()) {
            path = en.nextElement().toString();
            file = new File(path + File.separatorChar + cl);
            if (file.exists())
                return file;
        }

        return null;
    }
}