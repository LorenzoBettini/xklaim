/*
 * Created on Apr 5, 2005
 *
 */
package examples.mobility;

import org.mikado.imc.mobility.JavaMigratingCode;

/**
 * @author bettini
 *
 */
public class MyMobileCode extends JavaMigratingCode {
    private static final long serialVersionUID = 1L;

    public void foo() {
        System.out.println("foo!");
    }
        
    public void run() {
        foo();
    }
}
