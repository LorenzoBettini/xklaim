/*
 * Created on Nov 23, 2005
 */
package klava.tests.junit;

import java.util.HashSet;

import junit.framework.TestCase;
import klava.Environment;
import klava.KlavaMalformedPhyLocalityException;
import klava.LogicalLocality;
import klava.PhysicalLocality;

/**
 * Tests for Environment
 * 
 * @author Lorenzo Bettini
 */
public class EnvironmentTest extends TestCase {
    Environment environment;
    
    protected void setUp() throws Exception {
        super.setUp();
        environment = new Environment();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddAndRemove() throws KlavaMalformedPhyLocalityException {
        LogicalLocality logicalLocality = new LogicalLocality("foo");
        LogicalLocality logicalLocality2 = new LogicalLocality("bar");
        LogicalLocality logicalLocality3 = new LogicalLocality("boo");
        
        PhysicalLocality physicalLocality = new PhysicalLocality("127.0.0.1", 9999);
        PhysicalLocality physicalLocality2 = new PhysicalLocality("127.0.0.1", 10000);
        
        assertTrue(environment.try_add(logicalLocality, physicalLocality));
        assertTrue(environment.toPhysical(logicalLocality) != null);
        assertEquals(environment.toPhysical(logicalLocality), physicalLocality);
        
        System.out.println("environment: " + environment);
        
        HashSet<LogicalLocality> logicalLocalities = environment.toLogical(physicalLocality);
        assertTrue(logicalLocalities != null);
        assertTrue(logicalLocalities.contains(logicalLocality));
        
        assertFalse(environment.try_add(logicalLocality, physicalLocality));
        
        /* map to the same physical locality */
        assertTrue(environment.try_add(logicalLocality2, physicalLocality));
        assertTrue(environment.toPhysical(logicalLocality2) != null);
        assertEquals(environment.toPhysical(logicalLocality2), physicalLocality);
        
        System.out.println("environment: " + environment);
        
        logicalLocalities = environment.toLogical(physicalLocality);
        assertTrue(logicalLocalities != null);
        assertTrue(logicalLocalities.contains(logicalLocality2));
        
        assertTrue(environment.try_add(logicalLocality3, physicalLocality2));
        assertTrue(environment.toPhysical(logicalLocality3) != null);
        assertEquals(environment.toPhysical(logicalLocality3), physicalLocality2);
        
        System.out.println("environment: " + environment);
        
        /* should remove also the mapping for logical locality 1 */
        assertTrue(environment.remove(logicalLocality2) != null);
        
        System.out.println("environment: " + environment);
        assertTrue(environment.toPhysical(logicalLocality) == null);
        assertTrue(environment.toPhysical(logicalLocality2) == null);
        
        /* should remove also the logical mapping */
        assertTrue(environment.removePhysical(physicalLocality2) != null);
        
        System.out.println("environment: " + environment);
        
        assertTrue(environment.toPhysical(logicalLocality3) == null);
    }
}
