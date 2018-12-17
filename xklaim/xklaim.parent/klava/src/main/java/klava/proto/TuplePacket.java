/*
 * Created on Sep 26, 2005
 */
package klava.proto;

import klava.PhysicalLocality;
import klava.Tuple;

/**
 * Packet concerning a Tuple operation
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class TuplePacket extends NodePacket {
    public static final String IN_S = "IN";

    public static final String OUT_S = "OUT";

    public static final String EVAL_S = "EVAL";

    public static final String READ_S = "READ";

    public static final String TUPLEBACK_S = "TUPLEBACK";

    public static final String TUPLEABSENT_S = "TUPLEABSENT";

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * The Tuple involved in this packet.
     */
    public Tuple tuple;

    /**
     * The operation performed/requested by this packet
     */
    public String operation;

    /**
     * Whether the requested operation is blocking. Default: false.
     */
    public boolean blocking = false;

    /**
     * Timeout for the operation. If negative it is not considered (i.e.,
     * willing to wait forever).
     * 
     * If the timeout is not negative, then blocking must not be considered.
     */
    public long timeout = -1;

    /**
     * @param dest
     * @param source
     * @param operation
     * @param tuple
     */
    public TuplePacket(PhysicalLocality dest, PhysicalLocality source,
            String operation, Tuple tuple) {
        super(dest, source);
        this.operation = operation;
        this.tuple = tuple;
    }

    public TuplePacket() {
    }

    /**
     * @see klava.proto.NodePacket#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (obj instanceof TuplePacket) {
                TuplePacket tuplePacket = (TuplePacket) obj;
                return operation.equals(tuplePacket.operation)
                        && tuple.equals(tuplePacket.tuple);
            }
        }

        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return super.toString() + "\n" + "OP: " + operation + ", blocking("
                + blocking + "), timeout(" + timeout + ")\n" + "TUPLE: "
                + tuple;
    }
}
