# Step 8 — Convert JAX-RS Controllers to Spring MVC (Detailed Incremental Plan)

## Progress Tracker

> Update the Status column as each step is completed.
> Statuses: `⬜ TODO` | `🔄 IN PROGRESS` | `✅ DONE` | `⏭️ SKIPPED`

| Step     | Description                                       | Status | Commit/PR | Verified | Notes |
|----------|---------------------------------------------------|--------|-----------|----------|-------|
| **8.0**  | Create `WebMvcConfig`                             | ✅ DONE |           | ✅        |       |
| **8.1**  | Convert `IntygInfoController`                     | ✅ DONE |           | ✅        |       |
| **8.2**  | Convert `MessageController` + test                | ✅ DONE |           | ✅        |       |
| **8.3**  | Convert `TestCertificateController`               | ✅ DONE |           | ✅        |       |
| **8.4**  | Convert `CertificateListController`               | ✅ DONE |           | ✅        |       |
| **8.5**  | Convert `CertificateExportController`             | ✅ DONE |           | ✅        |       |
| **8.6**  | Convert `RekoController` + test                   | ✅ DONE |           | ✅        |       |
| **8.7**  | Convert `CitizenCertificateController` + test     | ✅ DONE |           | ✅        |       |
| **8.8**  | Convert `CitizenController` + test                | ✅ DONE |           | ✅        |       |
| **8.9**  | Convert `TypedCertificateController`              | ✅ DONE |           | ✅        |       |
| **8.10** | Convert `SickLeaveController` + test              | ✅ DONE |           | ✅        |       |
| **8.11** | **SWITCH `/internalapi`** — deploy 8.0–8.11       | ✅ DONE |           | ✅        |       |
| **8.12** | Convert + switch `/api/send-message-to-care`      | ✅ DONE |           | ✅        |       |
| **8.13** | Convert `/resources` controllers                  | ✅ DONE |           | ✅        |       |
| **8.14** | Switch `/resources` — deploy 8.13–8.14            | ✅ DONE |           | ✅        |       |
| **8.15** | Cleanup: remove `jaxrs-context.xml` + JAX-RS deps | ⬜ TODO |           |          |       |

**Deployment batches:**

- 🚀 **Batch 1:** Steps 8.0–8.11 (deploy together)
- 🚀 **Batch 2:** Step 8.12 (deploy independently)
- 🚀 **Batch 3:** Steps 8.13–8.14 (deploy together)
- 🚀 **Batch 4:** Step 8.15 (deploy independently)

---

## Understanding the Current Servlet Architecture

### How CXF Servlet Works

The `CXFServlet` is registered in `web.xml` mapped to `/*`:

```
Browser/Client → Tomcat → /inera-certificate/* → CXFServlet (/*) → CXF Bus
```

The CXF Bus maintains an internal registry of all endpoints:

- **SOAP (JAX-WS)** endpoints from `application-context-ws.xml` (`jaxws:endpoint`)
- **REST (JAX-RS)** endpoints from `jaxrs-context.xml` (`jaxrs:server`)

When CXFServlet receives a request, it looks up the path (after the servlet mapping prefix) against its registered addresses:

- `/list-sick-leaves-for-person/v1.0` → matches a `jaxws:endpoint` → SOAP handler
- `/internalapi/citizens/certificates` → matches `jaxrs:server address="/internalapi"` → JAX-RS, routes to `CitizenController`
- `/api/send-message-to-care/ping` → matches `jaxrs:server address="/api/send-message-to-care"` → JAX-RS
- `/resources/certificate/123` → matches `jaxrs:server address="/resources"` → JAX-RS
- `/unknown/path` → CXF returns 404

**Key insight:** CXF's `/*` mapping means it **intercepts everything** — including any path Spring MVC's `DispatcherServlet` might want to
handle. Two servlets can't both be mapped to `/*` and both work.

### Current URL Structure (deployed)

All paths are relative to the context path `/inera-certificate`:

| Type               | URL Pattern                        | Example Full URL                                                      |
|--------------------|------------------------------------|-----------------------------------------------------------------------|
| SOAP               | `/{service-name}/{version}`        | `/inera-certificate/list-sick-leaves-for-person/v1.0`                 |
| REST (internal)    | `/internalapi/{controller-path}`   | `/inera-certificate/internalapi/citizens/certificates`                |
| REST (stub)        | `/api/send-message-to-care/{path}` | `/inera-certificate/api/send-message-to-care/ping`                    |
| REST (testability) | `/resources/{controller-path}`     | `/inera-certificate/resources/certificate/123`                        |
| SOAP stubs         | `/stubs/...`                       | `/inera-certificate/stubs/SendMedicalCertificateQuestion/1/rivtabp20` |

