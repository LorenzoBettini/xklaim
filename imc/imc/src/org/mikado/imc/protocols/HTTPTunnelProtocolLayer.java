/*
 * Created on 10-gen-2005
 */
package org.mikado.imc.protocols;

import org.mikado.imc.common.UnknownRequest;

import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <p>
 * A simple ProtocolLayer that tunnels requests and answers into an HTTP
 * request.
 * </p>
 * 
 * <p>
 * It reads requests of the shape:<br>
 * <pre>
 * GET /&lt;string&gt; HTTP/&lt;num&gt;&lt;CR&gt;&lt;LF&gt;
 * other headers...&lt;CR&gt;&lt;LF&gt;
 * &lt;CR&gt;&lt;LF&gt;
 * </pre>
 * where the "string" is passed to the tunneled layer.   Then it encapsulates
 * the "answer" in the shaper of a (very simplified) HTML page:<br>
 * <pre>
 * &lt;!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"&gt;
 * &lt;html&gt;
 * &lt;head&gt;
 * answer&lt;CR&gt;&lt;LF&gt;
 * &lt;/body&gt;
 * &lt;/html&gt;
 * </pre>
 * </p>
 * 
 * <p>
 * If set up in sender mode, then it sends requests of the shape:<br>
 * <pre>
 * GET /&lt;string&gt; HTTP/&lt;num&gt;&lt;CR&gt;&lt;LF&gt;
 * &lt;CR&gt;&lt;LF&gt;
 * </pre>
 * where "string" is the tunneled request, and waits for an answer encapsulated
 * in an HTTP response (see above).
 * </p>
 *
 * @author Lorenzo Bettini
 * @version $Revision: 1.4 $
 */
public class HTTPTunnelProtocolLayer extends TunnelProtocolLayer {
    /** The regular expression for HTTP requests. */
    private static final String http_request_regex = "(GET [/]?)((?:.|[\r\n])*)( HTTP.*)((?:.|[\r\n])*)\r\n\r\n";
    private static final Pattern request_pattern = Pattern.compile(http_request_regex);
	
    /** The regular expression for HTTP response. */
    private static final String http_response_regex = "HTTP/1.1 200 OK\r\n" +
        "Content-Length: (?:[0-9]*)\r\n" + "Content-Type: text/html\r\n" +
        "\r\n" +
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\r\n" +
        "<html><head></head><body>\r\n" + "((?:.|[\r\n])*\r\n)" + // this the actual content
        "</body></html>\r\n\r\n";
    private static final Pattern response_pattern = Pattern.compile(http_response_regex);

    /** The original marshaler returned by a prepare of a lower layer. */
    protected Marshaler origMarshaler;

    /**
     * Whether it is used to send requests, instead of receiving requests.
     * Default is <tt>false</tt>.
     */
    protected boolean senderMode = false;	

    /**
     * Constructs an HTTP tunnel
     *
     * @throws ProtocolException
     */
    public HTTPTunnelProtocolLayer() throws ProtocolException {
        super();
    }

    /**
     * Waits for an HTTP GET request and strips out the requested resource.
     *
     * @param unMarshaler
     *
     * @return the UnMarshaler after reading the HTTP request
     *
     * @throws ProtocolException
     */
    public UnMarshaler doCreateUnMarshaler(UnMarshaler unMarshaler)
        throws ProtocolException {
        try {
            while (true) {
                try {
                    String data = "";

                    if (!senderMode) {
                        data = stripRequest(readHTTPRequest(unMarshaler));
                        tunneledMarshaler.writeBytes(data + "\r\n");
                    } else {
                        data = stripResponse(readHTTPResponse(unMarshaler));
                        tunneledMarshaler.writeBytes(data);
                    }

                    break;
                } catch (UnknownRequest e) {
                    Marshaler marshaler = createMarshaler();
                    marshaler.writeStringLine("UNKNOWN REQUEST");
                    marshaler.flush();

                    // DO NOT call down otheriwse, you'll be blocked
                    // see the doDown implementation below
                    // TODO: check this!
                }
            }
        } catch (IOException e) {
            throw new ProtocolException(e);
        }

        return newUnMarshaler;
    }

    /**
     * Returns the piped marshaler.
     *
     * @param marshaler
     *
     * @return the piped Marshaler
     *
     * @throws ProtocolException
     */
    public Marshaler doCreateMarshaler(Marshaler marshaler)
        throws ProtocolException {
        origMarshaler = marshaler;

        return newMarshaler;
    }

