/*
 * Created on Nov 14, 2005
 */
package klava.events;

import klava.LogicalLocality;

import org.mikado.imc.events.Event;
import org.mikado.imc.protocols.Session;


/**
 * An event concerning a login or a subscribe.
 * 
 * @author Lorenzo Bettini
 */
public class LoginSubscribeEvent extends Event {
    /**
     * The (possible) logical locality involved in the event (in case it is a
     * subscribe event)
     */
    public LogicalLocality logicalLocality = null;

    /**
     * The Session in the event
     */
    public Session session = null;
    
    /**
     * Whether it is a new login/subscribe or it is a
     * logout/unsubscribe
     */
    public boolean removed = false;

    public static final String LOGIN_EVENT = "LOGIN";
    
    public static final String LOGOUT_EVENT = "LOGOUT";

    public static final String SUBSCRIBE_EVENT = "SUBSCRIBE";
    
    public static final String UNSUBSCRIBE_EVENT = "UNSUBSCRIBE";

    /**
     * Generates a Login event
     * 
     * @param source
     * @param session
     */
    public LoginSubscribeEvent(Object source, Session session) {
        super(source);
        this.session = session;
    }
    
    /**
     * Generates a Subscribe event
     * 
     * @param source
     * @param session
     * @param logicalLocality
     */
    public LoginSubscribeEvent(Object source, Session session, LogicalLocality logicalLocality) {
        this(source, session);
        this.logicalLocality = logicalLocality;
    }

    /**
     * @see org.mikado.imc.events.Event#toString()
     */
    @Override
    public String toString() {
        return (logicalLocality == null ? 
                (removed ? LOGOUT_EVENT : LOGIN_EVENT) :
                (removed ? UNSUBSCRIBE_EVENT : SUBSCRIBE_EVENT)) +
            ": " + (logicalLocality != null ? logicalLocality + " ~ " : "") +
            session;
    }

    
}
