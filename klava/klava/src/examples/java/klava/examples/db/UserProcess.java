/**
 *   Process.java  version 1.0  Feb 10, 2009
 */
package klava.examples.db;

import java.util.Vector;
import klava.KString;
import klava.KlavaException;
import klava.LogicalLocality;
import klava.Tuple;
import klava.topology.KlavaProcess;

/**
 * Place class description here
 * 
 * @version 1.0
 * @author Fan Yang
 * 
 */
public class UserProcess extends KlavaProcess {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    LogicalLocality db = new LogicalLocality("db");

    KString Item = new KString("Item");

    /*
     * (non-Javadoc)
     * 
     * @see klava.topology.KlavaProcess#executeProcess()
     */
    @Override
    public void executeProcess() throws KlavaException {
        // Define variable
        KString content = new KString();

        // The first tuple
        Vector<Object> vtuple1 = new Vector<Object>();
        vtuple1.addElement(Item);
        vtuple1.addElement(content);
        Tuple tuple1 = new Tuple(vtuple1);

        System.out.println(this.getName() + "--before action 1");

        // ////////the first action/////////////////////
        read(tuple1, db);

        System.out.println(this.getName() + "--after action 1");

        // The second tuple
        Vector<Object> vtuple2 = new Vector<Object>();
        vtuple2.addElement(new KString(this.getName()));
        vtuple2.addElement(content);
        Tuple tuple2 = new Tuple(vtuple2);

        System.out.println(this.getName() + "--before action 2");
        // ////////the second action/////////////////////
        out(tuple2, db);

        System.out.println(this.getName() + "--after action 2");

    }

}
