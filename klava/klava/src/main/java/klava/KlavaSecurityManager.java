/*
 Customized version of a Security Manager
 */

package klava;

import java.io.FileDescriptor;
import java.util.Enumeration;
import java.util.Vector;
import java.net.InetAddress;

import org.mikado.imc.common.FileUtils;
import org.mikado.imc.mobility.NodeClassLoader;

/**
 * The security manager is an abstract class that allows applications to
 * implement a security policy. It allows an application to determine, before
 * performing a possibly unsafe or sensitive operation, what the operation is
 * and whether the operation is being performed by a class created via a class
 * loader rather than installed locally. Classes loaded via a class loader
 * (especially if they have been downloaded over a network) may be less
 * trustworthy than classes from files installed locally. The application can
 * allow or disallow the operation.
 * <p>
 * The <code>SecurityManager</code> class contains many methods with names
 * that begin with the word <code>check</code>. These methods are called by
 * various methods in the Java libraries before those methods perform certain
 * potentially sensitive operations. The invocation of such a check method
 * typically looks like this:
 * <p>
 * <blockquote>
 * 
 * <pre>
 *      SecurityManager security = System.getSecurityManager();
 *      if (security != null) {
 *          security.check
 * </code>
 * &lt;i&gt;XXX&lt;/i&gt;
 * <code>
 * (argument,  . . . );
 *      }
 * </pre>
 * 
 * </blockquote>
 * <p>
 * The security manager is thereby given an opportunity to prevent completion of
 * the operation by throwing an exception. A security manager routine simply
 * returns if the operation is permitted, but throws a
 * <code>SecurityException</code> if the operation is not permitted. The only
 * exception to this convention is <code>checkTopLevelWindow</code>, which
 * returns a <code>boolean</code> value.
 * <p>
 * The current security manager is set by the <code>setSecurityManager</code>
 * method in class <code>System</code>. The current security manager is
 * obtained by the <code>getSecurityManager</code> method.
 * <p>
 * The default implementation of each of the <code>check</code><i>XXX</i>
 * methods is to assume that the caller does <i>not</i> have permission to
 * perform the requested operation.
 * 
 * @author Arthur van Hoff
 * @version 1.48, 03/10/97
 * @see java.lang.ClassLoader
 * @see java.lang.SecurityException
 * @see java.lang.SecurityManager#checkTopLevelWindow(java.lang.Object)
 * @see java.lang.System#getSecurityManager()
 * @see java.lang.System#setSecurityManager(java.lang.SecurityManager)
 * @since JDK1.0
 */
public class KlavaSecurityManager extends SecurityManager {
    protected Vector classpath_directories;

