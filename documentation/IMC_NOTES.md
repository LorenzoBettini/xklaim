# IMC — Developer Notes

IMC (*Implementing Mobile Calculi*, base package `org.mikado.imc`) is the
networking and mobility substrate that KLAVA builds on. It provides:

- A **protocol state-machine framework** for composable, typed communication protocols.
- A **layered marshaling/unmarshaling stack** over TCP, UDP, or local in-process
  pipes.
- A **session and routing layer** that tracks live connections and can forward
  messages through intermediate nodes.
- A **code-mobility layer** for serialising Java objects and bytecode across nodes.
- A lightweight **event bus** for observing connection lifecycle.

---

## Package Structure

| Package | Contents |
|---------|----------|
| `org.mikado.imc.protocols` | Core abstractions: `Protocol`, `ProtocolState`, `ProtocolStack`, `ProtocolLayer`, `Session`, `SessionId`, `Marshaler`/`UnMarshaler`, `SessionStarter`, `SessionStarterTable` |
| `org.mikado.imc.protocols.tcp` | `TcpSessionStarter`, `TcpIpProtocolLayer` |
| `org.mikado.imc.protocols.udp` | `UdpSessionStarter`, `UdpSessionStarterPure`, `UdpIpProtocolLayer`, `SessionNumberLayer`, `DatagramDispatcher` |
| `org.mikado.imc.protocols.pipe` | `LocalSessionStarter`, `ProtocolLayerPipe` |
| `org.mikado.imc.topology` | `Node`, `NodeProcess`, `NodeCoordinator`, `NodeProcessProxy`, `NodeCoordinatorProxy`, `SessionManager`, `RoutingTable`, `ConnectionServer`, `ConnectionStarter`, `ConnectState` |
| `org.mikado.imc.mobility` | `MigratingCode`, `JavaMigratingCode`, `MigratingCodeFactory`, `JavaByteCodeMigratingCodeFactory` |
| `org.mikado.imc.events` | `EventManager`, `EventGenerator`, `EventGeneratorAdapter` |
| `org.mikado.imc.common` | Shared exceptions (`IMCException`, `ProtocolException`, …) |
| `org.mikado.imc.log` | Logging helpers |
| `org.mikado.imc.ts` | Tuple-space support types shared with KLAVA |

---

## Protocol System

### Protocol — `org.mikado.imc.protocols.Protocol`

A `Protocol` is a **named state machine** used to carry out one complete
conversation over a connection.

```
Protocol
  states : Hashtable<String, ProtocolState>   // keyed by state id
  protocolStack : ProtocolStack               // shared by all states
  current_state : String                      // non-empty while running
  static START = "START"                      // reserved first-state id
  static END   = "END"                        // reserved sentinel id
```

`Protocol.start(Object param, TransmissionChannel channel)` drives the machine:

```
current_state = START
while current_state != END:
    state = states[current_state]
    state.enter(param, channel)
    current_state = state.getNextState()
for each state:
    state.closed()    // cleanup regardless of path
close protocolStack
```

`param` is passed to the START state's `enter()` and is typically
`null` or a `TuplePacket` / connection string depending on the protocol.

Additional API:

| Method | Purpose |
|--------|---------|
| `setState(id, state)` | Register a state; throws if id already used |
| `setState0(id, state)` | Register unconditionally (internal use) |
| `accept(SessionStarter)` | Delegate to `protocolStack.accept()` |
| `connect(SessionStarter)` | Delegate to `protocolStack.connect()` |
| `insertLayer(ProtocolLayer)` | Add a layer to the underlying stack |
| `setLowLayer(ProtocolLayer)` | Replace the bottom-most layer |

Extends `EventGeneratorAdapter`; events are forwarded via the stack/states.

---

### ProtocolState — `org.mikado.imc.protocols.ProtocolState` (abstract)

Represents one step in a protocol.

```java
abstract void enter(Object param, TransmissionChannel transmissionChannel)
    throws ProtocolException;
```

After `enter()` returns, `Protocol` reads `getNextState()` to decide the
transition. The default next state is `Protocol.END`.

Every state holds a reference to the owning `ProtocolStack`, through which it
creates marshalers/unmarshalers and obtains the current `Session`:

```java
Marshaler   createMarshaler()   // acquires write mutex
UnMarshaler createUnMarshaler() // reads from channel
Session     getSession()
void        releaseMarshaler(Marshaler m)
void        releaseUnMarshaler(UnMarshaler um)
void        closed()  // called by Protocol at exit; override for cleanup
```

