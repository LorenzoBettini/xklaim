package xklaim.runtime.util;

import klava.LogicalLocality;
import klava.PhysicalLocality;

/**
 * Static utility methods that can be used by Xklaim DSL.
 * 
 * @author Lorenzo Bettini
 *
 */
public class XklaimRuntimeUtil {

	public static LogicalLocality logloc(String s) {
		return new LogicalLocality(s);
	}

	public static PhysicalLocality phyloc(String s) {
		return new PhysicalLocality(s);
	}
}
