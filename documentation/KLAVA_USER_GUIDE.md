# KLAVA User Guide

KLAVA is the KLAIM runtime built on top of IMC. In plain Java, it gives you tuple spaces, logical and physical localities, and mobile processes.

If you want the implementation details, see [KLAVA_NOTES.md](KLAVA_NOTES.md). This guide focuses on how to use KLAVA from the point of view of an application developer.

## What KLAVA Is For

KLAVA lets processes communicate through shared tuple spaces instead of direct method calls. It is especially useful for:

- Distributed coordination.
- Mobile-agent style workflows.
- Asynchronous communication between nodes.
- Modeling distributed systems with logical node names.

## Main Building Blocks

### Tuple Space

A tuple space stores tuples and supports the classic KLAIM operations:

- `out` to insert a tuple.
- `in` to retrieve and remove a matching tuple.
- `read` to retrieve a matching tuple without removing it.
- `in_nb` and `read_nb` for non-blocking variants.
- `in_t` and `read_t` for timeout-based variants.

### Tuple

A tuple is an ordered collection of values. It can contain:

- Plain Java objects.
- KLAIM primitive wrappers such as `KString`, `KInteger`, `KBoolean`, and `KVector`.
- Localities.
- Nested tuples.
- Processes in mobility scenarios.

### Localities

Localities let you refer to nodes by name instead of hard-coding network addresses.

- `LogicalLocality` is the symbolic name, such as `writer` or `server`.
- `PhysicalLocality` is the actual network/session identity used by IMC.

### Nodes And Processes

A KLAVA node owns:

- A local tuple space.
- An environment mapping logical names to physical addresses.
- A closure maker for resolving localities before sending tuples or processes.
- A mobility engine for `eval`.

Processes run on nodes and use tuple-space operations to communicate.

## The Usual Flow

1. Define a node or a network of nodes.
2. Register logical-to-physical mappings in the environment.
3. Start the nodes.
4. Use `out`, `in`, `read`, and `eval` to coordinate work.
5. Call `waitForCompletion()` when the example should terminate cleanly.

## Quick Example: Hello World

The smallest KLAVA-style interaction is to put a tuple in one node and retrieve it from another.

```java
PhysicalLocality serverLoc = new PhysicalLocality("tcp-127.0.0.1:9999");
KlavaNode serverNode = new Net(serverLoc);
KlavaNode clientNode = new ClientNode(serverLoc);

serverNode.addNodeProcess(new KlavaProcess() {
    @Override
    public void executeProcess() throws KlavaException {
        in(new Tuple(new KString()), self);
    }
});

clientNode.addNodeProcess(new KlavaProcess() {
    @Override
    public void executeProcess() throws KlavaException {
        out(new Tuple(new KString("Hello World!")), serverLoc);
    }
});
```

This is the same pattern used in [KlavaHelloWorld.java](../xklaim/xklaim.parent/klava.tests/src/examples/java/klava/examples/hello/KlavaHelloWorld.java).

## Matching Tuples

Tuple matching is pattern-based. You build a template tuple and pass it to the retrieval operation.

Examples of useful matches:

```java
Tuple template = new Tuple(new KString());
clientNode.read_nb(template);

Tuple typedTemplate = new Tuple(new Object[] { String.class });
clientNode.in(typedTemplate, serverLoc);

clientNode.read_t(template, self, 1000);
```

Matching is positional. All tuple items must line up for the operation to succeed.

## Blocking, Non-Blocking, And Timeout Operations

KLAVA exposes three families of retrieval operations:

- Blocking: wait until a matching tuple appears.
- Non-blocking: check once and return immediately.
- Timeout: wait for a limited period.

Example patterns:

```java
Tuple template = new Tuple(new KString());
clientNode.in(template, self);
clientNode.read_nb(template, serverLoc);
clientNode.in_t(template, serverLoc, 1000);
```

Timeout variants are especially useful when you want a protocol to keep making progress even if a tuple never shows up.

## Logical And Physical Localities

Logical localities are how you write portable code. KLAVA resolves them to physical addresses at runtime through the environment.

```java
LogicalLocality destination = new LogicalLocality("destination");
clientNode.addToEnvironment(destination, serverLoc);
clientNode.read_nb(new Tuple(new KString()), destination);
```

You can also register logical mappings explicitly inside a `KlavaProcess` or on the node itself before starting execution.

## `self`

`self` is a special locality meaning “the current node”.

Typical uses:

- Sending a tuple to the local tuple space: `out(new Tuple(new KString("Hello")), self)`
- Receiving a tuple from the current node: `in(new Tuple(new KString()), self)`
- Inspecting the current execution location with `getPhysical(self)`

When a process is migrated with `eval`, `self` resolves on the destination node.

## Process Mobility

KLAVA can ship a process to another node with `eval`.

```java
MigratingProcess migratingProcess = new MigratingProcess(serverLoc);
clientNode.eval(migratingProcess);
```

That process is serialized, transmitted, and executed on the remote node. This is the main building block for code mobility.

## Login And Logout

Some examples use an explicit login/logout flow before remote evaluation:

Login usually happens during setup:

```java
public DatabaseNode(String screenTitle, Locality serverNodeLoc) throws KlavaException {
    super(screenTitle);
    if (serverNodeLoc != null) {
        login(serverNodeLoc);
    }
}
```

Logout is explicit when the client is done:

```java
boolean logoutResult = clientNode.logout(serverLoc);
assertTrue(logoutResult);
```

See the mobility examples:

- [DatabaseNode.java](../xklaim/xklaim.parent/klava.tests/src/examples/java/klava/examples/newsgatherer/DatabaseNode.java)
- [SimpleProcess.java](../xklaim/xklaim.parent/klava.tests/src/test/java/klava/tests/junit/SimpleProcess.java)
- [MigratingProcess.java](../xklaim/xklaim.parent/klava.tests/src/test/java/klava/tests/junit/MigratingProcess.java)

## Example: Remote Process Sent Over A Tuple Space

The `NodeProcessTest` suite shows a process being received as a tuple and then executed locally:

```java
public class ReceiveProcess extends KlavaProcess {
    @Override
    public void executeProcess() throws KlavaException {
        KlavaProcessVar klavaProcessVar = new KlavaProcessVar();
        Tuple template = new Tuple(klavaProcessVar);
        in(template, self);
        System.out.println("received a process");
        eval(klavaProcessVar.klavaProcess, self);
    }
}
```

This is a good introduction to combining tuple-space communication with mobility in plain Java.

## Practical Tips

- Use logical localities in your source code and keep physical addresses in the environment.
- Prefer `read` when you want to observe a tuple without consuming it.
- Prefer `in` when you want a tuple to be consumed exactly once.
- Use `out(new Tuple(...), destination)` and `eval(process)` for mobile code workflows.
- Keep an eye on the `self` locality when debugging mobility, because it resolves differently before and after migration.

## Good Starting Points

- [IMC user guide](IMC_USER_GUIDE.md) if you want the networking layer underneath KLAVA.
- [XKLAIM user guide](XKLAIM_USER_GUIDE.md) if you want the language syntax and examples.
- [KLAVA_NOTES.md](KLAVA_NOTES.md) for implementation-level details.
