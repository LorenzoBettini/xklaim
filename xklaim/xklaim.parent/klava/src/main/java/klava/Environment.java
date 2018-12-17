package klava;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Enumeration;

import klava.events.EnvironmentEvent;

import org.mikado.imc.events.EventGeneratorAdapter;


/**
 * Contains the mappings between LogicalLocality and PhysicalLocality. It also
 * stores the reverse mapping in order to make it fast to remove a
 * PhysicalLocality and all its associated LogicalLocalities.
 * 
 * @author Lorenzo Bettini
 * @version $Revision: 1.2 $
 */
public class Environment extends EventGeneratorAdapter implements Serializable {
    /**
     * Represents an entry in the environment
     * 
     * @author Lorenzo Bettini
     * @version $Revision: 1.2 $
     */
    public static class EnvironmentEntry {
        public LogicalLocality logicalLocality;

        public PhysicalLocality physicalLocality;

        /**
         * @param logicalLocality
         * @param physicalLocality
         */
        public EnvironmentEntry(LogicalLocality logicalLocality,
                PhysicalLocality physicalLocality) {
            this.logicalLocality = logicalLocality;
            this.physicalLocality = physicalLocality;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return logicalLocality + " ~ " + physicalLocality;
        }

    }

    /**
     * 
     */
    private static final long serialVersionUID = 3176548384726327181L;

    /**
     * The mapping between a LogicalLocality and a PhysicalLocality. This is one
     * to one.
     */
    protected Hashtable<LogicalLocality, PhysicalLocality> environment;

    /**
     * Stores all the LogicalLocalities a PhysicalLocality is mapped into.
     */
    protected Hashtable<PhysicalLocality, HashSet<LogicalLocality>> reverseEnvironment;

    public Environment() {
        environment = new Hashtable<LogicalLocality, PhysicalLocality>();
        reverseEnvironment = new Hashtable<PhysicalLocality, HashSet<LogicalLocality>>();
    }

    public Environment(String filename)
            throws KlavaMalformedPhyLocalityException {
        environment = new Hashtable<LogicalLocality, PhysicalLocality>();
        reverseEnvironment = new Hashtable<PhysicalLocality, HashSet<LogicalLocality>>();

        if (filename != null)
            readEnvironment(filename);
    }

    protected void readEnvironment(String filename)
            throws KlavaMalformedPhyLocalityException {
        try {
            System.out.println("Reading Environment " + filename + "...");
            FileInputStream file = new FileInputStream(filename);
            BufferedReader ifile = new BufferedReader(new InputStreamReader(
                    file));
            StringTokenizer st;
            String line;
            LogicalLocality lLoc;
            PhysicalLocality fLoc;
            try {
                while ((line = ifile.readLine()) != null) {
                    st = new StringTokenizer(line, "=:");
                    lLoc = new LogicalLocality(st.nextToken());
                    fLoc = new PhysicalLocality(st.nextToken());
                    add(lLoc, fLoc);
                }
                ifile.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.err.println("No file " + filename
                    + " for the environment found... skipping.");
        } catch (SecurityException ex) {
            System.err
                    .println("Skipping reading environment, due to Security:");
            System.err.println(ex);
            // this catches SecurityException in Applets as well
        }
    }

    synchronized public PhysicalLocality toPhysical(LogicalLocality l) {
        return environment.get(l);
    }

    synchronized public PhysicalLocality toPhysical(String l) {
        return environment.get(new LogicalLocality(l));
    }

    synchronized public HashSet<LogicalLocality> toLogical(
            PhysicalLocality physicalLocality) {
        return reverseEnvironment.get(physicalLocality);
    }

    synchronized public void add(String ll, PhysicalLocality pl) {
        add(new LogicalLocality(ll), pl);
    }

    /**
     * Adds a mapping logical locality - physical locality. If a mapping for the
     * passed logical locality was already present, it is replaced with this
     * one.
     * 
     * @param ll
     * @param pl
     */
    synchronized public void add(LogicalLocality ll, PhysicalLocality pl) {
        environment.put(ll, pl);

        HashSet<LogicalLocality> logicalLocalities = reverseEnvironment.get(pl);
        if (logicalLocalities == null) {
            logicalLocalities = new HashSet<LogicalLocality>();
            reverseEnvironment.put(pl, logicalLocalities);
        }
        logicalLocalities.add(ll);

        generate(EnvironmentEvent.EnvironmentEventId, new EnvironmentEvent(
                this, EnvironmentEvent.EventType.ADDED, ll, pl));
    }

    /**
     * Adds a mapping logical locality - physical locality only if a mapping for
     * the specified logical locality was not already present.
     * 
     * @param ll
     * @param pl
     */
    synchronized public boolean try_add(LogicalLocality ll, PhysicalLocality pl) {
        boolean ret = (toPhysical(ll) == null);

        if (ret)
            add(ll, pl);

        return ret;
    }

    /**
     * Inserts in this environment the mappings taken from the passed
     * environment. Only those mapping involving logical localities that are not
     * present in this environment are inserted.
     * 
     * @param environment
     */
    synchronized public void addFromEnvironment(Environment environment) {
        if (environment == null)
            return;

        synchronized (environment) {
            Set<Map.Entry<LogicalLocality, PhysicalLocality>> set = environment.environment
                    .entrySet();
            Iterator<Map.Entry<LogicalLocality, PhysicalLocality>> iterator = set
                    .iterator();
            while (iterator.hasNext()) {
                Map.Entry<LogicalLocality, PhysicalLocality> entry = iterator
                        .next();
                try_add(entry.getKey(), entry.getValue());
            }
        }
    }

    synchronized public PhysicalLocality remove(LogicalLocality l) {
        PhysicalLocality physicalLocality = environment.remove(l);
        
        generate(EnvironmentEvent.EnvironmentEventId, new EnvironmentEvent(
                this, EnvironmentEvent.EventType.REMOVED, l, physicalLocality));

        removePhysical(physicalLocality);

        return physicalLocality;
    }

    /**
     * Remove the mappings involved by this PhysicalLocality
     * 
     * @param physicalLocality
     * @return The set of associated LogicalLocalities or null if there's no
     *         association
     */
    synchronized public HashSet<LogicalLocality> removePhysical(
            PhysicalLocality physicalLocality) {
        HashSet<LogicalLocality> logicalLocalities = null;

        if (physicalLocality != null) {
            /* we must remove also the other mappings for this PhysicalLocality */
            logicalLocalities = reverseEnvironment.remove(physicalLocality);
            if (logicalLocalities != null) {
                /* this shouldn't be null, but it might be in the future? */
                Iterator<LogicalLocality> iterator = logicalLocalities
                        .iterator();
                while (iterator.hasNext())
                    remove(new LogicalLocality(iterator.next()));
                // FIXME in the environment put LogicalLocality not String
            }
        }

        return logicalLocalities;
    }

    public String toString() {
        return environment.toString() + "\n" + "Reverse: " + reverseEnvironment;
    }

    synchronized public Enumeration<LogicalLocality> keys() {
        return environment.keys();
    }
}
