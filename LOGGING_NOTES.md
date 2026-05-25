# Logging Notes

These notes capture the logging design discussion for IMC, Klava, and XKlaim runtime.

## Motivation

IMC and Klava currently use `System.out.println`, `System.err.println`, and direct
`printStackTrace()` calls for diagnostics. That makes debugging and monitoring harder
because there are no log levels, no structured fields, and no easy filtering.

Logging is especially sensitive in high-frequency runtime paths such as tuple-space
operations, so disabled log statements must avoid unnecessary message construction.

## Direction

Use SLF4J 2.x as the logging API in library bundles such as `imc` and `klava`.
Keep concrete logging backends out of those libraries. Applications, examples, tests,
and Eclipse products should provide the backend and the logging configuration.

This keeps the libraries reusable and avoids forcing a client's project to use a
particular backend.

## Efficient Logging

Use ordinary SLF4J parameterized logging for cheap values:

```java
LOGGER.debug("received sequence number: {}", sequence);
```

Use the SLF4J 2 fluent API with suppliers for expensive messages or values:

```java
LOGGER.atTrace()
	.setMessage(() -> "expensive tuple state: " + tuple.toLongDebugString())
	.log();
```

Use structured key-value pairs where they improve diagnostics:

```java
LOGGER.atWarn()
	.addKeyValue("result", result)
	.addKeyValue("destination", destination)
	.log("cannot route response");
```

Avoid explicit `if (LOGGER.isDebugEnabled())` guards for normal code paths. Prefer the
fluent API and suppliers when lazy construction is needed.

## Backend And Configuration

Library bundles:

- Depend on `slf4j-api`.
- Do not ship Logback, Log4j, or a global logging configuration.

Command-line applications and examples:

- Add a runtime backend such as Logback, Log4j2, or `slf4j-simple`.
- Provide application-level configuration such as `logback.xml`,
  `log4j2.xml`, or `simplelogger.properties`.
- Clients can override by putting their own configuration earlier on the classpath or
  by setting backend-specific system properties such as `logback.configurationFile`.

Eclipse/OSGi:

- Resolve `slf4j-api` from the target platform.
- Use an OSGi-compatible SLF4J provider. The Eclipse platform provides
  `org.eclipse.equinox.slf4j`, which routes SLF4J records into the Eclipse/Equinox log.
- Because SLF4J 2 providers are discovered via Java service loading, OSGi products need
  a service-loader mediator/extender such as SPI Fly when the provider requires it.

## Client Projects

`xklaim.runtime` should not force a concrete backend on client projects. Clients that
use XKlaim/Klava should transitively receive the SLF4J API through the runtime stack,
but they should remain free to choose their own backend and configuration.

For a friendlier out-of-the-box experience, default backend/configuration belongs in:

- generated Maven example/client projects;
- the XKlaim Eclipse product;
- tests.

It should not live inside the core runtime libraries.

## XKlaim Wizard Projects

Wizard-created projects are plain Java launches, so they need an SLF4J provider on
their project classpath. `xklaim.runtime` reexports only `slf4j-api`; the wizard adds
`slf4j.simple` as a required bundle to provide a small default console backend.

The XKlaim feature includes `slf4j.simple`, and the target platform resolves that
bundle from the Eclipse release train, so it is available in the installed IDE and in
development. Existing wizard-created projects can opt in by adding `slf4j.simple` to
their `Require-Bundle` list.

Because the development target may contain both `org.eclipse.equinox.slf4j` and
`slf4j.simple`, the Xtext/MWE2 generator is pinned to the Equinox provider at build
time to avoid multiple-provider warnings during Maven builds.

## Initial Implementation Plan

1. Add SLF4J API dependencies/imports to `imc` and `klava`.
2. Add explicit SLF4J/Eclipse logging units to the target platform where needed.
3. Convert console output and stack traces incrementally, starting with active runtime
   code and leaving generated/old code alone.
4. Use parameterized logging for cheap messages and supplier-based fluent logging for
   expensive message construction.
5. Add application/test-level backend configuration separately once the library API
   migration is stable.
