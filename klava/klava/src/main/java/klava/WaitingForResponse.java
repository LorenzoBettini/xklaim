/*
 * Created on Oct 7, 2005
 */
package klava;

import java.util.Hashtable;

import klava.proto.Response;


/**
 * Associates a process name to the TupleResponse the process is
 * waiting.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.1 $
 */
public class WaitingForResponse<ResponseType extends Response> extends Hashtable<String, ResponseType> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


}
