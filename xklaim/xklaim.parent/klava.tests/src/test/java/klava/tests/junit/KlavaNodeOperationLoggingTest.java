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
import klava.Tuple;
import klava.topology.KlavaNode;

/**
 * Tests for {@link KlavaNode} operation logging at DEBUG level.
 *
 * <p>Each public tuple-space operation ({@code out}, {@code in}, {@code read}
 * and their non-blocking / timeout variants) should emit a DEBUG log message in
 * the form {@code <op>( <tuple> )@<locality>} when it starts, and – for
 * retrieval operations that succeed – a second message in the form
 * {@code <op>( <original> )@<locality> -> <op>( <result> )@<physicalLocality>}
 * when it completes.</p>
 */
public class KlavaNodeOperationLoggingTest {

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
}
