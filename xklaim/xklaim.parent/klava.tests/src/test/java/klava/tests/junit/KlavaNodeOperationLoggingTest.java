package klava.tests.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import klava.KString;
import klava.KlavaException;
import klava.Locality;
import klava.LogicalLocality;
import klava.PhysicalLocality;
import klava.Tuple;
import klava.topology.KlavaNode;
import klava.topology.KlavaProcess;

/**
 * Tests for {@link KlavaNode} operation logging at DEBUG level.
 *
 * <p>Each public tuple-space operation ({@code out}, {@code in}, {@code read},
 * {@code eval} and their non-blocking / timeout variants) should emit a DEBUG
 * log message in the form {@code <op>( <tuple> )@<locality>} when it starts,
 * and – for retrieval operations that succeed – a second message in the form
 * {@code <op>( <original> )@<locality> -> <op>( <result> )@<physicalLocality>}
 * when it completes.</p>
 */
public class KlavaNodeOperationLoggingTest {

	private static class DoNothingProcess extends KlavaProcess {
		private static final long serialVersionUID = 1L;

		DoNothingProcess(String name) {
			super(name);
		}

		@Override
		public void executeProcess() {
			// do nothing
		}
	}

	/**
	 * Returns a {@link KlavaNode} whose {@code checkLocalDestination} treats
	 * {@code logLoc} as resolving locally to {@code physLoc}.
	 */
	private static KlavaNode createNodeForLogicalLocality(
			LogicalLocality logLoc, PhysicalLocality physLoc) {
		return new KlavaNode() {
			@Override
			protected boolean checkLocalDestination(Locality destLocality,
					PhysicalLocality destination) throws KlavaException {
				if (destLocality == logLoc) {
					destination.setValue((Object) physLoc);
					return true;
				}
				return super.checkLocalDestination(destLocality, destination);
			}
		};
	}

	private KlavaNode node;

	private ListAppender<ILoggingEvent> listAppender;

	@Before
	public void setUp() {
		node = new KlavaNode();

		Logger klavaNodeLogger = (Logger) LoggerFactory.getLogger(KlavaNode.class);
		klavaNodeLogger.setLevel(Level.DEBUG);

		listAppender = new ListAppender<>();
		listAppender.start();
		klavaNodeLogger.addAppender(listAppender);
	}

	@After
	public void tearDown() {
		Logger klavaNodeLogger = (Logger) LoggerFactory.getLogger(KlavaNode.class);
		klavaNodeLogger.detachAppender(listAppender);
		klavaNodeLogger.setLevel(null);
		listAppender.stop();
	}

	private List<ILoggingEvent> debugMessages() {
		return listAppender.list.stream()
				.filter(e -> e.getLevel() == Level.DEBUG)
				.toList();
	}

	// --- out ---

	@Test
	public void testOutLocalLogs() {
		Tuple tuple = new Tuple(new KString("hello"));
		node.out(tuple);

		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(1, msgs.size());
		assertEquals("out( hello )@self", msgs.get(0).getFormattedMessage());
	}

	@Test
	public void testOutSelfLogs() throws KlavaException {
		Tuple tuple = new Tuple(new KString("hello"));
		node.out(tuple, KlavaNode.self);

		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(1, msgs.size());
		assertEquals("out( hello )@self", msgs.get(0).getFormattedMessage());
	}

	// --- in ---

	@Test
	public void testInLocalLogsBeforeAndAfter() throws KlavaException {
		node.out(new Tuple(new KString("hello")));
		listAppender.list.clear();

		Tuple template = new Tuple(new KString());
		node.in(template);

		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(2, msgs.size());
		assertEquals("in( !KString )@self", msgs.get(0).getFormattedMessage());
		assertEquals("in( !KString )@self -> in( hello )@self",
				msgs.get(1).getFormattedMessage());
	}

