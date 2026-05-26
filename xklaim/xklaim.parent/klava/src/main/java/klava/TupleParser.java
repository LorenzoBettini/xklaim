package klava;

import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TupleParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TupleParser.class);

    public TupleParser() {
    }

    public static Tuple parseString(String s) throws KlavaTupleParsingException {
        StringTokenizer st = new StringTokenizer(s, ":,", true);
        String column, elem, type;
        Tuple tuple = new Tuple();
        try {
            while (st.hasMoreElements()) {
                elem = st.nextToken().trim();
                LOGGER.debug("{}", elem);
                if (!st.hasMoreTokens()) {
                    TupleParser.createTupleElem(tuple, elem, null);
                    break;
                }
                column = st.nextToken();
                if (column.equals(":")) {
                    type = st.nextToken();
                    if (st.hasMoreTokens()) {
                        column = st.nextToken();
                        if (column.equals(":")) {
                            // we assume a physical locality IP:port
                            // was specified, then type is not the type but the
                            // port number
                            elem = elem + ":" + type;
                            type = st.nextToken();
                        }
                    }
                    TupleParser.createTupleElem(tuple, elem, type);
                    LOGGER.debug("ELEM: {} TYPE: {}", elem, type);
                } else { // assumes string type
                    TupleParser.createTupleElem(tuple, elem, null);
                }
                if ((!column.equals(","))
                        && (st.hasMoreTokens() && !st.nextToken().equals(",")))
                    throw new KlavaTupleParsingException(", expected!");
            }
        } catch (java.util.NoSuchElementException e) {
            throw new KlavaTupleParsingException("Unexpected end of string");
        }
        return tuple;
    }

    protected static void createTupleElem(Tuple t, String elem, String type)
            throws KlavaTupleParsingException {
        if (type == null || type.equalsIgnoreCase("str"))
            t.add(new KString(elem));
        else if (type.equalsIgnoreCase("int")) {
            try {
                t.add(new KInteger(Integer.valueOf(elem)));
            } catch (NumberFormatException nfe) {
                throw new KlavaTupleParsingException("Bad number : " + elem);
            }
        } else if (type.equalsIgnoreCase("bool"))
            t.add(new KBoolean(Boolean.valueOf(elem)));
        else if (type.equalsIgnoreCase("loc")
                || type.equalsIgnoreCase("phyloc")) {
            try {
                t.add(new PhysicalLocality(NetUtils.createNodeAddress(elem)));
            } catch (KlavaMalformedPhyLocalityException mfl) {
                throw new KlavaTupleParsingException("malformed loc : " + elem);
            }
        } else if (type.equalsIgnoreCase("logloc")) {
            t.add(new LogicalLocality(elem));
        } else
            throw new KlavaTupleParsingException("unknown type : " + type);
    }

    public static boolean isType(String s) {
        return s.equalsIgnoreCase("str") || s.equalsIgnoreCase("int")
                || s.equalsIgnoreCase("bool") || s.equalsIgnoreCase("loc")
                || s.equalsIgnoreCase("logloc") || s.equalsIgnoreCase("phyloc");
    }

    public static void main(String[] args) throws KlavaException {
        Tuple t;
        try {
            t = TupleParser
                    .parseString(args[0] + " " + args[1] + " " + args[2]);
            LOGGER.info("TUPLE : {}", t);
        } catch (KlavaTupleParsingException e) {
            LOGGER.error("parsing error", e);
        }
    }

}
