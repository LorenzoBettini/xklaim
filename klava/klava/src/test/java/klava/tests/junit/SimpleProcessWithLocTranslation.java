/*
 * Created on Jan 12, 2006
 */
package klava.tests.junit;

import klava.KString;
import klava.KlavaException;
import klava.Tuple;

/**
 * Performs a Locality translation
 * 
 * @author Lorenzo Bettini
 */
public class SimpleProcessWithLocTranslation extends SimpleProcess {

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
        out(new Tuple(translateLocality(self)), destination);
        result = "done";
    }
}
