# Step 1 — Migrate All Tests to JUnit 5: Detailed Plan & Checklist

> **Goal:** Remove all JUnit 4 (`org.junit.*`, `junit.framework.*`) usage from the codebase and eliminate
> `junit:junit` + `junit-vintage-engine` dependencies. Zero runtime impact — only test code changes.
>
> **Verification after each batch:** `./gradlew test` — all tests pass. No production code is changed.

---

## Current State Summary

| Metric                                                | Count                       |
|-------------------------------------------------------|-----------------------------|
| Total test files                                      | ~123                        |
| Files still on JUnit 4 (`import org.junit.Test`)      | **60**                      |
| Files already on JUnit 5 (`import org.junit.jupiter`) | **63**                      |
| Files using `@RunWith(MockitoJUnitRunner.class)`      | **46**                      |
| Files using `@RunWith(SpringJUnit4ClassRunner.class)` | **1** (TestSupport.java)    |
| Files using `@Before` (JUnit 4)                       | **19**                      |
| Files using `@After` (JUnit 4)                        | **1**                       |
| Files using `import static org.junit.Assert.*`        | **~55**                     |
| Files using `import junit.framework.*`                | **1**                       |
| Files using `import org.junit.Assert` (non-static)    | **1**                       |
| Logging module                                        | ✅ Already JUnit 5 (2 files) |

---

## Migration Patterns (Reference)

These are the 5 distinct JUnit 4 patterns found in the codebase. Each batch below is categorized by pattern
to make the migration mechanical and predictable.

### Pattern A — Plain JUnit 4 test (no runner, no base class)

**7 files.** Simplest migration. Only imports need changing.

| JUnit 4                             | JUnit 5                                            |
|-------------------------------------|----------------------------------------------------|
| `import org.junit.Test`             | `import org.junit.jupiter.api.Test`                |
| `import org.junit.Before`           | `import org.junit.jupiter.api.BeforeEach`          |
| `import static org.junit.Assert.*`  | `import static org.junit.jupiter.api.Assertions.*` |
| `import junit.framework.TestCase.*` | `import static org.junit.jupiter.api.Assertions.*` |
| `public class FooTest`              | `class FooTest` (remove `public`, convention)      |
| `public void testFoo()`             | `void testFoo()` (remove `public`)                 |
| `@Before public void setup()`       | `@BeforeEach void setup()`                         |

**Assertion API changes:**

- `assertEquals(message, expected, actual)` → `assertEquals(expected, actual, message)` *(message moves to last param)*
- `assertTrue(message, condition)` → `assertTrue(condition, message)`
- `assertNull(obj)` → same
- `fail(message)` → same

### Pattern B — `@RunWith(MockitoJUnitRunner.class)` + Mockito

**46 files.** Most common pattern. Replace runner with extension.

| JUnit 4                                       | JUnit 5                                             |
|-----------------------------------------------|-----------------------------------------------------|
| `@RunWith(MockitoJUnitRunner.class)`          | `@ExtendWith(MockitoExtension.class)`               |
| `import org.junit.runner.RunWith`             | `import org.junit.jupiter.api.extension.ExtendWith` |
| `import org.mockito.junit.MockitoJUnitRunner` | `import org.mockito.junit.jupiter.MockitoExtension` |

Plus all the import changes from Pattern A.

### Pattern C — `@RunWith(MockitoJUnitRunner.class)` + `@After`

**1 file** (MonitoringLogServiceImplTest). Same as Pattern B but also:

| JUnit 4                  | JUnit 5                                  |
|--------------------------|------------------------------------------|
| `import org.junit.After` | `import org.junit.jupiter.api.AfterEach` |
| `@After`                 | `@AfterEach`                             |

### Pattern D — `@RunWith(SpringJUnit4ClassRunner.class)` (Spring integration tests)

**1 base class + 5 subclasses.** TestSupport is the base class; subclasses inherit the runner.

| JUnit 4                                                                  | JUnit 5                                                                 |
|--------------------------------------------------------------------------|-------------------------------------------------------------------------|
| `@RunWith(SpringJUnit4ClassRunner.class)`                                | `@ExtendWith(SpringExtension.class)`                                    |
| `import org.springframework.test.context.junit4.SpringJUnit4ClassRunner` | `import org.springframework.test.context.junit.jupiter.SpringExtension` |

### Pattern E — `junit.framework.TestCase` imports

**1 file** (CitizenControllerTest). Legacy JUnit 3 assertion style.

| JUnit 4                                                | JUnit 5                                                        |
|--------------------------------------------------------|----------------------------------------------------------------|
| `import static junit.framework.TestCase.assertEquals`  | `import static org.junit.jupiter.api.Assertions.assertEquals`  |
| `import static junit.framework.TestCase.assertNotNull` | `import static org.junit.jupiter.api.Assertions.assertNotNull` |

