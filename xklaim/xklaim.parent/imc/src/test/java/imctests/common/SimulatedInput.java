/*
 * Created on Dec 29, 2004
 *
 */
package imctests.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

/**
 * @author bettini
 *
 * provide a reader that simulates the standard input, useful for tests
 */
public class SimulatedInput {
	PipedReader pipedReader;
	PipedWriter pipedWriter;

	public SimulatedInput() throws IOException {
		pipedReader = new PipedReader();
		pipedWriter = new PipedWriter();
		pipedWriter.connect(pipedReader);
	}
	
	public void write(String s) throws IOException {
		pipedWriter.write(s);
		pipedWriter.flush();
	}
	
	public void writeln(String s) throws IOException {
		pipedWriter.write(s + "\n");
		pipedWriter.flush();
	}
	
	public BufferedReader getBufferedReader() {
		return new BufferedReader(pipedReader, 1);
	}
}
