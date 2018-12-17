/*
 * Created on Mar 23, 2006
 */
package klava.tests.junit;

import klava.KlavaException;
import klava.Locality;
import klava.Tuple;
import klava.topology.KlavaProcess;

/**
 * Performs an in of a tuple at a specific locality and out the result at
 * another locality.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class InOutProcess extends KlavaProcess {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * The destination locality of in (default: self)
     */
    public Locality inDestination = self;

    /**
     * The destination Locality of out (default: self)
     */
    public Locality outDestination = self;

    /**
     * The tuple to in from the inDestination and then to out to the
     * outDestination.
     */
    protected Tuple template;

    /**
     * @param template
     */
    public InOutProcess(Tuple template) {
        this.template = template;
    }

    /**
     * @param inDestination
     * @param outDestination
     * @param template
     */
    public InOutProcess(Locality inDestination, Locality outDestination,
            Tuple template) {
        this.inDestination = inDestination;
        this.outDestination = outDestination;
        this.template = template;
    }

    /**
     * @see klava.topology.KlavaProcess#executeProcess()
     */
    @Override
    public void executeProcess() throws KlavaException {
        SystemOutPrint("performing in" + template + "@" + inDestination + "\n");
        in(template, inDestination);
        SystemOutPrint("retrieved " + template + "\n");
        SystemOutPrint("performing out" + template + "@" + outDestination
                + "\n");
        out(template, outDestination);
        SystemOutPrint("exiting" + "\n");
    }

}