    /**
     * Reads an HTTP request
     *
     * @param input
     *
     * @return HTTP request
     *
     * @throws ProtocolException
     */
    public static String readHTTPRequest(UnMarshaler input)
        throws ProtocolException {
        return readHTTPHeader(input);
    }

    /**
     * Reads an HTTP response
     *
     * @param input
     *
     * @return HTTP response
     *
     * @throws ProtocolException
     */
    public static String readHTTPResponse(UnMarshaler input)
        throws ProtocolException {
        // we have to read two blocks separated by an empty line
        return readHTTPHeader(input) + readHTTPHeader(input);
    }

    /**
     * Reads an HTTP header terminated with an empty line.
     *
     * @param input
     *
     * @return HTTP header
     *
     * @throws ProtocolException
     */
    protected static String readHTTPHeader(UnMarshaler input)
        throws ProtocolException {
        StringBuffer buffer = new StringBuffer();
        String line;

        while (true) {
            try {
                // wait for an empty line
                line = input.readStringLine();
            } catch (IOException e) {
                throw new ProtocolException(e);
            }

            System.err.println("http read line: " + line);
            buffer.append(line + "\r\n");

            if (line.length() == 0) {
                break;
            }
        }

        return buffer.toString();
    }

    /**
     * strips out the requested resource from an HTTP GET request.
     *
     * @param s The HTTP GET request
     *
     * @return the requested resourse in the HTTP GET request, without the
     *         leading /
     *
     * @throws UnknownRequest
     */
    public static String stripRequest(String s) throws UnknownRequest {
        Matcher matcher = request_pattern.matcher(s);

        if (!matcher.matches()) {
            throw new UnknownRequest("unknown HTTP request");
        }

        // the second subexpression represents the requested resource
        return matcher.group(2);
    }

    /**
     * strips out the requested resource from an HTTP response.
     *
     * @param s The HTTP response
     *
     * @return the contents of the response.
     *
     * @throws UnknownRequest
     */
    public static String stripResponse(String s) throws UnknownRequest {
        Matcher matcher = response_pattern.matcher(s);

        if (!matcher.matches()) {
            throw new UnknownRequest("unknown HTTP response");
        }

        // the first subexpression represents the requested resource
        return matcher.group(1);
    }

    /**
     * Encapsulates a string written by a higher layer into an HTML page.
     *
     * @param s The string written by the tunneled layer.
     *
     * @return The string encapsulated in an HTML page
     */
    public static String encapsulateResponse(String s) {
        StringBuffer buffer = new StringBuffer();

        buffer.append(
            "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\r\n");
        buffer.append("<html><head></head><body>\r\n");

        buffer.append(s + "\r\n");
        buffer.append("</body></html>\r\n\r\n");

        String encapsulated_data = buffer.toString();

        buffer = new StringBuffer();
        buffer.append("HTTP/1.1 200 OK\r\n");
        buffer.append("Content-Length: " + encapsulated_data.length() + "\r\n");
        buffer.append("Content-Type: text/html\r\n");
        buffer.append("\r\n");
        buffer.append(encapsulated_data);

        return buffer.toString();
    }

    /**
     * Encapsulates a request written by a higher layer into an HTML page.
     *
     * @param s The string written by the tunneled layer.
     *
     * @return The string encapsulated in an HTML page
     */
    public static String encapsulateRequest(String s) {
        return "GET /" + s + " HTTP/1.1\r\n\r\n";
    }

    /**
     * Writes the output of the higher layer into an HTML page
     *
     * @param marshaler
     *
     * @throws ProtocolException
     */
    public void doReleaseMarshaler(Marshaler marshaler) throws ProtocolException {
        try {
            String data = tunneledUnMarshaler.readStringLine();
            String encapsulated_data = "";

            if (!senderMode) {
                encapsulated_data = encapsulateResponse(data);
            } else {
                encapsulated_data = encapsulateRequest(data);
            }

            origMarshaler.writeBytes(encapsulated_data);
            origMarshaler.flush();
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

    /**
     * Whether it is in sender mode.
     *
     * @return Returns the senderMode.
     */
    public final boolean isSenderMode() {
        return senderMode;
    }

    /**
     * Sets the sender mode.
     *
     * @param senderMode The senderMode to set.
     */
    public final void setSenderMode(boolean senderMode) {
        this.senderMode = senderMode;
    }
}
