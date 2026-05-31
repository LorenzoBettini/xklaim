# KLAVA — Developer Notes

KLAVA implements the **KLAIM** (*Kernel Language for Agent Interaction and Mobility*)
process algebra on top of the IMC networking library. It provides:

- **Tuple spaces** as the shared, pattern-matched data store at each node.
- **KLAIM operations** (`out`, `in`, `read`, `eval`, `newloc`) with transparent
  local/remote dispatch.
- **Logical and physical localities** so processes refer to nodes by name rather
  than by network address.
- **Process mobility** (code + state migration) via Java serialization.

See [IMC_NOTES.md](IMC_NOTES.md) for the networking substrate that KLAVA builds on.

---

## Package Structure

| Package | Contents |
|---------|----------|
| `klava` | Core types: `Tuple`, `TupleItem`, `TupleSpace`, `TupleSpaceVector`, localities, `Environment` |
| `klava.topology` | `KlavaNode`, `KlavaProcess`, `KlavaNodeCoordinator`, `ClientNode`, `Net`, `LogicalNet`, `ClosureMaker`, `ExecutionEngine`, proxy classes |
| `klava.proto` | Protocol states and managers: `TuplePacket`, `TupleOpState`, `TupleOpManager`, `ResponseState`, `LoginSubscribeState`, `AcceptRegisterState`, `RouteFinderState`, `LocalityResolverState` |
| `klava.events` | `TupleEvent`, `LocalityEvent`, `LoginSubscribeEvent` |

---

## Tuple Types and Matching

### TupleItem — `klava.TupleItem` (interface)

Marks formal (unbound) variables inside tuple templates:

```java
boolean isFormal()             // true iff unbound / null value
void    setValue(Object o)     // bind a value
void    setValue(String o)     // bind from string representation
Object  duplicate()            // deep copy
```

Concrete implementations: `KString`, `KInteger`, `KBoolean`, `KVector`
(the KLAIM primitive types), `LogicalLocality`, `PhysicalLocality`,
`TupleSpaceVector`.

A `TupleItem` is **formal** when its internal value is `null` (localities) or an
equivalent sentinel (numeric types).

---

### Tuple — `klava.Tuple`

```
Tuple
  id               : String          // GUID: timestamp + monotonic counter
  items            : Vector<Object>  // elements; any Object allowed
  already_retrieved: HashSet<String> // ids of tuples seen in this wait cycle
  matched          : Tuple           // result of last successful match
  original_template: Tuple           // saved template before binding
  handleRetrieved  : boolean         // whether to skip already-retrieved tuples
```

Items are added via `add(Object)` or passed in the constructor. Supported element
kinds:

| Kind | Matched by | During `match()` |
|------|-----------|-----------------|
| `Class` object | Any instance of that class | Checks `instanceof` |
| Formal `TupleItem` | Any value of compatible type | Binds (`setValue`) the matched value into the template element |
| Concrete `TupleItem` | Equal value | `TupleItem.equals()` |
| Nested `Tuple` | Matching sub-tuple | Recursion into `Tuple.match()` |
| Plain `Object` | Equal object | `Object.equals()` |

**Matching algorithm** (`match(Tuple template)`):

1. `preMatch(template)`:
   - Length check.
   - If `handleRetrieved` and the candidate tuple's `id` is in `already_retrieved`,
     return false (avoids re-matching the same tuple in one blocking wait cycle).
2. Save `original_template` (copy of template before binding).
3. For each position: apply the rules above; on failure, restore template and
   return false.
4. On success, set `matched = template copy` and return true.

`resetRetrieved()` clears `already_retrieved` between wait cycles.
`updateAlreadyRetrieved(t)` records `t.id` when a tuple is skipped.

---

### Primitive TupleItem Types

| Class | Internal field | Formal condition |
|-------|---------------|-----------------|
| `KString` | `String value` | `value == null` |
| `KInteger` | `int value` | tracked via `isFormal` flag |
| `KBoolean` | `boolean value` | tracked via `isFormal` flag |
| `KVector` | `Vector<Object> value` | `value == null` |

All are in the `klava` package and implement `Serializable`.

---

## Tuple Space

### TupleSpace — `klava.TupleSpace` (interface)

Extends `EventGenerator`. The full set of operations:

