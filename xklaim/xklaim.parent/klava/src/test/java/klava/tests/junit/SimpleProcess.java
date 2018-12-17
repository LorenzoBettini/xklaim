/*
 * Created on Jan 11, 2006
 */
package klava.tests.junit;

import klava.KString;
import klava.KlavaException;
import klava.Locality;
import klava.Tuple;
import klava.topology.KlavaProcess;

/**
 * @author Lorenzo Bettini
 * 
 */
public class SimpleProcess extends KlavaProcess {
    String result = "";

    /**
     * By default uses self as the destination
     */
    Locality destination = self;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see klava.topology.KlavaProcess#executeProcess()
     */
    @Override
    public void executeProcess() throws KlavaException {
        System.out.println(getClass().getName() + ": communicating with "
                + destination);
        out(new Tuple(new KString("foo")), destination);
        Tuple template = new Tuple(new KString());
        read(template, destination);
        result = "done";
    }

}