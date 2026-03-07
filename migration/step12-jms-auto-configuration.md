# Step 12 — Replace JMS Manual Config with Spring Boot Auto-Configuration (Detailed Incremental Plan)

## Progress Tracker

> Update the Status column as each step is completed.
> Statuses: `⬜ TODO` | `🔄 IN PROGRESS` | `✅ DONE` | `⏭️ SKIPPED`

| Step      | Description                                                                | Status  | Commit/PR | Verified | Notes |
|-----------|----------------------------------------------------------------------------|---------|-----------|----------|-------|
| **12.1**  | Add `spring-boot-starter-activemq` dependency                             | ✅ DONE | | ✅ | Starter on classpath (v3.5.10 via BOM); `compileJava` succeeds; auto-config still excluded |
| **12.2**  | Map `activemq.broker.*` properties to Spring Boot conventions              | ✅ DONE | | ✅ | spring.activemq.* and spring.jms.* added to application.properties; inert until 12.3; build + tests pass |
| **12.3**  | Remove auto-config exclusion for ActiveMQ                                  | ✅ DONE | | ✅ | Removed from @SpringBootApplication exclude and spring.autoconfigure.exclude; auto-config backs off due to manual ConnectionFactory bean; build + tests pass |
| **12.4**  | Remove manual `ConnectionFactory`, `JmsTransactionManager`, and `DestinationResolver` beans | ✅ DONE | | ✅ | Removed from JmsConfig; listener factory now injects auto-configured ConnectionFactory; build + tests pass |
| **12.5**  | Simplify `JmsListenerContainerFactory` to use auto-configured beans       | ✅ DONE | | ✅ | Removed custom bean from JmsConfig; added spring.jms.listener.session-transacted=true; @JmsListener uses auto-configured factory by default; build + tests pass |
| **12.6**  | Remove manual `JmsTemplate` beans — replace with auto-configured `JmsTemplate` | ⬜ TODO |           |          |       |
| **12.7**  | Update `JmsTemplate` consumers to use destination names instead of `Queue` beans | ⬜ TODO |           |          |       |
| **12.8**  | Remove `Queue` beans and `Receiver` bean from `JmsConfig`                 | ⬜ TODO |           |          |       |
| **12.9**  | Remove `JmsConfig` class and `@EnableJms`                                 | ⬜ TODO |           |          |       |
| **12.10** | Remove redundant explicit dependencies from `web/build.gradle`            | ⬜ TODO |           |          |       |
| **12.11** | Update persistence `TestConfig` — remove `ActiveMQAutoConfiguration` exclusion | ⬜ TODO |           |          |       |
| **12.12** | Final verification — `./gradlew bootRun` + `./gradlew test`               | ⬜ TODO |           |          |       |

**Deployment batches:**

- 🚀 **Batch 1:** Steps 12.1–12.2 (additive property mapping — manual config still active, Spring Boot auto-config still excluded)
- 🚀 **Batch 2:** Steps 12.3–12.5 (the actual switch — enable auto-config, remove manual connection infrastructure, simplify listener factory)
- 🚀 **Batch 3:** Steps 12.6–12.9 (remove manual `JmsTemplate` beans, `Queue` beans, `Receiver` bean, and delete `JmsConfig`)
- 🚀 **Batch 4:** Steps 12.10–12.12 (dependency cleanup, test config update, final verification)

---

## Pre-conditions — Verified Current State

Before planning, the following assumptions from the incremental migration plan were verified against the actual codebase:

