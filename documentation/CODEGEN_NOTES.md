# XKlaim Code Generation — `done()` and `waitForCompletion()`

This note describes the Java code the XKlaim compiler generates for `node` and `net`
declarations, and how the `done()` / `waitForCompletion()` lifecycle is meant to work.

---

## What the compiler generates

### For each top-level `node`

A Java class extending `KlavaNode` is produced. It contains:

| Member | Description |
|---|---|
| `<NodeName>Process` | Private static inner class extending `KlavaNodeCoordinator`. Its `executeProcess()` body is the DSL node body. |
| `<locality> = new LogicalLocality(…)` | One `static final` field per environment entry (nodes with `[env]` block). |
| `setupEnvironment()` | Populates the environment map; only generated for nodes with an `[env]` block. |
| `addMainProcess()` | Creates the coordinator, registers it via `setMainCoordinator()`, and starts it via `addNodeCoordinator()`. |

Example — DSL input:

```
package foo

node TestNode [other -> phyloc("localhost:9999")] {
    out("hello")@other
    done()
}
```

Generated `TestNode.java` (abbreviated):

```java
public class TestNode extends KlavaNode {
  private static class TestNodeProcess extends KlavaNodeCoordinator {
    @Override
    public void executeProcess() {
      out("hello", other);
      done();
    }
  }

  private static final LogicalLocality other = new LogicalLocality("other");

  public void setupEnvironment() {
    addToEnvironment(other, getPhysical(XklaimRuntimeUtil.phyloc("localhost:9999")));
  }

  public void addMainProcess() throws IMCException {
    KlavaNodeCoordinator _coordinator = new TestNode.TestNodeProcess();
    setMainCoordinator(_coordinator);
    addNodeCoordinator(_coordinator);
  }
}
```

### For each `net`

A Java class extending `LogicalNet` is produced. Each node inside the net becomes
a public static inner class extending `ClientNode`. The net class also has:

| Member | Description |
|---|---|
| `<locality> = new LogicalLocality(…)` | One `static final` field per node (used as the logical locality name). |
| `<NodeName>` (inner class) | Extends `ClientNode`; same structure as a top-level node but its constructor calls `super(physicalLocality, logicalLocality)`. |
| constructor | Calls `super(new PhysicalLocality("…"))`. |
| `addNodes()` | Instantiates all inner nodes, calls `setupEnvironment()` where needed, calls `addManagedNode()` for each, then `addMainProcess()` for each. |

Example — DSL input:

```
package foo

net HelloNet physical "localhost:9999" {
    node Hello {
        out("Hello World")@self
        in(var String message)@self
        println(message)
        done()
    }
}
```

Generated `HelloNet.addNodes()`:

```java
public void addNodes() throws IMCException {
    HelloNet.Hello hello = new HelloNet.Hello();
    addManagedNode(hello);
    hello.addMainProcess();
}
```

### The generated `main` class

One main class per XKlaim source file with a `main(String[])` method that:

- Uses the source file name with first letter upper case.
- Appends the suffix `Main`.
- Example: `my.xklaim` generates the launcher class `MyMain`.

The launcher class then:

1. Instantiates all top-level nodes and nets.
2. Calls `setupEnvironment()` on nodes that have an environment block.
3. Calls `addMainProcess()` on each node / `addNodes()` on each net (starts execution).
4. Calls `waitForCompletion()` on each node and each net (waits for orderly shutdown).

```java
public static void main(final String[] args) throws Exception {
    // top-level nodes
    TestNode testNode = new TestNode();
    testNode.setupEnvironment();
    testNode.addMainProcess();
    testNode.waitForCompletion();

    // nets
    TestNet testNet = new TestNet();
    testNet.addNodes();
    testNet.waitForCompletion();
}
```

---

## `done()`

`done()` is defined on `KlavaNodeCoordinator`. Call it at the end of a node body to
signal that the node has finished its work:

```
node Hello {
    out("Hello World")@self
    in(var String message)@self
    println(message)
    done()          // terminates this coordinator cleanly
}
```

Internally, `done()` throws `KlavaNodeDoneException` (an unchecked exception).
`KlavaNodeCoordinator.execute()` catches it as normal completion — no stack trace,
no error log. Any code after `done()` is unreachable.

Without a `done()` call the coordinator keeps running indefinitely (waiting for
further tuple-space operations), which means `waitForCompletion()` in `main()` would
block forever.

---

## `waitForCompletion()`

`KlavaNode.waitForCompletion()` performs the following steps in order:

1. **Join the main coordinator thread** (if one was registered via `setMainCoordinator()`).
   This blocks until `executeProcess()` returns — either normally, via `done()`, or via
   an interrupt.
2. **Recursively call `waitForCompletion()`** on every node registered via
   `addManagedNode()`. For a net, these are the nodes created by `addNodes()`.
3. **Call `close()`** to release all sockets, sessions, and resources.

A timeout variant is also available:

```java
node.waitForCompletion(5000); // wait at most 5 000 ms
```

---

## `addMainProcess()` and `setMainCoordinator()`

`addMainProcess()` always does two distinct things:

```java
KlavaNodeCoordinator _coordinator = new <NodeName>Process();
setMainCoordinator(_coordinator);   // tells waitForCompletion() which thread to join
addNodeCoordinator(_coordinator);   // registers and starts the coordinator thread
```

Both calls are necessary:

- `addNodeCoordinator` starts the process running in a thread managed by the node.
- `setMainCoordinator` stores a reference to that thread so `waitForCompletion()` can
  join it later.

---

## Lifecycle diagrams

### Standalone node

```
new NodeClass()
└─ setupEnvironment()          (if [env] block present)
└─ addMainProcess()
   ├─ setMainCoordinator(…)
   └─ addNodeCoordinator(…)    ← coordinator thread starts here
      └─ executeProcess()      ← runs DSL body; calls done() when finished
└─ waitForCompletion()
   ├─ join(mainCoordinator)    ← blocks until executeProcess() returns
   └─ close()
```

### Net with nodes

```
new NetClass()
└─ addNodes()
   ├─ new NodeA(); addManagedNode(nodeA); nodeA.addMainProcess()
   └─ new NodeB(); addManagedNode(nodeB); nodeB.addMainProcess()
└─ waitForCompletion()
   ├─ nodeA.waitForCompletion()
   │  ├─ join(nodeA.mainCoordinator)
   │  └─ nodeA.close()
   ├─ nodeB.waitForCompletion()
   │  ├─ join(nodeB.mainCoordinator)
   │  └─ nodeB.close()
   └─ net.close()
```

---

## Shutdown noise suppression

When a node closes, the transport layer produces `EOFException` (remote side closed)
and `SocketException("Socket closed")` (local side closed). These are normal and are
silently downgraded to DEBUG level by `ProtocolExceptionUtils.isCausedByConnectionClose()`,
which traverses both the standard Java cause chain (`Throwable.getCause()`) and
`ProtocolException`'s custom `actual` field (via `ProtocolException.represents()`).
