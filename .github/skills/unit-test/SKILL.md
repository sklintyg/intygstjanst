---
name: unit-test
description: Write focused, fast JUnit 5 + Mockito unit tests for Java classes. Use this when adding tests for existing classes, covering new branches, or verifying behaviour changes. Follows TDD red-green flow when modifying behaviour. Never spins up a Spring context.
license: MIT
---

# Unit test skill

Use this skill when the task is to **write, extend, or update unit tests** for Java classes in this Spring Boot / Gradle codebase.

## Guiding principles

1. **One assertion per test.** Each test verifies exactly one behaviour or branch.
2. **No Spring context.** Use `@ExtendWith(MockitoExtension.class)` — never `@SpringBootTest`, `@DataJpaTest`, or similar.
3. **Fast and isolated.** Every test must run in milliseconds. No I/O, no databases, no network.
4. **Red → Green when changing behaviour.** If the user asks to change production logic:
   - First update or add the test so it **fails** against the current implementation.
   - Confirm the failure by running the test.
   - Then implement the production change.
   - Run all tests for the class to catch regressions.
5. **Surgical changes.** Only touch what is needed — do not refactor unrelated tests or production code.

## Test conventions for this project

### Framework and dependencies

| Dependency | Usage |
|---|---|
| JUnit Jupiter (JUnit 5) | `@Test`, assertions |
| Mockito + `mockito-junit-jupiter` | `@Mock`, `@InjectMocks`, `@Spy`, `@Captor` |
| JUnit Jupiter Assertions | `assertEquals`, `assertTrue`, `assertFalse`, `assertThrows`, `assertNotNull` |

**Do not use:** AssertJ, Hamcrest, JUnit 4, `@RunWith`, TestNG, or PowerMock.

### Class structure

```java
@ExtendWith(MockitoExtension.class)
class FooServiceTest {

  @InjectMocks
  private FooService fooService;

  @Mock
  private BarRepository barRepository;

  @Test
  void shouldReturnBarWhenIdExists() {
    when(barRepository.findById("123")).thenReturn(Optional.of(new Bar("123")));

    final var result = fooService.getBar("123");

    assertEquals("123", result.id());
  }
}
```

### Naming

| Element | Convention | Example |
|---|---|---|
| Test class | `<ClassUnderTest>Test` | `ApiBasePathEnforcingInterceptorTest` |
| Test method | `should<Expected>When<Condition>` | `shouldBlockAndSendNotFoundWhenServletPathDoesNotMatch` |
| Constants | `UPPER_SNAKE_CASE` | `private static final String CERTIFICATE_ID = "cert-1";` |
| Variables | `final var` preferred | `final var result = service.process(input);` |

### Imports

Use static imports for assertions and Mockito methods. Do not use wildcard imports.

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
```

### Test body layout

Follow the **Arrange → Act → Assert** pattern with blank lines separating each section:

```java
@Test
void shouldReturnEmptyWhenNotFound() {
  when(repository.findById("x")).thenReturn(Optional.empty());

  final var result = service.find("x");

  assertTrue(result.isEmpty());
}
```

### Mock setup patterns

- Prefer `when(...).thenReturn(...)` for return values.
- Use `doReturn(...).when(...)` when working with spies or void methods.
- Use `lenient()` in `@BeforeEach` only when multiple tests share setup and not all of them exercise every stub.
- Use `@Captor` with `ArgumentCaptor` when verifying complex arguments.
- For generic return types that cause Mockito type inference issues, use `thenAnswer(inv -> ...)` instead of `thenReturn(...)`.

### Comments

Do not add comments to test code. Test method names and the arrange/act/assert structure should be self-documenting.

### License header

Every test file must start with the project license header:

```java
/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
```

### Code formatting

The project uses Google Java Format via Spotless. Tests will be auto-formatted. Do not fight the formatter.

## Required workflow

### Adding tests for an untested class

1. **Read the class under test.** Understand every branch, return path, and side effect.
2. **Identify test cases.** Map each branch/condition to one test method.
3. **Create the test file** at the mirror path under `src/test/java`. Create the directory if needed.
4. **Write all tests.**
5. **Run the tests** with `./gradlew :app:test --tests "<fully.qualified.TestClass>" --no-daemon` and confirm they pass.

### Adding tests for a behaviour change (TDD flow)

1. **Write or update the test first** so it expresses the new expected behaviour.
2. **Run the test and confirm it fails** (red).
3. **Implement the production change.**
4. **Run all tests for the class** and confirm they pass (green).
5. If the change affects other classes, run the full test suite for the module.

### Verifying log output

When a test needs to verify that a log message was emitted:

```java
@Mock
private Appender<ILoggingEvent> mockAppender;

@Captor
private ArgumentCaptor<LoggingEvent> logCaptor;

@BeforeEach
void attachLogAppender() {
  final Logger logger = (Logger) LoggerFactory.getLogger(ClassUnderTest.class);
  logger.addAppender(mockAppender);
}

@AfterEach
void detachLogAppender() {
  final Logger logger = (Logger) LoggerFactory.getLogger(ClassUnderTest.class);
  logger.detachAppender(mockAppender);
}

@Test
void shouldLogWarnWhenSomethingBadHappens() {
  // arrange & act ...

  verify(mockAppender).doAppend(logCaptor.capture());
  assertEquals(Level.WARN, logCaptor.getValue().getLevel());
}
```

Required imports for log verification:

```java
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.slf4j.LoggerFactory;
```

### Testing annotation-driven behaviour

When the class under test reads annotations from bean types (e.g. controller classes), use inner static stub classes as fixtures:

```java
@MyAnnotation("/path-a")
static class AnnotatedController {}

@MyAnnotation({"/path-a", "/path-b"})
static class MultiPathController {}

static class UnannotatedController {}
```

Then configure the mock to return the stub class:

```java
when(handlerMethod.getBeanType()).thenAnswer(inv -> AnnotatedController.class);
```

## Running tests

```bash
# Single test class
./gradlew :app:test --tests "se.inera.intyg.intygstjanst.some.package.MyClassTest" --no-daemon

# All tests in the app module
./gradlew :app:test --no-daemon
```

## Anti-patterns to avoid

| Anti-pattern | Do instead |
|---|---|
| Spinning up Spring context | Use `@ExtendWith(MockitoExtension.class)` |
| Testing multiple behaviours in one test | Split into separate test methods |
| Using `@Test` from JUnit 4 (`org.junit.Test`) | Use `org.junit.jupiter.api.Test` |
| Writing comments to explain tests | Use descriptive method names |
| Adding `public` modifier to test class/methods | Package-private is sufficient |
| Hardcoding values inline repeatedly | Extract to `private static final` constants |
| Ignoring existing test utilities in `src/test/.../support/` | Reuse `CertificateFactory` etc. when applicable |
| Catching exceptions to assert them | Use `assertThrows(...)` |