| Assumption                                                                  | Verified? | Actual State                                                                                                                                                                                               |
|-----------------------------------------------------------------------------|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Step 11 is complete — JPA auto-configured via Spring Boot                  | ✅         | `JpaConfigBase`, `JpaConfig`, `JpaConstants`, `persistence.xml` removed. `@EntityScan` and `@EnableJpaRepositories` on `IntygstjanstApplication`. `spring.datasource.*` / `spring.jpa.*` / `spring.liquibase.*` properties drive JPA. |
| `JmsConfig` manually configures ConnectionFactory, JmsTemplate, TxManager   | ✅         | `JmsConfig.java` creates `PooledConnectionFactory(new ActiveMQConnectionFactory(...))`, `JmsTransactionManager`, two `JmsTemplate` beans, `JmsListenerContainerFactory`, `DestinationResolver`, three `Queue` beans, and a `Receiver` bean. Uses `@Value`-injected `activemq.broker.*` properties. |
| `@EnableJms` is on `JmsConfig`                                              | ✅         | `JmsConfig` is annotated with `@Configuration @EnableJms`.                                                                                                                                                  |
| Auto-config exclusion for ActiveMQ is set                                   | ✅         | Both `@SpringBootApplication(exclude={ActiveMQAutoConfiguration.class, ...})` and `application.properties` `spring.autoconfigure.exclude` list `ActiveMQAutoConfiguration`.                                  |
| JMS properties use `activemq.broker.*` prefix (not Spring Boot `spring.activemq.*`) | ✅  | `application.properties`: `activemq.broker.url`, `activemq.broker.username`, `activemq.broker.password`. Dev overrides in `application-dev.properties`.                                                     |
| Three queue names are configured via properties                              | ✅         | `activemq.destination.queue.name=certificate.queue`, `activemq.internal.notification.queue.name=internal.notification.queue`, `certificate.event.queue.name=intygstjanst.certificate.event.queue`.            |
| Two `JmsTemplate` beans exist with different default destinations           | ✅         | `jmsTemplate` (default, targets `destinationQueue`), `jmsCertificateEventTemplate` (targets `certificateEventQueue`).                                                                                        |
| `JmsTemplate` consumers use `Queue` beans for destination                   | ✅         | `StatisticsServiceImpl` injects `JmsTemplate` + `Queue destinationQueue`. `InternalNotificationServiceImpl` injects `JmsTemplate` + `@Qualifier("internalNotificationQueue") Queue`. `CertificateEventRedeliveryServiceImpl` injects `@Qualifier("jmsCertificateEventTemplate") JmsTemplate` (uses template's default destination). |
| One `@JmsListener` exists                                                   | ✅         | `CertificateEventListenerServiceImpl.processMessage()` has `@JmsListener(destination = "${certificate.event.queue.name}")`. Destination is resolved from property — does NOT reference a `Queue` bean.        |
| `Receiver` bean is only used for test/integration purposes                  | ✅         | `JmsConfig` declares `@Bean Receiver receiver()`. `Receiver` class consumes from `destinationQueue` for integration testing. It is in the `web.integration.test` package.                                    |
| `spring-boot-starter-activemq` not yet in dependencies                      | ✅         | Not present in `web/build.gradle`. Only `org.apache.activemq:activemq-spring` and `org.springframework:spring-jms` are declared explicitly.                                                                   |
| Connection pooling uses `activemq-pool` `PooledConnectionFactory`           | ✅         | `JmsConfig.connectionFactory()` returns `new PooledConnectionFactory(new ActiveMQConnectionFactory(...))`. The `PooledConnectionFactory` class is from `org.apache.activemq.pool` (bundled via `activemq-spring`). |
| `JmsListenerContainerFactory` has custom settings                           | ✅         | `JmsConfig.jmsListenerContainerFactory()`: `setSessionTransacted(true)`, `setTransactionManager(jmsTransactionManager)`, `setCacheLevelName("CACHE_CONSUMER")`, `setConcurrency("1-10")`, `setDestinationResolver(destinationResolver())`. |
| `JmsTemplate` beans set `sessionTransacted=true`                            | ✅         | Both templates created via `JmsConfig.template()` helper: `jmsTemplate.setSessionTransacted(true)`.                                                                                                          |

### Key Architectural Insight

The current JMS setup manually replicates what Spring Boot's `ActiveMQAutoConfiguration` + `JmsAutoConfiguration` provide out of the box:

```
Current (manual — JmsConfig)                  Spring Boot auto-config equivalent
────────────────────────────                  ──────────────────────────────────────
ActiveMQConnectionFactory (broker.url, user)  →  ActiveMQAutoConfiguration (spring.activemq.*)
PooledConnectionFactory wrapper               →  spring.activemq.pool.enabled=true (uses activemq-pool)
JmsTransactionManager bean                    →  Not auto-configured — but JmsTemplate can use sessionTransacted
JmsTemplate (default destination)             →  JmsAutoConfiguration creates a JmsTemplate bean
JmsListenerContainerFactory (custom config)   →  JmsAutoConfiguration + spring.jms.listener.* properties
@EnableJms                                    →  JmsAutoConfiguration enables @EnableJms automatically
DestinationResolver (DynamicDestinationResolver) →  Spring Boot default (DynamicDestinationResolver)
Queue beans (3x ActiveMQQueue)                →  Not needed — use destination name strings directly
Receiver bean (test helper)                   →  Move to test profile or standalone @Configuration
```

**The migration strategy is:**

1. **First**, add the Spring Boot `spring.activemq.*` / `spring.jms.*` properties alongside the existing `activemq.broker.*` properties (the manual config still reads the old ones).
2. **Then**, remove the auto-config exclusion so Spring Boot creates the `ConnectionFactory`. Remove the manual connection infrastructure beans (`ConnectionFactory`, `JmsTransactionManager`, `DestinationResolver`). The `JmsListenerContainerFactory` is simplified.
3. **Then**, remove manual `JmsTemplate` beans. Update consumers to either use the auto-configured `JmsTemplate` with destination name strings, or inject the auto-configured `JmsTemplate` with `@Qualifier` where needed.
4. **Finally**, delete the now-empty `JmsConfig`, remove redundant dependencies, and update test config.

This ensures the app is never in a broken state — the manual beans exist until auto-config takes over.

---

## Step 12.1 — Add `spring-boot-starter-activemq` Dependency

**What:** Add the Spring Boot ActiveMQ starter to `web/build.gradle`. This brings in `ActiveMQAutoConfiguration`, `JmsAutoConfiguration`, `activemq-client`, `spring-jms`, and connection pool support — but they remain inactive because the auto-config exclusion is still in place.

**Why safe:** The auto-configuration is excluded via `@SpringBootApplication(exclude={ActiveMQAutoConfiguration.class})` and `spring.autoconfigure.exclude` in `application.properties`. Adding the starter just puts the classes on the classpath without activating them. The existing manual `JmsConfig` beans continue to drive JMS.

**Changes:**

1. **`web/build.gradle`** — Add the starter dependency:
   ```groovy
   dependencies {
       implementation "org.springframework.boot:spring-boot-starter-activemq"
       // ...existing deps...
   }
   ```

   Since `web/build.gradle` already applies the `org.springframework.boot` plugin, the starter version is managed by the Spring Boot BOM automatically.

**Verify:**

```bash
./gradlew clean build                                    # Compiles, all tests pass
./gradlew :intygstjanst-web:dependencies --configuration runtimeClasspath | grep activemq  # Starter on classpath
```

**Risks:**

- Version conflict between explicitly declared `activemq-spring` and the version from the starter. Run `./gradlew :intygstjanst-web:dependencies` to check. Conflicts resolved in Step 12.10 when explicit deps are removed.

---

## Step 12.2 — Map `activemq.broker.*` Properties to Spring Boot Conventions

**What:** Add Spring Boot–conventional `spring.activemq.*` and `spring.jms.*` properties to `application.properties`, mapped from the existing `activemq.broker.*` values. Keep the old properties too — they're still read by `JmsConfig` (which is still active).

**Why safe:** Adding new properties that nobody reads yet has zero effect. The auto-config that would read them is still excluded.

**Changes:**

1. **`web/src/main/resources/application.properties`** — Add Spring Boot JMS properties below the existing JMS configuration section:

   ```properties
   # ============================================================
   # Spring Boot ActiveMQ configuration (Step 12)
   # Maps from the legacy activemq.broker.* properties to Spring Boot conventions.
   # ============================================================
   spring.activemq.broker-url=${activemq.broker.url}
   spring.activemq.user=${activemq.broker.username}
   spring.activemq.password=${activemq.broker.password}

   # Connection pooling — replaces manual PooledConnectionFactory
   spring.activemq.pool.enabled=true
   spring.activemq.pool.max-connections=8

   # ============================================================
   # Spring Boot JMS configuration (Step 12)
   # ============================================================
   spring.jms.listener.auto-startup=true
   spring.jms.listener.concurrency=1
   spring.jms.listener.max-concurrency=10
   spring.jms.cache.consumers=true
   spring.jms.template.default-destination=${activemq.destination.queue.name}
   ```

   **Key decisions:**

   - `spring.activemq.*` properties reference `${activemq.broker.*}` so all existing environment/profile overrides continue to work without duplication.
   - `spring.activemq.pool.enabled=true` — replaces the manual `PooledConnectionFactory` wrapper. Spring Boot will create a `PooledConnectionFactory` from the ActiveMQ pool library (already on classpath via `activemq-spring`).
   - `spring.activemq.pool.max-connections=8` — reasonable default. The current `PooledConnectionFactory` uses its default (which is 1 connection). Tune as needed.
   - `spring.jms.listener.concurrency=1` and `spring.jms.listener.max-concurrency=10` — matches `JmsConfig.setConcurrency("1-10")`.
   - `spring.jms.cache.consumers=true` — matches `JmsConfig.setCacheLevelName("CACHE_CONSUMER")`.
   - `spring.jms.template.default-destination` — sets the default destination on the auto-configured `JmsTemplate`, matching the current `jmsTemplate` bean's default destination.

2. **`devops/dev/config/application-dev.properties`** — No changes needed. The `activemq.broker.username` and `activemq.broker.password` defined there will be picked up by the `${activemq.broker.*}` references in the new `spring.activemq.*` properties.

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew bootRun         # Starts — still using manual JmsConfig
```

The new properties exist but are inert — the auto-configuration that reads them is still excluded.

---

## Step 12.3 — Remove Auto-Config Exclusion for ActiveMQ

**What:** Remove `ActiveMQAutoConfiguration` from the auto-configuration exclusion lists. This is **the moment Spring Boot takes over ConnectionFactory creation**.

**Why this works:** Spring Boot auto-configuration checks for existing beans. When it finds an existing `ConnectionFactory` bean, `ActiveMQAutoConfiguration` **backs off**. Since `JmsConfig.connectionFactory()` still exists at this point, the auto-configured `ConnectionFactory` is NOT created — the manual bean wins.

**The transition moment:** After this step, the auto-config exclusion is gone, but the manual `ConnectionFactory` bean still exists (in `JmsConfig`), so auto-config backs off. This is a no-op transition — but it unlocks the ability to remove manual beans in subsequent steps.

**Changes:**

1. **`IntygstjanstApplication.java`** — Remove `ActiveMQAutoConfiguration.class` from the `exclude` list:

   ```java
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
       exclude = {
           // REMOVED: ActiveMQAutoConfiguration.class,
           RedisAutoConfiguration.class
       }
   )
   ```

   Also remove the now-unused import:
   ```java
   // REMOVED: import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
   ```

2. **`web/src/main/resources/application.properties`** — Remove `ActiveMQAutoConfiguration` from `spring.autoconfigure.exclude`:

   ```properties
   spring.autoconfigure.exclude=\
     org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
   ```

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew bootRun         # Starts — auto-config backs off, manual beans still active
```