### The Core Problem

To use Spring MVC, we need a `DispatcherServlet`. But:

1. CXF is at `/*` — it catches everything first.
2. We **cannot** move CXF away from `/*` because that would change all SOAP endpoint URLs, and we don't control all consumers.
3. We can't have two servlets both at `/*`.

---

## The Solution: Path-Partitioned Servlets (Zero URL Changes)

The key realization is that REST and SOAP endpoints **already live under distinct URL prefixes**:

- REST APIs: `/internalapi/*`, `/api/*`, `/resources/*`
- SOAP endpoints: `/list-sick-leaves-for-person/*`, `/get-certificate-se/*`, `/register-certificate-se/*`, `/stubs/*`, etc.

**We can partition the servlet mappings so each servlet handles only its own paths**, and all external URLs remain identical:

```
web.xml servlet mappings:
  DispatcherServlet → /internalapi/*       (more specific)
  DispatcherServlet → /api/*               (more specific)
  DispatcherServlet → /resources/*         (more specific)
  CXFServlet        → /*                   (catches everything else = SOAP)
```

### How Servlet Mapping Priority Works (Servlet Spec §12.1)

When Tomcat receives a request, it selects the servlet using this priority:

1. **Exact match** (`/foo/bar`) wins first
2. **Longest path-prefix match** (`/internalapi/*` beats `/*`) wins second
3. **Extension match** (`*.do`) wins third
4. **Default servlet** (`/*` or `/`) wins last

So `/internalapi/*` is a **more specific** match than `/*`. When a request comes in for `/internalapi/citizens/certificates`:

- Tomcat sees both `/internalapi/*` (DispatcherServlet) and `/*` (CXFServlet)
- `/internalapi/*` is a longer prefix match → **DispatcherServlet handles it**
- CXF never sees it

Meanwhile, `/list-sick-leaves-for-person/v1.0` only matches `/*` → **CXFServlet handles it** as before.

**No URL changes for any consumer — REST or SOAP.**

### @RequestMapping Path Adjustment

Because `DispatcherServlet` is mapped to `/internalapi/*`, the **servlet path** is `/internalapi`. Spring MVC `@RequestMapping` paths are
relative to the servlet path:

- Old CXF: `jaxrs:server address="/internalapi"` + `@Path("/citizens")` → Full URL: `/internalapi/citizens`
- New Spring MVC: `DispatcherServlet at /internalapi/*` + `@RequestMapping("/citizens")` → Full URL: `/internalapi/citizens` ✅

**The paths in `@RequestMapping` should NOT include `/internalapi`** — the servlet mapping already provides that prefix.

---

## Incremental Migration Strategy — Batch Deployment

### The "convert then switch" pattern

Within each URL prefix group, we:

1. Convert controllers one at a time (separate commits, unit tests pass after each)
2. Then do a single "switch" commit that adds the DispatcherServlet mapping and removes the CXF JAX-RS server block
3. **Deploy the entire batch together** — all controller conversions + the switch commit

**Why batch deployment:** Steps 8.1–8.10 convert controllers from JAX-RS annotations to Spring MVC annotations. Once the JAX-RS annotations
are removed, CXF can no longer route to those controllers. The controllers only become functional again when the DispatcherServlet is
registered (Step 8.11). Therefore steps 8.0–8.11 must be **deployed as a single unit**.

However, each step is a **separate commit** that compiles and passes unit tests independently. This keeps changes small, reviewable,
and easy to bisect if issues arise.

### Deployment batches

| Batch          | Steps     | What                                            | Runnable after deploy?        |
|----------------|-----------|-------------------------------------------------|-------------------------------|
| **🚀 Batch 1** | 8.0–8.11  | Convert all `/internalapi` controllers + switch | ✅ Yes — full app verification |
| **🚀 Batch 2** | 8.12      | Convert + switch `/api/send-message-to-care`    | ✅ Yes                         |
| **🚀 Batch 3** | 8.13–8.14 | Convert all `/resources` controllers + switch   | ✅ Yes                         |
| **🚀 Batch 4** | 8.15      | Cleanup: remove JAX-RS deps                     | ✅ Yes                         |