	@Test
	public void testInSelfLogsBeforeAndAfter() throws KlavaException {
		node.out(new Tuple(new KString("hello")));
		listAppender.list.clear();

		Tuple template = new Tuple(new KString());
		node.in(template, KlavaNode.self);

		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(2, msgs.size());
		assertEquals("in( !KString )@self", msgs.get(0).getFormattedMessage());
		assertEquals("in( !KString )@self -> in( hello )@self",
				msgs.get(1).getFormattedMessage());
	}

	// --- in_nb ---

	@Test
	public void testInNbLocalLogsOnlyBefore() {
		Tuple template = new Tuple(new KString());
		boolean found = node.in_nb(template);

		assertFalse(found);
		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(1, msgs.size());
		assertEquals("in_nb( !KString )@self",
				msgs.get(0).getFormattedMessage());
	}

	@Test
	public void testInNbSelfLogsOnlyBefore() throws KlavaException {
		Tuple template = new Tuple(new KString());
		boolean found = node.in_nb(template, KlavaNode.self);

		assertFalse(found);
		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(1, msgs.size());
		assertEquals("in_nb( !KString )@self",
				msgs.get(0).getFormattedMessage());
	}

	// --- in_t ---

	@Test
	public void testInTLocalSuccessLogsBeforeAndAfter() throws KlavaException {
		node.out(new Tuple(new KString("hello")));
		listAppender.list.clear();

		Tuple template = new Tuple(new KString());
		boolean found = node.in_t(template, 1000);

		assertTrue(found);
		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(2, msgs.size());
		assertEquals("in_t( !KString )@self", msgs.get(0).getFormattedMessage());
		assertEquals("in_t( !KString )@self -> in_t( hello )@self",
				msgs.get(1).getFormattedMessage());
	}

	@Test
	public void testInTLocalTimeoutLogsOnlyBefore() throws KlavaException {
		Tuple template = new Tuple(new KString());
		boolean found = node.in_t(template, 1);

		assertFalse(found);
		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(1, msgs.size());
		assertEquals("in_t( !KString )@self", msgs.get(0).getFormattedMessage());
	}

	@Test
	public void testInTSelfSuccessLogsBeforeAndAfter() throws KlavaException {
		node.out(new Tuple(new KString("hello")));
		listAppender.list.clear();

		Tuple template = new Tuple(new KString());
		boolean found = node.in_t(template, KlavaNode.self, 1000);

		assertTrue(found);
		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(2, msgs.size());
		assertEquals("in_t( !KString )@self", msgs.get(0).getFormattedMessage());
		assertEquals("in_t( !KString )@self -> in_t( hello )@self",
				msgs.get(1).getFormattedMessage());
	}

	// --- read ---

	@Test
	public void testReadLocalLogsBeforeAndAfter() throws KlavaException {
		node.out(new Tuple(new KString("hello")));
		listAppender.list.clear();

		Tuple template = new Tuple(new KString());
		node.read(template);

		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(2, msgs.size());
		assertEquals("read( !KString )@self",
				msgs.get(0).getFormattedMessage());
		assertEquals("read( !KString )@self -> read( hello )@self",
				msgs.get(1).getFormattedMessage());
	}

	@Test
	public void testReadSelfLogsBeforeAndAfter() throws KlavaException {
		node.out(new Tuple(new KString("hello")));
		listAppender.list.clear();

		Tuple template = new Tuple(new KString());
		node.read(template, KlavaNode.self);

		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(2, msgs.size());
		assertEquals("read( !KString )@self",
				msgs.get(0).getFormattedMessage());
		assertEquals("read( !KString )@self -> read( hello )@self",
				msgs.get(1).getFormattedMessage());
	}

	// --- read_nb ---

	@Test
	public void testReadNbLocalLogsOnlyBefore() {
		Tuple template = new Tuple(new KString());
		boolean found = node.read_nb(template);

		assertFalse(found);
		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(1, msgs.size());
		assertEquals("read_nb( !KString )@self",
				msgs.get(0).getFormattedMessage());
	}

	@Test
	public void testReadNbSelfLogsOnlyBefore() throws KlavaException {
		Tuple template = new Tuple(new KString());
		boolean found = node.read_nb(template, KlavaNode.self);

		assertFalse(found);
		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(1, msgs.size());
		assertEquals("read_nb( !KString )@self",
				msgs.get(0).getFormattedMessage());
	}