Check the logs for:
- **No** duplicate `ConnectionFactory` warnings — auto-config backs off because `JmsConfig.connectionFactory()` provides the existing bean.
- **Yes** JMS listeners start (from `JmsConfig.jmsListenerContainerFactory()`), same as before.

**Risks:**

- If Spring Boot auto-config does NOT back off correctly (e.g., different bean names or types), you'll get duplicate `ConnectionFactory` beans → startup failure. If this happens, re-add the exclusion and investigate. The fix is usually that the manual bean method name matches what auto-config expects (Spring Boot looks for a bean of type `ConnectionFactory`). The current `JmsConfig.connectionFactory()` returns `ConnectionFactory`, so back-off should work.

---

## Step 12.4 — Remove Manual `ConnectionFactory`, `JmsTransactionManager`, and `DestinationResolver` Beans

**What:** Remove the `connectionFactory()`, `jmsTransactionManager()`, and `destinationResolver()` bean methods from `JmsConfig`. After this, Spring Boot auto-configuration creates the `ConnectionFactory` from the `spring.activemq.*` properties.

**Why now:** Step 12.3 removed the auto-config exclusion. The manual `ConnectionFactory` was causing auto-config to back off. By removing it, `ActiveMQAutoConfiguration` activates and creates a `PooledConnectionFactory` using `spring.activemq.*` properties.

**Changes:**

1. **`JmsConfig.java`** — Remove these three bean methods and their associated fields:

   ```java
   // REMOVE these fields:
   // @Value("${activemq.broker.url}")
   // private String brokerUrl;
   //
   // @Value("${activemq.broker.username}")
   // private String brokerUsername;
   //
   // @Value("${activemq.broker.password}")
   // private String brokerPassword;

   // REMOVE these bean methods:
   // @Bean
   // public ConnectionFactory connectionFactory() { ... }
   //
   // @Bean
   // public JmsTransactionManager jmsTransactionManager() { ... }
   //
   // @Bean
   // public DestinationResolver destinationResolver() { ... }
   ```

   Also remove the now-unused imports:
   ```java
   // REMOVED:
   import org.apache.activemq.ActiveMQConnectionFactory;
   import org.apache.activemq.pool.PooledConnectionFactory;
   import org.springframework.jms.connection.JmsTransactionManager;
   import org.springframework.jms.support.destination.DestinationResolver;
   import org.springframework.jms.support.destination.DynamicDestinationResolver;
   ```

2. **Update `jmsListenerContainerFactory`** — It currently depends on `JmsTransactionManager`. After removal, it needs to use the auto-configured `ConnectionFactory` directly:

   ```java
   @Bean
   public JmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
       DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
       factory.setConnectionFactory(connectionFactory);
       factory.setSessionTransacted(true);
       factory.setCacheLevelName("CACHE_CONSUMER");
       factory.setConcurrency("1-10");
       return factory;
   }
   ```

   **Key changes:**
   - Parameter changed from `JmsTransactionManager jmsTransactionManager` to `ConnectionFactory connectionFactory`.
   - `factory.setConnectionFactory(Objects.requireNonNull(jmsTransactionManager.getConnectionFactory()))` → `factory.setConnectionFactory(connectionFactory)`.
   - `factory.setTransactionManager(jmsTransactionManager)` → **removed** (local JMS transactions via `setSessionTransacted(true)` are sufficient; no external transaction manager needed unless JMS+DB transactions are coordinated via JTA).
   - `factory.setDestinationResolver(destinationResolver())` → **removed** (Spring Boot defaults to `DynamicDestinationResolver`, same as what was configured manually).

**What changes in behavior:**

| Aspect                        | Before (JmsConfig manual)                                                | After (Spring Boot auto-config)                                                 |
|-------------------------------|--------------------------------------------------------------------------|---------------------------------------------------------------------------------|
| ConnectionFactory             | `PooledConnectionFactory(new ActiveMQConnectionFactory(user, pass, url))` | `PooledConnectionFactory` via `ActiveMQAutoConfiguration` + `spring.activemq.pool.enabled=true` |
| Transaction manager           | `JmsTransactionManager(connectionFactory())` bean                        | **Removed** — local JMS transactions via `sessionTransacted=true` are sufficient |
| Destination resolver           | `DynamicDestinationResolver` bean                                        | **Removed** — Spring Boot defaults to `DynamicDestinationResolver`               |
| Listener factory connection    | `jmsTransactionManager.getConnectionFactory()`                           | Auto-configured `ConnectionFactory` injected directly                            |

**⚠️ Transaction Manager Decision:**

The manual config had an explicit `JmsTransactionManager`. This provided external transaction management for JMS listeners. With `setSessionTransacted(true)`, the JMS session itself handles commit/rollback (local transaction). This is functionally equivalent for the current use case because:

1. The `@JmsListener` in `CertificateEventListenerServiceImpl` does NOT participate in JPA transactions — it processes events independently.
2. No `@Transactional` annotations span both JMS and JPA operations.