    public KlavaSecurityManager() {
        super();
        classpath_directories = FileUtils.getClassPathDirectories();
        // System.out.println("+++ classpath: " + classpath_directories);
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to create a new class loader.
     * <p>
     * The <code>checkCreateClassLoader</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @exception SecurityException
     *                if the caller does not have permission to create a new
     *                class loader.
     * @see java.lang.ClassLoader#ClassLoader()
     * @since JDK1.0
     */
    public void checkCreateClassLoader() {
        // throw new SecurityException();
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to modify the thread argument.
     * <p>
     * This method is invoked for the current security manager by the
     * <code>stop</code>, <code>suspend</code>, <code>resume</code>,
     * <code>setPriority</code>, <code>setName</code>, and
     * <code>setDaemon</code> methods of class <code>Thread</code>.
     * <p>
     * The <code>checkAccess</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param g
     *            the thread to be checked.
     * 
     * @exception SecurityException
     *                if the caller does not have permission to modify the
     *                thread.
     * @see java.lang.System#getSecurityManager()
     * @see java.lang.Thread#setDaemon(boolean)
     * @see java.lang.Thread#setName(java.lang.String)
     * @see java.lang.Thread#setPriority(int)
     * @since JDK1.0
     */
    public void checkAccess(Thread g) {
        // throw new SecurityException();
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to modify the thread group argument.
     * <p>
     * This method is invoked for the current security manager when a new child
     * thread or child thread group is created, and by the
     * <code>setDaemon</code>, <code>setMaxPriority</code>,
     * <code>stop</code>, <code>suspend</code>, <code>resume</code>,
     * and <code>destroy</code> methods of class <code>ThreadGroup</code>.
     * <p>
     * The <code>checkAccess</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param g
     *            the thread group to be checked.
     * @exception SecurityException
     *                if the caller does not have permission to modify the
     *                thread group.
     * @see java.lang.System#getSecurityManager()
     * @see java.lang.ThreadGroup#destroy()
     * @see java.lang.ThreadGroup#setDaemon(boolean)
     * @see java.lang.ThreadGroup#setMaxPriority(int)
     * @since JDK1.0
     */
    public void checkAccess(ThreadGroup g) {
        // throw new SecurityException();
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to cause the Java Virtual Machine to halt with the specified
     * status code.
     * <p>
     * This method is invoked for the current security manager by the
     * <code>exit</code> method of class <code>Runtime</code>. A status of
     * <code>0</code> indicates success; other values indicate various errors.
     * <p>
     * The <code>checkExit</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param status
     *            the exit status.
     * @exception SecurityException
     *                if the caller does not have permission to halt the Java
     *                Virtual Machine with the specified status.
     * @see java.lang.Runtime#exit(int)
     * @see java.lang.System#getSecurityManager()
     * @since JDK1.0
     */
    public void checkExit(int status) {
        if (notTrustedProcess("checkExit", new Integer(status)))
            throw buildException("checkExit", new Integer(status));
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to create a subprocss.
     * <p>
     * This method is invoked for the current security manager by the
     * <code>exec</code> methods of class <code>Runtime</code>.
     * <p>
     * The <code>checkExec</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param cmd
     *            the specified system command.
     * @exception SecurityException
     *                if the caller does not have permission to create a
     *                subprocess.
     * @see java.lang.Runtime#exec(java.lang.String)
     * @see java.lang.Runtime#exec(java.lang.String, java.lang.String[])
     * @see java.lang.Runtime#exec(java.lang.String[])
     * @see java.lang.Runtime#exec(java.lang.String[], java.lang.String[])
     * @see java.lang.System#getSecurityManager()
     * @since JDK1.0
     */
    public void checkExec(String cmd) {
        if (notTrustedProcess("checkExec", cmd))
            throw buildException("checkExec", cmd);
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to dynamic link the library code specified by the string argument
     * file. The argument is either a simple library name or a complete
     * filename.
     * <p>
     * This method is invoked for the current security manager by methods
     * <code>load</code> and <code>loadLibrary</code> of class
     * <code>Runtime</code>.
     * <p>
     * The <code>checkLink</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param lib
     *            the name of the library.
     * @exception SecurityException
     *                if the caller does not have permission to dynamically link
     *                the library.
     * @see java.lang.Runtime#load(java.lang.String)
     * @see java.lang.Runtime#loadLibrary(java.lang.String)
     * @see java.lang.System#getSecurityManager()
     * @since JDK1.0
     */
    public void checkLink(String lib) {
        if (notTrustedProcess("checkLink", lib))
            throw buildException("checkLink", lib);
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to read from the specified file descriptor.
     * <p>
     * The <code>checkRead</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param fd
     *            the system-dependent file descriptor.
     * @exception SecurityException
     *                if the caller does not have permission to access the
     *                specified file descriptor.
     * @see java.io.FileDescriptor
     * @since JDK1.0
     */
    public void checkRead(FileDescriptor fd) {
        if (notTrustedProcess("checkRead", fd))
            throw buildException("checkRead", fd);
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to read the file specified by the string argument.
     * <p>
     * The <code>checkRead</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param file
     *            the system-dependent file name.
     * @exception SecurityException
     *                if the caller does not have permission to access the
     *                specified file.
     * @since JDK1.0
     */
    public void checkRead(String file) {
        if (notTrustedProcess("checkRead", file) && !isInClassPath(file))
            throw buildException("checkRead", file);
    }

    /**
     * Throws a <code>SecurityException</code> if the specified security
     * context is not allowed to read the file specified by the string argument.
     * The context must be a security context returned by a previous call to
     * <code>getSecurityContext</code>.
     * <p>
     * The <code>checkRead</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param file
     *            the system-dependent filename.
     * @param context
     *            a system-dependent security context.
     * @exception SecurityException
     *                if the specified security context does not have permission
     *                to read the specified file.
     * @see java.lang.SecurityManager#getSecurityContext()
     * @since JDK1.0
     */
    public void checkRead(String file, Object context) {
        if (notTrustedProcess("checkRead", file + "-" + context))
            throw buildException("checkRead", file + "-" + context);
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to write to the specified file descriptor.
     * <p>
     * The <code>checkWrite</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param fd
     *            the system-dependent file descriptor.
     * @exception SecurityException
     *                if the caller does not have permission to access the
     *                specified file descriptor.
     * @see java.io.FileDescriptor
     * @since JDK1.0
     */
    public void checkWrite(FileDescriptor fd) {
        if (notTrustedProcess("checkWrite", fd))
            throw buildException("checkWrite", fd);
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to write to the file specified by the string argument.
     * <p>
     * The <code>checkWrite</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param file
     *            the system-dependent filename.
     * @exception SecurityException
     *                if the caller does not have permission to access the
     *                specified file.
     * @since JDK1.0
     */
    public void checkWrite(String file) {
        if (notTrustedProcess("checkWrite", file))
            throw buildException("checkWrite", file);
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to delete the specified file.
     * <p>
     * This method is invoked for the current security manager by the
     * <code>delete</code> method of class <code>File</code>.
     * <p>
     * The <code>checkDelete</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param file
     *            the system-dependent filename.
     * @exception SecurityException
     *                if the caller does not have permission to delete the file.
     * @see java.io.File#delete()
     * @see java.lang.System#getSecurityManager()
     * @since JDK1.0
     */
    public void checkDelete(String file) {
        if (notTrustedProcess("checkDelete", file))
            throw buildException("checkDelete", file);
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to open a socket connection to the specified host and port
     * number.
     * <p>
     * A port number of <code>-1</code> indicates that the calling method is
     * attempting to determine the IP address of the specified host name.
     * <p>
     * The <code>checkConnect</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param host
     *            the host name port to connect to.
     * @param port
     *            the protocol port to connect to.
     * @exception SecurityException
     *                if the caller does not have permission to open a socket
     *                connection to the specified <code>host</code> and
     *                <code>port</code>.
     * @since JDK1.0
     */
    public void checkConnect(String host, int port) {
        // throw new SecurityException();
    }

    /**
     * Throws a <code>SecurityException</code> if the specified security
     * context is not allowed to open a socket connection to the specified host
     * and port number.
     * <p>
     * A port number of <code>-1</code> indicates that the calling method is
     * attempting to determine the IP address of the specified host name.
     * <p>
     * The <code>checkConnect</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param host
     *            the host name port to connect to.
     * @param port
     *            the protocol port to connect to.
     * @param context
     *            a system-dependent security context.
     * @exception SecurityException
     *                if the specified security context does not have permission
     *                to open a socket connection to the specified
     *                <code>host</code> and <code>port</code>.
     * @see java.lang.SecurityManager#getSecurityContext()
     * @since JDK1.0
     */
    public void checkConnect(String host, int port, Object context) {
        // throw new SecurityException();
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to wait for a connection request on the specified local port
     * number.
     * <p>
     * The <code>checkListen</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param port
     *            the local port.
     * @exception SecurityException
     *                if the caller does not have permission to listen on the
     *                specified port.
     * @since JDK1.0
     */
    public void checkListen(int port) {
        // throw new SecurityException();
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * permitted to accept a socket connection from the specified host and port
     * number.
     * <p>
     * This method is invoked for the current security manager by the
     * <code>accept</code> method of class <code>ServerSocket</code>.
     * <p>
     * The <code>checkAccept</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param host
     *            the host name of the socket connection.
     * @param port
     *            the port number of the socket connection.
     * @exception SecurityException
     *                if the caller does not have permission to accept the
     *                connection.
     * @see java.lang.System#getSecurityManager()
     * @see java.net.ServerSocket#accept()
     * @since JDK1.0
     */
    public void checkAccept(String host, int port) {
        // throw new SecurityException();
    }

    /**
     * Tests if current execution context is allowed to use
     * (join/leave/send/receive) IP multicast.
     * 
     * @param maddr
     *            Internet group address to be used.
     * @exception SecurityException
     *                if a security error has occurred.
     * @since JDK1.1
     */
    public void checkMulticast(InetAddress maddr) {
        throw buildException("checkMulticast", maddr);
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to access or modify the system properties.
     * <p>
     * This method is used by the <code>getProperties</code> and
     * <code>setProperties</code> methods of class <code>System</code>.
     * <p>
     * The <code>checkPropertiesAccess</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @exception SecurityException
     *                if the caller does not have permission to access or modify
     *                the system properties.
     * @see java.lang.System#getProperties()
     * @see java.lang.System#setProperties(java.util.Properties)
     * @since JDK1.0
     */
    public void checkPropertiesAccess() {
        // throw new SecurityException();
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to access the system property with the specified <code>key</code>
     * name.
     * <p>
     * This method is used by the <code>getProperty</code> method of class
     * <code>System</code>.
     * <p>
     * The <code>checkPropertiesAccess</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param key
     *            a system property key.
     * @exception SecurityException
     *                if the caller does not have permission to access the
     *                specified system property.
     * @see java.lang.System#getProperty(java.lang.String)
     * @since JDK1.0
     */
    public void checkPropertyAccess(String key) {
        // throw new SecurityException();
    }

    /**
     * Returns <code>false</code> if the calling thread is not trusted to
     * bring up the top-level window indicated by the <code>window</code>
     * argument. In this case, the caller can still decide to show the window,
     * but the window should include some sort of visual warning. If the method
     * returns <code>true</code>, then the window can be shown without any
     * special restrictions.
     * <p>
     * See class <code>Window</code> for more information on trusted and
     * untrusted windows.
     * <p>
     * The <code>checkSetFactory</code> method for class
     * <code>SecurityManager</code> always returns <code>false</code>.
     * 
     * @param window
     *            the new window that is being created.
     * @return <code>true</code> if the caller is trusted to put up top-level
     *         windows; <code>false</code> otherwise.
     * @exception SecurityException
     *                if creation is disallowed entirely.
     * @see java.awt.Window
     * @since JDK1.0
     */
    public boolean checkTopLevelWindow(Object window) {
        return false;
    }

    /**
     * Tests if a client can initiate a print job request.
     * 
     * @since JDK1.1
     */
    public void checkPrintJobAccess() {
        if (notTrustedProcess("checkPrintJobAccess", null))
            throw buildException("checkPrintJobAccess", null);
    }

    /**
     * Tests if a client can get access to the system clipboard.
     * 
     * @since JDK1.1
     */
    public void checkSystemClipboardAccess() {
        if (notTrustedProcess("checkSystemClipboardAccess", null))
            throw buildException("checkSystemClipboardAccess", null);
    }

    /**
     * Tests if a client can get access to the AWT event queue.
     * 
     * @since JDK1.1
     */
    public void checkAwtEventQueueAccess() {
        // throw new SecurityException();
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to access the package specified by the argument.
     * <p>
     * This method is used by the <code>loadClass</code> method of class
     * loaders.
     * <p>
     * The <code>checkPackageAccess</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param pkg
     *            the package name.
     * @exception SecurityException
     *                if the caller does not have permission to access the
     *                specified package.
     * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
     * @since JDK1.0
     */
    public void checkPackageAccess(String pkg) {
        // throw new SecurityException();
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to define classes in the package specified by the argument.
     * <p>
     * This method is used by the <code>loadClass</code> method of some class
     * loaders.
     * <p>
     * The <code>checkPackageDefinition</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @param pkg
     *            the package name.
     * @exception SecurityException
     *                if the caller does not have permission to define classes
     *                in the specified package.
     * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
     * @since JDK1.0
     */
    public void checkPackageDefinition(String pkg) {
        // throw new SecurityException();
    }

    /**
     * Throws a <code>SecurityException</code> if the calling thread is not
     * allowed to set the socket factory used by <code>ServerSocket</code> or
     * <code>Socket</code>, or the stream handler factory used by
     * <code>URL</code>.
     * <p>
     * The <code>checkSetFactory</code> method for class
     * <code>SecurityManager</code> always throws a
     * <code>SecurityException</code>.
     * 
     * @exception SecurityException
     *                if the caller does not have permission to specify a socket
     *                factory or a stream handler factory.
     * @see java.net.ServerSocket#setSocketFactory(java.net.SocketImplFactory)
     * @see java.net.Socket#setSocketImplFactory(java.net.SocketImplFactory)
     * @see java.net.URL#setURLStreamHandlerFactory(java.net.URLStreamHandlerFactory)
     * @since JDK1.0
     */
    public void checkSetFactory() {
        // throw new SecurityException();
    }

    /**
     * Tests if a client is allowed to access members. If access is denied,
     * throw a SecurityException. The default policy is to deny all accesses.
     * 
     * @since JDK1.1
     */
    public void checkMemberAccess(Class clazz, int which) {
        // throw new SecurityException();
    }

    /**
     * Tests access to certain operations for a security API action.
     * 
     * @since JDK1.1
     */
    public void checkSecurityAccess(String action) {
        if (notTrustedProcess("checkSecurityAccess", action))
            throw buildException("checkSecurityAccess", action);
    }

    // a thread is not trusted if it's remote, i.e., loaded by a NodeClassLoader
    protected boolean notTrustedProcess(String operation, Object param) {
        Class current_thread_class = Thread.currentThread().getClass();
        ClassLoader class_loader = current_thread_class.getClassLoader();

        if (class_loader != null && class_loader instanceof NodeClassLoader) {
            System.out.println("untrusted thread: " + current_thread_class
                    + ", operation: " + operation + "("
                    + (param != null ? param : "") + ")");
            return true;
        }
        return false;
    }

    protected boolean isInClassPath(String file) {
        Enumeration en = classpath_directories.elements();
        while (en.hasMoreElements()) {
            if (FileUtils.isInPath(en.nextElement().toString(), file))
                return true;
        }

        return false;
    }

    protected SecurityException buildException(String s, Object o) {
        return new SecurityException(s + "(" + (o != null ? o : "") + ")");
    }
}
