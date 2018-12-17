/*
 * Created on Jan 26, 2005
 *
 */
package org.mikado.imc.protocols;

import java.io.Serializable;


/**
 * Represents and identifier for a session.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.6 $
 */
public class SessionId implements Serializable, Comparable<SessionId> {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
	 * The identifier of the connection layer. Default: tcp
	 */
	protected String connectionProtocolId = "tcp";

	/**
	 * The string used for separating the connection protocol from the rest
	 */
	public static final String PROTO_SEPARATOR = "-";

	/**
	 * A textual representation of this identifier.
	 */
	protected String text;

	/**
	 * Constructs a SessionId.
	 */
	public SessionId() {
	}

	/**
	 * Constructs a SessionId.
	 * 
	 * @param connectionProtocolId
	 *           identifier of the connection layer.
	 */
	public SessionId(String connectionProtocolId) {
		this.connectionProtocolId = connectionProtocolId;
	}
    
    /**
     * Copy-constructs a SessionId.
     * 
     * @param sessionId
     */
    public SessionId(SessionId sessionId) {
        connectionProtocolId = sessionId.connectionProtocolId;
        text = sessionId.text;
    }

	/**
	 * Constructs a SessionId.
	 * 
	 * @param connectionProtocolId
	 *           identifier of the connection layer.
	 * @param text
	 *           textual representation of this session identifier.
	 */
	public SessionId(String connectionProtocolId, String text) {
		this(connectionProtocolId);
		this.text = text;
	}

	/**
	 * Used to compare a session identifier with an object.
	 * 
	 * @param o
	 *           The object to compare to.
	 * 
	 * @return true if the connection layer is the same with this identifier's
	 *         connection layer.
	 */
	public boolean sameId(Object o) {
		if (o == null)
			return false;

		return o.toString().equals(toString());
	}

	/**
	 * @return the connection layer identifier as a string
	 */
	public String toString() {
		return connectionProtocolId + PROTO_SEPARATOR + text;
	}

	/**
	 * @return Returns the connectionProtocolId.
	 */
	public String getConnectionProtocolId() {
		return connectionProtocolId;
	}

	/**
	 * @param connectionProtocolId
	 *           The connectionProtocolId to set.
	 */
	public void setConnectionProtocolId(String connectionProtocolId) {
		this.connectionProtocolId = connectionProtocolId;
	}

	/**
	 * @return Returns the text.
	 */
	public final String getText() {
		return text;
	}

	/**
	 * @param text
	 *           The text to set.
	 */
	public final void setText(String text) {
		this.text = text;
	}
	
	public static SessionId parseSessionId(String str)
			throws ProtocolException {
		int proto_index = str.indexOf(PROTO_SEPARATOR);
		if (proto_index == -1) {
			throw new ProtocolException("unspecified connection protocol identifier");
		}
		
		return new SessionId(str.substring(0, proto_index), str.substring(proto_index + 1));		
	}

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof SessionId) && 
			((obj != null && obj.toString().equals(toString())));
	}

	public int compareTo(SessionId o) {
		return toString().compareTo(o.toString());
	}    
}