---

## Batch Plan & Checklist

Work is organized into 8 batches. Each batch is a self-contained commit that leaves all tests green.
Run `./gradlew test` after every batch.

---

### Batch 0: Logging module

> ✅ **Already done** — both `MdcHelperTest` and `HashUtilityTest` are on JUnit 5. No action needed.

- [x] `logging/` — MdcHelperTest.java — already JUnit 5
- [x] `logging/` — HashUtilityTest.java — already JUnit 5

---

### Batch 1: Plain JUnit 4 tests — no runner, no base class *(Pattern A — 7 files)*

> Simplest migration. Good warm-up to validate the approach.

**Persistence module (3 files):**

- [x] `persistence/.../dao/CertificateStateHistoryEntryTest.java` — Pattern A
- [x] `persistence/.../dao/CertificateTest.java` — Pattern A
- [x] `persistence/.../dao/util/DaoUtilTest.java` — Pattern A

**Web module (4 files):**

- [x] `web/.../integration/converter/ArendeConverterTest.java` — Pattern A + `@Before`
- [x] `web/.../integration/rehabstod/converter/SjukfallCertificateRegisterIntygsDataConverterTest.java` — Pattern A
- [x] `web/.../service/converter/CertificateToSjukfallCertificateConverterTest.java` — Pattern A
- [x] `web/.../service/converter/CertificateToSickLeaveCertificateConverterTest.java` — Pattern A

**Verify:** `./gradlew test`

---

### Batch 2: Plain JUnit 4 tests with `@Before` but no runner *(Pattern A — 3 files)*

> Tests that instantiate objects manually in `@Before`, no Mockito runner.

- [x] `web/.../integration/validator/LakarutlatandeEnkelTypeValidatorTest.java` — Pattern A + `@Before`
- [x] `web/.../service/converter/CertificateToDiagnosedCertificateConverterTest.java` — Pattern A
- [x] `web/.../integration/validator/SendCertificateRequestValidatorTest.java` — Pattern A / B (check runner)

**Verify:** `./gradlew test`

---

### Batch 3: Mockito runner — validator & stub tests *(Pattern B — 8 files)*

> Straightforward `@RunWith(MockitoJUnitRunner.class)` → `@ExtendWith(MockitoExtension.class)`.

- [x] `web/.../integration/validator/SendMessageToRecipientValidatorTest.java` — Pattern B
- [x] `web/.../integration/validator/RevokeRequestValidatorTest.java` — Pattern B
- [x] `web/.../integration/stub/SendMedicalCertificateQuestionResponderStubTest.java` — Pattern B
- [x] `web/.../integration/stub/RevokeMedicalCertificateResponderStubTest.java` — Pattern B
- [x] `web/.../integration/stub/FkStubResourceTest.java` — Pattern B
- [x] `web/.../integration/stub/SendMessageToCareResponderStubTest.java` — Pattern B
- [x] `web/.../integration/vardensintyg/RegisterApprovedReceiversResponderImplTest.java` — Pattern B
- [x] `web/.../integration/vardensintyg/ListPossibleReceiversResponderImplTest.java` — Pattern B

**Verify:** `./gradlew test`

---

### Batch 4: Mockito runner — integration responder tests part 1 *(Pattern B — 10 files)*

> The `vardensintyg`, `v2`, `v3`, `v4`, and main integration responder tests.

- [x] `web/.../integration/vardensintyg/ListApprovedReceiversResponderImplTest.java` — Pattern B
- [x] `web/.../integration/v2/SetCertificateStatusResponderImplTest.java` — Pattern B + `@Before`
- [x] `web/.../integration/v2/GetCertificateResponderImplTest.java` — Pattern B
- [x] `web/.../integration/v3/ListCertificatesForCitizenResponderImplTest.java` — Pattern B + `@Before`
- [x] `web/.../integration/v3/ListCertificatesForCareResponderImplTest.java` — Pattern B + `@Before`
- [x] `web/.../integration/v4/ListCertificatesForCitizenResponderImplTest.java` — Pattern B + `@Before`
- [x] `web/.../integration/ListSickLeavesForCareResponderImplTest.java` — Pattern B + `@Before`
- [x] `web/.../integration/RevokeCertificateResponderImplTest.java` — Pattern B + `@Before`
- [x] `web/.../integration/RevokeMedicalCertificateResponderImplTest.java` — Pattern B + `@Before`
- [x] `web/.../integration/SendMessageToRecipientResponderImplTest.java` — Pattern B + `@Before`

**Verify:** `./gradlew test`

---

### Batch 5: Mockito runner — integration responder tests part 2 *(Pattern B/E — 14 files)*

