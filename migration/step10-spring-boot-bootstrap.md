# Step 10 — Spring Boot Bootstrap (Detailed Incremental Plan)

## Progress Tracker

> Update the Status column as each step is completed.
> Statuses: `⬜ TODO` | `🔄 IN PROGRESS` | `✅ DONE` | `⏭️ SKIPPED`

| Step      | Description                                                     | Status | Commit/PR | Verified | Notes |
|-----------|-----------------------------------------------------------------|--------|-----------|----------|-------|
| **10.1**  | Add Spring Boot Gradle plugin (no code changes)                 | ✅ DONE |           | ✅        |       |
| **10.2**  | Create `IntygstjanstApplication.java` main class                | ✅ DONE |           | ✅        |       |
| **10.3**  | Adapt `ApplicationConfig` for Spring Boot coexistence           | ⬜ TODO |           |          |       |
| **10.4**  | Register CXF servlet via `ServletRegistrationBean`              | ⬜ TODO |           |          |       |
| **10.5**  | Register filters via `FilterRegistrationBean`                   | ⬜ TODO |           |          |       |
| **10.6**  | Adapt `WebMvcConfig` — remove `@EnableWebMvc`                   | ⬜ TODO |           |          |       |
| **10.7**  | Move/adapt `application.properties` for Spring Boot conventions | ⬜ TODO |           |          |       |
| **10.8**  | Remove `web.xml`, `version.jsp`, `webapp/` directory            | ⬜ TODO |           |          |       |
| **10.9**  | Switch from `war`/Gretty plugin to Spring Boot `jar`            | ⬜ TODO |           |          |       |
| **10.10** | Final verification — `./gradlew bootRun` + `./gradlew test`     | ⬜ TODO |           |          |       |

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
The existing properties (`db.*`, `hibernate.*`, `activemq.*`, etc.) continue to work because `JpaConfigBase`, `JmsConfig`,
etc. use `@Value` to read them.

**Changes:**

1. **`web/src/main/resources/application.properties`** — Add Spring Boot properties at the top:

   ```properties
   # Spring Boot server configuration
   server.port=${dev.http.port:8080}
   server.servlet.context-path=/inera-certificate
   
   # Logback configuration (replaces LogbackConfiguratorContextListener)
   logging.config=${logback.file:classpath:logback-spring.xml}
   
   # Disable Spring Boot auto-config for things we configure manually
   spring.autoconfigure.exclude=\
     org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
     org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,\
     org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration,\
     org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration,\
     org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
   ```

   **Note:** The `spring.autoconfigure.exclude` in `application.properties` is redundant with `@SpringBootApplication(exclude = ...)`
   but serves as a safety net and documentation. You can choose one or the other — using `application.properties` is preferred
   because it's more visible and doesn't require code changes in Step 11 when you enable auto-config.

2. **Create** `web/src/main/resources/logback-spring.xml` — A Spring Boot-compatible logback config:

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

   This is essentially the same as `devops/dev/config/logback-spring.xml` but placed on the classpath so Spring Boot finds it
   automatically.

3. **Create** `web/src/main/resources/application-dev.properties` — Spring Boot profile-specific properties for dev:

   ```properties
   # Dev profile overrides — loaded automatically by Spring Boot when profile 'dev' is active
   # Import the external dev config file
   spring.config.import=optional:file:${application.dir}/config/application-dev.properties
   ```

   **Alternative:** Instead of `spring.config.import`, use `spring.config.additional-location` as a JVM arg. This is simpler
   but less self-documenting.

