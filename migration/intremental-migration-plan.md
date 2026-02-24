# Incremental Migration Plan — Keeping the Application Working at Every Step

Based on the `first-migration-scope.md`, here is a reordering of the work into **small, independently verifiable increments**. After each
step, the application must: **compile ✅, pass all tests ✅, start ✅, and be deployable ✅**.

The key insight is that the migration document's 3 phases bundle too much together. Phase 1 especially (Spring Boot bootstrap + XML
elimination + auto-config replacements) is a big-bang change. Below, I break it into ~14 atomic steps.

---

## Step 1 — Migrate all tests to JUnit 5 *(~100 files, zero runtime impact)*

**Why first:** This is the only work item with **zero risk to the running application**. It only touches test code. It removes `junit:junit`
and `junit-vintage-engine` from both `persistence/build.gradle` and `web/build.gradle`. You can do it file-by-file, running `./gradlew test`
after each batch.

**Verify:** `./gradlew test` — all tests pass. Application starts normally (no production code changed).

---

## Step 2 — Inline `se.inera.intyg.infra` DTOs and utilities *(add code, don't remove deps yet)*

Copy the required classes from infra into the project **without removing the infra dependencies yet**. Change imports in your code to point
to the local copies. This is purely additive — the infra deps are still on the classpath but unused.

Do this for the low-risk, DTO-only modules first:

- `intyginfo` → copy `IntygInfoEvent`, `ItIntygInfo`, `IntygInfoEventType` (~2 files using it)
- `message` → copy `MessageFromIT` (~1 file)
- `testcertificate` → copy `TestCertificateEraseRequest`, `TestCertificateEraseResult` (~3 files)
- `certificate` → copy DTOs: `CertificateListEntry`, `CertificateListRequest`, etc. (~11 files)

**Verify:** `./gradlew test` + start the application. Everything still works, just using local copies.

---

## Step 3 — Inline `monitoring` *(~34 files)*

Copy `@PrometheusTimeMethod`, its AOP aspect, `MonitoringConfiguration`, and replace the `LogMarkers` import from infra with the local
`LogMarkers` already in the `logging` module. Remove `@Import(MonitoringConfiguration.class)` from `ApplicationConfig` and replace with
local config.

**Verify:** `./gradlew test` + start. Monitoring annotations still work.

---

## Step 4 — Inline `sjukfall-engine` *(~41 files)*

Copy the sjukfall DTOs and `SjukfallEngineServiceImpl` into the project. Update imports. Remove the XML bean declaration for
`sjukfallEngineService` in `application-context.xml` and make the local copy a `@Service`/`@Component`.

**Verify:** `./gradlew test` + start. Sick leave calculations still work.

---

## Step 5 — Inline `security-filter` *(1 file)*

Create a local `InternalApiFilter` as a simple `OncePerRequestFilter`. Replace the import in `application-context.xml`.

**Verify:** `./gradlew test` + start. Internal API filtering still works.

---

## Step 6 — Replace HSA and PU integrations with REST clients *(~4 files)*