If JTA (distributed transactions) were needed, a `JtaTransactionManager` would be required. The current codebase does NOT use JTA, so local JMS transactions are correct.

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew bootRun         # Starts — Spring Boot creates ConnectionFactory, listener factory uses it
```

Check the logs for:
- `ActiveMQ` connection established (now from auto-config)
- JMS listener starts for `certificate.event.queue.name`
- No `JmsTransactionManager` initialization log (it's been removed)

---

## Step 12.5 — Simplify `JmsListenerContainerFactory` — Consider Removing Custom Bean

**What:** Evaluate whether the custom `JmsListenerContainerFactory` bean can be removed entirely, letting Spring Boot auto-configure it via `spring.jms.listener.*` properties.

**Analysis:**

| Custom setting in `JmsConfig`            | Spring Boot property equivalent                  | Covered? |
|------------------------------------------|--------------------------------------------------|----------|
| `setSessionTransacted(true)`             | `spring.jms.listener.session-transacted=true`    | ✅ Yes    |
| `setCacheLevelName("CACHE_CONSUMER")`    | `spring.jms.cache.consumers=true`                | ✅ Yes    |
| `setConcurrency("1-10")`                | `spring.jms.listener.concurrency=1` + `spring.jms.listener.max-concurrency=10` | ✅ Yes    |
| `setTransactionManager(jmsTxManager)`    | N/A — removed in 12.4, using local transactions  | ✅ N/A    |
| `setDestinationResolver(dynamicResolver)` | Default (DynamicDestinationResolver)             | ✅ Default |

**Decision:** All custom settings are covered by Spring Boot properties already configured in Step 12.2. The custom `JmsListenerContainerFactory` bean can be **removed**.

**Changes:**

1. **`web/src/main/resources/application.properties`** — Ensure listener properties are set (already added in Step 12.2):

   ```properties
   spring.jms.listener.session-transacted=true
   ```

   **Note:** This was not explicitly in Step 12.2. Add it now if not already present.

2. **`JmsConfig.java`** — Remove the `jmsListenerContainerFactory` bean method:

   ```java
   // REMOVE:
   // @Bean
   // public JmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) { ... }
   ```

   Also remove the now-unused imports:
   ```java
   // REMOVED:
   import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
   import org.springframework.jms.config.JmsListenerContainerFactory;
   import java.util.Objects;
   ```

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew bootRun         # Starts — JMS listener for certificate events starts with auto-configured factory
```

Check that the `@JmsListener(destination = "${certificate.event.queue.name}")` in `CertificateEventListenerServiceImpl` still works — Spring Boot auto-configures a `DefaultJmsListenerContainerFactory` named `"jmsListenerContainerFactory"` (the default name that `@JmsListener` looks for).

**Risks:**

- If the `@JmsListener` specifies `containerFactory = "someCustomName"`, it won't find the auto-configured factory. Check: the `@JmsListener` in `CertificateEventListenerServiceImpl` does NOT specify `containerFactory` — it uses the default. ✅ Safe.

---

## Step 12.6 — Remove Manual `JmsTemplate` Beans — Replace with Auto-Configured `JmsTemplate`

**What:** Remove the two manual `JmsTemplate` bean methods (`jmsTemplate()` and `jmsCertificateEventTemplate()`) and the `template()` helper from `JmsConfig`. Spring Boot auto-configures a single `JmsTemplate` bean.

**Why now:** With the `ConnectionFactory` now auto-configured (Step 12.4), the manual `JmsTemplate` beans use a connection factory that no longer exists as a manual bean. They need to be either updated or removed.

**Current state:**

| Bean name                     | Default destination                | Used by                                      |
|-------------------------------|-------------------------------------|----------------------------------------------|
| `jmsTemplate` (default)       | `destinationQueue` (certificate.queue) | `StatisticsServiceImpl`, `InternalNotificationServiceImpl`, `Receiver` |
| `jmsCertificateEventTemplate` | `certificateEventQueue` (intygstjanst.certificate.event.queue) | `CertificateEventRedeliveryServiceImpl` |

**Strategy:** Spring Boot auto-configures a single `JmsTemplate` with `spring.jms.template.default-destination` as the default destination (set to `${activemq.destination.queue.name}` in Step 12.2). This replaces the `jmsTemplate` bean.

For `jmsCertificateEventTemplate`, which uses a different default destination, the consumers need to be updated to specify the destination explicitly when sending (Step 12.7).

**Changes:**

1. **`JmsConfig.java`** — Remove the `JmsTemplate` beans and helper:

   ```java
   // REMOVE:
   // @Bean
   // public JmsTemplate jmsTemplate() { ... }
   //
   // @Bean(value = "jmsCertificateEventTemplate")
   // public JmsTemplate jmsCertificateEventTemplate(ConnectionFactory jmsConnectionFactory) { ... }
   //
   // JmsTemplate template(final ConnectionFactory connectionFactory, final Queue queue) { ... }
   ```

2. **`web/src/main/resources/application.properties`** — Ensure `sessionTransacted` is set on the auto-configured `JmsTemplate`:

   ```properties
   spring.jms.template.session-transacted=true
   ```

   This replaces the manual `jmsTemplate.setSessionTransacted(true)` from `JmsConfig.template()`.

**What changes in behavior:**

| Aspect                        | Before (manual JmsTemplate)                                      | After (auto-configured JmsTemplate)                               |
|-------------------------------|------------------------------------------------------------------|-------------------------------------------------------------------|
| Number of JmsTemplate beans   | 2 (`jmsTemplate`, `jmsCertificateEventTemplate`)                 | 1 (auto-configured by Spring Boot)                                |
| Default destination           | `jmsTemplate` → `certificate.queue`; `jmsCertificateEventTemplate` → `intygstjanst.certificate.event.queue` | Single template → `certificate.queue` (from `spring.jms.template.default-destination`) |
| Session transacted            | `true` (set manually)                                            | `true` (set via `spring.jms.template.session-transacted=true`)    |
| Connection factory            | Injected from manual `connectionFactory()` bean                  | Injected from auto-configured `ConnectionFactory`                  |

**Impact on consumers:**

- `StatisticsServiceImpl` — uses `jmsTemplate.send(destinationQueue, messageCreator)` with an explicit `Queue` destination. After this step, it still injects `JmsTemplate` (now auto-configured) and `Queue destinationQueue`. **Will break** because `destinationQueue` bean is still in `JmsConfig` — addressed in Step 12.7.
- `InternalNotificationServiceImpl` — uses `jmsTemplate.send(internalNotificationQueue, messageCreator)` with `@Qualifier("internalNotificationQueue") Queue`. Same situation — works until `Queue` beans are removed.
- `CertificateEventRedeliveryServiceImpl` — uses `@Qualifier("jmsCertificateEventTemplate") JmsTemplate`. **Will break** because `jmsCertificateEventTemplate` bean is removed. **Must be updated in Step 12.7.**
- `Receiver` — uses `jmsTemplate` and `destinationQueue`. Same situation as `StatisticsServiceImpl`.

**⚠️ Important:** Steps 12.6 and 12.7 should be done as one atomic commit to avoid broken bean references.

**Verify:**

(Verify after completing Step 12.7 — these two steps must be atomic.)

---

## Step 12.7 — Update `JmsTemplate` Consumers to Use Destination Names Instead of `Queue` Beans

**What:** Refactor all JMS producers to use destination name strings instead of `Queue` bean injection. This eliminates the need for `Queue` beans in `JmsConfig` and makes the code simpler.

