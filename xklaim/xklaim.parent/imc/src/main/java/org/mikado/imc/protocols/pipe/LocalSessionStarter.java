/*
 * Created on Jan 30, 2006
 */
package org.mikado.imc.protocols.pipe;

import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingQueue;

import org.mikado.imc.protocols.AlreadyBoundSessionStarterException;
import org.mikado.imc.protocols.ProtocolException;
import org.mikado.imc.protocols.Session;
import org.mikado.imc.protocols.SessionId;
import org.mikado.imc.protocols.SessionIdBindException;
import org.mikado.imc.protocols.SessionStarter;
import org.mikado.imc.protocols.UnboundSessionStarterException;

/**
 * A SessionStarter that creates "local" sessions, i.e., communications among
 * local threads with shared memory (e.g., pipes).
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class LocalSessionStarter extends SessionStarter {

    /**
     * @author Lorenzo Bettini
     * @version $Revision: 1.4 $
     */
    public static class PipeTable {
        /**
         * table associating a SessionId to a PipeStruct, and will be used by
         * accept and connect.
         */
        protected Hashtable<SessionId, LinkedBlockingQueue<PipeStruct>> acceptAssociations = new Hashtable<SessionId, LinkedBlockingQueue<PipeStruct>>();

        /**
         * Tries to register a SessionId for accepts. If this is already
         * registered it returns false, otherwise true.
         * 
         * @param sessionId
         * @throws IOException
         * @throws SessionIdBindException
         */
        synchronized void registerForAccept(SessionId sessionId)
                throws IOException, SessionIdBindException {
            if (acceptAssociations.get(sessionId) != null)
                throw new SessionIdBindException(sessionId);

            LinkedBlockingQueue<PipeStruct> linkedBlockingQueue = new LinkedBlockingQueue<PipeStruct>();
            acceptAssociations.put(sessionId, linkedBlockingQueue);
        }

        /**
         * Waits for some one else to connect on the specified SessionId.
         * 
         * @param sessionId
         * @return the PipeStruct corresponding to the session
         * @throws InterruptedException
         * @throws ProtocolException
         */
        PipeStruct accept(SessionId sessionId) throws InterruptedException,
                ProtocolException {
            LinkedBlockingQueue<PipeStruct> linkedBlockingQueue = acceptAssociations
                    .get(sessionId);
            if (linkedBlockingQueue == null)
                throw new UnboundSessionStarterException(sessionId.toString());

            /* wait for something to be put in the queue */
            PipeStruct pipeStruct = linkedBlockingQueue.take();
            if (pipeStruct.closed) {
                /* put it back for others */
                linkedBlockingQueue.put(pipeStruct);
                throw new ProtocolException("closed " + sessionId);
            }

            return pipeStruct;
        }

        /**
         * Tries to connect to another end waiting on the specified SessionId.
         * 
         * @param sessionId
         * @return the PipeStruct corresponding to the session
         * @throws ProtocolException
         * @throws InterruptedException
         * @throws IOException
         */
        PipeStruct connect(SessionId sessionId) throws ProtocolException,
                InterruptedException, IOException {
            LinkedBlockingQueue<PipeStruct> linkedBlockingQueue = acceptAssociations
                    .get(sessionId);
            if (linkedBlockingQueue == null)
                throw new ProtocolException("cannot connect to "
                        + sessionId.toString());

            /*
             * upon connection, uses the text of the session id we connected to,
             * to generate our local session id
             */
            PipeStruct pipeStruct = new PipeStruct(sessionId.getText());
            linkedBlockingQueue.put(pipeStruct);

            return pipeStruct;
        }

        /**
         * Tries to connect to another end waiting on the specified SessionId.
         * Also specifies the SessionId of this local end
         * 
         * @param localSessionId
         * @param sessionId
         * @return the PipeStruct corresponding to the session
         * @throws ProtocolException
         * @throws InterruptedException
         * @throws IOException
         */
        PipeStruct connect(SessionId localSessionId, SessionId sessionId)
                throws ProtocolException, InterruptedException, IOException {
            /*
             * this is not actually for accepting, but only to ensure we lock
             * this local session id
             */
            registerForAccept(localSessionId);

            LinkedBlockingQueue<PipeStruct> linkedBlockingQueue = acceptAssociations
                    .get(sessionId);
            if (linkedBlockingQueue == null) {
                /*
                 * we must deregister the local session id, otherwise that would
                 * stay locked
                 */
                acceptAssociations.remove(localSessionId);

                throw new ProtocolException("cannot connect to "
                        + sessionId.toString());
            }

            /*
             * upon connection, uses specified local session identifier
             */
            PipeStruct pipeStruct = new PipeStruct("", localSessionId.getText());
            linkedBlockingQueue.put(pipeStruct);

            return pipeStruct;
        }

        /**
         * Signal all the threads waiting to accept on the specified SessionId
         * that this SessionId is now closed (another registerForAccept will be
         * required).
         * 
         * @param sessionId
         * @throws ProtocolException
         */
        synchronized void close(SessionId sessionId) throws ProtocolException {
            LinkedBlockingQueue<PipeStruct> linkedBlockingQueue = acceptAssociations
                    .remove(sessionId);
            if (linkedBlockingQueue == null)
                return; // ok, already closed

            /* remove all elements */
            linkedBlockingQueue.clear();

            /* and signal that it is closed */
            PipeStruct pipeStruct;
            try {
                pipeStruct = new PipeStruct("");
                pipeStruct.closed = true;
                linkedBlockingQueue.offer(pipeStruct);
            } catch (IOException e) {
                throw new ProtocolException(e);
            }
        }
    }

    /**
     * This is used internally for associating the pipe streams to a specific
     * SessionId
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.4 $
     */
    public static class PipeStruct {
        /**
         * The actual pipe streams
         */
        ProtocolLayerPipe protocolLayerPipe;

        /**
         * Whether this accepting channel was closed.
         */
        boolean closed = false;

        /**
         * The id of this PipeStruct
         */
        SessionId sessionId;

        /**
         * counter used to generate ids
         */
        static int i = 0;

        /**
         * generates a SessionId by using the incremental counter (and the
         * prefix)
         * 
         * @param prefix
         * @throws IOException
         */
        public PipeStruct(String prefix) throws IOException {
            protocolLayerPipe = new ProtocolLayerPipe();
            sessionId = nextId(prefix);
        }

        /**
         * generates a SessionId by NOT using the incremental counter, but only
         * the name (and the prefix)
         * 
         * @param prefix
         * @param name
         * @throws IOException
         */
        public PipeStruct(String prefix, String name) throws IOException {
            protocolLayerPipe = new ProtocolLayerPipe();
            sessionId = new SessionId("pipe", prefix + name);
        }

        static private synchronized SessionId nextId(String prefix) {
            return new SessionId("pipe", prefix + "/" + ++i);
        }
    }

    /**
     * The global table
     */
    protected static PipeTable pipeTable = new PipeTable();

    /**
     * This is used as a prefix in automatically generated SessionId in binds
     */
    String prefix = "";

    /**
     * @param prefix
     *            used as a prefix in automatically generated SessionId in binds
     */
    public LocalSessionStarter(String prefix) {
        this.prefix = prefix;
    }

    /**
     * 
     */
    public LocalSessionStarter() {

    }

    /**
     * @param localSessionId
     * @param remoteSessionId
     */
    public LocalSessionStarter(SessionId localSessionId,
            SessionId remoteSessionId) {
        super(localSessionId, remoteSessionId);
    }

    /**
     * @see org.mikado.imc.protocols.SessionStarter#accept()
     */
    @Override
    public Session accept() throws ProtocolException {
        checkLocalSessionId();

        if (usedForConnect)
            throw new ProtocolException("already used for connecting");

        if (!usedForAccept && !usedForConnect) {
            usedForAccept = true;

            /*
             * then it is the first time we use it, so we must try to register
             * it for accept
             */
            boolean bound = false;

            /* the last exception that made us fail */
            Exception exception = null;

            synchronized (this) {
                int retry = 5;
                int pause = 1;

                while (retry-- > 0) {
                    try {
                        pipeTable.registerForAccept(getLocalSessionId());
                        bound = true;
                        break;
                    } catch (IOException e) {
                        exception = e;
                        System.err.print("accept fail on "
                                + getLocalSessionId());
                    } catch (ProtocolException e) {
                        exception = e;
                        System.err.print("accept fail on "
                                + getLocalSessionId());
                    }
                    System.err.println(" retry in " + pause + " second(s)"
                            + Thread.currentThread().getName());

                    try {
                        Thread.sleep(pause * 1000);
                    } catch (InterruptedException e) {
                        throw new ProtocolException(e);
                    }
                }
            }

            if (!bound)
                throw new ProtocolException(exception);

            if (isClosed()) {
                /*
                 * we must be sure to remove our id from the accepting pipes. In
                 * fact, if some one closed this starter while he was trying to
                 * register the pipe id, then he succeded to register the pipe
                 * id but then does not use it, and others wouldn't be able to
                 * use it.
                 */
                close();
                throw new ProtocolException("closed");
            }
        }

        /* now wait for actual connections */
        try {
            PipeStruct pipeStruct = pipeTable.accept(getLocalSessionId());

            /*
             * build the session starting from the local session id and the one
             * contained in the PipeStruct
             */
            return new Session(
                    pipeStruct.protocolLayerPipe.getProtocolLayer1(),
                    getLocalSessionId(), pipeStruct.sessionId);
        } catch (InterruptedException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * @see org.mikado.imc.protocols.SessionStarter#connect()
     */
    @Override
    public Session connect() throws ProtocolException {
        checkRemoteSessionId();

        if (usedForAccept)
            throw new ProtocolException("already used for accepting");

        int retry = 5;
        int pause = 1;
        PipeStruct pipeStruct = null;

        /* the last exception that made us fail */
        Exception exception = null;

        while (retry-- > 0) {
            try {
                if (getLocalSessionId() != null)
                    pipeStruct = pipeTable.connect(getLocalSessionId(),
                            getRemoteSessionId());
                else
                    pipeStruct = pipeTable.connect(getRemoteSessionId());
                break;
            } catch (IOException e) {
                exception = e;
                System.err.print("connect fail to " + getRemoteSessionId());
            } catch (ProtocolException e) {
                exception = e;
                System.err.print("connect fail to " + getRemoteSessionId());
            } catch (InterruptedException e) {
                throw new ProtocolException(e);
            }
            System.err.println(" retry in " + pause + " second(s)"
                    + Thread.currentThread().getName());

            try {
                Thread.sleep(pause * 1000);
            } catch (InterruptedException e) {
                throw new ProtocolException(e);
            }
        }

        if (pipeStruct == null)
            throw new ProtocolException(exception);

        /*
         * build the session by using the session id of the PipeStruct as the
         * local id, and getSessionId as the "remote"
         */
        return new Session(pipeStruct.protocolLayerPipe.getProtocolLayer2(),
                pipeStruct.sessionId, getRemoteSessionId());
    }

    /**
     * @see org.mikado.imc.protocols.SessionStarter#doClose()
     */
    @Override
    protected synchronized void doClose() throws ProtocolException {
        checkLocalSessionId();
        pipeTable.close(getLocalSessionId());
    }

    /**
     * @see org.mikado.imc.protocols.SessionStarter#bindForAccept(org.mikado.imc.protocols.SessionId)
     */
    @Override
    public SessionId bindForAccept(SessionId sessionId)
            throws ProtocolException {
        if (getLocalSessionId() != null)
            throw new AlreadyBoundSessionStarterException(getLocalSessionId()
                    .toString());

        /* notifies that it is already "bound" */
        usedForAccept = true;
        try {
            if (sessionId == null) {
                /* create an auto-generated id */
                sessionId = PipeStruct.nextId(prefix);
            }
            pipeTable.registerForAccept(sessionId);
        } catch (IOException e) {
            throw new ProtocolException(e);
        }

        setLocalSessionId(sessionId);

        return sessionId;
    }

    public static SessionId createNewSessionId(String prefix) {
        return PipeStruct.nextId(prefix);
    }
}