---

### ProtocolStateSimple — `org.mikado.imc.protocols.ProtocolStateSimple`

The simplest concrete state. Its default `enter()` just creates an unmarshaler
and returns `END`. Concrete protocol steps extend this class, override `enter()`,
and set `next_state` in the constructor (or via `setNextState()`).

`updateReference(oldState, newState)` rewires the `next_state` link, which
`ProtocolComposite` uses when splicing protocols together.

---

### ProtocolComposite — `org.mikado.imc.protocols.ProtocolComposite`

Wraps an inner `Protocol` so it can be **embedded as a step** inside an outer
protocol. Construction:

```java
new ProtocolComposite(ProtocolState start, ProtocolState end, Protocol inner)
```

The composite creates an `InnerState` that calls `inner.start()` during its
`enter()`. `accept()` / `connect()` delegate to `inner`, ensuring both share the
same `ProtocolStack`. Event management is fully forwarded to `inner`.

This is the primary mechanism for building complex multi-phase protocols from
smaller reusable building blocks.

---

## Protocol Stack and Layers

### ProtocolStack — `org.mikado.imc.protocols.ProtocolStack`

```
ProtocolStack
  layers       : Vector<ProtocolLayer>   // ordered top → bottom
  writingMutex : WritingMutex            // serialises concurrent writes
  session      : Session                 // set after accept/connect
```

**Marshaling direction**: `createMarshaler()` iterates from bottom to top,
calling each layer's `doCreateMarshaler(Marshaler prev)`, so each layer wraps
the one below. The result is a decorator chain:

```
AppLayer.doCreateMarshaler(
  SessionNumberLayer.doCreateMarshaler(
    TcpIpLayer.doCreateMarshaler(null)))
```

`releaseMarshaler()` iterates top-to-bottom, calling `doReleaseMarshaler()` on
each layer (which typically flushes), then releases the write mutex.

**Unmarshaling direction**: `createUnMarshaler()` iterates bottom to top
symmetrically; each layer can decorate the underlying stream.

**`WritingMutex`** is an inner class implementing mutual exclusion:

```java
synchronized void startWriting()   // blocks until free, then acquires
synchronized void endWriting()     // releases and notifyAll
```

Stack mutation:

| Method | Effect |
|--------|--------|
| `setLowLayer(layer)` | Append at bottom |
| `insertLayer(layer)` | Same as `setLowLayer` |
| `insertFirstLayer(layer)` | Insert at top |
| `insertAfter(before, toInsert)` | Insert after named layer |
| `replace(toReplace, toInsert)` | Replace an existing layer |

Session establishment:

```java
Session accept(SessionStarter s)   // calls s.accept(); lowest layer = transport layer
Session connect(SessionStarter s)  // calls s.connect(); same
```

---

### ProtocolLayer — `org.mikado.imc.protocols.ProtocolLayer` (abstract)

Template-method base for all layers. Subclasses override hook methods:

```java
UnMarshaler doCreateUnMarshaler(UnMarshaler um)   // decorate or replace
Marshaler   doCreateMarshaler(Marshaler m)         // decorate or replace
void        doReleaseMarshaler(Marshaler m)        // flush/release (default: flush m)
void        doReleaseUnMarshaler(UnMarshaler um)   // (default: no-op)
void        doClose()                              // layer-specific teardown
```

---

### ProtocolLayerEndPoint — `org.mikado.imc.protocols.ProtocolLayerEndPoint`

The **terminal layer** at the bottom of every stack. It holds the actual
`Marshaler` and `UnMarshaler` instances that read/write the underlying socket
or pipe streams. Its `doCreateMarshaler` / `doCreateUnMarshaler` return the
stored instances, ignoring the parameter.

Transport-specific layers (`TcpIpProtocolLayer`, `UdpIpProtocolLayer`,
`ProtocolLayerPipe`) are typically subclasses or wrappers around
`ProtocolLayerEndPoint`.

---

### ProtocolLayerComposite — `org.mikado.imc.protocols.ProtocolLayerComposite`

A **self-expanding** composite of two layers. When `setProtocolStack()` is called
(i.e. when the composite is inserted into a real stack), it replaces itself with
`first` at the current position and inserts `second` after `first`:

```
Before insertion:  [..., composite, ...]
After insertion:   [..., first, second, ...]
```

