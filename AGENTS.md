# Project Guidelines

## Project Scope

- This repository builds the Java 17 `util-deps` library for utilities and integrations that use additional third-party libraries and frameworks.
- Reuse the base `io.github.sswroom:util` library for shared utility functionality; keep dependency-heavy integrations here rather than adding them to SJavaClass.
- Keep the base-library dependency version in `pom.xml` intentional and compatible with the APIs used here. Add or upgrade other dependencies only when required by the implementation.
- Treat public classes and methods as a reusable library API. Avoid unnecessary breaking changes and document public API additions with Javadoc.

## Development

- Keep Java sources under `src/main/java` and tests under `src/test/java`.
- Use the existing Maven configuration and Java 17 source and target level.
- Run `mvn test` from the repository root after code changes.
- Do not edit generated output under `target/`.