Replace `hsa-integration-api` + `hsa-integration-intyg-proxy-service` and `pu-integration-api` + `pu-integration-intyg-proxy-service` with
direct REST calls (using Spring `RestTemplate` for now — not `RestClient` yet, since you're not on Spring Boot yet). Define local DTOs for
the responses.

**Verify:** `./gradlew test` + start. HSA/PU lookups still work (now via REST instead of SOAP).

---

## Step 7 — Remove all `se.inera.intyg.infra` dependencies from `build.gradle`

Now that all infra code is either inlined or replaced, remove all 11 infra dependency lines from `web/build.gradle` and the `infraVersion`
property from `build.gradle`.

**Verify:** `./gradlew build` — compiles, all tests pass. `grep -r "se.inera.intyg.infra" web/build.gradle` returns nothing. Application
starts.

---

## Step 8 — Convert JAX-RS controllers to Spring MVC *(~15 files)*

Convert all `@Path`/`@GET`/`@POST` controllers to `@RestController`/`@GetMapping`/`@PostMapping`. Replace `Response` with
`ResponseEntity<T>`. Keep the CXF `jaxrs:server` XML temporarily — register the new Spring MVC controllers alongside (they'll be picked up
by the existing Spring component scan).

Once all controllers are converted:

- Remove `jaxrs-context.xml` and its `<import>` from `application-context.xml`
- Remove `jakarta.ws.rs:jakarta.ws.rs-api` and `com.fasterxml.jackson.jakarta.rs:jackson-jakarta-rs-json-provider` from `build.gradle`

**Note:** The current setup uses CXF's servlet mapped to `/*`, and the JAX-RS controllers are registered under `/internalapi` and `/api` via
CXF. After conversion to Spring MVC `@RestController`, these controllers will be served by Spring's `DispatcherServlet` instead. You need to
ensure the CXF servlet path is narrowed (e.g., to `/services/*`) so it doesn't clash. This can be done by updating `web.xml`'s CXF servlet
mapping before the full Spring Boot switch.

**Verify:** `./gradlew test` + start. Hit every REST endpoint — same responses.

---

## Step 9 — Convert XML bean configuration to Java *(except web.xml)*

Convert each XML file to Java `@Configuration`:

1. **`application-context-ws.xml`** → `CxfEndpointConfig.java` (all `jaxws:endpoint` beans registered programmatically)
2. **`application-context-ws-stub.xml`** → `CxfStubConfig.java` with `@Profile("it-fk-stub")`
3. **`application-context.xml`** → The remaining bean declarations move into `ApplicationConfig.java` or new config classes. The `<import>`
   statements for `common-config.xml`, `module-config.xml`, `it-module-cxf-servlet.xml` become `@ImportResource` on a config class.
4. **`META-INF/persistence.xml`** → Not yet removed (wait for Spring Boot auto-config). But you can start using `@EntityScan` annotations in
   `JpaConfig`.
5. **`test-application-context.xml`** → Replace with `@Configuration` inner classes in tests.

Remove `application-context.xml`, `jaxrs-context.xml` (already gone), `application-context-ws.xml`, `application-context-ws-stub.xml`,
`test-application-context.xml`.

Update `web.xml`'s `contextConfigLocation` to point to the Java config class instead of the XML file.

**Verify:** `./gradlew test` + start. All SOAP endpoints respond. All REST endpoints respond.

---

## Step 10 — Spring Boot bootstrap *(the big switch)*

This is the most critical step. Do it in one focused commit:

1. Add `org.springframework.boot` plugin to `build.gradle` / `web/build.gradle`.
2. Switch from `war` plugin to `jar` (remove Gretty).
3. Create `IntygstjanstApplication.java` with `@SpringBootApplication`.
4. Add Spring Boot starters: `spring-boot-starter-web`.
5. Remove `web.xml` — register `MdcServletFilter` and `InternalApiFilter` as `FilterRegistrationBean`s. Register CXF servlet via
   `ServletRegistrationBean`.
6. Configure CXF servlet path (e.g., `/services/*`) to coexist with `DispatcherServlet`.

**Do NOT change JPA, JMS, metrics, or Redis config in this step.** Keep `JpaConfigBase`, `JmsConfig`, etc. as-is. They will still work under
Spring Boot — Spring Boot auto-config backs off when it finds existing beans.

**Verify:** `./gradlew bootRun` — application starts. All endpoints respond. `./gradlew test` passes.

---

## Step 11 — Replace JPA manual config with Spring Boot auto-configuration

1. Remove `JpaConfigBase`, `JpaConfig`, `JpaConstants`, `persistence.xml`.
2. Add `spring-boot-starter-data-jpa` (if not already pulled transitively).
3. Add `@EntityScan` and `@EnableJpaRepositories` on the main app class.
4. Move DB properties to `application.properties` using Spring Boot conventions (`spring.datasource.*`, `spring.jpa.*`,
   `spring.liquibase.*`).
5. Remove explicit `HikariCP`, `hibernate-core`, `hibernate-hikaricp` dependencies from `persistence/build.gradle` (now provided by
   starter).

**Verify:** `./gradlew bootRun` — starts, connects to DB, Liquibase runs. `./gradlew test` passes.

---

## Step 12 — Replace JMS manual config with Spring Boot auto-configuration

1. Remove `JmsConfig`'s manual `ActiveMQConnectionFactory`, `PooledConnectionFactory`, `JmsTemplate`, `JmsTransactionManager`.
2. Add `spring-boot-starter-activemq`.
3. Move properties to `application.properties` (`spring.activemq.*`).
4. Keep custom `Queue` beans and `JmsListenerContainerFactory` customization if needed.

**Verify:** `./gradlew bootRun` — JMS listeners start. `./gradlew test` passes.

---

## Step 13 — Replace Prometheus with Spring Boot Actuator + Micrometer

1. Remove `io.prometheus:simpleclient_servlet`.
2. Add `spring-boot-starter-actuator` + `micrometer-registry-prometheus`.
3. Configure `management.endpoints.web.exposure.include=health,info,prometheus`.
4. Replace `@PrometheusTimeMethod` with Micrometer `@Timed` where appropriate.

**Verify:** `/actuator/health` responds. `/actuator/prometheus` serves metrics. `./gradlew test` passes.

---

## Step 14 — Replace Redis/caching manual config + Spring Boot ECS logging + Dockerfile

1. **Redis:** Add `spring-boot-starter-data-redis`. Remove manual Jedis/Redis config. Configure via `spring.data.redis.*`.
2. **Logging:** Remove `logback-ecs-encoder`, `LogbackConfiguratorContextListener`, `logback-spring-base.xml`. Add
   `logging.structured.format.console=ecs` to `application.properties`.
3. **Dockerfile:** Change from `ADD *.war $CATALINA_HOME/webapps/` to a Spring Boot JAR-based image (e.g., `COPY build/libs/*.jar app.jar` +
   `ENTRYPOINT ["java", "-jar", "app.jar"]`).

**Verify:** Docker build + run. ECS JSON logs on stdout. Redis caching works. All endpoints respond.

---

## Summary: When Is the App Working?

| After Step | What You Can Verify      | App Broken? |
|------------|--------------------------|-------------|
| **1**      | All tests on JUnit 5     | ❌ No        |
| **2**      | Infra DTOs inlined       | ❌ No        |
| **3**      | Monitoring inlined       | ❌ No        |
| **4**      | Sjukfall-engine inlined  | ❌ No        |
| **5**      | Security filter inlined  | ❌ No        |
| **6**      | HSA/PU via REST          | ❌ No        |
| **7**      | All infra deps removed   | ❌ No        |
| **8**      | All REST on Spring MVC   | ❌ No        |
| **9**      | All XML config → Java    | ❌ No        |
| **10**     | **Spring Boot runs**     | ❌ No        |
| **11**     | JPA auto-configured      | ❌ No        |
| **12**     | JMS auto-configured      | ❌ No        |
| **13**     | Actuator/Micrometer live | ❌ No        |
| **14**     | Logging + Redis + Docker | ❌ No        |

**Every step is a deployable, verifiable checkpoint.** Steps 1–9 don't even change how the application runs (still WAR/Tomcat/Gretty). Step
10 is the actual Spring Boot switch, and it's small because all the preparation is done. Steps 11–14 are safe because Spring Boot
auto-config backs off gracefully when existing beans are present, so you swap one concern at a time.

---

## Highest Risk Step

**Step 10** (Spring Boot bootstrap) is the riskiest single step. To de-risk it further, consider doing a quick spike first: create a branch,
add the Spring Boot plugin, create the main class, and see if it starts — before doing steps 1–9. This validates CXF + Spring Boot
coexistence early (Risk #1 and #5 from the risk assessment). If it works, proceed with the incremental plan above. If not, you'll know what
to fix before investing in the rest.