---

## Detailed Steps

### Step 8.0 — Create `WebMvcConfig` (preparation)

**Files:** NEW: `web/src/main/java/se/inera/intyg/intygstjanst/config/WebMvcConfig.java`

Creates the MVC infrastructure. No runtime impact yet since no DispatcherServlet is registered.

```java

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter jackson = new MappingJackson2HttpMessageConverter();
        jackson.setObjectMapper(objectMapper);
        converters.add(jackson);
    }
}
```

**Verify:** `./gradlew test`

---

### Step 8.1 — Convert `IntygInfoController`

**Why first:** Smallest (72 lines), 2 GET endpoints, no test file.

**Conversion:**
| JAX-RS | Spring MVC |
|--------|-----------|
| `@Path("/intygInfo")` | `@RequestMapping("/intygInfo")` |
| `@GET @Path("/{id}")` | `@GetMapping("/{id}")` |
| `@PathParam("id")` | `@PathVariable("id")` |
| `Response.ok(x).build()` | `ResponseEntity.ok(x)` |
| `Response.status(NOT_FOUND).build()` | `ResponseEntity.notFound().build()` |
| `@Produces`/`@Consumes` | removed (Jackson auto-configured) |
| — | add `@RestController` |

**Verify:** `./gradlew test`

---

### Step 8.2 — Convert `MessageController` (has test)

- `Response` → `ResponseEntity<?>`
- `Response.status(400, "msg").build()` → `ResponseEntity.badRequest().body("msg")`
- Update `MessageControllerTest` assertions from `Response.getStatus()` → `ResponseEntity.getStatusCode().value()`

**Verify:** `./gradlew test`

---

### Step 8.3 — Convert `TestCertificateController`

- `Response` → `ResponseEntity<?>`
- No test to update

**Verify:** `./gradlew test`

---

### Step 8.4 — Convert `CertificateListController`

- `@Controller` + `@Path` → `@RestController` + `@RequestMapping`
- Returns `CertificateListResponse` directly — no Response wrapper changes

**Verify:** `./gradlew test`

---

### Step 8.5 — Convert `CertificateExportController`

- `@PathParam` → `@PathVariable`, `@QueryParam` → `@RequestParam`
- `@DELETE` → `@DeleteMapping`
- Returns values directly

**Verify:** `./gradlew test`

---

### Step 8.6 — Convert `RekoController` (has test)

- Returns `RekoStatusDTO` directly — annotation changes only
- Remove `charset=utf-8` from `produces` (Spring handles this)
- Test: minimal changes

**Verify:** `./gradlew test`

---

### Step 8.7 — Convert `CitizenCertificateController` (has test)

- Returns DTOs directly — annotation changes only
- Test: minimal changes

**Verify:** `./gradlew test`

---

### Step 8.8 — Convert `CitizenController` (has test)

- Returns `List<ResponseObject>` — annotation changes only
- `@RequestBody` already from Spring (`org.springframework.web.bind.annotation`)

**Verify:** `./gradlew test`

---

### Step 8.9 — Convert `TypedCertificateController`

- `@Controller` + `@Path` → `@RestController` + `@RequestMapping`
- Returns lists directly

**Verify:** `./gradlew test`

---

### Step 8.10 — Convert `SickLeaveController` (has test)

- `Response` → `ResponseEntity<SickLeaveResponseDTO>` / `ResponseEntity<PopulateFiltersResponseDTO>`
- Update test assertions

**Verify:** `./gradlew test`

---

### Step 8.11 — **SWITCH**: Activate `/internalapi` via DispatcherServlet

**This is the "flip the switch" step.** All 10 controllers are already converted.

**Changes:**

1. **`web.xml`:** Add DispatcherServlet + mapping:

```xml

<servlet>
	<servlet-name>springmvc</servlet-name>
	<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
	<init-param>
		<param-name>contextConfigLocation</param-name>
		<param-value></param-value>
	</init-param>
	<load-on-startup>2</load-on-startup>
</servlet>
<servlet-mapping>
<servlet-name>springmvc</servlet-name>
<url-pattern>/internalapi/*</url-pattern>
</servlet-mapping>
```

