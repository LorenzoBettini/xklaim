# KLAVA User Guide

KLAVA is the KLAIM runtime built on top of IMC. It gives you tuple spaces, logical and physical localities, and mobile processes.

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
node Writer {
    out("Hello World")@self
}

node Reader [writerLoc -> writer] {
    in(var String s)@writerLoc
    println(s)
}
```

The same idea appears in the XKlaim example [Hello.xklaim](../xklaim/xklaim.parent/xklaim.examples/src/xklaim/examples/hello/Hello.xklaim).

## Matching Tuples

Tuple matching is pattern-based. Formal values are marked with `var` in X-Klaim or by using the formal KLAIM types directly in Java code.

Examples of useful matches:

```java
in("ID", var Integer myId)@rg
read(var String name)@self
in_nb(var Locality loc1, var Locality loc2)@self
```

Matching is positional. All tuple items must line up for the operation to succeed.

## Blocking, Non-Blocking, And Timeout Operations

KLAVA exposes three families of retrieval operations:

- Blocking: wait until a matching tuple appears.
- Non-blocking: check once and return immediately.
- Timeout: wait for a limited period.

Example patterns:

```java
in(var String s)@self
read_nb(var Integer i, s)@self
in(var Integer i, s)@self within 1000
```

Timeout variants are especially useful when you want a protocol to keep making progress even if a tuple never shows up.

## Logical And Physical Localities

Logical localities are how you write portable code. KLAVA resolves them to physical addresses at runtime through the environment.

```java
val rg = getPhysical(logloc("rg"))
in("ID", var Integer myId)@rg
```

You can also register logical mappings explicitly in a node or net definition.

## `self`

`self` is a special locality meaning “the current node”.

Typical uses:

- Sending a tuple to the local tuple space: `out("Hello")@self`
- Receiving a tuple from the current node: `in(var String s)@self`
- Inspecting the current execution location with `getPhysical(self)`

When a process is migrated with `eval`, `self` resolves on the destination node.

## Process Mobility

KLAVA can ship a process to another node with `eval`.

```java
eval({
    println("Hello from " + getPhysical(self))
    out("DONE")@myLoc
})@server
```

That process is serialized, transmitted, and executed on the remote node. This is the main building block for code mobility.

## Login And Logout

Some examples use an explicit login/logout flow before remote evaluation:

```java
login(server)
eval(proc {
    println("running remotely")
})@server
logout(server)
```

See the mobility examples:

- [CodeMobilitySender.xklaim](../xklaim/xklaim.parent/xklaim.example.mobility.sender/src/xklaim/example/mobility/sender/CodeMobilitySender.xklaim)
- [CodeMobilityReceiver.xklaim](../xklaim/xklaim.parent/xklaim.example.mobility.receiver/src/xklaim/example/mobility/receiver/CodeMobilityReceiver.xklaim)

## Example: Remote Process Sent Over A Tuple Space

The `HelloFromReceivedProc` example shows a process being sent as a tuple, retrieved remotely, and then executed:

```java
node Reader logical "reader" {
    out(proc {
        in(var String s)@self
        println(s)
        System.exit(0)
    })@writer
}

node Writer logical "writer" {
    out("Hello World")@self
    in(var KlavaProcess P)@self
    eval(P)@self
}
```

This example is a good introduction to combining tuple-space communication with mobility.

## Practical Tips

- Use logical localities in your source code and keep physical addresses in the environment.
- Prefer `read` when you want to observe a tuple without consuming it.
- Prefer `in` when you want a tuple to be consumed exactly once.
- Use `out(proc { ... })` and `eval(P)` for mobile code workflows.
- Keep an eye on the `self` locality when debugging mobility, because it resolves differently before and after migration.

## Good Starting Points

- [IMC user guide](IMC_USER_GUIDE.md) if you want the networking layer underneath KLAVA.
- [XKLAIM user guide](XKLAIM_USER_GUIDE.md) if you want the language syntax and examples.
- [KLAVA_NOTES.md](KLAVA_NOTES.md) for implementation-level details.
