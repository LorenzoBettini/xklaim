/*
 * Created on Jan 11, 2005
 *
 */
package org.mikado.imc.protocols;

import java.util.Vector;

/**
 * Represents a ProtocolStack whose elements are instances of ProtocolLayer
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.13 $
 */
public class ProtocolStack {
    /** The first layer of the stack, i.e., the one at the top level. */
    protected Vector<ProtocolLayer> layers;

    /** the mutex for writing */
    private WritingMutex writingMutex = new WritingMutex();

    /**
     * The session associated to this stack.
     */
    protected Session session = null;

    /**
     * Constructs a stack initialized with one layer
     * 
     * @param first
     *            The layer with which the stack must be initialized.
     * @throws ProtocolException
     */
    public ProtocolStack(ProtocolLayer first) throws ProtocolException {
        layers = new Vector<ProtocolLayer>();
        insertLayer(first);
    }

    /**
     * Creates an empty ProtocolStack object.
     */
    public ProtocolStack() {
        layers = new Vector<ProtocolLayer>();
    }

    /**
     * Adds a layer at the bottom of the stack. If no layer is set, then the
     * passed layer is set as the first layer.
     * 
     * @param layer
     *            The layer to add at the bottom of the stack
     * @throws ProtocolException
     */
    public void setLowLayer(ProtocolLayer layer) throws ProtocolException {
        insertLayer(layer);
    }

    /**
     * Inserts a layer after the first (highest) layer of the stack If no layer
     * is set, then the passed layer is set as the first layer.
     * 
     * @param layer
     *            The layer to add at the bottom of the stack
     * @throws ProtocolException
     */
    public void insertLayer(ProtocolLayer layer) throws ProtocolException {
        layers.addElement(layer);
        layer.setProtocolStack(this);
    }

    /**
     * Inserts a layer as the first (highest) layer of the stack If no layer is
     * set, then the passed layer is set as the first layer.
     * 
     * @param layer
     *            The layer to add at the bottom of the stack
     * @throws ProtocolException
     */
    public void insertFirstLayer(ProtocolLayer layer) throws ProtocolException {
        layers.add(0, layer);
        layer.setProtocolStack(this);
    }

    /**
     * Inserts the toInsertLayer after the before layer.
     * 
     * If the specified before layer is not found throw an exception.
     * 
     * @param before
     * @param toInsert
     * @throws ProtocolException
     */
    public void insertAfter(ProtocolLayer before, ProtocolLayer toInsert)
            throws ProtocolException {
        int index = layers.indexOf(before);
        if (index < 0)
            throw new ProtocolException("ProtocolLayer not found");

        layers.add(index + 1, toInsert);
        toInsert.setProtocolStack(this);
    }

    /**
     * Inserts the specified toInsert ProtocolLayer in the place of the
     * specified toReplace ProtocolLayer (which will then no longer belong to
     * the stack).
     * 
     * If the specified toReplace layer is not found throw an exception.
     * 
     * @param toReplace
     * @param toInsert
     * @throws ProtocolException
     */
    public void replace(ProtocolLayer toReplace, ProtocolLayer toInsert)
            throws ProtocolException {
        int index = layers.indexOf(toReplace);
        if (index < 0)
            throw new ProtocolException("ProtocolLayer not found");

        layers.setElementAt(toInsert, index);
        toInsert.setProtocolStack(this);
    }

    /**
     * Invokes doCreateUnMarshaler() on each element of the stack, starting from
     * the lowest.
     * 
     * @return the created UnMarshaler
     * 
     * @throws ProtocolException
     */
    public UnMarshaler createUnMarshaler() throws ProtocolException {
        return createUnMarshaler(null);
    }

    /**
     * Invokes doCreateUnMarshaler() on each element of the stack (considering
     * currentLayer as the top of the stack), starting from the lowest.
     * 
     * @param currentLayer
     * @return the created UnMarshaler
     * @throws ProtocolException
     */
    public UnMarshaler createUnMarshaler(ProtocolLayer currentLayer)
            throws ProtocolException {
        UnMarshaler unMarshaler = null;

        if (layers.size() == 0) {
            throw new ProtocolException("protocol stack is empty");
        }

        for (int i = layers.size(); i > 0; --i) {
            if (currentLayer == null || currentLayer != layers.elementAt(i - 1)) {
                unMarshaler = layers.elementAt(i - 1).doCreateUnMarshaler(
                        unMarshaler);
            } else {
                break;
            }
        }

        return unMarshaler;
    }

    /**
     * Closes this protocol stack. Start from the highest layer down to the
     * lowest (not considering currentLayer).
     * 
     * @throws ProtocolException
     */
    public void close() throws ProtocolException {
        ProtocolException protocolException = null;

        try {
            for (int i = 0; i < layers.size(); ++i) {
                ProtocolLayer protocolLayer = layers.elementAt(i);
                /* we must call close on every layer */
                try {
                    protocolLayer.doClose();
                } catch (ProtocolException e) {
                    /* so if there's an exception we must go on */
                    protocolException = e;
                }
            }
        } finally {
            writingMutex.endWriting();
        }

        // if there's someone waiting for writing they will be waken-up.

        /* the possible last exception is thrown */
        if (protocolException != null)
            throw protocolException;
    }

    /**
     * Invokes doReleaseMarshaler() on each element of the stack, starting from
     * the highest.
     * 
     * @param marshaler
     * @throws ProtocolException
     */
    public void releaseMarshaler(Marshaler marshaler) throws ProtocolException {
        releaseMarshaler(marshaler, null);
    }

