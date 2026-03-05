# Step 10 — Spring Boot Bootstrap (Detailed Incremental Plan)

## Progress Tracker

> Update the Status column as each step is completed.
> Statuses: `⬜ TODO` | `🔄 IN PROGRESS` | `✅ DONE` | `⏭️ SKIPPED`

| Step      | Description                                                     | Status | Commit/PR | Verified | Notes |
|-----------|-----------------------------------------------------------------|--------|-----------|----------|-------|
| **10.1**  | Add Spring Boot Gradle plugin (no code changes)                 | ✅ DONE |           | ✅        |       |
| **10.2**  | Create `IntygstjanstApplication.java` main class                | ✅ DONE |           | ✅        |       |
| **10.3**  | Adapt `ApplicationConfig` for Spring Boot coexistence           | ✅ DONE |           | ✅        |       |
| **10.4**  | Register CXF servlet via `ServletRegistrationBean`              | ✅ DONE |           | ✅        |       |
| **10.5**  | Register filters via `FilterRegistrationBean`                   | ✅ DONE |           | ✅        |       |
| **10.6**  | Adapt `WebMvcConfig` — remove `@EnableWebMvc`                   | ✅ DONE |           | ✅        |       |
| **10.7**  | Move/adapt `application.properties` for Spring Boot conventions | ✅ DONE |           | ✅        |       |
| **10.8**  | Remove `web.xml`, `version.jsp`, `webapp/` directory            | ✅ DONE |           | ✅        |       |
| **10.9**  | Switch from `war`/Gretty plugin to Spring Boot `jar`            | ✅ DONE |           | ✅        |       |
| **10.10** | Final verification — `./gradlew bootRun` + `./gradlew test`     | ✅ DONE |           | ✅        |       |

**Deployment batches:**

- 🚀 **Batch 1:** Steps 10.1–10.3 (build still works as WAR, can verify compilation)
- 🚀 **Batch 2:** Steps 10.4–10.6 (servlet/filter registration, still WAR-compatible)
- 🚀 **Batch 3:** Steps 10.7–10.9 (the actual switch to Spring Boot JAR)
- 🚀 **Batch 4:** Step 10.10 (final verification)

---

## Pre-conditions — Verified Current State

Before planning, the following assumptions from the incremental migration plan were verified against the actual codebase:

| Assumption                                                         | Verified? | Actual State                                                                                                                                                                                        |
|--------------------------------------------------------------------|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Steps 1–9 are complete                                             | ✅         | Step 8 tracker: all 15 sub-steps ✅ DONE. Step 9 tracker: all 13 sub-steps ✅ DONE.                                                                                                                   |
| No XML Spring config files remain                                  | ✅         | No `application-context*.xml`, no `jaxrs-context.xml`. Only `persistence.xml` remains (expected — removed in Step 11).                                                                              |
| All controllers are `@RestController` (no JAX-RS)                  | ✅         | 15 `@RestController` classes found. No `@Path` (JAX-RS) annotations. All use `@RequestMapping`/`@GetMapping`/`@PostMapping`.                                                                        |
| Config is already Java-based `@Configuration`                      | ✅         | `ApplicationConfig.java` is the root config. `web.xml` points to `AnnotationConfigWebApplicationContext` with `contextConfigLocation = se.inera.intyg.intygstjanst.config.ApplicationConfig`.       |
| `web.xml` still exists and drives the app                          | ✅         | `web.xml` registers: `ContextLoaderListener`, `DispatcherServlet` (→ `/internalapi/*`, `/api/*`, `/resources/*`), `CXFServlet` (→ `/*`), two filters, one logback listener.                         |
| CXF is mapped to `/*` for SOAP endpoints                           | ✅         | CXF servlet at `/*` catches SOAP. DispatcherServlet at `/internalapi/*`, `/api/*`, `/resources/*` catches REST. No URL conflicts.                                                                   |
| Dual-port Tomcat setup exists (`internal.api.port`)                | ✅         | `tomcat-gretty.xml` configures two connectors. `InternalApiFilter` checks `request.getLocalPort()`. This must be preserved under Spring Boot embedded Tomcat.                                       |
| `war` plugin + Gretty are active                                   | ✅         | `web/build.gradle` applies `war` and `org.gretty` plugins. Gretty config in `web/build.gradle` with `servletContainer = 'tomcat10'`.                                                                |
| No `@SpringBootApplication` exists yet                             | ✅         | No `@SpringBootApplication` annotation found anywhere.                                                                                                                                              |
| Tests are all Mockito-based (no Spring context tests)              | ✅         | All test classes use `@ExtendWith(MockitoExtension.class)`. No `@SpringBootTest`, `@ContextConfiguration`, or `@WebMvcTest` found.                                                                  |
| All infra deps inlined, not removed from classpath                 | ✅         | `se.inera.intyg.infra` packages exist in `infra/`, `integration-api/`, `integration-intyg-proxy-service/` subprojects. Still referenced via `project(':intygstjanst-infra')` in `web/build.gradle`. |
| `JpaConfigBase` manually configures HikariCP/JPA                   | ✅         | Full manual DataSource, EntityManagerFactory, TransactionManager, and Liquibase setup in `JpaConfigBase.java`. Uses `db.*` and `hibernate.*` properties. **Kept as-is** in Step 10.                 |
| JMS manually configured                                            | ✅         | `JmsConfig.java` creates `ActiveMQConnectionFactory`, `PooledConnectionFactory`, `JmsTransactionManager`, `JmsTemplate` beans. Uses `activemq.broker.*` properties. **Kept as-is** in Step 10.      |
| Redis/caching manually configured                                  | ✅         | `JobConfig.java` uses `JedisConnectionFactory`. Cache config in `integration-intyg-proxy-service`. **Kept as-is** in Step 10.                                                                       |
| Prometheus manually configured                                     | ✅         | `io.prometheus:simpleclient_servlet` in `web/build.gradle`. **Kept as-is** in Step 10.                                                                                                              |
| Logback configured via custom `LogbackConfiguratorContextListener` | ✅         | Custom `ServletContextListener` reads `logback.file` system property. Will be replaced by Spring Boot's native logback support.                                                                     |

### Key Architectural Insight

The current servlet architecture is:

```
Tomcat (Gretty, external WAR)
├── ContextLoaderListener → creates root ApplicationContext from ApplicationConfig.java
├── DispatcherServlet (no child context, empty contextConfigLocation)
│   ├── /internalapi/*
│   ├── /api/*
│   └── /resources/*
├── CXFServlet → /*  (SOAP endpoints)
├── InternalApiFilter → /internalapi/*
├── MdcServletFilter → /*
└── LogbackConfiguratorContextListener
```

Under Spring Boot, **we replicate the exact same servlet layout** — zero URL changes:

```
Embedded Tomcat (Spring Boot)
├── @SpringBootApplication → creates single ApplicationContext
├── DispatcherServlet (Spring Boot auto-configured, remapped via spring.mvc.servlet.path)
│   ├── /internalapi/*    ← via ServletRegistrationBean url-mappings
│   ├── /api/*
│   └── /resources/*
├── CXFServlet (via ServletRegistrationBean) → /*
│   ├── SOAP endpoints (jaxws:endpoint beans) — paths unchanged
├── InternalApiFilter (via FilterRegistrationBean) → /internalapi/*
├── MdcServletFilter (via FilterRegistrationBean) → /*
└── Logback configured by Spring Boot natively (logging.config property)
```