`UdpSessionStarter` uses this to transparently combine its base UDP layer with
`SessionNumberLayer` for reliable sequencing.

---

### Concrete Layers

| Layer | Package | Purpose |
|-------|---------|---------|
| `TcpIpProtocolLayer` | `protocols.tcp` | Wraps TCP `Socket` streams in `IMCMarshaler`/`IMCUnMarshaler` |
| `UdpIpProtocolLayer` | `protocols.udp` | Wraps `DatagramSocket` for UDP |
| `SessionNumberLayer` | `protocols.udp` | Adds sequence numbers on top of UDP for in-order reliable delivery |
| `ProtocolLayerPipe` | `protocols.pipe` | Bidirectional `LinkedBlockingQueue` pair for in-JVM communication |

---

## Sessions and Transport

### Session — `org.mikado.imc.protocols.Session`

```
Session
  protocolLayer : ProtocolLayer   // handles I/O
  local         : SessionId       // this endpoint
  remote        : SessionId       // peer endpoint
  closed        : boolean
```

`Session.close()` closes the `protocolLayer` and sets `closed = true`.
`toString()` returns `"local->remote"`.

---

### SessionId — `org.mikado.imc.protocols.SessionId`

Protocol-agnostic endpoint identifier:

```
SessionId
  connectionProtocolId : String   // e.g. "tcp", "udp", "pipe"
  text                 : String   // protocol-specific address
```

`toString()` → `"<protocol>-<text>"`.

`SessionId.parseSessionId(String str)` splits on the first `"-"` separator.

**`IpSessionId`** (extends `SessionId`) adds `host` and `port`, and converts
to/from `"<protocol>-<host>:<port>"`. `IpSessionId.parseAddress(str)` returns an
`InetSocketAddress`.

Both are `Serializable` and implement `Comparable<SessionId>` (by string
representation) so they can be used as map keys.

---

### SessionStarter — `org.mikado.imc.protocols.SessionStarter` (abstract)

Factory for one connection endpoint:

```java
Session  accept()                                 // block until peer connects
Session  connect()                                // connect to remote
SessionId bindForAccept(SessionId id)             // bind server socket; may allocate port
void     close()                                  // shut down starter
```

`localSessionId` must be set before `accept()`; `remoteSessionId` before
`connect()`. Either can be set explicitly or via `bindForAccept()`.

---

### TcpSessionStarter — `org.mikado.imc.protocols.tcp.TcpSessionStarter`

| Direction | Implementation |
|-----------|---------------|
| `accept()` | Creates `ServerSocket` (re-uses address, retry × 5 with backoff); calls `serverSocket.accept()`; wraps socket in `TcpIpProtocolLayer`; returns `Session(layer, localId, remoteId)` |
| `connect()` | Creates unbound `Socket`; optionally binds to `localSessionId`; calls `socket.connect(remoteAddress)`; wraps in `TcpIpProtocolLayer` |
| `bindForAccept()` | Creates `ServerSocket(port, backlog, addr, reuseAddress=true)` |

---

### UdpSessionStarter — `org.mikado.imc.protocols.udp.UdpSessionStarter`

Extends `UdpSessionStarterPure` (which handles core `DatagramSocket` / `DatagramDispatcher` logic). Overrides `createProtocolLayer()` to return a `ProtocolLayerComposite(SessionNumberLayer, baseUdpLayer)`, giving sequence-numbered reliable delivery.

`DatagramDispatcher` is an internal multiplexer that reads from a shared
`DatagramSocket` and routes packets to per-session `DatagramQueue` instances.

---

### LocalSessionStarter — `org.mikado.imc.protocols.pipe.LocalSessionStarter`

Establishes **in-process** sessions with no network I/O. Uses a static
`PipeTable`:

```
PipeTable
  acceptAssociations : Hashtable<SessionId, LinkedBlockingQueue<PipeStruct>>
```

| Direction | Steps |
|-----------|-------|
| `accept()` | Registers `localSessionId` in table; blocks on `queue.take()` waiting for a `PipeStruct` from the connecting end |
| `connect()` | Creates a `PipeStruct` (pair of bidirectional queues); posts it to `queue`; returns symmetric session |

`PipeStruct` holds a `ProtocolLayerPipe` (two `LinkedBlockingQueue<byte[]>`) and
an auto-generated `SessionId` with a "pipe" protocol prefix.

