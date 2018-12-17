package klava.tests.junit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.mikado.imc.protocols.IMCMarshaler;
import org.mikado.imc.protocols.IMCUnMarshaler;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.TransmissionChannel;

import junit.framework.TestCase;
import klava.KInteger;
import klava.KString;
import klava.KlavaMalformedPhyLocalityException;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.TupleSpaceVector;
import klava.proto.TupleOpState;
import klava.proto.TuplePacket;
import klava.proto.TupleState;

/**
 * Tests for the TupleState class
 * 
 * @author Lorenzo Bettini
 * @versione $Revision: 1.1 $
 * 
 */
public class TupleStateTest extends TestCase {
    TupleState tupleState = new TupleState();

    public void testString() throws ProtocolException, IOException,
            ClassNotFoundException {
        System.out.println("*** testString");

        /*
         * the string contains some \n to test the right sending and retrieving
         * of actual fields
         */
        Tuple t1 = new Tuple("foo\n\n");

        write_and_read_tuple(t1, true);
        write_and_read_tuple(new Tuple(t1 + " bar"), true);
    }

    public void testEmptyTuple() throws ProtocolException, IOException,
            ClassNotFoundException {
        System.out.println("*** testEmpty");
        Tuple t1 = new Tuple();

        write_and_read_tuple(t1, true);
    }

    public void testEmptyTS() throws ProtocolException, IOException,
            ClassNotFoundException {
        System.out.println("*** testEmptyTS");
        Tuple t1 = new Tuple(new TupleSpaceVector());

        write_and_read_tuple(t1, true);
    }

    public void testTupleWrite() {
        IMCMarshaler marshaler = new IMCMarshaler(System.out);

        Tuple tuple = new Tuple(Integer.class, new KString("hello"),
                new KString());
        TupleSpaceVector tupleSpace = new TupleSpaceVector();
        tupleSpace.add(tuple);
        tupleSpace.add(tuple);

        tupleState.setDoRead(false);

        try {
            System.out.println("writing " + tuple);
            tupleState.setTuple(tuple);
            tupleState.enter(null, new TransmissionChannel(marshaler));
            tupleState.setTuple(new Tuple(tuple, tuple));
            System.out.println("writing " + tupleState.getTuple());
            tupleState.enter(null, new TransmissionChannel(marshaler));
            tupleState.setTuple(new Tuple(tupleSpace));
            System.out.println("writing " + tupleState.getTuple());
            tupleState.enter(null, new TransmissionChannel(marshaler));
        } catch (ProtocolException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testTupleWriteRead() throws ProtocolException, IOException,
            ClassNotFoundException {
        Tuple t1 = new Tuple(Integer.class, new KString("hello"), new KString());
        Tuple t2 = new Tuple(new KInteger(10), new KString("foo"), new Boolean(
                false));
        Tuple t3 = new Tuple(t1, t2);
        Tuple t4 = new Tuple(Tuple.class, t1);

        write_and_read_tuple(t1, true);
        write_and_read_tuple(t2, true);
        write_and_read_tuple(t3, true);
        write_and_read_tuple(t4, true);

        TupleSpaceVector tupleSpace = new TupleSpaceVector();
        tupleSpace.add(t1);
        tupleSpace.add(t2);
        tupleSpace.add(t3);
        tupleSpace.add(t4);

        Tuple complex = new Tuple(tupleSpace);
        write_and_read_tuple(complex, true);

        Tuple wretrieved = new Tuple(new String("foo"));
        wretrieved.setHandleRetrieved(true);
        wretrieved.addRetrieved(t1.getTupleId());
        wretrieved.addRetrieved(t2.getTupleId());
        write_and_read_tuple(wretrieved, true);
    }

    public void testTuplePacketWriteRead() throws ProtocolException,
            IOException, ClassNotFoundException,
            KlavaMalformedPhyLocalityException {
        Tuple t1 = new Tuple(Integer.class, new KString("hello"), new KString());
        Tuple t2 = new Tuple(new KInteger(10), new KString("foo"), new Boolean(
                false));
        Tuple t3 = new Tuple(t1, t2);

        PhysicalLocality from = new PhysicalLocality("localhost", 9999);
        PhysicalLocality to = new PhysicalLocality("localhost", 11000);

        TuplePacket tuplePacket = new TuplePacket(from, to, TuplePacket.OUT_S,
                t1);

        TuplePacket tuplePacket2 = new TuplePacket(from, to, TuplePacket.OUT_S,
                t3);
        tuplePacket2.processName = "foo";

        write_and_read_tuple_packet(tuplePacket);
        write_and_read_tuple_packet(tuplePacket2);
    }

    /**
     * Write a tuple into a Marshaler then read it from an UnMarshaler and
     * checks that they are equal.
     * 
     * @param tuple
     * @param expectedEqual
     *            TODO
     * 
     * @throws IOException
     * @throws ProtocolException
     * @throws ClassNotFoundException
     */
    private void write_and_read_tuple(Tuple tuple, boolean expectedEqual)
            throws IOException, ProtocolException, ClassNotFoundException {
        System.out.println("Write tuple: " + tuple.toString());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        tupleState.setDoRead(false);
        tupleState.setTuple(tuple);
        tupleState.enter(null, new TransmissionChannel(new IMCMarshaler(out)));

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        TupleState tupleState2 = new TupleState();
        tupleState2.setDoRead(true);
        tupleState2
                .enter(null, new TransmissionChannel(new IMCUnMarshaler(in)));
        Tuple read = tupleState2.getTuple();

        System.out.println("Read tuple: " + read.toString());

        assertEquals(tuple.getTupleId(), read.getTupleId());
        assertEquals(tuple, read);
    }

    /**
     * Write a TuplePacket into a Marshaler then read it from an UnMarshaler and
     * checks that they are equal.
     * 
     * @param tuple
     * 
     * @throws IOException
     * @throws ProtocolException
     * @throws ClassNotFoundException
     */
    private void write_and_read_tuple_packet(TuplePacket tuplePacket)
            throws IOException, ProtocolException, ClassNotFoundException {
        System.out.println("Write tuple packet:\n" + tuplePacket.toString());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        TupleOpState tupleOpState = new TupleOpState();

        tupleOpState.setDoRead(false);
        tupleOpState.setTuplePacket(tuplePacket);
        tupleOpState
                .enter(null, new TransmissionChannel(new IMCMarshaler(out)));

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        TupleOpState tupleOpState2 = new TupleOpState();
        tupleOpState2.setDoRead(true);
        tupleOpState2.enter(null, new TransmissionChannel(
                new IMCUnMarshaler(in)));
        TuplePacket read = tupleOpState2.getTuplePacket();

        System.out.println("Read tuple packet:\n" + read.toString());

        assertEquals(tuplePacket, read);
    }
}