**How this works — keeping ALL URLs unchanged:**

The Servlet spec says **more specific path mappings win**. This is exactly what the current `web.xml` relies on:
`/internalapi/*`, `/api/*`, and `/resources/*` are more specific than `/*`, so the DispatcherServlet handles
those paths and CXF gets everything else.

Under Spring Boot, we do the same thing:

1. Register `CXFServlet` at `/*` via `ServletRegistrationBean`.
2. Override Spring Boot's auto-configured `DispatcherServlet` mapping from `/*` to the three specific paths
   (`/internalapi/*`, `/api/*`, `/resources/*`) via a `DispatcherServletRegistrationBean`.

This means:

- **SOAP endpoints**: `http://host/inera-certificate/get-certificate-se/v2.0` → **unchanged** ✅
- **REST endpoints**: `http://host:8180/inera-certificate/internalapi/v1/certificatetexts` → **unchanged** ✅
- **Stub endpoints**: `http://host/inera-certificate/stubs/...` → **unchanged** ✅
- **Test API endpoints**: `http://host/inera-certificate/resources/certificate/...` → **unchanged** ✅

No reverse proxy changes, no consumer updates, no URL rewrites needed.

---

## Step 10.1 — Add Spring Boot Gradle Plugin (No Code Changes)

**What:** Add the Spring Boot Gradle plugin to the build, but don't activate it yet. This step only verifies that the plugin resolves
and the build still compiles.

**Why safe:** The plugin is applied but the application still runs as a WAR under Gretty. The Spring Boot plugin adds tasks like
`bootJar` and `bootRun` but doesn't interfere with the existing `war` task.

**Changes:**

1. **`build.gradle` (root)** — Add Spring Boot plugin with `apply false`:
   ```groovy
   plugins {
       // ...existing plugins...
       id "org.springframework.boot" version "3.4.3" apply false
   }
   ```

2. **`web/build.gradle`** — Apply the plugin:
   ```groovy
   apply plugin: 'org.springframework.boot'
   ```

   Also add the Spring Boot BOM for dependency management:
   ```groovy
   apply plugin: 'io.spring.dependency-management'
   ```

   **Note:** The `io.spring.dependency-management` plugin is automatically applied when using the Spring Boot plugin.
   However, since the project already uses an `intygBomVersion` platform, we need to be careful about version conflicts.
   Spring Boot's dependency management should complement the existing BOM, not override it.
   Add a `dependencyManagement` block to import Spring Boot's BOM:
   ```groovy
   dependencyManagement {
       imports {
           mavenBom org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES
       }
   }
   ```