**Why:** Spring Boot's `JmsTemplate` (and Spring JMS in general) supports sending to destinations by name. The `DynamicDestinationResolver` (Spring's default) resolves string names to JMS `Destination` objects at runtime. This is the Spring Boot–idiomatic approach.

**Changes:**

### 12.7a — Refactor `StatisticsServiceImpl`

Replace `Queue` bean injection with a destination name property:

```java
@Service
public class StatisticsServiceImpl implements StatisticsService {

    // ... constants unchanged ...

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private MonitoringLogService monitoringLogService;

    // REMOVED: @Autowired private Queue destinationQueue;

    @Value("${activemq.destination.queue.name}")
    private String destinationQueueName;

    @Value("${statistics.enabled}")
    private boolean enabled;

    // ... business methods unchanged ...

    boolean send(final MessageCreator messageCreator) {
        jmsTemplate.send(destinationQueueName, messageCreator);  // Changed: Queue → String
        return true;
    }
}
```

**Key change:** `jmsTemplate.send(destinationQueue, messageCreator)` → `jmsTemplate.send(destinationQueueName, messageCreator)`. The `JmsTemplate.send(String, MessageCreator)` overload uses the `DynamicDestinationResolver` to resolve the name to a JMS `Queue` at runtime.

**Removed imports:**
```java
// REMOVED: import jakarta.jms.Queue;
```

**Added imports:**
```java
import org.springframework.beans.factory.annotation.Value;
```

### 12.7b — Refactor `InternalNotificationServiceImpl`

Replace `@Qualifier("internalNotificationQueue") Queue` injection with a destination name property:

```java
@Service
public class InternalNotificationServiceImpl implements InternalNotificationService {

    // ... constants unchanged ...

    @Autowired
    private JmsTemplate jmsTemplate;

    // REMOVED: @Autowired @Qualifier("internalNotificationQueue") private Queue internalNotificationQueue;

    @Value("${activemq.internal.notification.queue.name}")
    private String internalNotificationQueueName;

    // ... business methods unchanged ...

    private boolean send(final MessageCreator messageCreator) {
        jmsTemplate.send(internalNotificationQueueName, messageCreator);  // Changed: Queue → String
        return true;
    }
}
```

**Removed imports:**
```java
// REMOVED: import jakarta.jms.Queue;
// REMOVED: import org.springframework.beans.factory.annotation.Qualifier;
```

### 12.7c — Refactor `CertificateEventRedeliveryServiceImpl`

Replace `@Qualifier("jmsCertificateEventTemplate") JmsTemplate` injection with the standard auto-configured `JmsTemplate` + explicit destination name:

```java
@Service
@Slf4j
public class CertificateEventRedeliveryServiceImpl implements CertificateEventRedeliveryService {

    // ... constants unchanged ...

    private final JmsTemplate jmsTemplate;

    @Value("${certificate.event.queue.name}")
    private String certificateEventQueueName;

    public CertificateEventRedeliveryServiceImpl(JmsTemplate jmsTemplate) {  // Removed @Qualifier
        this.jmsTemplate = jmsTemplate;
    }

    // ... resend() unchanged ...

    private void send(Message message, String eventType, String certificateId, String messageId, int redeliveries, Long redeliveryDelay) {
        jmsTemplate.send(certificateEventQueueName, session -> {  // Changed: uses explicit destination name
            final var textMessage = session.createTextMessage("");
            textMessage.setStringProperty(EVENT_TYPE, eventType);
            textMessage.setStringProperty(CERTIFICATE_ID, certificateId);
            if (message.propertyExists(MESSAGE_ID)) {
                textMessage.setStringProperty(MESSAGE_ID, messageId);
            }
            textMessage.setIntProperty(REDELIVERIES, redeliveries);
            textMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, redeliveryDelay);
            return textMessage;
        });
    }

    // ... getRedeliveryDelay() unchanged ...
}
```

**Key change:** Constructor no longer needs `@Qualifier("jmsCertificateEventTemplate")` — uses the single auto-configured `JmsTemplate`. The destination is specified as a string argument to `jmsTemplate.send(destinationName, messageCreator)`.

**Removed imports:**
```java
// REMOVED: import org.springframework.beans.factory.annotation.Qualifier;
```

**Added imports:**
```java
import org.springframework.beans.factory.annotation.Value;
```

### 12.7d — Refactor `Receiver`

Replace `Queue` bean injection with a destination name:

```java
public class Receiver {

    // ... constants unchanged ...

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${activemq.destination.queue.name}")
    private String destinationQueueName;

    private static final Logger LOG = LoggerFactory.getLogger(Receiver.class);

    public Map<String, String> getMessages() {
        final Map<String, String> map = new HashMap<>();
        this.consume(TIMEOUT, msg -> {
            try {
                final String action = msg.getStringProperty(ACTION);
                final String id = msg.getStringProperty(FK_MESSAGE_ACTION.equals(action) ? MESSAGE_ID : CERTIFICATE_ID);
                final String key = generateKey(id, action);
                map.put(key, ((TextMessage) msg).getText());
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        });
        return map;
    }

    public int consume(final long timeout, final Consumer<Message> consumer) {
        return jmsTemplate.execute(session -> {
            final Queue queue = session.createQueue(destinationQueueName);  // Create Queue from name
            final MessageConsumer messageConsumer = session.createConsumer(queue);
            try {
                Message msg;
                int n = 0;
                while ((msg = next(timeout, messageConsumer)) != null) {
                    consumer.accept(msg);
                    n++;
                }
                if (session.getTransacted()) {
                    JmsUtils.commitIfNecessary(session);
                }
                LOG.info("Received {} messages", n);
                return n;
            } finally {
                JmsUtils.closeMessageConsumer(messageConsumer);
            }
        }, true);
    }

    // ... rest unchanged ...
}
```

**Key change:** `session.createConsumer(destinationQueue)` → `session.createQueue(destinationQueueName)` then `session.createConsumer(queue)`. The `Receiver` still needs a JMS `Queue` object for its low-level consumer, but it creates it from the session rather than injecting a bean.

**Removed field:**
```java
// REMOVED: @Autowired private Queue destinationQueue;
```

### Update Tests

**`StatisticsServiceImplTest`** — Replace `@Mock Queue destinationQueue` with a `String` field:

```java
@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private JmsTemplate template;

    // REMOVED: @Mock private Queue destinationQueue;

    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private StatisticsServiceImpl serviceImpl;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(serviceImpl, "destinationQueueName", "certificate.queue");
    }

    // Update all verify() calls:
    // Before: verify(template, ...).send(any(Queue.class), any(MessageCreator.class));
    // After:  verify(template, ...).send(anyString(), any(MessageCreator.class));

    // ... rest of tests updated similarly ...
}
```

**`InternalNotificationServiceImplTest`** — Replace `@Mock Queue internalNotificationQueue` with a `String` field:

```java
@ExtendWith(MockitoExtension.class)
class InternalNotificationServiceImplTest {

    @Mock
    private JmsTemplate jmsTemplate;

    // REMOVED: @Mock private Queue internalNotificationQueue;

    @InjectMocks
    private InternalNotificationServiceImpl testee;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(testee, "internalNotificationQueueName", "internal.notification.queue");
    }

    // Update verify() calls:
    // Before: verify(jmsTemplate, ...).send(any(Queue.class), any(MessageCreator.class));
    // After:  verify(jmsTemplate, ...).send(anyString(), any(MessageCreator.class));

    // ... rest of tests updated similarly ...
}
```

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew bootRun         # Starts — messages sent using destination name strings
```

Test by triggering a certificate event and checking the logs for JMS send confirmation.

---

## Step 12.8 — Remove `Queue` Beans and `Receiver` Bean from `JmsConfig`

**What:** Remove the three `Queue` bean methods (`destinationQueue()`, `internalNotificationQueue()`, `certificateEventQueue()`) and the `Receiver` bean from `JmsConfig`. No code references `Queue` beans anymore (all consumers now use destination name strings).

**Why safe:** Steps 12.7a–12.7d changed all consumers to use `String` destination names. The `Queue` beans are now unreferenced. The `Receiver` bean should be moved to a test-profile–specific configuration or left as a standalone `@Component` (it's already in the `web.integration.test` package).

**Changes:**

1. **`JmsConfig.java`** — Remove `Queue` beans and `Receiver` bean:

   ```java
   // REMOVE these @Value fields:
   // @Value("${activemq.destination.queue.name}")
   // private String destinationQueueName;
   //
   // @Value("${activemq.internal.notification.queue.name}")
   // private String internalNotificationQueue;
   //
   // @Value("${certificate.event.queue.name}")
   // private String certificateEventQueue;

   // REMOVE these bean methods:
   // @Bean("destinationQueue")
   // public Queue destinationQueue() { ... }
   //
   // @Bean("internalNotificationQueue")
   // public Queue internalNotificationQueue() { ... }
   //
   // @Bean("certificateEventQueue")
   // public Queue certificateEventQueue() { ... }
   //
   // @Bean
   // public Receiver receiver() { ... }
   ```

   Also remove the now-unused imports:
   ```java
   // REMOVED:
   import jakarta.jms.Queue;
   import org.apache.activemq.command.ActiveMQQueue;
   import se.inera.intyg.intygstjanst.web.integration.test.Receiver;
   ```

2. **`Receiver.java`** — Make it a Spring-managed component conditionally:

   ```java
   @Component
   @Profile({"dev", "testability-api"})
   public class Receiver {
       // ... unchanged ...
   }
   ```

   **Alternative:** If `Receiver` is only used in integration tests, consider annotating with `@Profile("testability-api")` only or moving it to test source set. Since it's currently in `web/src/main/java/.../web/integration/test/`, adding `@Component` + `@Profile` is the minimal-change approach.

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
# Confirm no Queue bean references remain:
grep -rn "Queue destinationQueue\|Queue internalNotificationQueue\|Queue certificateEventQueue" web/src/main/
# Should return nothing
```

---

## Step 12.9 — Remove `JmsConfig` Class and `@EnableJms`

**What:** Delete `JmsConfig.java`. At this point, all its beans have been removed or replaced. Spring Boot's `JmsAutoConfiguration` provides `@EnableJms` automatically.

**Why safe:** After Steps 12.3–12.8, `JmsConfig` should be empty (or contain only the class declaration with `@Configuration @EnableJms`). Spring Boot's `JmsAutoConfiguration` adds `@EnableJms` automatically when it detects `spring-jms` on the classpath, so the explicit annotation is redundant.

**Changes:**

1. **Delete** `web/src/main/java/se/inera/intyg/intygstjanst/config/jms/JmsConfig.java`

2. **Verify `@EnableJms` is auto-provided:** Spring Boot's `JmsAnnotationDrivenConfiguration` (inner class of `JmsAutoConfiguration`) is annotated with `@EnableJms`. Since we removed the `ActiveMQAutoConfiguration` exclusion in Step 12.3, this is now active.

3. **Optional cleanup:** If the `config/jms/` package is now empty, delete the package directory.

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew bootRun         # Starts — JMS fully auto-configured
# Confirm JmsConfig is gone:
find . -name "JmsConfig.java"  # Should return nothing
```

---

## Step 12.10 — Remove Redundant Explicit Dependencies from `web/build.gradle`

**What:** Remove explicit dependency declarations for libraries now provided transitively by `spring-boot-starter-activemq`.

**Why safe:** The starter brings these dependencies with Spring Boot–managed versions. Removing explicit declarations avoids version conflicts and lets Spring Boot manage the dependency graph.

**Changes:**

1. **`web/build.gradle`** — Remove the following explicit dependencies (now provided by `spring-boot-starter-activemq`):

   ```groovy
   // REMOVE — provided by spring-boot-starter-activemq:
   // implementation "org.apache.activemq:activemq-spring"
   // implementation "org.springframework:spring-jms"

   // KEEP:
   implementation "org.springframework.boot:spring-boot-starter-activemq"
   ```

   **Decision on `activemq-spring`:** `spring-boot-starter-activemq` brings `activemq-client` and `spring-jms` transitively. `activemq-spring` is a convenience module that bundles `activemq-broker`, `activemq-client`, `spring-jms`, etc. After removing the manual `JmsConfig`, none of the `activemq-spring`–specific classes (like `PooledConnectionFactory` from `activemq-pool`) are imported in production code. The `PooledConnectionFactory` is now created by Spring Boot's auto-config, which uses `activemq-pool` (a dependency of the starter when `spring.activemq.pool.enabled=true`).

   **⚠️ Verify:** Check that `activemq-pool` is still on the classpath after removing `activemq-spring`. Run:
   ```bash
   ./gradlew :intygstjanst-web:dependencies --configuration runtimeClasspath | grep activemq-pool
   ```
   If `activemq-pool` is NOT present, add it explicitly:
   ```groovy
   implementation "org.apache.activemq:activemq-pool"
   ```
   Or better, add `org.messaginghub:pooled-jms` which is the newer pool library supported by Spring Boot:
   ```groovy
   implementation "org.messaginghub:pooled-jms"
   ```
   And change `spring.activemq.pool.enabled` to `spring.jms.pool.enabled` (Spring Boot supports both `activemq-pool` and `pooled-jms`).

   **Decision on `spring-jms`:** `spring-boot-starter-activemq` → `spring-boot-starter` + `spring-jms` + `activemq-client`. The `spring-jms` dependency is transitive. Can be removed.

   **Decision on `ScheduledMessage` import:** `CertificateEventRedeliveryServiceImpl` imports `org.apache.activemq.ScheduledMessage`. This class is in `activemq-client`, which is provided by the starter. ✅ No issue.

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew :intygstjanst-web:dependencies --configuration runtimeClasspath | grep -E "activemq|spring-jms|pooled-jms"
# Should show activemq-client, spring-jms from starter
```

---

## Step 12.11 — Update Persistence `TestConfig` — Remove `ActiveMQAutoConfiguration` Exclusion

