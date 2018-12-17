package org.mikado.imc.log;

import java.io.*;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * A MessagePrinter that print the messages in a file
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class FileMessagePrinter implements MessagePrinter {
    /** the file output stream */
    protected FileWriter out;

    /**
     * The file name
     */
    protected String fileName;

    /**
     * Creates a new FileMessagePrinter object.
     * 
     * @param filename
     *            the name of the output file (that will be overwritten)
     * 
     * @throws IOException
     */
    public FileMessagePrinter(String filename) throws IOException {
        fileName = filename;
        out = new FileWriter(filename);
    }

    /**
     * print a message to the file
     * 
     * @param s
     *            the message to print
     */
    public synchronized void Print(String s) {
        try {
            out.write(s + "\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the file.
     * 
     * @throws IOException
     */
    public synchronized void close() throws IOException {
        if (out != null) {
            out.flush();
            out.close();
        }
    }

    /**
     * Overwrites the already written file with a sorted version.
     * 
     * @throws IOException
     */
    synchronized public void sort() throws IOException {
        close();

        TreeSet<String> ordered = new TreeSet<String>();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));

        String line = reader.readLine();
        while (line != null) {
            ordered.add(line);
            line = reader.readLine();
        }

        reader.close();

        out = new FileWriter(fileName);

        Iterator<String> it = ordered.iterator();
        while (it.hasNext())
            out.write(it.next().toString() + "\n");

        out.close();
    }
}
