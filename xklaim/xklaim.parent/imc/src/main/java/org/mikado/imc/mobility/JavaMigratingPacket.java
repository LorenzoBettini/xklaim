package org.mikado.imc.mobility;

import java.io.*;

import java.util.Hashtable;


/**
 * Implement a packet to be written to a stream, containing both the serialized
 * version of the JavaMigratingCode object and the byte code of its class and of
 * all the classes used.
 *
 * @author $author$
 * @version $Revision: 1.3 $
 */
public class JavaMigratingPacket extends MigratingPacket {
    private static final long serialVersionUID = 3258689901351219249L;

	/** The class name of the object serialized */
    public String className;

    /** The byte code of the class of the object serialized */
    public byte[] classBytes;

    /**
     * The table with all the classes used by the object serialized key = the
     * class name value = the byte array with the byte code
     */
    public Hashtable<String, byte[]> usedClasses;

    /**
     * Create a new JavaMigratingPacket object.
     *
     * @param ClassName class name of the object
     * @param ClassBytes the byte code of the class of the object
     * @param UsedClasses the table with all the classes used by the object
     * @param code the serialized version of the object
     */
    public JavaMigratingPacket(String ClassName, byte[] ClassBytes,
        Hashtable<String, byte[]> UsedClasses, byte[] code) {
        	super(code);
        	className = ClassName;
        	classBytes = ClassBytes;
        	usedClasses = UsedClasses;        
    }

    /**
     * Create a new JavaMigratingPacket object.
     *
     * @param code the object to be serialized
     *
     * @throws IOException
     */
    public JavaMigratingPacket(JavaMigratingCode code) throws IOException {
        this(code.getClass().getName(),
             (code.hasToDeliverCode() ? code.getClassBytes() : null),
             (code.hasToDeliverCode() ? code.getUsedClasses() : null),
             serialize_object(code));
    }

    /**
     * Actually serialize the object into a byte array
     *
     * @param code the object to be serialized
     *
     * @return the byte array with the serialized object
     *
     * @throws IOException
     */
    static protected byte[] serialize_object(JavaMigratingCode code)
        throws IOException {
        byte[] ser_code = null;
        ByteArrayOutputStream byteOStream = new ByteArrayOutputStream();
        ObjectOutputStream objOStream = new ObjectOutputStream(byteOStream);
        objOStream.writeObject(code);
        ser_code = byteOStream.toByteArray();

        return ser_code;
    }

    /**
     * Serialize the object and store it in this packet
     *
     * @param code the object to be serialized
     *
     * @throws IOException
     */
    protected void store_serialized_object(JavaMigratingCode code)
        throws IOException {
        obj_bytes = serialize_object(code);
    }
}