2. **`jaxrs-context.xml`:** Remove entire `<jaxrs:server id="internalApi">` block (lines 37–53).

3. **No URL changes.** `/inera-certificate/internalapi/*` → DispatcherServlet. SOAP at `/*` → CXF.

**`internalApiFilter`** continues to work — filters execute before servlet dispatch.

**Verify:**

- `./gradlew test`
- Start app
- Test all `/internalapi` endpoints
- Test SOAP endpoints (unchanged)

---

### Step 8.12 — Convert + switch `/api/send-message-to-care`

1. Convert `SendMessageToCareResponderStubRestApi.java` to Spring MVC
    - `Response` → `ResponseEntity<String>`
    - `@Produces(APPLICATION_XML)` → `produces = MediaType.APPLICATION_XML_VALUE`
2. Add servlet mapping: `<url-pattern>/api/*</url-pattern>`
3. Remove `<jaxrs:server id="sendMessageToCareStub">` from `jaxrs-context.xml`

**Verify:** `./gradlew test` + curl

---

### Step 8.13 — Convert all `/resources` controllers

Convert all 5 testability/dev controllers to `@RestController` with `@Profile({"dev", "testability-api"})`:

1. `TestabilityController`
2. `FkStubResource`
3. `CertificateResource`
4. `SjukfallCertResource`
5. `StatisticsServiceResource`

Convert `medicalCertificateStore` XML bean → `@Bean` in `@Configuration` + `@Profile`.

**Verify:** `./gradlew test`

---

### Step 8.14 — Switch `/resources` to DispatcherServlet

1. Add: `<url-pattern>/resources/*</url-pattern>`
2. Remove `<jaxrs:server id="certificateService">` + bean definitions from `jaxrs-context.xml`

**Verify:** `./gradlew test` + start with dev profile + curl

---

### Step 8.15 — Cleanup

1. Delete `jaxrs-context.xml` (should be empty now)
2. Remove `<import resource="jaxrs-context.xml"/>` from `application-context.xml`
3. Remove from `web/build.gradle`:
    - `implementation "jakarta.ws.rs:jakarta.ws.rs-api"`
    - `implementation "com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-json-provider"`
4. Remove `jacksonJsonProvider` bean from `ApplicationConfig.java`
5. Verify: `grep -r "jakarta.ws.rs" web/src/main/java/` returns nothing

**Verify:** `./gradlew build` — compiles, all tests pass. Start app, verify all endpoints.

---

## Summary

| Step     | What                                        | Batch | Unit tests pass? | URL Changes |
|----------|---------------------------------------------|-------|------------------|-------------|
| **8.0**  | Create WebMvcConfig                         | 🚀 1  | ✅                | None        |
| **8.1**  | Convert IntygInfoController                 | 🚀 1  | ✅                | None        |
| **8.2**  | Convert MessageController + test            | 🚀 1  | ✅                | None        |
| **8.3**  | Convert TestCertificateController           | 🚀 1  | ✅                | None        |
| **8.4**  | Convert CertificateListController           | 🚀 1  | ✅                | None        |
| **8.5**  | Convert CertificateExportController         | 🚀 1  | ✅                | None        |
| **8.6**  | Convert RekoController + test               | 🚀 1  | ✅                | None        |
| **8.7**  | Convert CitizenCertificateController + test | 🚀 1  | ✅                | None        |
| **8.8**  | Convert CitizenController + test            | 🚀 1  | ✅                | None        |
| **8.9**  | Convert TypedCertificateController          | 🚀 1  | ✅                | None        |
| **8.10** | Convert SickLeaveController + test          | 🚀 1  | ✅                | None        |
| **8.11** | **SWITCH /internalapi**                     | 🚀 1  | ✅                | **None**    |
| **8.12** | Convert + switch /api                       | 🚀 2  | ✅                | None        |
| **8.13** | Convert /resources controllers              | 🚀 3  | ✅                | None        |
| **8.14** | Switch /resources                           | 🚀 3  | ✅                | None        |
| **8.15** | Cleanup: remove JAX-RS                      | 🚀 4  | ✅                | None        |

**Key points:**

- Each step is a **separate commit** — small, reviewable, unit tests pass
- Steps within the same **batch** must be **deployed together**
- **Zero URL changes** throughout the entire migration
- SOAP endpoints are completely untouched
- 4 natural "verify by running the app" checkpoints — one per batch