	// --- eval ---

	@Test
	public void testEvalLocalLogs() throws KlavaException {
		DoNothingProcess process = new DoNothingProcess("testProcess");
		node.eval(process);

		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(1, msgs.size());
		assertEquals("eval( testProcess )@self", msgs.get(0).getFormattedMessage());
	}

	@Test
	public void testEvalSelfLogs() throws KlavaException {
		DoNothingProcess process = new DoNothingProcess("testProcess");
		node.eval(process, KlavaNode.self);

		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(1, msgs.size());
		assertEquals("eval( testProcess )@self", msgs.get(0).getFormattedMessage());
	}

	// --- read_t ---

	@Test
	public void testReadTLocalSuccessLogsBeforeAndAfter() throws KlavaException {
		node.out(new Tuple(new KString("hello")));
		listAppender.list.clear();

		Tuple template = new Tuple(new KString());
		boolean found = node.read_t(template, 1000);

		assertTrue(found);
		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(2, msgs.size());
		assertEquals("read_t( !KString )@self",
				msgs.get(0).getFormattedMessage());
		assertEquals("read_t( !KString )@self -> read_t( hello )@self",
				msgs.get(1).getFormattedMessage());
	}

	@Test
	public void testReadTLocalTimeoutLogsOnlyBefore() throws KlavaException {
		Tuple template = new Tuple(new KString());
		boolean found = node.read_t(template, 1);

		assertFalse(found);
		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(1, msgs.size());
		assertEquals("read_t( !KString )@self",
				msgs.get(0).getFormattedMessage());
	}

	// --- Logical locality tests ---
	//
	// When the destination is a LogicalLocality that resolves to a local
	// physical address, log messages should include both the logical name and
	// the resolved physical address (format: "logicalName - physicalAddr").

	@Test
	public void testOutLogicalLocalityLogs() throws KlavaException {
		PhysicalLocality physLoc = new PhysicalLocality("127.0.0.1", 9000);
		LogicalLocality logLoc = new LogicalLocality("myNode");
		KlavaNode localNode = createNodeForLogicalLocality(logLoc, physLoc);

		Tuple tuple = new Tuple(new KString("hello"));
		localNode.out(tuple, logLoc);

		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(1, msgs.size());
		assertEquals("out( hello )@" + logLoc + " - " + physLoc,
				msgs.get(0).getFormattedMessage());
	}

	@Test
	public void testInLogicalLocalityLogsBeforeAndAfter() throws KlavaException {
		PhysicalLocality physLoc = new PhysicalLocality("127.0.0.1", 9000);
		LogicalLocality logLoc = new LogicalLocality("myNode");
		KlavaNode localNode = createNodeForLogicalLocality(logLoc, physLoc);

		localNode.out(new Tuple(new KString("hello")));
		listAppender.list.clear();

		Tuple template = new Tuple(new KString());
		localNode.in(template, logLoc);

		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(2, msgs.size());
		assertEquals("in( !KString )@" + logLoc + " - " + physLoc,
				msgs.get(0).getFormattedMessage());
		assertEquals("in( !KString )@" + logLoc + " - " + physLoc
				+ " -> in( hello )@" + physLoc,
				msgs.get(1).getFormattedMessage());
	}

	@Test
	public void testEvalLogicalLocalityLogs() throws KlavaException {
		PhysicalLocality physLoc = new PhysicalLocality("127.0.0.1", 9000);
		LogicalLocality logLoc = new LogicalLocality("myNode");
		KlavaNode localNode = createNodeForLogicalLocality(logLoc, physLoc);

		DoNothingProcess process = new DoNothingProcess("testProcess");
		localNode.eval(process, logLoc);

		List<ILoggingEvent> msgs = debugMessages();
		assertEquals(1, msgs.size());
		assertEquals("eval( testProcess )@" + logLoc + " - " + physLoc,
				msgs.get(0).getFormattedMessage());
	}
}