    /**
     * Invokes doReleaseMarshaler() on each element of the stack (considering
     * currentLayer as the top of the stack), starting from the highest.
     * 
     * @param marshaler
     * @param currentLayer
     * @throws ProtocolException
     */
    public void releaseMarshaler(Marshaler marshaler, ProtocolLayer currentLayer)
            throws ProtocolException {
        if (layers.size() == 0) {
            throw new ProtocolException("protocol stack is empty");
        }

        try {
            int i = 0;

            if (currentLayer != null) {
                for (; i < (layers.size()); ++i) {
                    if (currentLayer == layers.elementAt(i)) {
                        ++i;
                        break;
                    }
                }
            }

            for (; i < (layers.size()); ++i) {
                layers.elementAt(i).doReleaseMarshaler(marshaler);
            }
        } catch (ProtocolException e) {
            throw e;
        } finally {
            writingMutex.endWriting();
        }
    }

    /**
     * Invokes doReleaseUnMarshaler() on each element of the stack, starting
     * from the highest.
     * 
     * @param unMarshaler
     * 
     * @throws ProtocolException
     */
    public void releaseUnMarshaler(UnMarshaler unMarshaler)
            throws ProtocolException {
        releaseUnMarshaler(unMarshaler, null);
    }

    /**
     * Invokes doReleaseUnMarshaler() on each element of the stack (considering
     * currentLayer as the top of the stack), starting from the highest.
     * 
     * @param unMarshaler
     * @param currentLayer
     * @throws ProtocolException
     */
    public void releaseUnMarshaler(UnMarshaler unMarshaler, ProtocolLayer currentLayer)
            throws ProtocolException {
        if (layers.size() == 0) {
            throw new ProtocolException("protocol stack is empty");
        }

        int i = 0;

        if (currentLayer != null) {
            for (; i < (layers.size()); ++i) {
                if (currentLayer == layers.elementAt(i)) {
                    ++i;
                    break;
                }
            }
        }

        for (; i < (layers.size()); ++i) {
            layers.elementAt(i).doReleaseUnMarshaler(unMarshaler);
        }
    }

    /**
     * Invokes doCreateMarshaler() on each element of the stack, starting from the lowest.
     * 
     * @return the created Marshaler
     * 
     * @throws ProtocolException
     */
    public Marshaler createMarshaler() throws ProtocolException {
        return createMarshaler(null);
    }
    
    /**
     * Invokes doCreateMarshaler() on each element of the stack (considering
     * currentLayer as the top of the stack), starting from the lowest.
     * 
     * @param currentLayer
     * @return the created Marshaler
     * @throws ProtocolException
     */
    public Marshaler createMarshaler(ProtocolLayer currentLayer) throws ProtocolException {
        if (layers.size() == 0) {
            throw new ProtocolException("protocol stack is empty");
        }

        writingMutex.startWriting();

        Marshaler marshaler = null;

        try {
            for (int i = layers.size(); i > 0; --i) {
                if (currentLayer == null
                        || currentLayer != layers.elementAt(i - 1)) {
                    marshaler = layers.elementAt(i - 1).doCreateMarshaler(
                            marshaler);
                } else {
                    break;
                }
            }
        } catch (ProtocolException e) {
            writingMutex.endWriting();
            throw e;
        }

        return marshaler;
    }

    /**
     * Accepts a new session using the passed SessionStarter. It updates the
     * stack setting as the low layer the one returned by the SessionStarter.
     * 
     * @param sessionStarter
     * @return The new created Session.
     * @throws ProtocolException
     */
    public Session accept(SessionStarter sessionStarter)
            throws ProtocolException {
        Session startedSession = sessionStarter.accept();
        setLowLayer(startedSession.getProtocolLayer());
        session = startedSession;
        return startedSession;
    }

    /**
     * Establishes a new session using the passed SessionStarter. It updates the
     * stack setting as the low layer the one returned by the SessionStarter.
     * 
     * @param sessionStarter
     * @return the established Session
     * @throws ProtocolException
     */
    public Session connect(SessionStarter sessionStarter)
            throws ProtocolException {
        Session startedSession = sessionStarter.connect();
        setLowLayer(startedSession.getProtocolLayer());
        session = startedSession;
        return startedSession;
    }

    /**
     * @return The Session associated with this stack.
     * @throws ProtocolException
     */
    public Session getSession() throws ProtocolException {
        return session;
    }

    /**
     * Implements a simple mutex to serialize writing activities.
     * 
     * @author bettini
     */
    public class WritingMutex {
        /** the actual mutex */
        private boolean writing = false;

        /**
         * If no one is writing acquires the mutex. Otherwise, blocks until the
         * mutex is released.
         */
        synchronized void startWriting() {
            while (writing) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // simply return
                }
            }

            writing = true;
        }

        /**
         * Releases the mutex and notifies those who are waiting to write.
         */
        synchronized void endWriting() {
            writing = false;
            notifyAll();
        }
    }

    /**
     * @param session
     *            The session to set.
     */
    public final void setSession(Session session) {
        this.session = session;
    }

    /**
     * Returns the string represenation of the underlying session.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return (session == null ? "null session" : session.toString());
    }

    /**
     * @see java.util.Vector#contains(java.lang.Object)
     */
    public boolean contains(ProtocolLayer elem) {
        return layers.contains(elem);
    }

    /**
     * @see java.util.Vector#indexOf(java.lang.Object)
     */
    public int indexOf(ProtocolLayer elem) {
        return layers.indexOf(elem);
    }
}
