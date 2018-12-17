/*
 * Created on Feb 1, 2006
 */
package klava.tests.junit;

import klava.KString;
import klava.KlavaException;
import klava.Tuple;
import klava.topology.KlavaProcess;

/**
 * Simply performs in of a tuple with a string at self, and puts
 * back in the tuple space a tuple with the read string and the
 * process name.
 * 
 * @author Lorenzo Bettini
 */
public class InProcessAtSelf extends KlavaProcess {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public InProcessAtSelf() {
        super();
    }

    /**
     * @param name
     */
    public InProcessAtSelf(String name) {
        super(name);
    }

    /**
     * Simply performs in of a tuple with a string at self
     * 
     * @see klava.topology.KlavaProcess#executeProcess()
     */
    @Override
    public void executeProcess() throws KlavaException {
        System.out.println("waiting for a tuple");
        Tuple template = new Tuple(new KString());
        in(template, self);
        System.out.println("found tuple: " + template);
        Tuple tuple = new Tuple(new KString(getName()), template.getItem(0)); 
        out(tuple, self);
        System.out.println("put tuple: " + tuple);
    }

}
