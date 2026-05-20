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
        assertNotNull(environment.toPhysical(logicalLocality));
        assertEquals(environment.toPhysical(logicalLocality), physicalLocality);
        
        System.out.println("environment: " + environment);
        
        HashSet<LogicalLocality> logicalLocalities = environment.toLogical(physicalLocality);
        assertNotNull(logicalLocalities);
        assertTrue(logicalLocalities.contains(logicalLocality));
        
        assertFalse(environment.try_add(logicalLocality, physicalLocality));
        
        /* map to the same physical locality */
        assertTrue(environment.try_add(logicalLocality2, physicalLocality));
        assertNotNull(environment.toPhysical(logicalLocality2));
        assertEquals(environment.toPhysical(logicalLocality2), physicalLocality);
        
        System.out.println("environment: " + environment);
        
        logicalLocalities = environment.toLogical(physicalLocality);
        assertNotNull(logicalLocalities);
        assertTrue(logicalLocalities.contains(logicalLocality2));
        
        assertTrue(environment.try_add(logicalLocality3, physicalLocality2));
        assertNotNull(environment.toPhysical(logicalLocality3));
        assertEquals(environment.toPhysical(logicalLocality3), physicalLocality2);
        
        System.out.println("environment: " + environment);
        
        /* should remove also the mapping for logical locality 1 */
        assertNotNull(environment.remove(logicalLocality2));
        
        System.out.println("environment: " + environment);
        assertNull(environment.toPhysical(logicalLocality));
        assertNull(environment.toPhysical(logicalLocality2));
        
        /* should remove also the logical mapping */
        assertNotNull(environment.removePhysical(physicalLocality2));
        
        System.out.println("environment: " + environment);
        
        assertNull(environment.toPhysical(logicalLocality3));
    }
}