3. **Important:** Add `spring-boot-starter-web` dependency (but don't change anything else yet):
   ```groovy
   implementation "org.springframework.boot:spring-boot-starter-web"
   ```

   This brings in embedded Tomcat, Spring MVC, and Jackson — but since the app still runs as a WAR under Gretty,
   the embedded Tomcat won't start. It's just on the classpath.

**Verify:**

```bash
./gradlew clean build        # Compiles, all tests pass
./gradlew :intygstjanst-web:dependencies | grep spring-boot  # Spring Boot deps on classpath
```

The application can still start under Gretty:

```bash
./gradlew appRun             # Still works as before
```

**Risks:**

- Version conflicts between `intygBomVersion` platform and Spring Boot's managed versions. If conflicts arise, use `exclude` or
  explicit version overrides. Check `./gradlew :intygstjanst-web:dependencies` for unexpected version changes.

---

## Step 10.2 — Create `IntygstjanstApplication.java` Main Class

**What:** Create the Spring Boot main class. This class won't be used yet (the app still runs under Gretty/WAR), but it establishes
the entry point for later steps.

**Why safe:** An unused main class has zero effect on the running application.

**Changes:**

1. **Create** `web/src/main/java/se/inera/intyg/intygstjanst/IntygstjanstApplication.java`:

   ```java
   package se.inera.intyg.intygstjanst;

   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
   import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
   import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
   import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
   import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;

   @SpringBootApplication(
       exclude = {
           DataSourceAutoConfiguration.class,
           HibernateJpaAutoConfiguration.class,
           LiquibaseAutoConfiguration.class,
           ActiveMQAutoConfiguration.class,
           RedisAutoConfiguration.class
       }
   )
   public class IntygstjanstApplication {

       public static void main(String[] args) {
           SpringApplication.run(IntygstjanstApplication.class, args);
       }
   }
   ```

   **Key design decisions:**
    - `exclude` auto-configurations for JPA, JMS, Liquibase, and Redis because Steps 11–14 handle those migrations separately.
      The existing manual `JpaConfigBase`, `JmsConfig`, and Redis config will continue to work.
    - The `@SpringBootApplication` default `scanBasePackages` would be `se.inera.intyg.intygstjanst` — but `ApplicationConfig`
      already has a broader `@ComponentScan` that includes `se.inera.intyg.infra.*` and `se.inera.intyg.common.*`. We'll handle
      that overlap in Step 10.3.

**Verify:**

```bash
./gradlew clean build    # Compiles, all tests pass
```

---

## Step 10.3 — Adapt `ApplicationConfig` for Spring Boot Coexistence

**What:** Prepare `ApplicationConfig` so it works both under the current WAR setup and under Spring Boot. The goal is that when
`IntygstjanstApplication` starts, `ApplicationConfig` is still discovered and all its beans/scan are used.

**Why this step:** Under WAR (`web.xml`), `ApplicationConfig` is the root context config. Under Spring Boot, `IntygstjanstApplication`
is the root — and `ApplicationConfig` will be discovered via component scan. We need to ensure there are no conflicts.

**Changes:**

1. **`ApplicationConfig.java`** — The `@ComponentScan` on `ApplicationConfig` overlaps with `@SpringBootApplication`'s default scan.
   This is fine — Spring handles duplicate scans gracefully. However, we need to ensure the broader scan packages
   (`se.inera.intyg.infra.*`, `se.inera.intyg.common.*`) are still reached.

   Move the broad `@ComponentScan` to `IntygstjanstApplication` instead:
   ```java
   // IntygstjanstApplication.java
   @SpringBootApplication(
       scanBasePackages = {
           "se.inera.intyg.intygstjanst",
           "se.inera.intyg.infra.integration.intygproxyservice",
           "se.inera.intyg.infra.pu.integration.intygproxyservice",
           "se.inera.intyg.common.support.modules.support.api",
           "se.inera.intyg.common.services",
           "se.inera.intyg.common",
           "se.inera.intyg.common.support.services",
           "se.inera.intyg.common.util.integration.json"
       },
       exclude = { ... }
   )
   ```

   Then **remove** `@ComponentScan` from `ApplicationConfig.java` to avoid double-scanning ambiguity.

2. **`ApplicationConfig.java`** — Remove `@PropertySource` annotations. Under Spring Boot, `application.properties` is loaded
   automatically. The `dev.config.file` external property source can be handled via Spring Boot's
   `spring.config.additional-location` or `spring.config.import`.

   However, **for this step**, keep `@PropertySource` as-is since the app still runs under WAR. We'll remove it in Step 10.7.

3. **`ApplicationConfig.java`** — Remove `@DependsOn("transactionManager")` at the class level. Under Spring Boot, this is not
   needed because the `TransactionManagementConfigurer` interface already handles this. The `@DependsOn` was a workaround for
   XML-era bean ordering.

4. **`ApplicationConfig.java`** — The `PropertySourcesPlaceholderConfigurer` bean can be removed — Spring Boot provides one
   automatically.

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew appRun          # Still works under Gretty
```

---

## Step 10.4 — Register CXF Servlet and Override DispatcherServlet Mapping

**What:** Create Spring Boot `ServletRegistrationBean`s that replicate the exact servlet layout from `web.xml`:
CXF at `/*`, DispatcherServlet at `/internalapi/*`, `/api/*`, `/resources/*`. This keeps all URLs unchanged.

**Why safe:** Under WAR (Gretty), `web.xml` still defines the servlets. The `ServletRegistrationBean`s will be ignored because
there's no embedded Tomcat. Under Spring Boot, the `ServletRegistrationBean`s will be used instead.

**How it works:** The Servlet spec says more specific path mappings win over `/*`. Spring Boot auto-configures a
`DispatcherServlet` mapped to `/`. We override that mapping to use three specific sub-paths. We then register `CXFServlet`
at `/*` which catches everything else (= SOAP). This is **exactly** what `web.xml` does today.

**Changes:**

1. **Create** `web/src/main/java/se/inera/intyg/intygstjanst/config/ServletConfig.java`:

   ```java
   package se.inera.intyg.intygstjanst.config;

   import org.apache.cxf.transport.servlet.CXFServlet;
   import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
   import org.springframework.boot.web.servlet.ServletRegistrationBean;
   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.Configuration;
   import org.springframework.web.servlet.DispatcherServlet;

   @Configuration
   public class ServletConfig {

       /**
        * Overrides Spring Boot's default DispatcherServlet registration.
        * Instead of mapping to "/", we map to the exact same sub-paths
        * as the current web.xml — preserving all REST endpoint URLs.
        */
       @Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)
       public ServletRegistrationBean<DispatcherServlet> dispatcherServletRegistration(DispatcherServlet dispatcherServlet) {
           final var registration = new ServletRegistrationBean<>(dispatcherServlet,
               "/internalapi/*", "/api/*", "/resources/*");
           registration.setName("dispatcherServlet");
           registration.setLoadOnStartup(2);
           return registration;
       }

       /**
        * Registers the CXF servlet at /* — exactly as in web.xml.
        * Because /internalapi/*, /api/*, and /resources/* are more specific,
        * the servlet container routes those to DispatcherServlet first.
        * Everything else (SOAP paths) goes to CXF.
        */
       @Bean
       public ServletRegistrationBean<CXFServlet> cxfServletRegistration() {
           final var registration = new ServletRegistrationBean<>(new CXFServlet(), "/*");
           registration.setName("cxf");
           registration.setLoadOnStartup(1);
           return registration;
       }
   }
   ```

   **Key design decisions:**

    - By naming the `DispatcherServlet` registration bean with `DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME`,
      we **replace** Spring Boot's auto-configured registration rather than creating a second `DispatcherServlet`.
    - The CXF servlet at `/*` works because the three DispatcherServlet paths are more specific.
    - SOAP endpoint paths stay at `/get-certificate-se/v2.0` etc. — no changes to `CxfEndpointConfig.java`.
    - REST endpoint paths stay at `/internalapi/...`, `/api/...`, `/resources/...` — no changes to controllers.

   **URL verification (all unchanged):**

   | Type         | Current URL                                                              | After Spring Boot                                                         |
                                    |--------------|--------------------------------------------------------------------------|---------------------------------------------------------------------------|
   | SOAP         | `http://localhost:8080/inera-certificate/get-certificate-se/v2.0`        | `http://localhost:8080/inera-certificate/get-certificate-se/v2.0` ✅       |
   | SOAP stub    | `http://localhost:8080/inera-certificate/stubs/.../SendMessageToCare/...` | `http://localhost:8080/inera-certificate/stubs/.../SendMessageToCare/...` ✅ |
   | REST (int)   | `http://localhost:8180/inera-certificate/internalapi/v1/certificatetexts` | `http://localhost:8180/inera-certificate/internalapi/v1/certificatetexts` ✅ |
   | REST (api)   | `http://localhost:8080/inera-certificate/api/send-message-to-care/ping`  | `http://localhost:8080/inera-certificate/api/send-message-to-care/ping` ✅  |
   | REST (test)  | `http://localhost:8080/inera-certificate/resources/certificate/123`       | `http://localhost:8080/inera-certificate/resources/certificate/123` ✅      |

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew appRun          # Still works (Gretty ignores ServletRegistrationBean)
```

---

## Step 10.5 — Register Filters via `FilterRegistrationBean`

**What:** Create Spring Boot `FilterRegistrationBean`s for `InternalApiFilter` and `MdcServletFilter`, replacing the `web.xml`
`<filter>` declarations.

**Why safe:** Same as 10.4 — under WAR, `web.xml` filters are used. Under Spring Boot, `FilterRegistrationBean`s are used.

**Changes:**

1. **`ServletConfig.java`** (or create `FilterConfig.java`) — Add filter registrations:

   ```java
   @Bean
   public FilterRegistrationBean<InternalApiFilter> internalApiFilterRegistration(InternalApiFilter filter) {
       final var registration = new FilterRegistrationBean<>(filter);
       registration.addUrlPatterns("/internalapi/*");
       registration.setOrder(1);
       return registration;
   }

   @Bean
   public FilterRegistrationBean<MdcServletFilter> mdcServletFilterRegistration(MdcServletFilter filter) {
       final var registration = new FilterRegistrationBean<>(filter);
       registration.addUrlPatterns("/*");
       registration.setOrder(2);
       return registration;
   }
   ```

2. **`MdcServletFilter.java`** — The current `init()` method calls `SpringBeanAutowiringSupport.processInjectionBasedOnServletContext()`.
   This is needed under WAR because the filter is instantiated by the servlet container (via `web.xml`), outside Spring's control.
   Under Spring Boot, the filter will be a Spring-managed bean (it's already `@Component`), so `@Autowired` works natively.

   **Action:** Keep the `init()` method for now — it's a no-op when the filter is already autowired. It can be removed after
   `web.xml` is deleted and the WAR path is gone. Alternatively, make `init()` conditional:
   ```java
   @Override
   public void init(FilterConfig filterConfig) {
       if (mdcHelper == null) {
           SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this,
               filterConfig.getServletContext());
       }
   }
   ```

3. **`InternalApiFilter`** — Already a Spring bean (`ApplicationConfig.internalApiFilter()`). The `FilterRegistrationBean` can
   inject it directly. No changes needed to the filter class itself.

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew appRun          # Still works under Gretty
```

---

## Step 10.6 — Adapt `WebMvcConfig` — Remove `@EnableWebMvc`

**What:** Remove `@EnableWebMvc` from `WebMvcConfig.java`. Under Spring Boot, `@EnableWebMvc` **disables** Spring Boot's
auto-configuration for Spring MVC. We want Spring Boot's defaults (which include Jackson, content negotiation, etc.) and just
customize what we need.

**Why now:** This change is compatible with both WAR and Spring Boot. Under WAR, removing `@EnableWebMvc` means Spring MVC is
configured via the existing component scan of `WebMvcConfigurer` implementations. Under Spring Boot, it allows auto-configuration
to work.

**Changes:**

1. **`WebMvcConfig.java`** — Remove `@EnableWebMvc`:
   ```java
   @Configuration
   // @EnableWebMvc  ← REMOVED
   @RequiredArgsConstructor
   public class WebMvcConfig implements WebMvcConfigurer {
       // ...existing code unchanged...
   }
   ```

**Important note:** Removing `@EnableWebMvc` under the current WAR setup **may change behavior** because the
`DispatcherServlet` in `web.xml` has `contextConfigLocation` set to empty (it uses the parent context). Without `@EnableWebMvc`,
Spring MVC is still bootstrapped because `WebMvcConfigurer` beans are present. However, verify carefully that message converters
and content negotiation still work as expected.

**Test strategy:** After this change, test a representative REST endpoint under Gretty to confirm JSON serialization/deserialization
still works correctly.

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew appRun          # Start and hit a REST endpoint, verify JSON response
```

---

## Step 10.7 — Move/Adapt `application.properties` for Spring Boot Conventions

**What:** Add Spring Boot-specific properties to `application.properties` without breaking the existing property setup.
Wire up the three-port topology (8080 = public/SOAP, 8081 = internal REST, 8082 = Actuator), add a logback configuration
on the classpath, and implement an allowlist filter that enforces which paths are reachable on port 8080.

The existing properties (`db.*`, `hibernate.*`, `activemq.*`, etc.) continue to work because `JpaConfigBase`, `JmsConfig`,
etc. use `@Value` to read them.

---

### Current URL taxonomy (verified against codebase)

| Port                | Servlet                              | Path prefix                | Controllers / Purpose                                                                                                                                                                                                                                                                                                             |
|---------------------|--------------------------------------|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **8080** (main)     | CXFServlet (`/*` catch-all)          | anything not matched below | All SOAP endpoints — e.g. `/get-recipients-for-certificate/v1.1`, `/list-known-recipients/v1.0`, `/send-message-to-care/v2.0`, …                                                                                                                                                                                                  |
| **8080**            | DispatcherServlet (`/api/*`)         | `/api/`                    | `SendMessageToCareResponderStubRestApi` → `/api/send-message-to-care/…` *(public REST)*                                                                                                                                                                                                                                           |
| **8080**            | DispatcherServlet (`/resources/*`)   | `/resources/`              | `CertificateResource`, `SjukfallCertResource`, `StatisticsServiceResource`, `FkStubResource` *(test/testability)*                                                                                                                                                                                                                 |
| **8081** (internal) | DispatcherServlet (`/internalapi/*`) | `/internalapi/`            | `CertificateListController` (`/certificatelist`), `TypedCertificateController` (`/typedcertificate`), `IntygInfoController` (`/intygInfo`), `SickLeaveController` (`/sickleave`), `RekoController` (`/reko`), `MessageController` (`/message`), `CitizenCertificateController` (`/citizen`), `CertificateExportController` (`v1`) |
| **8082** (actuator) | Spring Boot management server        | `/actuator/`               | Health, info, readiness/liveness probes — managed separately by `management.server.port`                                                                                                                                                                                                                                          |

> **Note on CXF path:** CXF endpoints are published **relative to the CXFServlet's context root**, which is `/*`.
> Because `/api/*`, `/internalapi/*`, and `/resources/*` are more-specific servlet mappings, those go to
> DispatcherServlet. Every other path (SOAP) falls through to CXFServlet.
> The `cxf.path` property (Spring Boot CXF starter) is **not** used here — we rely on the existing
> `ServletRegistrationBean` at `/*` from Step 10.4. No change to `CxfEndpointConfig` is needed.

---

### Changes

#### 1 — `web/src/main/resources/application.properties` — add Spring Boot properties

Add the following block **at the top**, before the existing properties:

```properties
# ============================================================
# Spring Boot server configuration
# ============================================================
server.port=8080
server.servlet.context-path=/inera-certificate

# Internal REST port (extra Tomcat connector — see TomcatConfig.java)
internal.api.port=8081

# Spring Boot Actuator (management) — completely separate port, not affected by any filter
management.server.port=8082
management.endpoints.web.exposure.include=health,info
management.endpoint.health.probes.enabled=true
management.endpoint.health.show-details=always

# Logback configuration (replaces LogbackConfiguratorContextListener)
logging.config=${logback.file:classpath:logback-spring.xml}

# ============================================================
# Disable Spring Boot auto-config for things we configure manually.
# Redundant with @SpringBootApplication(exclude=…) but kept here
# as visible documentation and for safety.
# ============================================================
spring.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
  org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,\
  org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration,\
  org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration,\
  org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration

# ============================================================
# Port-8080 allowlist filter — configure allowed path prefixes
# (read by PublicApiAllowlistFilter; see below)
# ============================================================
# SOAP: CXF endpoints are at /* so all non-REST paths on 8080 are SOAP.
# We do NOT enumerate individual SOAP paths here — the filter allows
# everything that does NOT start with /internalapi/, /api/, or /resources/.
# Instead we declare only the public REST + optional extras below.
public.api.allowlist.prefixes=/api/,/resources/,/error,/actuator
```

**Key decisions:**

- `server.port=8080` — explicit, not derived from a system property (`dev.http.port` was a Gretty-specific convention; under
  Spring Boot the property is `server.port`). Override via `-Dserver.port=…` or env var `SERVER_PORT` if needed.
- `internal.api.port=8081` — keeps the existing property name so no other config changes are required.
- `management.server.port=8082` — Spring Boot puts the actuator on a fully separate `TomcatWebServer` instance on this port.
  It is **not** affected by the allowlist filter running on the main (8080) `TomcatWebServer`.
- `public.api.allowlist.prefixes` — a comma-separated list read by `PublicApiAllowlistFilter` (created below). Adjust freely
  without code changes.

---

#### 2 — `web/src/main/resources/logback-spring.xml` — classpath logback config

Create this file so Spring Boot can find it automatically (`logging.config=classpath:logback-spring.xml`):

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<configuration scan="true" scanPeriod="15 seconds">
	<property name="APP_NAME" value="${APP_NAME:-intygstjanst}"/>

	<logger name="org.apache.cxf.phase.PhaseInterceptorChain" level="ERROR"/>

	<include resource="logback/logback-spring-base.xml"/>

	<root level="INFO">
		<appender-ref ref="CONSOLE"/>
	</root>
</configuration>
```

This mirrors `devops/dev/config/logback-spring.xml` but lives on the classpath as the default.
The existing `-Dlogback.file=…` JVM arg still overrides it via `logging.config=${logback.file:classpath:logback-spring.xml}`.

---

#### 3 — `web/src/main/resources/application-dev.properties` — dev profile overrides

```properties
# Dev profile overrides — loaded automatically by Spring Boot when 'dev' profile is active.
# Pulls in the external dev config file (mirrors current dev.config.file pattern).
spring.config.import=optional:file:${application.dir}/config/application-dev.properties
```

---

#### 4 — Create `TomcatConfig.java` — dual-port embedded Tomcat

**Create** `web/src/main/java/se/inera/intyg/intygstjanst/config/TomcatConfig.java`:

```java
package se.inera.intyg.intygstjanst.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Adds a second embedded Tomcat connector on {@code internal.api.port} (default 8081).
 *
 * <p>This replicates the two-connector setup from {@code tomcat-gretty.xml}.
 * The existing {@link se.inera.intyg.infra.security.filter.InternalApiFilter} checks
 * {@code request.getLocalPort()} and blocks requests from the wrong port — that
 * behaviour is unchanged.</p>
 *
 * <p>Spring Boot's management server runs on {@code management.server.port} (8082) as a
 * completely separate {@code TomcatWebServer} instance managed by
 * {@code ManagementServerConfiguration} — it does NOT go through this customizer.</p>
 */