**What:** Remove `ActiveMQAutoConfiguration.class` from the persistence `TestConfig`'s `@EnableAutoConfiguration(exclude = ...)`. This exclusion was needed because ActiveMQ auto-config would try to connect to a broker during persistence tests. Now that the starter is on the classpath, the exclusion may still be needed if ActiveMQ classes are visible to the persistence test context.

**Analysis:** The persistence module does NOT have `spring-boot-starter-activemq` as a dependency — it's only in `web/build.gradle`. The `TestConfig` in the persistence module won't see `ActiveMQAutoConfiguration` on its classpath unless it's transitive.

**Decision:** Check if `ActiveMQAutoConfiguration` is on the persistence test classpath:

```bash
./gradlew :intygstjanst-persistence:dependencies --configuration testRuntimeClasspath | grep activemq
```

- **If NOT on classpath:** Remove the exclusion — it was referencing a class not on the classpath, which is harmless but unnecessary. Clean it up.
- **If on classpath:** Keep the exclusion to prevent ActiveMQ auto-config from activating during persistence tests (which don't need JMS).

**Changes (assuming NOT on classpath — most likely scenario):**

1. **`persistence/src/test/java/.../TestConfig.java`** — Remove `ActiveMQAutoConfiguration` from exclusions:

   ```java
   @Configuration
   @EnableAutoConfiguration(exclude = {
       // REMOVED: ActiveMQAutoConfiguration.class,
       RedisAutoConfiguration.class
   })
   @EntityScan("se.inera.intyg.intygstjanst.persistence.model")
   @EnableJpaRepositories("se.inera.intyg.intygstjanst.persistence.model.dao")
   @ComponentScan(basePackages = {"se.inera.intyg.intygstjanst.persistence", "se.inera.intyg.intygstjanst.logging"})
   @PropertySource("classpath:test.properties")
   public class TestConfig {
   }
   ```

   Remove the unused import:
   ```java
   // REMOVED: import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
   ```

**Verify:**

```bash
./gradlew :intygstjanst-persistence:test   # All persistence tests pass
./gradlew test                              # All tests pass across all modules
```

---

## Step 12.12 — Final Verification

**What:** Comprehensive verification that JMS auto-configuration works identically to the manual configuration.

**Start the application:**

```bash
./gradlew :intygstjanst-web:bootRun
```

**Verification checklist:**

| Check                                | How                                                           | Expected                                                   |
|--------------------------------------|---------------------------------------------------------------|------------------------------------------------------------|
| Application starts                   | `./gradlew bootRun` — no exceptions                           | Started successfully                                       |
| ActiveMQ connection established      | Check logs for `ActiveMQ` connection                          | Connection to broker at `activemq.broker.url`              |
| Connection pooling active            | Check logs for pool initialization                            | `PooledConnectionFactory` or `JmsPoolConnectionFactory`    |
| JMS listener starts                  | Check logs for `certificate.event.queue` listener             | Listener container started, consuming from queue           |
| No `JmsConfig` logs                  | Grep logs for manual config log messages                      | Not present — manual config is gone                        |
| Statistics messages sent             | Trigger a certificate creation                                | JMS message sent to `certificate.queue`                    |
| Internal notifications sent          | Trigger a citizen-sent certificate                            | JMS message sent to `internal.notification.queue`          |
| Certificate events redelivered       | Trigger a failing certificate event                           | Redelivery message sent to `certificate.event.queue`       |
| `@JmsListener` processes messages    | Send a message to `certificate.event.queue`                   | `CertificateEventListenerServiceImpl.processMessage()` invoked |
| All tests pass                       | `./gradlew test`                                               | BUILD SUCCESSFUL                                           |
| No `JmsConfig` references remain     | `grep -rn "JmsConfig" web/src/`                                | No matches (except possible migration docs)                |
| No manual `ConnectionFactory` import | `grep -rn "PooledConnectionFactory\|ActiveMQConnectionFactory" web/src/main/` | No matches                                    |

**Verify property mapping:**

| Old property (still in `application.properties`)     | Spring Boot property                                 | Value source          |
|------------------------------------------------------|------------------------------------------------------|-----------------------|
| `activemq.broker.url=tcp://localhost:61616?...`      | `spring.activemq.broker-url=${activemq.broker.url}`  | Same broker URL       |
| `activemq.broker.username=`                          | `spring.activemq.user=${activemq.broker.username}`   | Same username         |
| `activemq.broker.password=`                          | `spring.activemq.password=${activemq.broker.password}` | Same password       |
| `activemq.destination.queue.name=certificate.queue`  | `spring.jms.template.default-destination`            | Same queue name       |
| N/A (PooledConnectionFactory defaults)               | `spring.activemq.pool.enabled=true`                  | Explicit pooling      |
| N/A (`setConcurrency("1-10")`)                       | `spring.jms.listener.concurrency/max-concurrency`    | Same concurrency      |
| N/A (`setCacheLevelName("CACHE_CONSUMER")`)          | `spring.jms.cache.consumers=true`                    | Same caching          |

---

## Risk Register

| #  | Risk                                                                                 | Impact | Mitigation                                                                                                                                                                                                                                         |
|----|--------------------------------------------------------------------------------------|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1  | `PooledConnectionFactory` not on classpath after removing `activemq-spring`          | High   | `spring-boot-starter-activemq` does NOT include `activemq-pool` by default. **Mitigation:** Verify with `./gradlew dependencies`. If missing, add `org.messaginghub:pooled-jms` (preferred) or `org.apache.activemq:activemq-pool` explicitly. Use `spring.activemq.pool.enabled=true`. |
| 2  | `JmsTemplate` bean name mismatch — consumers inject by name                          | High   | `StatisticsServiceImpl` injects `JmsTemplate jmsTemplate` by type (field name happens to match). `CertificateEventRedeliveryServiceImpl` uses `@Qualifier("jmsCertificateEventTemplate")`. **Mitigation:** Remove `@Qualifier` and update to use single auto-configured `JmsTemplate` + explicit destination names. Done in Step 12.7. |
| 3  | `@JmsListener` factory name mismatch                                                 | Medium | If `@JmsListener` specifies a custom `containerFactory` name, it won't find the auto-configured factory. **Mitigation:** Verified — `@JmsListener` in `CertificateEventListenerServiceImpl` does NOT specify `containerFactory`, so it uses the default. |
| 4  | Redelivery policy lost — URL-embedded vs Spring Boot config                          | Medium | The broker URL contains `jms.redeliveryPolicy.*` parameters (embedded in the URL). These are ActiveMQ client-side settings, NOT Spring Boot properties. **Mitigation:** These settings remain in the `activemq.broker.url` property and are passed through `spring.activemq.broker-url=${activemq.broker.url}`. No change needed. |
| 5  | `JmsTransactionManager` removal changes transaction behavior                         | Medium | The manual config used `JmsTransactionManager` on the listener factory. Removing it and relying on `sessionTransacted=true` changes from external to local transaction management. **Mitigation:** The current code does NOT coordinate JMS+JPA transactions. Local JMS transactions via `sessionTransacted=true` are sufficient. If rollback behavior changes, re-add a `JmsTransactionManager` bean. |
| 6  | `ScheduledMessage.AMQ_SCHEDULED_DELAY` requires ActiveMQ-specific class              | Low    | `CertificateEventRedeliveryServiceImpl` uses `org.apache.activemq.ScheduledMessage` — an ActiveMQ-specific class. This is in `activemq-client` which is provided by the starter. **Mitigation:** Verified — still on classpath. |
| 7  | `Receiver` class needs `@Component` to be picked up without explicit `@Bean`         | Low    | `JmsConfig` had `@Bean Receiver receiver()`. After removing this, `Receiver` needs `@Component` (or similar) to be Spring-managed. **Mitigation:** Add `@Component @Profile({"dev", "testability-api"})` to `Receiver`. Done in Step 12.8. |
| 8  | `spring.jms.template.default-destination` affects all auto-configured `JmsTemplate` usage | Low | Setting a default destination means any `jmsTemplate.send(messageCreator)` call (without destination) goes to `certificate.queue`. **Mitigation:** This matches the previous behavior of the `jmsTemplate` bean. The `jmsCertificateEventTemplate` bean is gone — its consumers now specify the destination explicitly. |
| 9  | Dev/test profiles may need ActiveMQ broker or embedded broker                        | Low    | In `dev` profile, `activemq.broker.url=tcp://localhost:61616` expects a running broker. **Mitigation:** No change from current behavior. Consider adding `spring.activemq.in-memory=true` for test profiles if needed. |
| 10 | Connection pool size differs between manual and auto-config                           | Low    | `PooledConnectionFactory` default max connections is 1. `spring.activemq.pool.max-connections` defaults to 1 in Spring Boot. Setting `spring.activemq.pool.max-connections=8` in Step 12.2 is an intentional improvement. **Mitigation:** Monitor connection usage in production. |

---

## Rollback Plan

If Step 12 causes issues:

1. **Git revert** the commit(s) back to the state after Step 11. The manual `JmsConfig` will be restored.
2. Re-add the `ActiveMQAutoConfiguration` exclusion in `IntygstjanstApplication` and `application.properties`.
3. The application will use manual JMS config again while issues are investigated.

**Partial rollback:** If only the `JmsTemplate` consumer changes are broken (Steps 12.6–12.7), revert just those steps — the connection factory switch (Steps 12.3–12.5) can be kept.

---

## Summary: What Changes at Each Sub-step

| Step   | Auto-config exclusion    | Manual JmsConfig                   | ConnectionFactory           | JmsTemplate beans              | Queue beans     | Listener factory   |
|--------|--------------------------|------------------------------------|-----------------------------|--------------------------------|-----------------|--------------------|
| Before | ActiveMQ excluded        | Full `JmsConfig` active            | Manual `PooledConnectionFactory` | 2 manual (`jmsTemplate`, `jmsCertificateEventTemplate`) | 3 `ActiveMQQueue` beans | Manual `DefaultJmsListenerContainerFactory` |
| 12.1   | ActiveMQ excluded        | Active (unchanged)                 | Manual (unchanged)          | Unchanged                      | Unchanged       | Unchanged          |
| 12.2   | ActiveMQ excluded        | Active (unchanged)                 | Manual (unchanged)          | Unchanged                      | Unchanged       | Unchanged          |
| 12.3   | ❌ **Removed**           | Active (auto-config backs off)     | Manual (backs off auto-config) | Unchanged                   | Unchanged       | Unchanged          |
| 12.4   | Removed                  | Partially active                   | ✅ **Auto-configured**      | Manual (use auto CF)           | Unchanged       | Simplified (no TxManager) |
| 12.5   | Removed                  | Partially active                   | Auto-configured             | Manual                         | Unchanged       | ✅ **Auto-configured** (or removed) |
| 12.6   | Removed                  | Partially active                   | Auto-configured             | ✅ **Auto-configured** (single) | Unchanged      | Auto-configured    |
| 12.7   | Removed                  | Partially active                   | Auto-configured             | Auto-configured                | Unused (consumers use names) | Auto-configured |
| 12.8   | Removed                  | Nearly empty                       | Auto-configured             | Auto-configured                | ❌ **Removed** | Auto-configured    |
| 12.9   | Removed                  | ❌ **Deleted**                     | Auto-configured             | Auto-configured                | Removed         | Auto-configured    |
| 12.10  | Removed                  | Deleted                            | Auto-configured             | Auto-configured                | Removed         | Auto-configured    |
| 12.11  | Removed                  | Deleted                            | Auto-configured             | Auto-configured                | Removed         | Auto-configured    |
| 12.12  | Removed                  | Deleted                            | Auto-configured             | Auto-configured                | Removed         | ✅ Fully auto-configured |

**Recommended atomic commit grouping:**

- **Commit 1:** Steps 12.1–12.2 (additive, zero risk)
- **Commit 2:** Steps 12.3–12.5 (connection infrastructure switch)
- **Commit 3:** Steps 12.6–12.8 (must be atomic — remove JmsTemplate beans + update consumers + remove Queue beans)
- **Commit 4:** Step 12.9 (delete JmsConfig — now empty)
- **Commit 5:** Steps 12.10–12.11 (dependency + test config cleanup)

---

## Design Note: Why Property Indirection (`${activemq.broker.*}`)

The Spring Boot properties reference the legacy `activemq.broker.*` properties via `${activemq.broker.url}`, `${activemq.broker.username}`, etc. This is intentional:

1. **Zero changes to deployment configs.** All existing environment variables, Kubernetes ConfigMaps, Helm values, and dev config files that set `activemq.broker.url`, `activemq.broker.username`, etc. continue to work without modification.
2. **Gradual deprecation.** After Step 12 is stable in production, a follow-up task can rename `activemq.broker.*` → `spring.activemq.*` in deployment configs and remove the indirection.
3. **Minimal blast radius.** If something goes wrong with auto-config, reverting the code changes restores the manual config — no deployment config rollback needed.

Once the migration is fully validated, the legacy `activemq.broker.*` properties can be removed from `application.properties` and replaced with direct `spring.activemq.*` values in a separate, low-risk follow-up.

---

## Design Note: Queue Bean Elimination

The original `JmsConfig` created three `Queue` beans (`ActiveMQQueue`) that were injected into service classes. This pattern is unnecessary in Spring Boot:

- **Spring JMS supports sending by destination name.** `JmsTemplate.send(String destinationName, MessageCreator)` uses `DynamicDestinationResolver` to resolve names at runtime.
- **`@JmsListener(destination = "...")` already uses names.** The existing `@JmsListener` annotation in `CertificateEventListenerServiceImpl` already uses a property-resolved string — not a `Queue` bean.
- **Queue beans couple code to ActiveMQ.** `ActiveMQQueue` is ActiveMQ-specific. Using destination name strings is vendor-neutral and more portable.

The migration moves all producers to use destination name strings, making the code simpler and aligned with Spring Boot conventions.
