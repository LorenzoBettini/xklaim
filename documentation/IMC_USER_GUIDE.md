# IMC User Guide

IMC (*Implementing Mobile Calculi*) is the runtime layer that provides sessions, protocol stacks, marshaling, event hooks, and code mobility. You usually interact with it indirectly through KLAVA or XKlaim, but it is still useful when you want to understand how nodes connect and how messages travel.

This guide is intentionally practical. If you need the deeper internal design, see [IMC_NOTES.md](IMC_NOTES.md).

## What IMC Gives You

- A connection and session model for TCP, UDP, and local pipe-based communication.
- A protocol/state-machine abstraction for building request/response exchanges.
- Marshaling and unmarshaling support for sending Java objects over the wire.
- A routing and session-management layer for node-to-node communication.
- Mobility support for serializing code across nodes.
- An event system for observing connection lifecycle changes.

## When You Need IMC Directly

You usually touch IMC directly if you are:

- Writing or debugging KLAVA/XKlaim runtime code.
- Implementing a custom protocol or transport layer.
- Tracking a connection problem at the session/protocol level.
- Building a small Java demo around IMC rather than using the Xtext language.

## Core Concepts

### Node

A node is the main runtime object. It owns a protocol stack, a routing table, and the listeners that monitor activity.

At a high level, a node:

1. Starts or accepts a session.
2. Creates a protocol stack for the chosen transport.
3. Runs one or more processes on top of that connection.
4. Waits for completion or keeps serving incoming requests.

### Session

A session is the live communication channel between two endpoints. IMC gives each session a stable identity, which is later used by KLAVA to track physical localities.

### Protocol Stack

The protocol stack is a layered pipeline. Each layer can add behavior such as framing, session numbering, or transport-specific setup.

### Protocol State

Protocols are modeled as state machines. A state reads from or writes to the channel, then returns the next state to execute.

## Typical Workflow

The usual IMC flow is:

1. Create a node or node subclass.
2. Configure the transport and protocol stack.
3. Accept an incoming session or connect to a remote endpoint.
4. Create and run a node process.
5. Wait for completion if the program is meant to terminate.

## Minimal Example

The exact APIs vary depending on the transport and higher-level stack you use, but the shape is always similar:

```java
public class DemoNode extends Node {
    public void addMainProcess() throws IMCException {
        addNodeProcess(new NodeProcess() {
            @Override
            public void executeProcess() throws Exception {
                System.out.println("Running inside IMC");
            }
        });
    }
}
```

In a real project, the node process usually does more than print. It may accept a connection, send or receive packets, or run a protocol state machine.

## Working With Transports

IMC supports several transport styles:

- TCP for ordinary client/server communication.
- UDP for datagram-style messaging.
- Local pipes for in-process or same-machine communication.

The transport choice affects how the bottom layer of the protocol stack is created, but the higher-level protocol logic stays the same.

## Events And Debugging

IMC can emit events for session and connection lifecycle changes. This is useful when you need to trace:

- When a session starts or ends.
- Whether a node accepted or rejected a connection.
- How routing tables change over time.

For tracing problems, it is often helpful to enable logging in the surrounding KLAVA or XKlaim project as well, because those layers build directly on IMC.

## Code Mobility

IMC also supports moving code as serialized Java objects and bytecode. KLAVA uses this capability for `eval`, where a process is shipped to another node and executed there.

## How IMC Relates To The Rest Of The Workspace

- KLAVA uses IMC for networking, sessions, and mobility.
- XKlaim uses KLAVA as its generated runtime target.
- Example applications in `xklaim.examples` and `xklaim.example.*` are the easiest way to see IMC in action indirectly.

## Practical Tips

- Prefer the existing node/process patterns in KLAVA and XKlaim unless you truly need a custom IMC protocol.
- If a remote operation is behaving oddly, inspect the session and routing layer first.
- If a connection appears to succeed but data never arrives, the problem is often in the stack layering or marshaling path.
- Keep transport configuration and logical naming separate. KLAVA and XKlaim rely on IMC session IDs for the physical layer, not for language-level names.

## Good Starting Points

- [KLAVA user guide](KLAVA_USER_GUIDE.md) if you want to use tuple spaces and mobility.
- [XKLAIM user guide](XKLAIM_USER_GUIDE.md) if you want to write programs in the language.
- [IMC_NOTES.md](IMC_NOTES.md) for an internal breakdown of packages and runtime behavior.

