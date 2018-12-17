package klava;

import org.mikado.imc.protocols.IpSessionId;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.SessionId;

public class PhysicalLocality extends Locality {
    /**
     * The session identifier underlying this locality
     */
    protected SessionId sessionId;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public PhysicalLocality() {
        super();
    }

    public PhysicalLocality(String l) throws KlavaMalformedPhyLocalityException {
        try {
            sessionId = SessionId.parseSessionId(l);
        } catch (ProtocolException e) {
            /*
             * instead of quitting try to add the default protocol tcp
             * identifier
             */
            try {
                sessionId = SessionId.parseSessionId("tcp"
                        + SessionId.PROTO_SEPARATOR + l);
            } catch (ProtocolException e1) {
                /* OK: there are real problems! */
                throw new KlavaMalformedPhyLocalityException(l + " "
                        + e.getMessage());
            }
        }

        locality = sessionId.toString();
    }

    public PhysicalLocality(String host, int port)
            throws KlavaMalformedPhyLocalityException {
        this("tcp" + SessionId.PROTO_SEPARATOR + host + ":" + port);
    }

    public PhysicalLocality(PhysicalLocality l) {
        super(l);
        sessionId = l.sessionId;
    }

    public PhysicalLocality(Locality l) {
        super(l);
    }

    public PhysicalLocality(KString s)
            throws KlavaMalformedPhyLocalityException {
        this(s.toString());
    }

    public PhysicalLocality(SessionId l) {
        sessionId = l;

        locality = sessionId.toString();
    }

    public Object duplicate() {
        return new PhysicalLocality(this);
    }

    public String toString() {
        if (locality != null)
            return locality.toString();
        else
            return "!PhysicalLocality";
    }

    // TODO: remove these two methods
    public String getHost() {
        return ((IpSessionId) sessionId).getHost();
    }

    public int getPort() {
        return ((IpSessionId) sessionId).getPort();
    }

    /**
     * @return Returns the ipSessionId.
     */
    public final SessionId getSessionId() {
        return sessionId;
    }

    public void setValue(Object o) {
        if (o instanceof PhysicalLocality) {
            sessionId = ((PhysicalLocality) o).sessionId;
        }

        super.setValue(o);
    }

    public void setValue(String o) throws KlavaException {
        setValue(new PhysicalLocality(o));
    }
}
