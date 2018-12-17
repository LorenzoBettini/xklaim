/*
 * Created on Jan 25, 2006
 */
package klava.tests.junit;

import java.util.Vector;

import org.mikado.imc.common.IMCException;

import junit.framework.TestCase;
import klava.KlavaException;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.topology.ClientNode;
import klava.topology.LogicalNet;
import klava.topology.Net;

/**
 * Tests for Net and ClientNode
 * 
 * @author Lorenzo Bettini
 */
public class NetTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testNet() throws IMCException, KlavaException {
        Vector<PhysicalLocality> localities = new Vector<PhysicalLocality>();
        PhysicalLocality physicalLocality = new PhysicalLocality("tcp-localhost:9999");
        localities.addElement(physicalLocality);
        Net net = new Net(localities);
        
        ClientNode clientNode = new ClientNode(physicalLocality);        
        net.close();
        clientNode.close();
    }
    
    protected void clientLogin(PhysicalLocality physicalLocality) throws KlavaException, IMCException {
        ClientNode clientNode = new ClientNode(physicalLocality);
        clientNode.close();
    }
    
    public void testNetMany() throws IMCException, KlavaException {
        Vector<PhysicalLocality> localities = new Vector<PhysicalLocality>();
        PhysicalLocality physicalLocality = new PhysicalLocality("tcp-localhost:9999");
        localities.addElement(physicalLocality);        
        PhysicalLocality physicalLocality2 = new PhysicalLocality("tcp-localhost:9998");
        localities.addElement(physicalLocality2);        
        PhysicalLocality physicalLocality3 = new PhysicalLocality("tcp-localhost:9997");
        localities.addElement(physicalLocality3);
        Net net = new Net(localities);
        
        clientLogin(physicalLocality);
        clientLogin(physicalLocality2);
        clientLogin(physicalLocality3);
        net.close();        
    }

    
    public void testLogicalNet() throws IMCException, KlavaException {
        Vector<PhysicalLocality> localities = new Vector<PhysicalLocality>();
        PhysicalLocality physicalLocality = new PhysicalLocality("tcp-localhost:9999");
        LogicalLocality logicalLocality = new LogicalLocality("foo");
        localities.addElement(physicalLocality);
        Net net = new LogicalNet(localities);
        
        ClientNode clientNode = new ClientNode(physicalLocality, logicalLocality);
        
        net.close();
        clientNode.close();
    }
}
