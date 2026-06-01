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
    out("message", "Hello World")@self
    in("message", var String message)@self
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
        in("message", var String s)@writerLoc
        println(s)
        done()
    }

    node Writer logical "writer" {
        out("message", "Hello World")@self
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
    in("message", var String s)@writerLoc
}
```

The generated Java code resolves those mappings into the runtime environment.

## Tuple-Space Operations

The main operations are:

```xklaim
out("ready")@self
in("message", var String s)@writerLoc
read(var Integer i)@self
in_nb(var String s)@self
read_nb(var String s)@self
eval(proc { println("remote") })@server
```

Timeout-based forms are also supported:

```xklaim
in(var Integer i)@self within 1000
read("message", var String s)@self within timeout
```

Single-field tuples are valid and useful for small examples. In larger
protocols, it is often clearer to use more than one field, for example a first
tag field such as `"message"`, `"result"`, or `"done"` followed by the payload.
Tags make the protocol easier to read and help avoid accidental matches between
unrelated tuples.

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
        in("result", var Integer i)@self
        println(i)
    },
    proc {
        read("message", var String s2)@self
        println(s2)
    }
)
```

## Variables And Pattern Matching

Use `var` to declare formal tuple fields:

```xklaim
in("message", var String message)@self
read("route", var Locality loc1, var Locality loc2, var Locality loc3)@self
```

Use `val` when you want to keep a value concrete in a local binding:

```xklaim
val myLoc = getPhysical(self)
```

These bindings are useful both in ordinary expressions and inside processes.

Tuple matching is positional. Concrete fields must be equal to the stored tuple
field, while formal fields declared with `var` match any value of the requested
type and are bound after a successful retrieval. For example:

```xklaim
out("person", "Ada", 37)@self
in("person", var String name, var Integer age)@self
println(name + " is " + age)
```

The template `("person", var String name, var Integer age)` only matches a
three-field tuple whose first item is `"person"`, whose second item is a
`String`, and whose third item is an `Integer`. After the match, `name` and
`age` are available in the continuation.

Formal fields also participate in Xbase scoping when the tuple operation is used
as a condition. In an `if`, the variables declared in the condition are scoped
so they can be referenced from the branch bodies:

```xklaim
if (in("person", var String name, var Integer age)@self within 1000) {
    println("received " + name + " / " + age)
} else {
    println("no matching person tuple arrived")
}
```

For blocking operations used as ordinary statements, the continuation is the code
after the operation. For operations used as boolean conditions, especially
timeout and non-blocking forms, the continuation is the branch whose condition
actually succeeded.

For non-blocking retrievals, bind and use the variables only on the branch where
the operation succeeded:

```xklaim
if (in_nb("person", var String name, var Integer age)@self) {
    println("received " + name + " / " + age)
} else {
    println("no matching person tuple was available")
}
```

`in_nb` and `read_nb` try once and return immediately. If they return `false`,
there was no matching tuple, so the formal fields should be treated as
unbound for the failed branch. In compound boolean expressions, remember normal
short-circuiting rules: a formal field is meaningful only if the operation that
declared it was actually evaluated and succeeded.

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
        out("status", "DONE")@myLoc
    })@server
    in("status", "DONE")@self
    logout(server)
    done()
}
```

## Tuple And Process Closure

X-Klaim follows the KLAVA closure rules when values move between nodes.

Tuple operations such as `out`, `in`, `read`, `in_nb`, and `read_nb` perform
automatic tuple closure before the tuple is sent. Logical localities inside the
tuple are resolved through the sender's environment, and `self` is translated to
the sender's physical locality. Formal fields remain formal because they are
templates, not values to resolve.

`eval` is different: it sends the process as it is. The process starts at the
destination node, and unresolved `self` references are interpreted there. This
is the key difference between sending a process as tuple data with `out` and
executing a process remotely with `eval`:

```xklaim
node Reader logical "reader" [writerLoc -> writer] {
    out(proc {
        out("origin", self)@writerLoc
    }, "stored-process")@writerLoc

    eval(proc {
        out("origin", self)@writerLoc
    })@writerLoc
}
```

In the `out` case, the process is placed inside a tuple, so tuple closure is
applied to the tuple before it travels. In the `eval` case, process closure is
not applied by `eval`; the process runs remotely and evaluates `self` at the
remote site. This distinction matters most when mobile code contains logical
localities or sends information about its current node.

## A Complete Example

This is an example of end-to-end X-KLAIM program from the repository:

```xklaim
package xklaim.examples.hello

net HelloNet physical "tcp-127.0.0.1:9999" {
	node Reader logical "reader" [writerLoc -> writer] {
		in("message", var String s)@writerLoc
		println(s)
		done()
	}
	node Writer logical "writer" {
		out("message", "Hello World")@self
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

## Creating A Project With The Wizard

The Eclipse UI includes an X-Klaim project wizard. Use it when starting a fresh
example or application so the project gets the expected nature, source layout,
runtime dependencies, and launch support.

Typical workflow:

1. Choose `File` -> `New` -> `Project...`.
2. Select the X-Klaim project wizard.
3. Enter the project name and finish the wizard.
4. Create or open a `.xklaim` file in the generated source folder.
5. Run it with `Run As` -> `Xklaim Application`.

Wizard-created projects are also set up for the default SLF4J simple logging
backend used by the runtime examples.

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

## Logging

X-Klaim, KLAVA, and IMC use SLF4J for runtime diagnostics. Wizard-created
projects and examples can use the simple backend, which reads a
`simplelogger.properties` file from the classpath.

For quick debugging, add a file named `simplelogger.properties` to the project's
resources or source folder:

```properties
org.slf4j.simpleLogger.log.klava=debug
org.slf4j.simpleLogger.log.xklaim=debug
org.slf4j.simpleLogger.showDateTime=true
```

You can also pass equivalent JVM system properties when launching the generated
program. See [LOGGING_NOTES.md](LOGGING_NOTES.md) for backend choices and more
detailed logging guidance.

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