4. **Dual-port Tomcat configuration** — The current setup uses two Tomcat connectors (main port + internal API port).
   Under Spring Boot embedded Tomcat, this requires a `WebServerFactoryCustomizer`:

   **Create** `web/src/main/java/se/inera/intyg/intygstjanst/config/TomcatConfig.java`:

   ```java
   package se.inera.intyg.intygstjanst.config;

   import org.apache.catalina.connector.Connector;
   import org.springframework.beans.factory.annotation.Value;
   import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
   import org.springframework.boot.web.server.WebServerFactoryCustomizer;
   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.Configuration;

   @Configuration
   public class TomcatConfig {

       @Value("${internal.api.port:8081}")
       private int internalApiPort;

       @Bean
       public WebServerFactoryCustomizer<TomcatServletWebServerFactory> multiPortCustomizer() {
           return factory -> {
               final var connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
               connector.setPort(internalApiPort);
               factory.addAdditionalTomcatConnectors(connector);
           };
       }
   }
   ```

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
# NOTE: Still running under Gretty for now
./gradlew appRun          # Gretty still works (ignores Spring Boot properties)
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

| # | Risk                                                                              | Impact | Mitigation                                                                                                                              |
|---|-----------------------------------------------------------------------------------|--------|-----------------------------------------------------------------------------------------------------------------------------------------|
| 1 | CXF + Spring Boot embedded Tomcat compatibility                                   | High   | CXF's `SpringBus` is already a Spring bean. `CXFServlet` works with `ServletRegistrationBean`. Well-documented pattern.                 |
| 2 | DispatcherServlet override — overriding Spring Boot's default registration        | Medium | We use `DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME` which is the documented way to replace the default mapping. Test thoroughly. |
| 3 | Dual-port Tomcat under Spring Boot                                                | Medium | `WebServerFactoryCustomizer` with additional connector is standard Spring Boot pattern.                                                 |
| 4 | `@EnableWebMvc` removal changes behavior                                          | Medium | Test JSON serialization thoroughly after Step 10.6. If issues, keep `@EnableWebMvc` and troubleshoot later.                             |
| 5 | BOM version conflicts                                                             | Medium | Run `./gradlew dependencies` after Step 10.1 and compare key library versions before and after.                                         |
| 6 | `@ComponentScan` overlap between `ApplicationConfig` and `@SpringBootApplication` | Low    | Spring handles duplicate scans. Move all scan config to one place.                                                                      |
| 7 | Logback configuration under Spring Boot                                           | Low    | Create `logback-spring.xml` on classpath. Spring Boot auto-detects it. Fall back to `logging.config` property.                          |
| 8 | `persistence.xml` conflicts with Spring Boot                                      | Low    | `persistence.xml` is kept in Step 10 and removed in Step 11. `HibernateJpaAutoConfiguration` is excluded.                               |

---

## Rollback Plan

If Step 10 causes issues:

1. **Git revert** the commit(s) for Steps 10.7–10.9 to restore `web.xml`, `war` plugin, and Gretty.
2. Steps 10.1–10.6 are safe to keep — they don't affect the WAR deployment.
3. The application will run under Gretty again while issues are investigated.

---

## Summary: What Changes at Each Sub-step

| Step   | Build Type | Runs Under      | CXF Path | DispatcherServlet Paths                    | web.xml |
|--------|------------|-----------------|----------|--------------------------------------------|---------|
| Before | WAR        | Gretty/Tomcat   | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ✅ Yes   |
| 10.1   | WAR        | Gretty/Tomcat   | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ✅ Yes   |
| 10.2   | WAR        | Gretty/Tomcat   | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ✅ Yes   |
| 10.3   | WAR        | Gretty/Tomcat   | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ✅ Yes   |
| 10.4   | WAR        | Gretty/Tomcat   | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ✅ Yes   |
| 10.5   | WAR        | Gretty/Tomcat   | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ✅ Yes   |
| 10.6   | WAR        | Gretty/Tomcat   | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ✅ Yes   |
| 10.7   | WAR        | Gretty/Tomcat   | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ✅ Yes   |
| 10.8   | JAR        | **Neither**     | N/A      | N/A                                        | ❌ No    |
| 10.9   | JAR        | **Spring Boot** | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ❌ No    |
| 10.10  | JAR        | Spring Boot     | `/*`     | `/internalapi/*`, `/api/*`, `/resources/*` | ❌ No    |

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