```java
void    out(Tuple t)
boolean in(Tuple t)       throws InterruptedException  // blocking, destructive
boolean read(Tuple t)     throws InterruptedException  // blocking, non-destructive
boolean in_t(Tuple t, long timeout)                    // timeout, destructive
boolean read_t(Tuple t, long timeout)                  // timeout, non-destructive
boolean in_nb(Tuple t)                                 // non-blocking, destructive
boolean read_nb(Tuple t)                               // non-blocking, non-destructive
int     length()
void    removeTuple(int i)
void    removeAllTuples()
Enumeration<Tuple> getTupleEnumeration()
```

---

### TupleSpaceVector — `klava.TupleSpaceVector`

Extends `EventGeneratorAdapter`, implements `TupleSpace` and `TupleItem`
(so a tuple space can itself be embedded in a tuple).

```
TupleSpaceVector
  tuples : Vector<Tuple>   // synchronized; monitor used for blocking ops
```

**`out(t)`**: `tuples.add(t)`, then `tuples.notifyAll()`, then fires
`TupleEvent.ADDED`.

**Blocking `in(t)` / `read(t)`**:
```
synchronized (tuples):
  while no match:
    tuples.wait()       // releases lock until out() notifies
  // found: for in(), remove tuple; for read(), leave it
  return true
```

**Timeout variants** (`in_t`, `read_t`): track elapsed time; throw
`KlavaTimeOutException` if time expires before a match.

**Non-blocking variants** (`in_nb`, `read_nb`): single scan; return `false`
immediately if no match.

`out()` is the only writer and always calls `notifyAll()`, so all waiting
threads re-check their templates.

---

## Localities

### Locality — `klava.Locality` (abstract)

```
Locality implements Serializable, TupleItem, Comparable<Locality>
  locality : Object    // null → formal; String or SessionId → concrete
```

Subclasses:

---

### LogicalLocality — `klava.LogicalLocality`

Stores the `locality` field as a plain `String` (e.g., `"server"`, `"worker"`).
Formal if `locality == null`. `toString()` returns the string, or
`"!LogicalLocality"` if formal.

Logical localities must be resolved to `PhysicalLocality` before any message is
sent to a remote node. Resolution is handled by `Environment` and
`LogicalLocalityResolver`.

---

### PhysicalLocality — `klava.PhysicalLocality`

Stores the `locality` field as an IMC `SessionId`. Constructed from:
- `"tcp://host:port"` string (protocol prefix optional; defaults to `"tcp"`)
- Explicit `(host, port)` pair
- An existing `SessionId`

Delegates parsing to `SessionId.parseSessionId()`. `getHost()` and `getPort()`
delegate to the underlying `IpSessionId`.

---

### Environment — `klava.Environment`

Bidirectional mapping between logical and physical localities:

```
Environment extends EventGeneratorAdapter implements Serializable
  environment        : Hashtable<LogicalLocality, PhysicalLocality>
  reverseEnvironment : Hashtable<PhysicalLocality, HashSet<LogicalLocality>>
```

All public methods are `synchronized`. Key API:

```java
PhysicalLocality        toPhysical(LogicalLocality l)
HashSet<LogicalLocality> toLogical(PhysicalLocality p)
void   add(LogicalLocality ll, PhysicalLocality pl)
boolean try_add(LogicalLocality ll, PhysicalLocality pl)   // no-op if already present
PhysicalLocality remove(LogicalLocality l)
HashSet<LogicalLocality> removePhysical(PhysicalLocality p)
```

`try_add` is used during subscribe registration to avoid overwriting existing
entries. `addFromEnvironment(env)` merges another environment.

Fires `EnvironmentEvent` on `add` / `remove`, which `KlavaNode` listeners use to
track subscription changes.

Environment files (line format: `logicalName=physicalAddress`) can be loaded via
the `Environment(String filename)` constructor.

---

### The `self` Special Locality

`KlavaNode.self` is a `static final LogicalLocality("self")`. Every process has a
`self` field (default `KlavaNode.self`) representing "this node".

`ClosureMaker.makeClosure()` can resolve `self` to the **current** node's concrete
`PhysicalLocality` (not the destination). Closure is automatic for tuple operations
and opt-out:

