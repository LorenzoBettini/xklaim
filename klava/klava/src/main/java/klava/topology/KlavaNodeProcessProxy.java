/**
 * created: Jan 3, 2006
 */
package klava.topology;

import klava.KlavaException;
import klava.Locality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.TupleItem;

import org.mikado.imc.common.IMCException;
import org.mikado.imc.topology.NodeProcess;
import org.mikado.imc.topology.NodeProcessProxy;


/**
 * A specialization of NodeProcessProxy with functionalities that are specific
 * of a klava node. It contains a reference to a klava node and delegates to it
 * all the operations.
 * 
 * @author Lorenzo Bettini
 * 
 */
public class KlavaNodeProcessProxy extends NodeProcessProxy {
    /**
     * The klava node to delegate operations to.
     */
    protected KlavaNode klavaNode;

    /**
     * @param node
     */
    public KlavaNodeProcessProxy(KlavaNode node) {
        super(node);
        klavaNode = node;
    }

    /**
     * @see org.mikado.imc.topology.NodeProcessProxy#addNodeProcess(org.mikado.imc.topology.NodeProcess)
     */
    public void addNodeProcess(NodeProcess nodeProcess) throws IMCException {
        klavaNode.addNodeProcess(nodeProcess);
    }

    /**
     * @see org.mikado.imc.topology.NodeProcessProxy#executeNodeProcess(org.mikado.imc.topology.NodeProcess)
     */
    public void executeNodeProcess(NodeProcess nodeProcess)
            throws InterruptedException, IMCException {
        klavaNode.executeNodeProcess(nodeProcess);
    }

    /**
     * @see klava.topology.KlavaNode#in_nb(klava.Tuple, klava.Locality)
     */
    public boolean in_nb(Tuple tuple, Locality destination)
            throws KlavaException {
        return klavaNode.in_nb(tuple, destination);
    }

    /**
     * @see klava.topology.KlavaNode#in_nb(klava.Tuple)
     */
    public boolean in_nb(Tuple tuple) {
        return klavaNode.in_nb(tuple);
    }

    /**
     * @see klava.topology.KlavaNode#in_t(klava.Tuple, klava.Locality, long)
     */
    public boolean in_t(Tuple tuple, Locality destination, long timeout)
            throws KlavaException {
        return klavaNode.in_t(tuple, destination, timeout);
    }

    /**
     * @see klava.topology.KlavaNode#in_t(klava.Tuple, long)
     */
    public boolean in_t(Tuple tuple, long timeout) throws KlavaException {
        return klavaNode.in_t(tuple, timeout);
    }

    /**
     * @see klava.topology.KlavaNode#in(klava.Tuple, klava.Locality)
     */
    public void in(Tuple tuple, Locality destination) throws KlavaException {
        klavaNode.in(tuple, destination);
    }

    /**
     * @see klava.topology.KlavaNode#in(klava.Tuple)
     */
    public void in(Tuple tuple) throws KlavaException {
        klavaNode.in(tuple);
    }

    /**
     * @see klava.topology.KlavaNode#out(klava.Tuple, klava.Locality)
     */
    public void out(Tuple tuple, Locality destination) throws KlavaException {
        klavaNode.out(tuple, destination);
    }

    /**
     * @see klava.topology.KlavaNode#out(klava.Tuple)
     */
    public void out(Tuple tuple) {
        klavaNode.out(tuple);
    }

    /**
     * @see klava.topology.KlavaNode#read_nb(klava.Tuple, klava.Locality)
     */
    public boolean read_nb(Tuple tuple, Locality destination)
            throws KlavaException {
        return klavaNode.read_nb(tuple, destination);
    }

    /**
     * @see klava.topology.KlavaNode#read_nb(klava.Tuple)
     */
    public boolean read_nb(Tuple tuple) {
        return klavaNode.read_nb(tuple);
    }

    /**
     * @see klava.topology.KlavaNode#read_t(klava.Tuple, klava.Locality, long)
     */
    public boolean read_t(Tuple tuple, Locality destination, long timeout)
            throws KlavaException {
        return klavaNode.read_t(tuple, destination, timeout);
    }

    /**
     * @see klava.topology.KlavaNode#read_t(klava.Tuple, long)
     */
    public boolean read_t(Tuple tuple, long timeout) throws KlavaException {
        return klavaNode.read_t(tuple, timeout);
    }

    /**
     * @see klava.topology.KlavaNode#read(klava.Tuple, klava.Locality)
     */
    public void read(Tuple tuple, Locality destination) throws KlavaException {
        klavaNode.read(tuple, destination);
    }

    /**
     * @see klava.topology.KlavaNode#read(klava.Tuple)
     */
    public void read(Tuple tuple) throws KlavaException {
        klavaNode.read(tuple);
    }

    /**
     * @see klava.topology.KlavaNode#eval(klava.topology.KlavaProcess, klava.Locality)
     */
    public void eval(KlavaProcess klavaProcess, Locality destination) throws KlavaException {
        klavaNode.eval(klavaProcess, destination);
    }

    /**
     * @see klava.topology.KlavaNode#eval(klava.topology.KlavaProcess)
     */
    public void eval(KlavaProcess klavaProcess) throws KlavaException {
        klavaNode.eval(klavaProcess);
    }

    /**
     * @see klava.topology.KlavaNode#makeClosure(klava.Tuple,
     *      klava.PhysicalLocality)
     */
    public void makeClosure(Tuple tuple, PhysicalLocality forSelf)
            throws KlavaException {
        klavaNode.makeClosure(tuple, forSelf);
    }

    /**
     * @see klava.topology.KlavaNode#makeClosure(klava.TupleItem,
     *      klava.PhysicalLocality)
     */
    public TupleItem makeClosure(TupleItem tupleItem, PhysicalLocality forSelf)
            throws KlavaException {
        return klavaNode.makeClosure(tupleItem, forSelf);
    }

    /**
     * @see klava.topology.KlavaNode#getPhysical(klava.Locality)
     */
    public PhysicalLocality getPhysical(Locality locality)
            throws KlavaException {
        return klavaNode.getPhysical(locality);
    }

}
