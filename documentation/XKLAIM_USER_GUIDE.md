# X-KLAIM User Guide

X-KLAIM is the Xtext-based language for writing KLAVA programs. It lets you describe nodes, nets, tuple-space operations, and mobile processes in a concise syntax that compiles to Java.

If you want the compiler, formatter, or validator internals, see the developer notes in the `xklaim` module and the existing documentation in [CODEGEN_NOTES.md](CODEGEN_NOTES.md).

## What X-KLAIM Gives You

- A compact syntax for KLAVA nodes and networks.
- Direct support for `out`, `in`, `read`, `in_nb`, `read_nb`, and `eval`.
- Inline process expressions with `proc { ... }`.
- Logical and physical locality declarations.
- A generated Java entry point that starts the network and waits for completion.

## The Basic Shape Of A Program

An X-KLAIM file can contain:

- An optional `package` declaration.
- An optional import section.
- Zero or more standalone processes.
- Zero or more standalone nodes.
- Zero or more nets.

The grammar is defined in [Xklaim.xtext](../xklaim/xklaim.parent/xklaim/src/xklaim/Xklaim.xtext).

## Nodes

Nodes are the simplest executable unit.

```xklaim
node Hello {
    out("Hello World")@self
    in(var String message)@self
    println(message)
    done()
}
```

In this example:

- `out` inserts the tuple into the local tuple space.
- `in` retrieves the same tuple.
- `done()` tells the generated runtime that this node is finished.

## Nets

A `net` groups several nodes under one physical network name.

```xklaim
net HelloNet physical "tcp-127.0.0.1:9999" {
    node Reader logical "reader" [writerLoc -> writer] {
        in(var String s)@writerLoc
        println(s)
        done()
    }

    node Writer logical "writer" {
        out("Hello World")@self
    }
}
```

Use a net when your application has multiple nodes that should share a common physical deployment.

## Logical And Physical Localities

X-KLAIM lets you express both:

- `physical "tcp-127.0.0.1:9999"` for the network endpoint.
- `logical "writer"` for the symbolic node name.

You can also add environment entries to map logical names to other nodes:

```xklaim
node Reader logical "reader" [writerLoc -> writer] {
    in(var String s)@writerLoc
}
```

The generated Java code resolves those mappings into the runtime environment.

## Tuple-Space Operations

The main operations are:

```xklaim
out("value")@self
in(var String s)@writerLoc
read(var Integer i)@self
in_nb(var String s)@self
read_nb(var String s)@self
eval(proc { println("remote") })@server
```

Timeout-based forms are also supported:

```xklaim
in(var Integer i)@self within 1000
read(var String s)@self within timeout
```

## Inline Processes

An inline process is written with `proc { ... }`.

```xklaim
out(proc {
    in(var String s)@self
    println(s)
})@writer
```

Inline processes are especially useful with `out` and `eval`.

You can also combine them with `or(...)` to represent alternative behaviors:

```xklaim
or(
    proc {
        in(var Integer i)@self
        println(i)
    },
    proc {
        read(var String s2)@self
        println(s2)
    }
)
```

## Variables And Pattern Matching

Use `var` to declare formal tuple fields:

```xklaim
in(var String message)@self
read(var Locality loc1, var Locality loc2, var Locality loc3)@self
```

Use `val` when you want to keep a value concrete in a local binding:

```xklaim
val myLoc = getPhysical(self)
```

These bindings are useful both in ordinary expressions and inside processes.

## Built-In Helpers

X-KLAIM programs can call runtime helpers that are compiled into Java:

- `done()` to end a node process.
- `login(...)` and `logout(...)` for mobility workflows.
- `getPhysical(...)` to obtain a physical locality.
- `phyloc(...)` and `logloc(...)` to build locality values.

Example:

```xklaim
node Sender [server -> phyloc("tcp-127.0.0.1:9999")] {
    login(server)
    val myLoc = getPhysical(self)
    eval({
        println("Hello " + server)
        out("DONE")@myLoc
    })@server
    in("DONE")@self
    logout(server)
    done()
}
```

## A Complete Example

This is the simplest end-to-end X-KLAIM program from the repository:

```xklaim
package xklaim.examples.hello

net HelloNet physical "tcp-127.0.0.1:9999" {
    node Reader logical "reader" [writerLoc -> writer] {
        in(var String s)@writerLoc
        println(s)
        System.exit(0)
    }
    node Writer logical "writer" {
        out("Hello World")@self
    }
}
```

You can find the source in [Hello.xklaim](../xklaim/xklaim.parent/xklaim.examples/src/xklaim/examples/hello/Hello.xklaim).

## Running A Program

In the IDE, the common workflow is:

1. Open the `.xklaim` file.
2. Right click it.
3. Choose `Run As` -> `Xklaim Application`.

The generated launcher starts the net and waits for all main processes to complete.

## Generated Java

Every X-KLAIM source file generates Java code. In broad terms, the compiler creates:

- A Java class for each node or net.
- An `addMainProcess()` method for the root process.
- A `main(String[])` launcher that starts the generated node or net and waits for completion.

The exact generated shape is illustrated in [CODEGEN_NOTES.md](CODEGEN_NOTES.md).

## Common Patterns

- Use a `net` when you want several cooperating nodes in one program.
- Use `node` when you want a standalone executable unit.
- Use `logical` names to keep code portable across machines.
- Use `physical` when the node must bind to a specific endpoint.
- Use `self` for local tuple-space operations and for code that should travel with the process.

## Example Gallery

- [Hello World](../xklaim/xklaim.parent/xklaim.examples/src/xklaim/examples/hello/Hello.xklaim)
- [Hello Localities](../xklaim/xklaim.parent/xklaim.examples/src/xklaim/examples/hellolocalities/HelloLocalities.xklaim)
- [Remote Process Transfer](../xklaim/xklaim.parent/xklaim.examples/src/xklaim/examples/helloremote/HelloFromReceivedProc.xklaim)
- [Remote `eval`](../xklaim/xklaim.parent/xklaim.examples/src/xklaim/examples/helloremoteeval/HelloRemoveEvalProc.xklaim)
- [Leader Election](../xklaim/xklaim.parent/xklaim.example.leaderelection/src/xklaim/example/leaderelection/LeaderElection.xklaim)
- [Mobility Sender](../xklaim/xklaim.parent/xklaim.example.mobility.sender/src/xklaim/example/mobility/sender/CodeMobilitySender.xklaim)
- [Mobility Receiver](../xklaim/xklaim.parent/xklaim.example.mobility.receiver/src/xklaim/example/mobility/receiver/CodeMobilityReceiver.xklaim)

## Practical Tips

- Start with the `Hello.xklaim` example and then move to `HelloLocalities.xklaim`.
- If a tuple operation seems stuck, check the target locality and whether a matching tuple was ever produced.
- If you use `eval`, remember that the process runs remotely, so `self` is resolved on the destination side.
- When a program needs to finish on its own, pair the last operation with `done()` or `System.exit(0)` in the example code.

## Further Reading

- [KLAVA user guide](KLAVA_USER_GUIDE.md) for the runtime semantics behind the language.
- [IMC user guide](IMC_USER_GUIDE.md) for the networking layer under the hood.
- [Xklaim tests](../xklaim/xklaim.parent/xklaim.tests/src/xklaim/tests/XklaimCompilerTest.java) for more syntax examples.