Primarily used in unit tests to avoid real sockets.

---

### SessionStarterTable and IMCSessionStarterTable

`SessionStarterTable` maps protocol IDs (`String`) to `SessionStarterFactory`
instances (case-insensitive). `createSessionStarter(localId, remoteId)` extracts
the protocol from the IDs and invokes the registered factory.

`IMCSessionStarterTable` pre-registers:

| Protocol ID | Implementation |
|-------------|---------------|
| `"tcp"` | `TcpSessionStarter` |
| `"udp"` | `UdpSessionStarter` |
| `"pipe"` | `LocalSessionStarter` |

Custom transports are added by calling `associateSessionStarterFactory()`.

---

### SessionManager — `org.mikado.imc.topology.SessionManager`

Tracks live connections for a `Node`:

```
SessionManager
  connections    : Hashtable<Session, ProtocolStack>
  sessionStarters: SessionStarters   // pending (not yet accepted) starters
  eventManager   : EventManager
```

| Method | Behaviour |
|--------|-----------|
| `addSession(stack)` | Adds if not present; fires `CONNECTION` event |
| `removeSession(session)` | Removes; fires `DISCONNECTION` event |
| `getNodeStack(NodeLocation loc)` | Finds the `ProtocolStack` whose session's remote end matches `loc` |
| `isLocal(NodeLocation loc)` | Returns true if `loc` matches any local session end |
| `close()` | Closes all sessions and pending starters |

All methods are `synchronized`.

---

## Marshaling

### Marshaler / UnMarshaler Interfaces

Both extend standard Java I/O interfaces and add IMC-specific methods:

```java
// Marshaler extends DataOutput, Closeable, Flushable, MigratingCodeHandler
void writeStringLine(String s)              // writes s + "\r\n"
void writeReference(Serializable o)         // Java object serialization
void writeMigratingCode(MigratingCode code) // bytecode + object state
void writeMigratingPacket(MigratingPacket p)

// UnMarshaler extends DataInput, Closeable, MigratingCodeHandler
String       readStringLine()               // reads until '\n'; strips '\r'
Object       readReference()                // Java object deserialization
MigratingCode readMigratingCode()
MigratingPacket readMigratingPacket()
void         clear()                        // discard available bytes
```

`MigratingCodeHandler` provides `get/setMigratingCodeFactory()` so the factory
can be swapped independently of the stream.

---

### IMCMarshaler / IMCUnMarshaler

| Class | Extends | Implementation notes |
|-------|---------|---------------------|
| `IMCMarshaler` | `DataOutputStream` | `writeReference` uses an intermediate `ByteArrayOutputStream` so the byte count can be written before the payload |
| `IMCUnMarshaler` | `DataInputStream` | Auto-wraps input in `BufferedInputStream`; `readReference` uses `ObjectInputStream` |

Both created by `TcpIpProtocolLayer` wrapping socket streams.

---

## Topology

### Node — `org.mikado.imc.topology.Node`

Top-level participant in the network. Key fields:

```
Node
  nodeName          : String
  connectionServer  : ConnectionServer       // incoming
  connectionStarter : ConnectionStarter      // outgoing
  sessionManagers   : SessionManagers        // incoming + outgoing managers
  eventManager      : EventManager
  sessionStarterTable: SessionStarterTable   // default: IMCSessionStarterTable
  processContainer  : ProcessContainer<Thread>
```

Primary API:

```java
// Outgoing
void         connect(SessionId id, Protocol p)   // connect and run p in a thread
ProtocolStack connect(SessionId id)              // connect and return stack

// Incoming (single accept)
void     acceptAndStart(SessionId id, Protocol p)
Protocol accept(SessionId id, Protocol p)
ProtocolStack accept(SessionId id)
```

Both families have variants that accept a `SessionStarter` directly instead of a
`SessionId`.

---

### NodeProcess — `org.mikado.imc.topology.NodeProcess` (abstract)

A serialisable, potentially migratable unit of computation:

```
NodeProcess extends JavaMigratingCode
  transient nodeProcessProxy : NodeProcessProxy
  finalException             : IMCException
  verbosity                  : boolean
```

Execution lifecycle:

```
run():
  preExecute()     // hook; default no-op
  execute()        // abstract; subclass implements logic
  postExecute()    // hook; default no-op
  remove self from ProcessContainer
```

`NodeProcessProxy` injects node services (event manager, adding child processes,
getting stacks) without giving the process a direct reference to the node.

