/**
 *   DataNode.java  version 1.0  Feb 10, 2009
 */
package klava.examples.db;

import java.util.Vector;

import klava.KString;
import klava.KlavaException;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.ClientNode;

/**
 * Place class description here
 * 
 * @version 1.0
 * @author Fan Yang
 * 
 */
public class DataNode extends ClientNode {

    /**
     * @param server
     * @param logicalLocality
     * @throws KlavaException
     */
    public DataNode(PhysicalLocality server, LogicalLocality logicalLocality)
            throws KlavaException {
        super(server, logicalLocality);

        Vector<Object> vtuple1 = new Vector<Object>();
        vtuple1.addElement(new KString("Item"));
        vtuple1.addElement(new KString("Value"));
        Tuple tuple1 = new Tuple(vtuple1);
        this.out(tuple1);
    }

    public void printTupleSpace() {
        System.out.println(this.nodeName + "--TupleSpace: " + getTupleSpace());
    }

}