- For tuple operations (`out`, `in`, `read`, and their variants), closure runs
  automatically when `doAutomaticClosure = true` on the sending process or node
  coordinator (default is `true`). The call is `makeAutomaticClosure(tuple)`,
  which delegates to `makeClosure(tuple, translateSelf())` (resolves `self` to
  the current node's physical locality). Formal tuple items are left untouched.
- **`eval` never triggers any closure**, regardless of `doAutomaticClosure`. The
  process is sent as-is. When it arrives at the destination and runs, the remote
  node's `KlavaNodeProcessProxy` is injected, so `self` lazily resolves to the
  remote node's physical locality.

---

## Node Architecture

### KlavaNode — `klava.topology.KlavaNode`

Extends IMC `Node`. The central class tying together all KLAIM subsystems.

```
KlavaNode
  tupleSpace               : TupleSpace                // local tuple store
  routingTable             : RoutingTable               // dest SessionId → ProtocolStack
  environment              : Environment                // logical ↔ physical mappings
  logicalLocalityResolver  : LogicalLocalityResolver    // network-based name resolution
  closureMaker             : ClosureMaker               // pre-send tuple closure
  migratingCodeFactory     : MigratingCodeFactory       // process serialization
  executionEngine          : ExecutionEngine            // runs incoming processes
  mainPhysicalLocality     : PhysicalLocality           // this node's address
  waitingForOkResponse     : WaitingForResponse<Response<String>>
  waitingForTuple          : WaitingForResponse<TupleResponse>
  waitingForLocality       : WaitingForResponse<Response<PhysicalLocality>>
  mainCoordinator          : KlavaNodeCoordinator       // accept loop coordinator
```

**Initialisation** (`initNode()`):
1. Create `ExecutionEngine`.
2. Create `TupleOpManagerFactory` pointing at the tuple space and the three
   `WaitingForResponse` maps.
3. Create `LogicalLocalityResolver` (backed by the `Environment`).
4. Create `MessageProtocolFactory` (builds protocol stacks for outgoing messages).
5. Create `ClosureMaker`.
6. Register event listeners for route events and login/logout events.

**Incoming connection** (`accept(PhysicalLocality local, PhysicalLocality remote)`):
Spawns an `AcceptNodeCoordinator` with `setLoop(true)` so it continuously accepts
new connections on the given address. Each accepted connection gets a
`MessageProtocol` stack including `TupleOpState` (read) and `ResponseState`.

**Outgoing connection** (`login(Locality remote)` / `subscribe(Locality remote, LogicalLocality logical)`):
Uses `LoginSubscribeState` to send the CONNECT handshake; on success, adds the
route to `routingTable` and (for subscribe) registers the logical locality in the
environment.

**Local vs remote dispatch** (used by `KlavaNodeProcessProxy`):
- Resolve `LogicalLocality` to `PhysicalLocality` via environment / resolver.
- If `physicalLocality.getSessionId().equals(mainPhysicalLocality.getSessionId())`:
  operate directly on `tupleSpace`.
- Otherwise: build a `TuplePacket`, send it via `routingTable.getProtocolStack(dest)`,
  and block in the appropriate `WaitingForResponse` map keyed by `processName`.

---

### ClientNode — `klava.topology.ClientNode`

Extends `KlavaNode`. The constructor calls either `login(server)` or
`subscribe(server, logicalLocality)` and throws `KlavaException` on failure.
Provides a `main()` entry point for command-line use.

---

### Net and LogicalNet

`Net` (extends `KlavaNode`) binds to one or more `PhysicalLocality` addresses and
starts an accept loop on each (`AcceptNodeCoordinator` with `setLoop(true)`).

`LogicalNet` (extends `Net`) overrides `startAccept()` to spawn
`RegisterNodeCoordinator` instead, which processes subscribe requests (logical
locality registration) rather than anonymous logins.

---

## KlavaProcess

### Class — `klava.topology.KlavaProcess`

Extends IMC `NodeProcess`. Subclasses override the single abstract method:

```java
public abstract void executeProcess() throws KlavaException;
```

IMC calls `execute()` → which calls `executeProcess()`.

```
KlavaProcess
  transient klavaNodeProcessProxy : KlavaNodeProcessProxy  // node service injection
  self                            : Locality               // default = KlavaNode.self
  environment                     : Environment            // process-local mappings
  closureMaker                    : ClosureMaker
  doAutomaticClosure              : boolean                 // default = true
  migrationStatus                 : int
  caller                          : KlavaProcess           // set on migration
```

---

### Migration Status

```java
static final int NOT_MIGRATED = 0   // initial
static final int MIGRATING    = 4   // migrate() called eval(this, dest) in progress
static final int MIGRATED     = 1   // left this node
static final int ARRIVED      = 3   // just deserialised on remote node
static final int CONTINUED    = 2   // migrate() to self (no actual move)
```

`execute()` sets status to `ARRIVED` before calling `executeProcess()` when the
process has been deserialised at its destination. The subclass checks
`getMigrationStatus()` to branch between first run and post-arrival logic.

---

### Interrupt Deferral

Java's NIO channels throw `ClosedByInterruptException` if the thread is interrupted
during a blocking I/O call, which permanently closes the channel. KLAVA avoids
this by deferring interrupts during critical sections:

```java
// AutoCloseable helper
try (InterruptDeferral d = deferInterrupts()) {
    // NIO operation here; interrupt() stores a flag instead of interrupting
}
// on close: if depth == 0 and interrupt was deferred, re-interrupt thread
```

`interrupt()` is overridden in `KlavaProcess` to increment `interruptDeferralDepth`
(when positive) instead of forwarding to the thread. When the last deferral scope
closes, the pending interrupt is replayed.

---

### Tuple Operations (protected API)

All operations are protected methods that delegate to `KlavaNodeProcessProxy`:

```java
void    out(Tuple t, Locality dest)
void    in(Tuple t, Locality dest)                     // blocking
boolean in_nb(Tuple t, Locality dest)
boolean in_t(Tuple t, Locality dest, long timeout)
void    read(Tuple t, Locality dest)                   // blocking
boolean read_nb(Tuple t, Locality dest)
boolean read_t(Tuple t, Locality dest, long timeout)
void    eval(KlavaProcess p, Locality dest)
```

If `doAutomaticClosure` is `true`, `makeAutomaticClosure(tuple)` is called before
each **tuple** operation (`in`, `in_nb`, `in_t`, `out`, `read`, `read_nb`, `read_t`),
but **not** before `eval`. This resolves logical localities inside the tuple using
the process-local environment, falling back to the node's `ClosureMaker` on failure.

---

## KLAIM Operations — Local vs Remote Paths

| Operation | Local path | Remote path |
|-----------|-----------|-------------|
| `out(t, dest)` | `tupleSpace.out(t)` | `TuplePacket(OUT_S)` → send → wait in `waitingForOkResponse` |
| `in(t, dest)` | `tupleSpace.in(t)` (blocks in monitor) | `TuplePacket(IN_S, blocking=true)` → send → wait in `waitingForTuple` |
| `in_nb(t, dest)` | `tupleSpace.in_nb(t)` | `TuplePacket(IN_S, blocking=false)` → send → wait for `TUPLEABSENT_S` or tuple |
| `in_t(t, dest, ms)` | `tupleSpace.in_t(t, ms)` | `TuplePacket(IN_S, timeout=ms)` |
| `read(t, dest)` | `tupleSpace.read(t)` | `TuplePacket(READ_S, blocking=true)` |
| `read_nb(t, dest)` | `tupleSpace.read_nb(t)` | `TuplePacket(READ_S, blocking=false)` |
| `read_t(t, dest, ms)` | `tupleSpace.read_t(t, ms)` | `TuplePacket(READ_S, timeout=ms)` |
| `eval(p, dest)` | `addNodeProcess(p)` (spawns locally) | `TuplePacket(EVAL_S)` → remote deserialises + spawns (no closure) |
| `newloc()` | Generate unique `SessionId`; create `ClientNode`; return `PhysicalLocality` | N/A |

For remote operations the key is `processName` (from `NodeProcess.getName()`),
which lets `ResponseState` on the remote node route the response back to the
exact waiting process.

---

## Protocol Layer for Tuple Operations

### TuplePacket — `klava.proto.TuplePacket`

The message type for all KLAIM remote operations. Extends `NodePacket`.

```
TuplePacket
  // inherited from NodePacket
  Dest        : PhysicalLocality   // destination node
  Source      : PhysicalLocality   // originating node
  processName : String             // identifies the waiting process

  // own fields
  operation : String               // one of the constants below
  tuple     : Tuple
  blocking  : boolean              // true → receiver blocks until match
  timeout   : long                 // ms; -1 = infinite
```

Operation constants:

| Constant | Value | Direction |
|----------|-------|-----------|
| `OUT_S` | `"OUT"` | client → server |
| `IN_S` | `"IN"` | client → server |
| `READ_S` | `"READ"` | client → server |
| `EVAL_S` | `"EVAL"` | client → server |
| `TUPLEBACK_S` | `"TUPLEBACK"` | server → client (put tuple back after failed IN) |
| `TUPLEABSENT_S` | `"TUPLEABSENT"` | server → client (non-blocking IN with no match) |

**Wire format** (text-based, then serialised tuple):

```
OPERATION
<operation>
FROM
<source PhysicalLocality>
TO
<destination PhysicalLocality>
PROCESS
<processName>
BLOCKING
<true|false>
TIMEOUT
<milliseconds>
[serialised Tuple via TupleState]
```

---

### TupleOpState — `klava.proto.TupleOpState`

Extends `ProtocolStateSimple`. Used as both reader and writer:

- **Write mode** (`doRead = false`, set `tupleOpManager = null`): `enter()` calls
  `writePacket(stack, tuplePacket)` which serialises `tuplePacket` to the
  channel marshaler.
- **Read mode** (`doRead = true`, supply a `TupleOpManager`): `enter()` calls
  `readPacket(param, channel)` which deserialises a `TuplePacket` from the
  channel, then calls `tupleOpManager.handle(packet, ourSessionId)`.

`closed()` shuts down the `TupleOpManager` if present.

`ourSessionId` is retrieved from `protocolStack.getSession().getLocalEnd()` and
passed to the manager so it can decide whether the packet is for this node or
must be forwarded.

---

### TupleOpManager — `klava.proto.TupleOpManager`

Routes each received `TuplePacket` to the appropriate action. Runs the action in
a background thread (extending `CollectableThread`) when it may block.

**Forwarding decision**:
```java
if (ourSessionId != null && !ourSessionId.equals(packet.Dest.getSessionId()))
    → forward (spawn ForwardTupleThread)
else
    → handle locally
```

**Per-operation logic**:

| Operation | Action |
|-----------|--------|
| `OUT_S` | `tupleSpace.out(packet.tuple)`; send OK response |
| `IN_S` | Call `findMatchingTuple()`; if blocking and no match, spawn `TupleThread` to wait; send tuple or fail response |
| `READ_S` | Same as `IN_S` but non-destructive |
| `EVAL_S` | Extract `KlavaProcess` from tuple; call `executionEngine.runProcess(p)`; send OK or error response |
| `TUPLEBACK_S` | `tupleSpace.out(packet.tuple)` (no response sent) |

**Inner thread classes**:

| Thread | Purpose |
|--------|---------|
| `TupleThread` | Waits in local tuple space for a matching tuple (blocking IN/READ) |
| `ForwardTupleThread` | Finds a route via `RouteFinderState` and forwards the packet |
| `ResponseOutThread` | Routes an OUT response back to source when no direct route yet exists |
| `ResponseEvalThread` | Routes an EVAL error response back to source |
| `ResponseTupleThread` | Routes a tuple response; if IN and routing fails, puts tuple back in tuple space |

All spawned threads are added to `waitingThreads` (`ThreadContainer`) for clean
shutdown via `interrupt()`.

---

### ResponseState — `klava.proto.ResponseState`

Extends `ProtocolSwitchState`. Handles response packets arriving at the originating
node (or any intermediate forwarding node) and wakes the blocked process.

`enter(Object param, channel)` dispatches on the `param` operation string to one
of three inner states:

| Inner state | Handles | Notifies |
|-------------|---------|---------|
| `ResponseOkErrorState` | OUT, EVAL responses | `waitingForOkResponse` map (keyed by `processName`) |
| `ResponseTupleState` | IN, READ responses (with or without tuple) | `waitingForTuple` map |
| `ResponseLocalityState` | Locality query responses | `waitingForLocality` map |

**Forwarding**: before notifying, each inner state checks whether `destination`
is this node; if not, it looks up a route in `routingTable` and re-sends the
response onwards (spawning a `ResponseXxxThread` if no route is available yet).

**Special case** (`ResponseTupleState`): if the response contains a tuple but no
process is waiting (e.g., the process timed out), the tuple is sent back with a
`TUPLEBACK_S` packet so the remote tuple space is not left empty.

**Static send helpers** allow `TupleOpManager` to write responses without
coupling directly to the response state:

```java
ResponseState.sendResponseOut(stack, dest, processName, ok)
ResponseState.sendResponseTuple(stack, op, tuple, from, dest, processName, ok)
ResponseState.sendResponseEval(stack, dest, processName, errorOrNull)
ResponseState.sendResponseLocality(stack, locality, processName, ok)
```

---

## Login / Subscribe Protocol

### LoginSubscribeState — `klava.proto.LoginSubscribeState`

Client-side. Writes:
- `"CONNECT"` + logical locality name for subscribe.
- `"CONNECT"` alone for anonymous login.
- `"DISCONNECT"` + optional logical locality for logout / unsubscribe.

Reads back `"OK"` or `"FAIL"`. On success, fires a `LoginSubscribeEvent`.

### AcceptRegisterState — `klava.proto.AcceptRegisterState`

Server-side. Reads the connection header:
- If subscribe: extracts the logical locality; calls
  `environment.try_add(logical, physicalSource)`.
- For both: adds the new route to `routingTable`; responds `"OK"`.

On disconnect: removes routes and environment entries; fires disconnect events.

---

## Closure and Code Mobility

### ClosureMaker — `klava.topology.ClosureMaker`

Resolves symbolic references inside a tuple before it is sent over the network,
replacing `self` with a concrete physical address and merging environments into
embedded processes.

`ClosureMaker` is invoked via `KlavaProcess.makeAutomaticClosure(tuple)` or
`KlavaNodeCoordinator.makeAutomaticClosure(tuple)` (only when
`doAutomaticClosure = true`) or via an explicit call to `KlavaProcess.makeProcessClosure()`.
**It is never invoked for `eval`.**

```
ClosureMaker implements Serializable
  logicalLocalityResolver : LogicalLocalityResolver (transient)
  environment             : Environment
  stopOnException         : boolean
```

**`makeClosure(Tuple tuple, PhysicalLocality forSelf)`** iterates every element:

1. `TupleItem`: call `makeClosure(TupleItem, forSelf)`:
   - If `LogicalLocality("self")`: replace with `forSelf` (throws if `forSelf == null`).
   - If other `LogicalLocality`: try `environment.toPhysical()`; if absent, try
     `logicalLocalityResolver.resolve(item)`.
   - Otherwise: return unchanged.
2. Nested `Tuple`: recurse.
3. `KlavaProcess`: call `process.makeProcessClosure(forSelf, environment)` (see below).

If `stopOnException = true` (default), the first unresolvable locality throws
`KlavaException`. Otherwise, `makeClosure` returns `false` and continues.

**`makeProcessClosure(PhysicalLocality forSelf, Environment env)`** on `KlavaProcess`:
- If `self` is still a `LogicalLocality`, replace it with a copy of `forSelf`
  (the *current* node's physical locality, **not** the destination).
- Merge `env` into the process's own environment.
- Other logical localities are **not** eagerly rewritten inside the process object
  graph.
- Instead, the copied environment becomes part of the process's own state. If the
  process is later migrated, that field is serialized together with the process
  object. `eval` does **not** send a separate environment payload and does not add
  the sender node's environment on its own.
- When the migrated process later resolves a locality through `toPhysical()` or
  `getPhysical()` (for example, a `destination` field passed to `out`, `in`,
  `read`, or `eval`), it consults its own environment first and only then falls
  back to the remote node.
- For these operation-level locality lookups, carrying the original environment is
  effectively equivalent to eager closure: the process keeps using the sender-side
  binding even after migration.
- The equivalence is not literal for every possible reference in the object graph:
  names that are never looked up remain symbolic, and names missing from the
  copied environment may still resolve through the destination node.

**Contrast with `eval`**: `KlavaProcess.eval()` sends the process without any closure.
`self` in the migrated process remains a `LogicalLocality` until the remote node
injects its own `KlavaNodeProcessProxy`, at which point locality resolution goes
through the remote node's environment.

---

### Process Migration — End-to-End Sequence

```
KlavaProcess.eval(p, remoteDest)       // NO closure is performed
  │
  ├─ [serialise] KlavaNode wraps p in new Tuple(p) and calls
  │    tupleOperation(EVAL_S, tuple, remoteDest, ...)
  │    MigratingCodeFactory marshals p:
  │    - Java serialization of object fields
  │    - JavaByteCodeMigratingCodeFactory bundles class bytecodes
  │      (excludes "klava.*" and "momi.*" packages)
  │
  ├─ [send] TuplePacket(EVAL_S, tuple containing serialised p) →
  │    routed via RoutingTable to remoteDest
  │
  └─ [remote] TupleOpManager.handle(packet, ourSessionId)
       ├─ deserialise KlavaProcess via MigratingCodeFactory
       ├─ inject remote KlavaNodeProcessProxy into p
       │    (self now resolves lazily through the remote node)
       ├─ set migrationStatus = ARRIVED
       └─ executionEngine.runProcess(p)
            └─ p.executeProcess()  // runs with ARRIVED status
```

**No closure on `eval`**: the process is sent exactly as it is, with no eager
rewriting of logical localities and no extra environment capture by `eval`
itself. The only environment that arrives remotely is whatever was already stored
inside `p` as part of its normal object state. When `p` later uses a locality in a
KLAVA operation, `toPhysical()` / `getPhysical()` consult that saved process
environment first, so previously captured logical-locality bindings behave like
eager closure. `self` is the main exception: unless it was already closed
beforehand, it remains a logical locality and resolves at the remote site through
the injected remote proxy.

**Contrast with `out(Tuple(p))` when `doAutomaticClosure = true`**: the sending
process calls `makeAutomaticClosure(tuple)` → `ClosureMaker.makeClosure(tuple,
translateSelf())` → `p.makeProcessClosure(currentNodePhysLoc, environment)`. This
pre-binds `p.self` to the *current* (sending) node's physical locality and merges
the current node's environment into `p`. Other logical localities are still not
eagerly rewritten, but the copied environment preserves the sender-side bindings
that `p` will use later during lazy resolution.

**In-process migration** (`migrate(self)`): `migrationStatus` is set to
`CONTINUED`; the process is re-added to the local execution engine rather than
sent over the network.

`NodeProcess.getName()` provides the `processName` that ties responses back to
the waiting originating process. For `eval`, no response need be awaited by the
caller; `KlavaNode` sends an OK/FAIL back that `ResponseOkErrorState` logs or
propagates if someone is waiting.

---

## IMC Integration Summary

| IMC component | Role in KLAVA |
|---------------|--------------|
| `Node` | Base of `KlavaNode`; provides `connect()`, `accept()`, `SessionManagers`, `ProcessContainer` |
| `NodeProcess` | Base of `KlavaProcess`; provides `run()` lifecycle, `NodeProcessProxy` injection, serialisability |
| `NodeCoordinator` | Base of `KlavaNodeCoordinator`; used as the accept-loop server thread |
| `Protocol` / `ProtocolState` | `TupleOpState`, `ResponseState`, `LoginSubscribeState`, `AcceptRegisterState`, `RouteFinderState` all extend `ProtocolStateSimple` |
| `ProtocolStack` / `ProtocolLayer` | `MessageProtocolFactory` builds stacks for each connection (TupleOp layer + Response layer + transport) |
| `SessionId` | Wrapped by `PhysicalLocality`; used as keys in `RoutingTable` and `WaitingForResponse` |
| `Session` / `SessionManager` | Connection tracking inside `KlavaNode`; session events drive route table updates |
| `RoutingTable` | Message routing in `KlavaNode`; `RouteEvent` listeners add environment entries on node arrival |
| `Marshaler` / `UnMarshaler` | Used directly in `TupleOpState`, `ResponseState`, `LoginSubscribeState` for reading/writing packets |
| `MigratingCodeFactory` | Injected into node marshalers; used by `EVAL_S` to serialise/deserialise `KlavaProcess` |
| `EventManager` | Propagates `TupleEvent`, `LocalityEvent`, `LoginSubscribeEvent` through the node |