> Remaining integration layer tests, including the `junit.framework` edge case.

- [x] `web/.../integration/SendMedicalCertificateResponderImplTest.java` — Pattern B + `@Before` + `@MockitoSettings(LENIENT)`
- [x] `web/.../integration/SendMessageToCareResponderImplTest.java` — Pattern B
- [x] `web/.../integration/GetRecipientsForCertificateResponderImplTest.java` — Pattern B
- [x] `web/.../integration/GetCertificateTypeInfoResponderImplTest.java` — Pattern B + `assertThrows`
- [x] `web/.../integration/ListCertificatesResponderImplTest.java` — Pattern B
- [x] `web/.../integration/ListKnownRecipientsResponderImplTest.java` — Pattern B
- [x] `web/.../integration/SetCertificateStatusResponderImplTest.java` — Pattern B + `@Before` + `@MockitoSettings(LENIENT)`
- [x] `web/.../integration/RegisterCertificateResponderImplTest.java` — Pattern B + `@Before` + `assertThrows` (3 try/catch/fail)
- [x] `web/.../integration/CitizenControllerTest.java` — Pattern B + **Pattern E** (`junit.framework` imports)
- [x] `web/.../integration/test/CertificateResourceTest.java` — Pattern B + `@Before` + `import org.junit.Assert` (non-static)
- [x] `web/.../integration/testcertificate/TestCertificateControllerTest.java` — Pattern B
- [x] `web/.../integration/rehabstod/ListSickLeavesForPersonResponderImplTest.java` — Pattern B
- [x] `web/.../integration/rehabstod/ListActiveSickLeavesForCareUnitResponderImplTest.java` — Pattern B + `@Before`
- [x] `web/.../integration/message/MessageControllerTest.java` — Pattern B

**Verify:** `./gradlew test`

---

### Batch 6: Mockito runner — service impl tests *(Pattern B/C — 12 files)*

> Service layer tests. Includes the single `@After` case.

- [x] `web/.../service/impl/RecipientServiceImplTest.java` — Pattern B + `@Before`
- [x] `web/.../service/impl/CertificateSenderServiceImplTest.java` — Pattern B + `@Before`
- [x] `web/.../service/impl/CertificateServiceImplTest.java` — Pattern B + `@Before`
- [x] `web/.../service/impl/SjukfallCertificateServiceImplTest.java` — Pattern B
- [x] `web/.../service/impl/MessageServiceImplTest.java` — Pattern B
- [x] `web/.../service/impl/MonitoringLogServiceImplTest.java` — **Pattern C** (B + `@After` → `@AfterEach`)
- [x] `web/.../service/impl/IntygInfoServiceImplTest.java` — Pattern B + `@Before`
- [x] `web/.../service/impl/StatisticsServiceImplTest.java` — Pattern B
- [x] `web/.../service/impl/CertificateListServiceImplTest.java` — Pattern B
- [x] `web/.../service/impl/ArendeServiceImplTest.java` — Pattern B
- [x] `web/.../service/impl/InternalNotificationServiceImplTest.java` — Pattern B
- [x] `web/.../service/impl/TestCertificateServiceImplTest.java` — Pattern B + `@Before`

**Verify:** `./gradlew test`

---

### Batch 7: Spring integration tests — persistence module *(Pattern D — 6 files)*

> **This is the trickiest batch.** The base class `TestSupport` uses `@RunWith(SpringJUnit4ClassRunner.class)`.
> Changing the base class migrates all 5 subclasses at once. All subclasses only use `@Test` and `Assert`.

**Step 7a — Migrate TestSupport base class:**

- [x] `persistence/.../dao/impl/TestSupport.java` — **Pattern D**: Replace `@RunWith(SpringJUnit4ClassRunner.class)` →
  `@ExtendWith(SpringExtension.class)`

**Step 7b — Migrate all subclasses (they inherit the extension from TestSupport):**

- [x] `persistence/.../dao/impl/CertificateDaoImplTest.java` — Change `@Test` + `Assert` imports
- [x] `persistence/.../dao/impl/RelationDaoImplTest.java` — Change `@Test` + `Assert` imports
- [x] `persistence/.../dao/impl/SjukfallCertificateDaoImplTest.java` — Change `@Test` + `Assert` imports
- [x] `persistence/.../dao/impl/ArendeRepositoryTest.java` — Change `@Test` + `Assert` imports
- [x] `persistence/.../dao/impl/ApprovedReceiverDaoImplTest.java` — Change `@Test` + `Assert` imports
- [x] `persistence/.../dao/impl/RekoRepositoryTest.java` — Change `@Test` + `Assert` imports

**Verify:** `./gradlew test` (especially `:intygstjanst-persistence:test`)

---