Processes are auto-named with a monotonic counter (`getNextId()`) unless a name
is supplied explicitly.

---

### NodeCoordinator — `org.mikado.imc.topology.NodeCoordinator` (abstract)

Privileged counterpart to `NodeProcess`. Extends `Thread` directly (rather than
going through the process container), and uses a `NodeCoordinatorProxy` for node
operations. Coordinators can `accept()` and `connect()` on behalf of the node.
KLAVA uses coordinators as the server-side loop that accepts incoming connections
(`KlavaNodeCoordinator`).

---

### ConnectionStarter and ConnectionServer

Both are helpers that wrap the raw `accept()` / `connect()` calls with the
CONNECT/DISCONNECT handshake:

- **`ConnectionStarter`**: wraps a `ProtocolComposite` around the given protocol
  with a `ConnectState` (sends `"CONNECT"`, expects `"OK"`). On success, registers
  the session with `SessionManager`.
- **`ConnectionServer`**: wraps with `ConnectionManagementState` (reads
  `"CONNECT"`, responds `"OK"` or `"FAIL"`). Single-use; closes after one accept.

`ConnectState` fields (`connection_string`, `ok_string`, etc.) are settable so
KLAVA can substitute its own handshake vocabulary.

---

### RoutingTable — `org.mikado.imc.topology.RoutingTable`

Maps a destination `SessionId` to the `ProtocolStack` that reaches it:

```
RoutingTable
  stacks  : Hashtable<SessionId, ProtocolStack>   // destination → route stack
  proxies : Hashtable<SessionId, TreeSet<SessionId>> // proxy → set of reachable destinations
```

**Direct route**: `destination == remote end` of the stored stack.  
**Proxy route**: `destination ≠ remote end`; an intermediate node relays messages.
The table automatically maintains consistency: removing a proxy cascades removal
of all destinations reachable only through it.

| Method | Behaviour |
|--------|-----------|
| `addRoute(dest, route, stack)` | Registers route; returns false if inconsistent |
| `addRoute(dest, stack)` | Unconditional direct-route insertion |
| `removeRoute(sessionId)` | Removes route and related proxy entries |
| `getProtocolStack(dest)` | Retrieves stack for direct or proxied destination |
| `addProxy(proxy, dest)` | Records proxy relationship |
| `removeProxy(proxy)` | Removes proxy and cascades |

Generates `RouteEvent` on add/remove; observers (e.g., `KlavaNode`) listen to
detect node arrival and departure.

---

## Code Mobility

| Class / Interface | Role |
|-------------------|------|
| `MigratingCode` | Marker interface for migratable code |
| `JavaMigratingCode` | Base class; provides class-bytecode extraction helpers |
| `NodeProcess` | Extends `JavaMigratingCode`; the primary mobile unit |
| `MigratingCodeFactory` | Abstract factory: creates `MigratingCodeMarshaler` / `MigratingCodeUnMarshaler` |
| `JavaByteCodeMigratingCodeFactory` | Concrete factory using Java serialization + `ClassLoader` bytecode extraction |
| `JavaByteCodeMigratingCodeFactoryVerbose` | Debug variant |

`MigratingCodeFactory` is injected into `IMCMarshaler` / `IMCUnMarshaler` so that
`writeMigratingCode()` / `readMigratingCode()` know how to package bytecode
alongside object state. KLAVA sets a `JavaByteCodeMigratingCodeFactory` instance
on each node; "klava." and "momi." package classes are excluded from bytecode
bundling to avoid re-serialising framework classes.

---

## Events

IMC uses a simple observer bus:

- **`EventGenerator`** (interface): `void generateEvent(String eventClass, Event event)`; `get/setEventManager()`
- **`EventGeneratorAdapter`** (abstract): default implementation of `EventGenerator`; `Protocol` and `RoutingTable` extend this
- **`EventManager`**: central dispatcher; listeners register for specific event classes

Event classes used by IMC:

| Class constant | Source | Payload |
|----------------|--------|---------|
| `SessionManager.EventClass = "CONNECTION"` | `SessionManager` | `SessionEvent` (CONNECT / DISCONNECT) |
| `"ROUTE_ADD"` / `"ROUTE_REMOVE"` | `RoutingTable` | `RouteEvent` |

KLAVA adds `TupleEvent`, `LocalityEvent`, and `LoginSubscribeEvent` on top of
these.