@Configuration
public class TomcatConfig {

    @Value("${internal.api.port:8081}")
    private int internalApiPort;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> internalApiConnectorCustomizer() {
        return factory -> {
            final var connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            connector.setPort(internalApiPort);
            factory.addAdditionalTomcatConnectors(connector);
        };
    }
}
```

**Why `WebServerFactoryCustomizer<TomcatServletWebServerFactory>` and not `TomcatEmbeddedServletContainerFactory`:**
`TomcatServletWebServerFactory` is the Spring Boot 3.x API. The customizer is called once on the **main** server factory
(port 8080 + 8081) only, not on the management server factory (port 8082). This matches the original `tomcat-gretty.xml`
two-connector configuration.

---

#### 5 — Create `PublicApiAllowlistFilter.java` — port-8080 allowlist filter

**Create** `web/src/main/java/se/inera/intyg/intygstjanst/config/PublicApiAllowlistFilter.java`:

```java
package se.inera.intyg.intygstjanst.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Enforces an allowlist on port 8080 (the public/SOAP port).
 *
 * <h3>Design</h3>
 * <ul>
 *   <li>Port 8080 is the only port that is publicly reachable.
 *       It should serve SOAP endpoints (via CXFServlet at {@code /*}) and a limited set of
 *       public REST paths (e.g. {@code /api/…}).</li>
 *   <li>Port 8081 (internal REST) must NOT be reachable from outside — enforced at the
 *       network/load-balancer level AND by {@link se.inera.intyg.infra.security.filter.InternalApiFilter}
 *       which already blocks wrong-port requests on {@code /internalapi/*}.
 *       This filter does NOT touch port-8081 requests.</li>
 *   <li>Port 8082 (Actuator) is managed by Spring Boot's separate management server.
 *       Requests on 8082 never reach this filter because it runs in the main app context only.</li>
 * </ul>
 *
 * <h3>Allowlist logic (port 8080 only)</h3>
 * <p>A request is <em>allowed</em> if its {@code requestURI} (without context path) starts with
 * any prefix in {@code public.api.allowlist.prefixes}. All other paths on port 8080 return
 * {@code 403 Forbidden}.</p>
 *
 * <h3>Why SOAP is automatically allowed</h3>
 * <p>SOAP endpoints are published by CXFServlet at {@code /*}. Their paths do NOT start with
 * {@code /api/}, {@code /internalapi/}, or {@code /resources/}, so they fall through to CXF.
 * To avoid having to enumerate every SOAP path, the filter uses an <em>inverse</em> strategy:
 * it only blocks REST paths that are NOT in the allowlist. SOAP paths are never in the block
 * list because they go to CXF directly — but we still need them on the allowlist if we want to
 * be explicit. The simplest correct approach is:</p>
 * <ol>
 *   <li>If port != 8080 → pass through (do nothing).</li>
 *   <li>If path starts with any allowed prefix → pass through.</li>
 *   <li>Otherwise → 403.</li>
 * </ol>
 * <p>Since SOAP paths (e.g. {@code /get-certificate-se/v2.0}) do NOT start with
 * {@code /internalapi/}, they would be blocked unless we add them. Rather than listing every
 * SOAP path, add a catch-all for unknown paths by letting CXF handle anything not matched by
 * the DispatcherServlet — i.e., include {@code /} (or no prefix check for paths going to CXF).
 * The cleanest solution: allow everything EXCEPT paths that start with {@code /internalapi/}.
 * The {@code /internalapi/} prefix is already protected by {@link se.inera.intyg.infra.security.filter.InternalApiFilter}
 * but we add a second line of defence here.</p>
 *
 * <h3>Actual allowlist strategy used</h3>
 * <p><strong>Deny-by-exception on port 8080:</strong> block only {@code /internalapi/*} on port
 * 8080. Everything else (SOAP, {@code /api/*}, {@code /resources/*}) is allowed. This is the
 * most correct model because we control the servlet mappings — SOAP paths can't "leak" into
 * DispatcherServlet because CXFServlet is at {@code /*}.</p>
 *
 * <p>The allowlist property {@code public.api.allowlist.prefixes} therefore lists the paths that
 * are <em>explicitly permitted</em> on port 8080. The filter blocks anything NOT in that list.
 * To permit SOAP, add {@code /} as a prefix (matches everything) — or configure individual
 * SOAP path prefixes. The default configuration uses {@code /api/,/resources/,/error,/actuator}
 * plus a wildcard by keeping the SOAP paths unlisted but CXF always handles them first.</p>
 *
 * <p><strong>Recommended property value</strong> (covers all real traffic on port 8080):</p>
 * <pre>
 * public.api.allowlist.prefixes=/api/,/resources/,/error,/actuator,/
 * </pre>
 * <p>Using {@code /} as the last entry effectively allows all SOAP paths without enumeration.
 * Only {@code /internalapi/} would then need to be actively blocked — and it already is by
 * {@link se.inera.intyg.infra.security.filter.InternalApiFilter}.</p>
 *
 * <p>If you want a strict allowlist instead (deny unknown SOAP paths), remove {@code /} and
 * enumerate every SOAP base path explicitly in {@code public.api.allowlist.prefixes}.</p>
 */
@Component
public class PublicApiAllowlistFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(PublicApiAllowlistFilter.class);

    /** The main public port. Only requests on this port are subject to the allowlist. */
    @Value("${server.port:8080}")
    private int publicPort;

    /**
     * Comma-separated path prefixes (without context path) that are allowed on the public port.
     * Paths that do NOT start with any of these prefixes will receive a 403 response.
     * Default: {@code /api/,/resources/,/error,/actuator,/} — the trailing {@code /} acts as
     * a wildcard that permits SOAP paths without enumeration.
     */
    @Value("${public.api.allowlist.prefixes:/api/,/resources/,/error,/actuator,/}")
    private String allowlistPrefixesRaw;

    private List<String> allowlistPrefixes;

    @Override
    protected void initFilterBean() {
        allowlistPrefixes = Arrays.stream(allowlistPrefixesRaw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
        LOG.info("PublicApiAllowlistFilter initialised: publicPort={}, allowedPrefixes={}",
            publicPort, allowlistPrefixes);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        final int localPort = request.getLocalPort();

        // Only enforce on the public port — pass through all other ports (8081, 8082, etc.)
        if (localPort != publicPort) {
            filterChain.doFilter(request, response);
            return;
        }

        final String path = getRequestPath(request);

        if (isAllowed(path)) {
            filterChain.doFilter(request, response);
        } else {
            LOG.warn("PublicApiAllowlistFilter: BLOCKED port={} path={}", localPort, path);
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                "Access to this path is not permitted on this port.");
        }
    }

    /**
     * Returns the path relative to the context root (strips the context path prefix).
     * Example: context=/inera-certificate, URI=/inera-certificate/api/foo → /api/foo
     */
    private String getRequestPath(HttpServletRequest request) {
        final String contextPath = request.getContextPath();
        final String uri = request.getRequestURI();
        return contextPath.isEmpty() ? uri : uri.substring(contextPath.length());
    }

    private boolean isAllowed(String path) {
        return allowlistPrefixes.stream().anyMatch(path::startsWith);
    }
}
```

---

#### 6 — Register `PublicApiAllowlistFilter` via `FilterRegistrationBean`

Add the following bean to the existing **`ServletConfig.java`** (where the other `FilterRegistrationBean`s live — see Step 10.5):

```java
import org.springframework.core.Ordered;
import se.inera.intyg.intygstjanst.config.PublicApiAllowlistFilter;

/**
 * Registers the port-8080 allowlist filter with HIGHEST precedence so it runs before
 * any other filter. Maps to "/*" on the main server.
 *
 * The management server (port 8082) is a separate TomcatWebServer instance and does NOT
 * share this filter chain — no defensive guard needed for actuator paths.
 */
@Bean
public FilterRegistrationBean<PublicApiAllowlistFilter> publicApiAllowlistFilterRegistration(
    PublicApiAllowlistFilter filter) {
    final var registration = new FilterRegistrationBean<>(filter);
    registration.addUrlPatterns("/*");
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);      // runs before InternalApiFilter and MdcServletFilter
    registration.setName("publicApiAllowlistFilter");
    return registration;
}
```

**Order summary after this change:**

| Order                                 | Filter                     | URL pattern      | Port enforced                        |
|---------------------------------------|----------------------------|------------------|--------------------------------------|
| `HIGHEST_PRECEDENCE` (-2 147 483 648) | `PublicApiAllowlistFilter` | `/*`             | 8080 only                            |
| 1                                     | `InternalApiFilter`        | `/internalapi/*` | 8081 only (port check inside filter) |
| 2                                     | `MdcServletFilter`         | `/*`             | all                                  |

---

#### 7 — Update `ServletConfig.java` — complete reference implementation

For clarity, here is the complete `ServletConfig.java` after both Step 10.5 and this step:

```java
package se.inera.intyg.intygstjanst.config;

import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;
import se.inera.intyg.infra.security.filter.InternalApiFilter;
import se.inera.intyg.intygstjanst.logging.MdcServletFilter;

@Configuration
public class ServletConfig {

    // ── Servlets ────────────────────────────────────────────────────────────────

    /**
     * Overrides Spring Boot's default DispatcherServlet registration (which maps to "/").
     * Maps to the same three sub-paths as the current web.xml — all REST URLs unchanged.
     */
    @Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)
    public ServletRegistrationBean<DispatcherServlet> dispatcherServletRegistration(
        DispatcherServlet dispatcherServlet) {
        final var registration = new ServletRegistrationBean<>(dispatcherServlet,
            "/internalapi/*", "/api/*", "/resources/*");
        registration.setName("dispatcherServlet");
        registration.setLoadOnStartup(2);
        return registration;
    }

    /**
     * Registers CXFServlet at /* — exactly as in web.xml.
     * /internalapi/*, /api/*, /resources/* are more specific, so those go to DispatcherServlet.
     * Everything else (SOAP) falls through to CXF.
     */
    @Bean
    public ServletRegistrationBean<CXFServlet> cxfServletRegistration() {
        final var registration = new ServletRegistrationBean<>(new CXFServlet(), "/*");
        registration.setName("cxf");
        registration.setLoadOnStartup(1);
        return registration;
    }

    // ── Filters — ordered from highest to lowest precedence ─────────────────────

    /**
     * Port-8080 allowlist filter — MUST run first so it can block before any business logic.
     * The management server (8082) is a separate TomcatWebServer and never calls this chain.
     */
    @Bean
    public FilterRegistrationBean<PublicApiAllowlistFilter> publicApiAllowlistFilterRegistration(
        PublicApiAllowlistFilter filter) {
        final var registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("publicApiAllowlistFilter");
        return registration;
    }

    /**
     * Blocks /internalapi/* requests that arrive on the wrong port.
     * The InternalApiFilter itself checks request.getLocalPort().
     */
    @Bean
    public FilterRegistrationBean<InternalApiFilter> internalApiFilterRegistration(
        InternalApiFilter internalApiFilter) {
        final var registration = new FilterRegistrationBean<>(internalApiFilter);
        registration.addUrlPatterns("/internalapi/*");
        registration.setOrder(1);
        return registration;
    }

    /**
     * Populates MDC with session/trace/span IDs for every request.
     */
    @Bean
    public FilterRegistrationBean<MdcServletFilter> mdcServletFilterRegistration(
        MdcServletFilter mdcServletFilter) {
        final var registration = new FilterRegistrationBean<>(mdcServletFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(2);
        return registration;
    }
}
```

---

### Port / Actuator isolation guarantee

Spring Boot's `management.server.port` property causes Spring Boot to start a **second, completely independent
`TomcatWebServer`** (created by `ManagementContextConfiguration`). This second server has its own `ServletContext`,
its own filter chain, and does **not** share the `FilterRegistrationBean`s registered in the main application context.
Therefore `PublicApiAllowlistFilter` is never invoked for actuator requests on port 8082 — even without the
`localPort != publicPort` guard (which is kept as a belt-and-braces defence).

```
Port 8080 (main TomcatWebServer)
  FilterChain: PublicApiAllowlistFilter → InternalApiFilter → MdcServletFilter → …
  Servlets:    CXFServlet (/*), DispatcherServlet (/internalapi/*, /api/*, /resources/*)

Port 8081 (additional Connector on same TomcatWebServer — added by TomcatConfig)
  FilterChain: PublicApiAllowlistFilter (passes through — localPort≠8080)
             → InternalApiFilter (enforces internal-port-only)
             → MdcServletFilter → …
  Servlets:    same as 8080 — but InternalApiFilter blocks /internalapi/* from 8080 clients

Port 8082 (separate management TomcatWebServer — Spring Boot built-in)
  FilterChain: Spring Boot management filters only — PublicApiAllowlistFilter NOT present
  Servlets:    Spring Boot Actuator WebMVC dispatcher
```

---

### Configuration reference table

| Property                                    | Default                                        | Description                                                        |
|---------------------------------------------|------------------------------------------------|--------------------------------------------------------------------|
| `server.port`                               | `8080`                                         | Main HTTP port (SOAP + public REST)                                |
| `internal.api.port`                         | `8081`                                         | Additional Tomcat connector (internal REST)                        |
| `management.server.port`                    | `8082`                                         | Actuator port — separate management web server                     |
| `public.api.allowlist.prefixes`             | `/api/,/resources/,/error,/actuator,/`         | Comma-separated path prefixes allowed on port 8080                 |
| `management.endpoints.web.exposure.include` | `health,info`                                  | Actuator endpoints to expose                                       |
| `management.endpoint.health.probes.enabled` | `true`                                         | Enables `/actuator/health/liveness` + `/actuator/health/readiness` |
| `server.servlet.context-path`               | `/inera-certificate`                           | Context path — replaces `web.xml` display-name convention          |
| `logging.config`                            | `${logback.file:classpath:logback-spring.xml}` | Logback config — overridable via `-Dlogback.file=…`                |

---

### Verify

```bash
./gradlew clean build     # Compiles, all tests pass
# NOTE: Still running under Gretty for this step
./gradlew appRun          # Gretty still works (ignores Spring Boot properties and filter beans)
```

After switching to `bootRun` in Step 10.9/10.10:

```bash
# Port 8080 — SOAP (should return WSDL)
curl "http://localhost:8080/inera-certificate/get-recipients-for-certificate/v1.1?wsdl"

# Port 8080 — public REST (should return 200)
curl "http://localhost:8080/inera-certificate/api/send-message-to-care/ping"

# Port 8080 — internal REST on wrong port (should return 403 from PublicApiAllowlistFilter)
curl "http://localhost:8080/inera-certificate/internalapi/v1/certificatetexts"

# Port 8081 — internal REST (should return 200)
curl "http://localhost:8081/inera-certificate/internalapi/v1/certificatetexts"

# Port 8082 — actuator health (should return 200 with UP status)
curl "http://localhost:8082/actuator/health"

# Port 8082 — readiness probe (Kubernetes-style)
curl "http://localhost:8082/actuator/health/readiness"
```

---

## Step 10.8 — Remove `web.xml`, `version.jsp`, `webapp/` Directory

**What:** Delete the `web.xml` and `version.jsp` files. Under Spring Boot, there is no `web.xml` — everything is configured
via Java config (Steps 10.4–10.5) and `IntygstjanstApplication`.

**Changes:**

1. **Delete** `web/src/main/webapp/WEB-INF/web.xml`
2. **Delete** `web/src/main/webapp/version.jsp`
3. **Delete** `web/src/main/webapp/` directory (if empty after above deletions)

4. **Create a version info endpoint** (optional, replaces `version.jsp`):

   ```java
   package se.inera.intyg.intygstjanst.web.integration;

   import org.springframework.beans.factory.annotation.Value;
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.RestController;
   import java.util.Map;

   @RestController
   public class VersionController {

       @Value("${project.version:unknown}")
       private String version;

       @Value("${buildNumber:unknown}")
       private String buildNumber;

       @Value("${buildTime:unknown}")
       private String buildTime;

       @GetMapping("/version")
       public Map<String, String> getVersion() {
           return Map.of(
               "version", version,
               "buildNumber", buildNumber,
               "buildTime", buildTime
           );
       }
   }
   ```

5. **`ApplicationConfig.java`** — Remove `@PropertySource` annotations. Under Spring Boot, `application.properties` is loaded
   automatically. The dev config file is handled via `application-dev.properties` and `spring.config.import` (from Step 10.7).

   ```java
   @Configuration
   @EnableCaching
   @EnableTransactionManagement
   @EnableAspectJAutoProxy
   // REMOVED: @DependsOn("transactionManager")
   // REMOVED: @PropertySource("classpath:application.properties")
   // REMOVED: @PropertySource(ignoreResourceNotFound = true, value = "file:${dev.config.file}")
   // REMOVED: @ComponentScan (moved to IntygstjanstApplication)
   public class ApplicationConfig implements TransactionManagementConfigurer {
       // ...existing code...
   }
   ```

6. **`LogbackConfiguratorContextListener.java`** — This class is no longer needed. Spring Boot handles logback configuration
   natively via `logging.config` property. However, **don't delete it yet** — it's harmless if not registered, and can be
   cleaned up later.

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
```

**Note:** After this step, `./gradlew appRun` (Gretty) will NO LONGER work because `web.xml` is deleted. The app can only start
via `./gradlew bootRun` going forward.

---

## Step 10.9 — Switch from `war`/Gretty Plugin to Spring Boot `jar`

**What:** Remove the `war` plugin and Gretty configuration. Configure the project to produce a Spring Boot executable JAR.

**Changes:**

1. **`build.gradle` (root)** — Remove the Gretty plugin declaration:
   ```groovy
   plugins {
       // ...existing plugins...
       // REMOVED: id "org.gretty" version "4.1.10" apply false
   }
   ```

2. **`web/build.gradle`** — Major changes:
   ```groovy
   // REMOVED: apply plugin: 'org.gretty'
   // REMOVED: apply plugin: 'war'
   
   // KEEP: apply plugin: 'org.cyclonedx.bom'
   // ALREADY ADDED in 10.1: apply plugin: 'org.springframework.boot'
   
   // REMOVE: entire gretty { ... } block
   ```

3. **`web/build.gradle`** — Change `jakarta.servlet:jakarta.servlet-api` from `testRuntimeOnly` to `compileOnly`
   (or remove it entirely — Spring Boot's embedded Tomcat provides it):
   ```groovy
   // REMOVE this line:
   // testRuntimeOnly "jakarta.servlet:jakarta.servlet-api"
   // Spring Boot starter-web already provides the servlet API
   ```

4. **`web/build.gradle`** — The `spring-boot-starter-web` added in Step 10.1 already provides:
    - Embedded Tomcat
    - Spring MVC
    - Jackson

   Remove explicit dependencies that are now provided by the starter (verify with `./gradlew dependencies` first):
   ```groovy
   // These may become redundant (verify):
   // implementation "org.springframework:spring-webmvc"  ← provided by starter-web
   // But keep them if other modules depend on them via project(':intygstjanst-web')
   ```

5. **`web/build.gradle`** — Configure `bootJar` task:
   ```groovy
   bootJar {
       archiveBaseName.set('intygstjanst')
   }
   ```

6. **`web/build.gradle`** — Disable the plain `jar` task (Spring Boot convention):
   ```groovy
   jar {
       enabled = false
   }
   ```

   **Wait — multi-module consideration:** Other subprojects (`persistence`, `infra`, `logging`, `integration-*`) produce JARs
   consumed by `web`. Only the `web` module should have `bootJar`. The subprojects should continue producing regular JARs.
   This is the default behavior — the Spring Boot plugin is only applied to `web/build.gradle`.

7. **Delete** `web/tomcat-gretty.xml` — No longer needed.

**Verify:**

```bash
./gradlew clean build                    # Compiles, all tests pass
./gradlew :intygstjanst-web:bootJar      # Produces executable JAR
ls web/build/libs/                       # Should show intygstjanst-*.jar
```

---

## Step 10.10 — Final Verification

**What:** Start the application with `bootRun` and verify all endpoints work.

**Start the application:**

```bash
./gradlew :intygstjanst-web:bootRun \
  --args='--spring.profiles.active=dev,bootstrap,testability-api,caching-enabled,it-fk-stub' \
  -Dlogback.file=devops/dev/config/logback-spring.xml \
  -Dapplication.dir=devops/dev \
  -Drecipient.config.file=devops/dev/config/recipients-dev.json \
  -Ddev.config.file=devops/dev/config/application-dev.properties
```

**Alternatively,** configure `bootRun` in `web/build.gradle`:

```groovy
bootRun {
    def applicationDir = "${rootProject.projectDir}/devops/dev"

    jvmArgs = [
            "-Dspring.profiles.active=dev,bootstrap,testability-api,caching-enabled,it-fk-stub",
            "-Dlogback.file=${applicationDir}/config/logback-spring.xml",
            "-Djava.awt.headless=true",
            "-Dfile.encoding=UTF-8",
            "-Dapplication.dir=${applicationDir}",
            "-Drecipient.config.file=${applicationDir}/config/recipients-dev.json",
            "-Ddev.config.file=${applicationDir}/config/application-dev.properties",
            "-Xmx512M"
    ]
}
```

**Verification checklist:**

| Check                         | How                                                                                      | Expected                                    |
|-------------------------------|------------------------------------------------------------------------------------------|---------------------------------------------|
| Application starts            | `./gradlew bootRun` — no exceptions                                                      | Started in ~X seconds                       |
| Spring Boot banner visible    | Console output                                                                           | Spring Boot version printed                 |
| Context path correct          | `curl http://localhost:8080/inera-certificate/version`                                   | Version JSON response                       |
| REST endpoints (internal API) | `curl http://localhost:8181/inera-certificate/internalapi/intygInfo/...`                 | Valid response via internal port            |
| REST endpoints (API)          | `curl http://localhost:8080/inera-certificate/api/send-message-to-care/ping`             | Valid response                              |
| SOAP endpoints                | SOAP UI / curl to `http://localhost:8080/inera-certificate/get-certificate-se/v2.0?wsdl` | WSDL response (same URL as before!)         |
| CXF servlet at `/*`           | Check CXF bus logging on startup                                                         | Endpoints published at same paths as before |
| InternalApiFilter works       | `curl http://localhost:8080/inera-certificate/internalapi/...`                           | 403 Forbidden (wrong port)                  |
| MdcServletFilter works        | Check logs for trace/session IDs                                                         | MDC values present in log output            |
| JMS connected                 | Check logs for ActiveMQ connection                                                       | JMS listener containers started             |
| Database connected            | Check logs for HikariCP pool                                                             | Connection pool initialized                 |
| Liquibase ran                 | Check logs for Liquibase                                                                 | Changelog executed successfully             |
| All tests pass                | `./gradlew test`                                                                         | BUILD SUCCESSFUL                            |

---

## Risk Register

| #  | Risk                                                                              | Impact | Mitigation                                                                                                                                                                                                                                         |
|----|-----------------------------------------------------------------------------------|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1  | CXF + Spring Boot embedded Tomcat compatibility                                   | High   | CXF's `SpringBus` is already a Spring bean. `CXFServlet` works with `ServletRegistrationBean`. Well-documented pattern.                                                                                                                            |
| 2  | DispatcherServlet override — overriding Spring Boot's default registration        | Medium | We use `DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME` which is the documented way to replace the default mapping. Test thoroughly.                                                                                                            |
| 3  | Dual-port Tomcat under Spring Boot                                                | Medium | `WebServerFactoryCustomizer` with additional connector is standard Spring Boot pattern.                                                                                                                                                            |
| 4  | `@EnableWebMvc` removal changes behavior                                          | Medium | Test JSON serialization thoroughly after Step 10.6. If issues, keep `@EnableWebMvc` and troubleshoot later.                                                                                                                                        |
| 5  | BOM version conflicts                                                             | Medium | Run `./gradlew dependencies` after Step 10.1 and compare key library versions before and after.                                                                                                                                                    |
| 6  | `@ComponentScan` overlap between `ApplicationConfig` and `@SpringBootApplication` | Low    | Spring handles duplicate scans. Move all scan config to one place.                                                                                                                                                                                 |
| 7  | Logback configuration under Spring Boot                                           | Low    | Create `logback-spring.xml` on classpath. Spring Boot auto-detects it. Fall back to `logging.config` property.                                                                                                                                     |
| 8  | `persistence.xml` conflicts with Spring Boot                                      | Low    | `persistence.xml` is kept in Step 10 and removed in Step 11. `HibernateJpaAutoConfiguration` is excluded.                                                                                                                                          |
| 9  | `PublicApiAllowlistFilter` blocks legitimate SOAP paths on port 8080              | High   | Default `public.api.allowlist.prefixes` includes `/` as last prefix, which matches all paths. Only add strict prefixes if you intentionally want to block unknown SOAP paths. Always smoke-test SOAP endpoints after enabling.                     |
| 10 | Actuator exposed without authentication on port 8082                              | Medium | Port 8082 must be firewalled / not exposed externally. Spring Boot Actuator has no authentication by default. Add `management.endpoint.health.show-details=when-authorized` or Spring Security on the management context for production hardening. |
| 11 | `public.api.allowlist.prefixes` misconfiguration blocks internal port (8081)      | Low    | `PublicApiAllowlistFilter` checks `localPort == publicPort (8080)` first and passes through all other ports. The 8081 connector is on the same `TomcatWebServer` instance, so the filter runs — but the port guard ensures it is a no-op for 8081. |

---

## Rollback Plan

If Step 10 causes issues:

1. **Git revert** the commit(s) for Steps 10.7–10.9 to restore `web.xml`, `war` plugin, and Gretty.
2. Steps 10.1–10.6 are safe to keep — they don't affect the WAR deployment.
3. The application will run under Gretty again while issues are investigated.

---

## Summary: What Changes at Each Sub-step

| Step   | Build Type | Runs Under      | CXF Path | DispatcherServlet Paths                    | web.xml | Ports                                                                   |
|--------|------------|-----------------|----------|--------------------------------------------|---------|-------------------------------------------------------------------------|
| Before | WAR        | Gretty/Tomcat   | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ✅ Yes   | 8080+8081 (Gretty)                                                      |
| 10.1   | WAR        | Gretty/Tomcat   | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ✅ Yes   | 8080+8081 (Gretty)                                                      |
| 10.2   | WAR        | Gretty/Tomcat   | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ✅ Yes   | 8080+8081 (Gretty)                                                      |
| 10.3   | WAR        | Gretty/Tomcat   | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ✅ Yes   | 8080+8081 (Gretty)                                                      |
| 10.4   | WAR        | Gretty/Tomcat   | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ✅ Yes   | 8080+8081 (Gretty)                                                      |
| 10.5   | WAR        | Gretty/Tomcat   | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ✅ Yes   | 8080+8081 (Gretty)                                                      |
| 10.6   | WAR        | Gretty/Tomcat   | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ✅ Yes   | 8080+8081 (Gretty)                                                      |
| 10.7   | WAR        | Gretty/Tomcat   | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ✅ Yes   | 8080+8081 (Gretty) — Spring Boot properties+classes added but inactive  |
| 10.8   | JAR        | **Neither**     | N/A      | N/A                                        | ❌ No    | N/A (transition)                                                        |
| 10.9   | JAR        | **Spring Boot** | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ❌ No    | **8080** (public/SOAP) + **8081** (internal REST) + **8082** (Actuator) |
| 10.10  | JAR        | Spring Boot     | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ❌ No    | 8080 + 8081 + 8082                                                      |

**All URLs are preserved identically** — both SOAP and REST. CXF stays at `/*`, DispatcherServlet stays at its three specific
sub-paths. The context path `/inera-certificate` is set via `server.servlet.context-path`.

**Note:** Steps 10.8 and 10.9 MUST be done in the same commit or deployed together, because deleting `web.xml` (10.8) breaks
Gretty, and switching to `bootJar` (10.9) requires no `web.xml`. **These two steps form an atomic unit.**

---

## Design Note: Why CXF at `/*` Works Under Spring Boot

A common misconception is that Spring Boot's `DispatcherServlet` *must* be at `/` or `/*`, and therefore CXF can't also be at `/*`.
In reality, the Servlet specification allows multiple servlets with overlapping mappings — **the most specific mapping wins**.

This is exactly what `web.xml` has been doing all along:

- `DispatcherServlet` → `/internalapi/*`, `/api/*`, `/resources/*` (more specific)
- `CXFServlet` → `/*` (catch-all for everything else)

Under Spring Boot, we replicate this by:

1. Overriding the `DispatcherServletRegistrationBean` to map to those three sub-paths instead of `/`.
2. Registering `CXFServlet` at `/*` via a separate `ServletRegistrationBean`.

The result is **bit-for-bit identical URL routing** to the current WAR setup. No SOAP URLs change, no REST URLs change,
no infrastructure or consumer changes needed.