### Batch 8: Remove JUnit 4 dependencies & vintage engine *(build.gradle changes)*

> Final cleanup. After all test code is migrated, remove the bridge dependencies.

- [ ] `persistence/build.gradle` — Remove `testImplementation "junit:junit"` and `testRuntimeOnly "org.junit.vintage:junit-vintage-engine""`
- [ ] `persistence/build.gradle` — Add `testImplementation "org.junit.jupiter:junit-jupiter"` and
  `testImplementation "org.mockito:mockito-junit-jupiter"` (if not already inherited)
- [ ] `web/build.gradle` — Remove `testRuntimeOnly "org.junit.vintage:junit-vintage-engine"`
- [ ] Verify no file imports `org.junit.Test`, `org.junit.runner`, `org.junit.Before`, `org.junit.After`, `org.junit.Assert`, or
  `junit.framework`

**Verify:**

```bash
./gradlew clean test
# Confirm zero JUnit 4 imports remain:
grep -r "import org.junit\." --include="*.java" -l  # should return nothing
grep -r "junit.framework" --include="*.java" -l       # should return nothing
grep -r "junit-vintage-engine" build.gradle persistence/build.gradle web/build.gradle logging/build.gradle  # should return nothing
grep -r '"junit:junit"' build.gradle persistence/build.gradle web/build.gradle logging/build.gradle          # should return nothing
```

---

## Example Migration: Representative Files

Below are 3 concrete before/after examples covering the main patterns.

### Example 1: Pattern A — `CertificateStateHistoryEntryTest.java` (plain, no runner)

**Before:**

```java
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class CertificateStateHistoryEntryTest {

    @Test
    public void testOrdering() { ...}
}
```

**After:**

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CertificateStateHistoryEntryTest {

    @Test
    void testOrdering() { ...}
}
```

### Example 2: Pattern B — `RecipientServiceImplTest.java` (MockitoJUnitRunner + @Before)

**Before:**

```java
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RecipientServiceImplTest {

    @Before
    public void setup() { ...}

    @Test
    public void testListRecipients() { ...}
}
```

**After:**

```java
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipientServiceImplTest {

    @BeforeEach
    void setup() { ...}

    @Test
    void testListRecipients() { ...}
}
```

### Example 3: Pattern D — `TestSupport.java` (SpringJUnit4ClassRunner base class)

**Before:**

```java
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles({"dev"})
@Transactional
public abstract class TestSupport { ...
}
```

**After:**

```java
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles({"dev"})
@Transactional
public abstract class TestSupport { ...
}
```

---

## Risk Notes

| Risk                                                                                                               | Mitigation                                                                                                                                                                                                                                                  |
|--------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `assertEquals(String message, expected, actual)` — JUnit 5 moves message to last param                             | Search for 3-arg `assertEquals` calls. In this codebase, messages are rarely used — most calls are 2-arg.                                                                                                                                                   |
| Mockito strict stubs — `MockitoExtension` uses strict stubs by default (like `MockitoJUnitRunner.Silent` does NOT) | If tests fail with `UnnecessaryStubbingException`, use `@MockitoSettings(strictness = Strictness.LENIENT)` on the class or remove the unnecessary stubs. `MockitoJUnitRunner` (without `.Silent`) already uses strict stubs, so this should be a non-issue. |
| Spring integration tests — `TestSupport` base class change affects 5 subclasses at once                            | Migrate all 5 subclasses + base class in a single batch. Run `./gradlew :intygstjanst-persistence:test` immediately.                                                                                                                                        |
| `persistence/build.gradle` has `testImplementation "junit:junit"` but no `junit-jupiter`                           | Must add JUnit 5 dependency to persistence before removing JUnit 4 in Batch 8.                                                                                                                                                                              |

---

## Progress Tracker

| Batch     | Description                                  | Files                         | Status        |
|-----------|----------------------------------------------|-------------------------------|---------------|
| 0         | Logging module                               | 2                             | ✅ Done        |
| 1         | Plain JUnit 4 tests (no runner)              | 7                             | ✅ Done        |
| 2         | Plain JUnit 4 + @Before (no runner)          | 3                             | ✅ Done        |
| 3         | Mockito runner — validators & stubs          | 8                             | ✅ Done        |
| 4         | Mockito runner — integration responders pt 1 | 10                            | ✅ Done        |
| 5         | Mockito runner — integration responders pt 2 | 14                            | ✅ Done        |
| 6         | Mockito runner — service impl tests          | 12                            | ✅ Done        |
| 7         | Spring integration tests — persistence       | 6+1                           | ✅ Done        |
| 8         | Remove JUnit 4 deps from build.gradle        | 4                             | ⬜ Not started |
| **Total** |                                              | **~60 files + 3 build files** |               |

