# Step 14 — Replace Redis/Caching Manual Config with Spring Boot Auto-Configuration (Detailed Incremental Plan)

> **Scope:** Redis/caching only. ECS logging and Dockerfile are already done.

## Progress Tracker

> Update the Status column as each step is completed.
> Statuses: `⬜ TODO` | `🔄 IN PROGRESS` | `✅ DONE` | `⏭️ SKIPPED`

| Step      | Description                                                                                                        | Status | Commit/PR | Verified | Notes |
|-----------|--------------------------------------------------------------------------------------------------------------------|--------|-----------|----------|-------|
| **14.1**  | Add `spring-boot-starter-data-redis` dependency                                                                    | ⬜ TODO |           |          |       |
| **14.2**  | Map `redis.*` properties to Spring Boot `spring.data.redis.*` conventions                                          | ⬜ TODO |           |          |       |
| **14.3**  | Remove auto-config exclusion for `RedisAutoConfiguration`                                                          | ⬜ TODO |           |          |       |
| **14.4**  | Remove manual `RedisConnectionFactory` and `RedisTemplate` beans                                                   | ⬜ TODO |           |          |       |
| **14.5**  | Replace `CacheFactory` / `RedisCacheOptionsSetter` with `RedisCacheManagerBuilderCustomizer`                       | ⬜ TODO |           |          |       |
| **14.6**  | Simplify `IntygProxyServiceHsaCacheConfiguration` — remove `Cache` bean wiring                                     | ⬜ TODO |           |          |       |
| **14.7**  | Delete `BasicCacheConfiguration`, `CacheConfig`, `CacheFactory`, `RedisCacheOptionsSetter`, `ConnectionStringUtil` | ⬜ TODO |           |          |       |
| **14.8**  | Remove redundant `@EnableCaching` annotations                                                                      | ⬜ TODO |           |          |       |
| **14.9**  | Clean up dependencies and orphaned properties                                                                      | ⬜ TODO |           |          |       |
| **14.10** | Final verification — `./gradlew bootRun` + `./gradlew test`                                                        | ⬜ TODO |           |          |       |

**Deployment batches:**

- 🚀 **Batch 1:** Steps 14.1–14.2 (additive property mapping — manual config still active, Spring Boot auto-config still excluded)
- 🚀 **Batch 2:** Steps 14.3–14.6 (the actual switch — enable auto-config, remove manual beans, simplify cache config)
- 🚀 **Batch 3:** Steps 14.7–14.9 (delete dead code, clean dependencies)
- 🚀 **Batch 4:** Step 14.10 (final verification)

---

## Pre-conditions — Verified Current State

| Assumption                                                                                                   | Verified? | Actual State                                                                                                                                                                                |
|--------------------------------------------------------------------------------------------------------------|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Step 12 is complete — JMS auto-configured via Spring Boot                                                    | ✅         | `JmsConfig` deleted; `spring.activemq.*` and `spring.jms.*` drive JMS.                                                                                                                      |
| `BasicCacheConfiguration` manually configures `JedisConnectionFactory`, `RedisTemplate`, `RedisCacheManager` | ✅         | `BasicCacheConfiguration.java` creates all three beans. Uses `@Value`-injected `redis.*` properties. Profile-based topology (standalone / sentinel / cluster).                              |
| `CacheFactory` extends `RedisCacheManager` for dynamic cache creation                                        | ✅         | `CacheFactory.java` — thin subclass exposing `createCache()` methods.                                                                                                                       |
| `RedisCacheOptionsSetter` creates individual caches with custom TTL                                          | ✅         | `RedisCacheOptionsSetter.java` — delegates to `CacheFactory.createCache()`.                                                                                                                 |
| `CacheConfig` imports `BasicCacheConfiguration` and enables caching                                          | ✅         | `CacheConfig.java` — `@Configuration @EnableCaching @Import(BasicCacheConfiguration.class)`.                                                                                                |
| `IntygProxyServiceHsaCacheConfiguration` creates 5 HSA cache beans                                           | ✅         | Creates `Cache` beans for employee, healthCareUnit, healthCareUnitMembers, unit, healthCareProvider. Each with configurable TTL via `hsa.intygproxyservice.*.cache.expiry` properties.      |
| `ApplicationConfig` also has `@EnableCaching`                                                                | ✅         | Redundant with `CacheConfig` and `BasicCacheConfiguration`.                                                                                                                                 |
| `HsaServiceImpl` uses `@Cacheable` with cache name `"employeeNameCache"`                                     | ✅         | No explicit bean for this cache — relies on the `RedisCacheManager` auto-creating it. Uses default TTL (86400s).                                                                            |
| Auto-config exclusion for `RedisAutoConfiguration`                                                           | ✅         | Both `@SpringBootApplication(exclude={RedisAutoConfiguration.class})` and `spring.autoconfigure.exclude` in `application.properties`.                                                       |
| Redis properties use `redis.*` prefix (not Spring Boot `spring.data.redis.*`)                                | ✅         | `application.properties`: `redis.host`, `redis.port`, `redis.password`, `redis.cache.default_entry_expiry_time_in_seconds`, `redis.sentinel.master.name`.                                   |
| Jedis is the Redis client (switching to Lettuce)                                                             | ✅         | `integration-intyg-proxy-service/build.gradle`: `redis.clients:jedis` and `spring-data-redis` (no Lettuce). Will be replaced by `spring-boot-starter-data-redis` which defaults to Lettuce. |
| `ConnectionStringUtil` parses semicolon-separated host/port for sentinel/cluster                             | ✅         | Custom utility used only by `BasicCacheConfiguration`.                                                                                                                                      |
| Orphaned properties exist in `application.properties`                                                        | ✅         | `pu.cache.expiry`, `hsa.unit.cache.expiry`, `hsa.healthcareunit.cache.expiry`, `hsa.healhcareunitmembers.cache.expiry` — not referenced by any Java code.                                   |
| Dev overrides                                                                                                | ✅         | `application-dev.properties`: `redis.password=redis`                                                                                                                                        |

### Key Architectural Insight

The current Redis setup manually replicates what Spring Boot's `RedisAutoConfiguration` + `RedisCacheConfiguration` provide out of the box:

```
Current (manual — BasicCacheConfiguration)         Spring Boot auto-config equivalent
──────────────────────────────────────────         ──────────────────────────────────────
JedisConnectionFactory (profile-based)       →     RedisAutoConfiguration creates LettuceConnectionFactory
                                                   Standalone: spring.data.redis.host/port
                                                   Sentinel:   spring.data.redis.sentinel.*
                                                   Cluster:    spring.data.redis.cluster.*
RedisTemplate<Object, Object>               →     RedisAutoConfiguration creates RedisTemplate + StringRedisTemplate
CacheFactory (custom RedisCacheManager)      →     RedisCacheConfiguration auto-creates RedisCacheManager
  + RedisCacheOptionsSetter (per-cache TTL)          with RedisCacheManagerBuilderCustomizer for per-cache TTL
ConnectionStringUtil (semicolon-separated)   →     Spring Boot uses comma-separated lists natively
@EnableCaching (3 classes)                   →     CacheAutoConfiguration enables caching automatically
```

### Jedis → Lettuce Decision

**Decision: Switch to Lettuce.** Spring Boot 3.x defaults to Lettuce. The current Jedis usage relies entirely on defaults (no custom pool
config). Lettuce is non-blocking, thread-safe, and the actively maintained default. No pool config needed (Lettuce uses a single connection
with pipelining by default). The `@Cacheable` annotations and service code are client-agnostic — only the connection layer changes.

---

## Step 14.1 — Add `spring-boot-starter-data-redis` Dependency

**What:** Add the Spring Boot Redis starter to `integration-intyg-proxy-service/build.gradle`. This brings in `RedisAutoConfiguration`,
`RedisCacheConfiguration`, Lettuce, and Spring Data Redis — but they remain inactive because the auto-config exclusion is still in place.

**Why safe:** The auto-configuration is excluded via `@SpringBootApplication(exclude={RedisAutoConfiguration.class})` and
`spring.autoconfigure.exclude` in `application.properties`. Adding the starter just puts the classes on the classpath without activating
them. The existing manual `BasicCacheConfiguration` beans continue to drive Redis.

**Changes:**

1. **`integration-intyg-proxy-service/build.gradle`** — Replace explicit dependencies with the starter:
   ```groovy
   dependencies {
       // Replace these two:
       //   implementation "org.springframework.data:spring-data-redis"
       //   implementation "redis.clients:jedis"
       // With:
       implementation "org.springframework.boot:spring-boot-starter-data-redis"
       // ...existing deps...
   }
   ```

   Since the `web` module applies the `org.springframework.boot` plugin and version management is shared via BOM (
   `configureIntygBom.gradle`), the starter version resolves automatically.

   **Note:** The starter brings in **Lettuce** by default (not Jedis). At this point it doesn't matter — auto-config is excluded, and the
   manual `BasicCacheConfiguration` still creates `JedisConnectionFactory`. Once we remove the manual config (step 14.4), the connection
   factory switches to Lettuce automatically.

**Verify:**

```bash
./gradlew :integration-intyg-proxy-service:compileJava   # Compiles with starter on classpath
./gradlew test                                            # All tests pass; manual config still active
```

---

## Step 14.2 — Map `redis.*` Properties to Spring Boot Conventions

**What:** Add Spring Boot `spring.data.redis.*` properties alongside the existing `redis.*` properties. The manual config continues to read
the old properties; the new ones are inert until auto-config is enabled.

**Why safe:** These are purely additive property additions. No code reads `spring.data.redis.*` yet (auto-config is excluded). The manual
`BasicCacheConfiguration` still uses the `redis.*` properties.

**Changes:**

1. **`web/src/main/resources/application.properties`** — Add Spring Boot conventions next to legacy properties:

   ```properties
   ################################################
   #
   # HSA/PU Cache (redis) configuration
   #
   ################################################

   # --- Legacy properties (read by BasicCacheConfiguration — will be removed in 14.7) ---
   redis.host=127.0.0.1
   redis.port=6379
   redis.password=
   redis.cache.default_entry_expiry_time_in_seconds=86400
   redis.sentinel.master.name=master

   # --- Spring Boot Redis configuration ---
   spring.data.redis.host=${redis.host}
   spring.data.redis.port=${redis.port}
   spring.data.redis.password=${redis.password}
   spring.data.redis.timeout=PT1M

   # Spring Boot Cache — default TTL for caches not explicitly configured
   spring.cache.redis.time-to-live=${redis.cache.default_entry_expiry_time_in_seconds}s
   spring.cache.type=redis
   ```

   **Sentinel/Cluster:** These are activated by Spring profiles in production. The property mapping for sentinel and cluster can be
   documented here but should only be set in the relevant profile-specific property files:

   ```properties
   # In a sentinel profile config (e.g., application-redis-sentinel.properties):
   # spring.data.redis.sentinel.master=${redis.sentinel.master.name}
   # spring.data.redis.sentinel.nodes=host1:port1,host2:port2
   # spring.data.redis.sentinel.password=${redis.password}

   # In a cluster profile config (e.g., application-redis-cluster.properties):
   # spring.data.redis.cluster.nodes=host1:port1,host2:port2
   # spring.data.redis.cluster.max-redirects=3
   ```

   **Important format difference:** The current `ConnectionStringUtil` parses semicolon-separated values (`host1;host2` and `port1;port2` as
   separate properties). Spring Boot uses comma-separated `host:port` pairs. Deployment config files for sentinel/cluster must be updated to
   use Spring Boot format when the switch happens.

2. **`devops/dev/config/application-dev.properties`** — Add Spring Boot equivalent:
   ```properties
   redis.password=redis
   spring.data.redis.password=${redis.password}
   ```

   Or, once legacy properties are removed (step 14.9), simply:
   ```properties
   spring.data.redis.password=redis
   ```

**Verify:**

```bash
./gradlew compileJava   # New properties are syntactically valid
./gradlew test           # All tests pass; manual config still active; new properties are inert
```

---

## Step 14.3 — Remove Auto-Config Exclusion for `RedisAutoConfiguration`

**What:** Remove `RedisAutoConfiguration` from both `@SpringBootApplication(exclude=...)` and `spring.autoconfigure.exclude` in
`application.properties`. This activates Spring Boot's Redis auto-configuration.

**Why safe:** When both a manual bean and an auto-configured bean of the same type exist, Spring Boot **backs off** (the auto-config classes
use `@ConditionalOnMissingBean`). Since `BasicCacheConfiguration` still defines `jedisConnectionFactory()`, `redisTemplate()`, and
`cacheManager()`, those manual beans take priority. The auto-config sees them and doesn't create duplicates. The effect of this step is: *
*nothing changes at runtime** — but auto-config is now ready to take over once the manual beans are removed.

**Changes:**

1. **`web/src/main/java/se/inera/intyg/intygstjanst/IntygstjanstApplication.java`** — Remove the exclusion:
   ```java
   @SpringBootApplication(
       scanBasePackages = {
           "se.inera.intyg.intygstjanst",
           "se.inera.intyg.intygstjanst.integration.intygproxyservice",
           "se.inera.intyg.intygstjanst.pu.integration.intygproxyservice",
           "se.inera.intyg.common.support.modules.support.api",
           "se.inera.intyg.common.services",
           "se.inera.intyg.common",
           "se.inera.intyg.common.support.services",
           "se.inera.intyg.common.util.integration.json"
       }
       // No more exclude — auto-config for Redis is now active (but backs off due to manual beans)
   )
   ```

2. **`web/src/main/resources/application.properties`** — Remove (or empty) the `spring.autoconfigure.exclude` property:
   ```properties
   # DELETE this block:
   # spring.autoconfigure.exclude=\
   #   org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
   ```

   If there are no other exclusions left, delete the property entirely.

**Verify:**

```bash
./gradlew bootRun       # App starts; manual beans still active; auto-config backs off
./gradlew test           # All tests pass
```

---

## Step 14.4 — Remove Manual `RedisConnectionFactory` and `RedisTemplate` Beans

**What:** Remove the `jedisConnectionFactory()` and `redisTemplate()` `@Bean` methods from `BasicCacheConfiguration`. Spring Boot
auto-config now creates the `RedisConnectionFactory` (Lettuce by default) and `RedisTemplate` beans.

**Why safe:** After step 14.3, auto-config is active but backs off because of the manual beans. Removing the manual beans allows auto-config
to create them instead. The `cacheManager()` bean still exists in `BasicCacheConfiguration` and will be migrated in the next step.

**Important — Jedis to Lettuce:** After this step, the connection factory switches from `JedisConnectionFactory` to
`LettuceConnectionFactory` (Spring Boot default). The `@Cacheable` annotations, `RedisTemplate`, and `RedisCacheManager` are all
connection-factory-agnostic — they program against the `RedisConnectionFactory` interface. No service code changes needed.

**Changes:**

1. **`BasicCacheConfiguration.java`** — Remove:
    - `jedisConnectionFactory()` method and all three private helper methods (`standAloneConnectionFactory()`,
      `sentinelConnectionFactory()`, `clusterConnectionFactory()`)
    - `redisTemplate()` method
    - `propertySourcesPlaceholderConfigurer()` static bean (Spring Boot already provides one)
    - All `@Value` fields for connection config (`redisHost`, `redisPort`, `redisPassword`, `redisSentinelMasterName`, `redisReadTimeout`,
      `redisClusterNodes`, `redisClusterPassword`, `redisClusterMaxRedirects`, `redisClusterReadTimeout`)
    - The `Environment` resource injection
    - All removed imports (`JedisConnectionFactory`, `JedisClientConfiguration`, `RedisStandaloneConfiguration`,
      `RedisSentinelConfiguration`, `RedisClusterConfiguration`, `RedisTemplate`, `StringRedisSerializer`, `ConnectionStringUtil`,
      `Environment`, etc.)

   The class now only contains the `cacheManager()` and `redisCacheOptionsSetter()` beans (to be removed in 14.5):

   ```java
   @Configuration
   @EnableCaching
   public class BasicCacheConfiguration {

       @Value("${redis.cache.default_entry_expiry_time_in_seconds}")
       long defaultEntryExpiry;

       @Bean
       @DependsOn("cacheManager")
       public RedisCacheOptionsSetter redisCacheOptionsSetter() {
           return new RedisCacheOptionsSetter();
       }

       @Bean
       public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
           return new CacheFactory(
               RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory),
               RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(defaultEntryExpiry))
           );
       }
   }
   ```

   **Note:** `cacheManager()` now takes `RedisConnectionFactory` as a parameter (injected by Spring — provided by auto-config) instead of
   calling `jedisConnectionFactory()` directly.

**Verify:**

```bash
./gradlew bootRun       # App starts with auto-configured Lettuce connection factory
./gradlew test           # All tests pass
# Optionally: verify Redis connectivity manually if a Redis instance is available
```

---

## Step 14.5 — Replace `CacheFactory` / `RedisCacheOptionsSetter` with `RedisCacheManagerBuilderCustomizer`

**What:** Replace the custom `CacheFactory` (subclass of `RedisCacheManager`) and `RedisCacheOptionsSetter` with a Spring Boot
`RedisCacheManagerBuilderCustomizer` bean. This is the idiomatic Spring Boot way to configure per-cache TTLs while letting auto-config
create the `RedisCacheManager`.

**Why:** Spring Boot's `RedisCacheAutoConfiguration` auto-creates a `RedisCacheManager` if no `CacheManager` bean exists. It also applies
any `RedisCacheManagerBuilderCustomizer` beans to the builder before creating the manager. By removing the manual `cacheManager()` bean and
providing a customizer instead, we let Spring Boot own the `RedisCacheManager` lifecycle.

**Why safe:** The `@Cacheable` annotations in the service layer only reference cache names (strings). They don't care how the
`RedisCacheManager` is created — only that a cache with the matching name exists. The customizer pre-configures the caches with the same
TTLs as before.

**Changes:**

1. **Create `RedisCacheConfig.java`** — Replace `IntygProxyServiceHsaCacheConfiguration` with a single customizer bean:

   **`integration-intyg-proxy-service/src/main/java/se/inera/intyg/infra/integration/intygproxyservice/configuration/RedisCacheConfig.java`
   **
   ```java
   package se.inera.intyg.intygstjanst.integration.intygproxyservice.configuration;

   import static constants.se.inera.intyg.intygstjanst.integration.hsa.intygproxyservice.HsaIntygProxyServiceConstants.EMPLOYEE_CACHE_NAME;
   import static constants.se.inera.intyg.intygstjanst.integration.hsa.intygproxyservice.HsaIntygProxyServiceConstants.HEALTH_CARE_PROVIDER_CACHE_NAME;
   import static constants.se.inera.intyg.intygstjanst.integration.hsa.intygproxyservice.HsaIntygProxyServiceConstants.HEALTH_CARE_UNIT_CACHE_NAME;
   import static constants.se.inera.intyg.intygstjanst.integration.hsa.intygproxyservice.HsaIntygProxyServiceConstants.HEALTH_CARE_UNIT_MEMBERS_CACHE_NAME;
   import static constants.se.inera.intyg.intygstjanst.integration.hsa.intygproxyservice.HsaIntygProxyServiceConstants.UNIT_CACHE_NAME;

   import java.time.Duration;
   import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.Configuration;
   import org.springframework.data.redis.cache.RedisCacheConfiguration;

   @Configuration
   public class RedisCacheConfig {

       @Bean
       public RedisCacheManagerBuilderCustomizer hsaCacheCustomizer(
               @Value("${hsa.intygproxyservice.getemployee.cache.expiry:60}")
               long employeeExpiry,
               @Value("${hsa.intygproxyservice.gethealthcareunit.cache.expiry:60}")
               long healthCareUnitExpiry,
               @Value("${hsa.intygproxyservice.gethealthcareunitmembers.cache.expiry:60}")
               long healthCareUnitMembersExpiry,
               @Value("${hsa.intygproxyservice.getunit.cache.expiry:60}")
               long unitExpiry,
               @Value("${hsa.intygproxyservice.gethealthcareprovider.cache.expiry:60}")
               long healthCareProviderExpiry) {

           return builder -> builder
               .withCacheConfiguration(EMPLOYEE_CACHE_NAME,
                   cacheConfig(employeeExpiry))
               .withCacheConfiguration(HEALTH_CARE_UNIT_CACHE_NAME,
                   cacheConfig(healthCareUnitExpiry))
               .withCacheConfiguration(HEALTH_CARE_UNIT_MEMBERS_CACHE_NAME,
                   cacheConfig(healthCareUnitMembersExpiry))
               .withCacheConfiguration(UNIT_CACHE_NAME,
                   cacheConfig(unitExpiry))
               .withCacheConfiguration(HEALTH_CARE_PROVIDER_CACHE_NAME,
                   cacheConfig(healthCareProviderExpiry));
       }

       private RedisCacheConfiguration cacheConfig(long ttlSeconds) {
           return RedisCacheConfiguration.defaultCacheConfig()
               .entryTtl(Duration.ofSeconds(ttlSeconds));
       }
   }
   ```

   **How this works with Spring Boot auto-config:**
    - `spring.cache.type=redis` + `spring.cache.redis.time-to-live=86400s` (from step 14.2) sets the **default** TTL for any cache not
      explicitly configured.
    - The `RedisCacheManagerBuilderCustomizer` bean configures the 5 HSA caches with their specific TTLs.
    - The `employeeNameCache` used by `HsaServiceImpl` doesn't need explicit config — it gets the default TTL (86400s) automatically when
      the `RedisCacheManager` creates it on first use.

2. **Remove the manual `cacheManager()` bean from `BasicCacheConfiguration.java`** — This is the key trigger: without a `CacheManager` bean,
   Spring Boot auto-creates `RedisCacheManager`.

**Verify:**

```bash
./gradlew bootRun       # App starts; RedisCacheManager auto-configured with customizer
./gradlew test           # All tests pass
```

---

## Step 14.6 — Simplify `IntygProxyServiceHsaCacheConfiguration` — Remove `Cache` Bean Wiring

**What:** The `IntygProxyServiceHsaCacheConfiguration` class currently creates 5 `Cache` `@Bean` methods. These are no longer needed — the
`RedisCacheManagerBuilderCustomizer` from step 14.5 pre-configures the caches, and `@Cacheable` annotations create them on demand. Delete
the class.

**Why safe:** `@Cacheable(cacheNames = "hsaIntygProxyServiceEmployeeCache")` doesn't require an explicit `Cache` bean. Spring's caching
infrastructure looks up caches by name from the `CacheManager`. Since the `RedisCacheManagerBuilderCustomizer` registered the configuration
for these cache names, the `RedisCacheManager` will create them on first use with the correct TTL.

**Changes:**

1. **Delete** `IntygProxyServiceHsaCacheConfiguration.java` (main).
2. **Delete** `IntygProxyServiceHsaCacheConfiguration.java` (test copy — identical file exists in test sources).

**Verify:**

```bash
./gradlew test           # All tests pass; caches are created on demand by RedisCacheManager
```

---

## Step 14.7 — Delete Manual Config Classes

**What:** Delete the now-unused manual configuration classes and utilities.

**Changes — Delete these files:**

| File                                                                                                                              | Reason                                                    |
|-----------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------|
| `integration-intyg-proxy-service/src/main/java/se/inera/intyg/infra/rediscache/core/BasicCacheConfiguration.java`                 | Fully replaced by Spring Boot auto-config                 |
| `integration-intyg-proxy-service/src/main/java/se/inera/intyg/infra/rediscache/core/CacheFactory.java`                            | No longer needed — auto-configured `RedisCacheManager`    |
| `integration-intyg-proxy-service/src/main/java/se/inera/intyg/infra/rediscache/core/RedisCacheOptionsSetter.java`                 | Replaced by `RedisCacheManagerBuilderCustomizer`          |
| `integration-intyg-proxy-service/src/main/java/se/inera/intyg/infra/rediscache/core/util/ConnectionStringUtil.java`               | Only used by `BasicCacheConfiguration`                    |
| `integration-intyg-proxy-service/src/main/java/se/inera/intyg/infra/integration/intygproxyservice/configuration/CacheConfig.java` | Only purpose was `@Import(BasicCacheConfiguration.class)` |

This removes the entire `se.inera.intyg.intygstjanst.rediscache.core` package.

**Verify:**

```bash
./gradlew compileJava   # No compilation errors (no references to deleted classes)
./gradlew test           # All tests pass
```

---

## Step 14.8 — Remove Redundant `@EnableCaching` Annotations

**What:** Spring Boot's `CacheAutoConfiguration` automatically enables caching (it provides `@EnableCaching`). Remove redundant
`@EnableCaching` from `ApplicationConfig.java`.

**Why safe:** Spring Boot already enables caching when `spring.cache.type=redis` is set and a `CacheManager` bean exists. Multiple
`@EnableCaching` annotations are harmless but noisy.

**Changes:**

1. **`web/src/main/java/se/inera/intyg/intygstjanst/config/ApplicationConfig.java`** — Remove `@EnableCaching`:
   ```java
   @Configuration
   // @EnableCaching — removed; Spring Boot CacheAutoConfiguration enables it
   @EnableAspectJAutoProxy
   public class ApplicationConfig {
   ```
   Also remove the import `org.springframework.cache.annotation.EnableCaching`.

**Verify:**

```bash
./gradlew test           # All tests pass; caching still works via auto-config
```

---

## Step 14.9 — Clean Up Dependencies and Orphaned Properties

**What:** Remove redundant dependencies and legacy properties that are no longer referenced.

**Changes:**

1. **`integration-intyg-proxy-service/build.gradle`** — The starter added in 14.1 already replaced the explicit dependencies. Verify that
   `spring-data-redis` and `redis.clients:jedis` are removed (done in 14.1). Also remove `com.google.guava:guava` if it was only used by
   `ConnectionStringUtil` (check other usages first).

2. **`web/src/main/resources/application.properties`** — Remove legacy `redis.*` properties and orphaned cache properties:

   **Remove:**
   ```properties
   redis.host=127.0.0.1
   redis.port=6379
   redis.password=
   redis.cache.default_entry_expiry_time_in_seconds=86400
   redis.sentinel.master.name=master
   pu.cache.expiry=86400
   hsa.unit.cache.expiry=86400
   hsa.healthcareunit.cache.expiry=86400
   hsa.healhcareunitmembers.cache.expiry=86400
   ```

   **Keep (from step 14.2, now standalone):**
   ```properties
   # Redis
   spring.data.redis.host=127.0.0.1
   spring.data.redis.port=6379
   spring.data.redis.password=
   spring.data.redis.timeout=PT1M

   # Cache
   spring.cache.type=redis
   spring.cache.redis.time-to-live=86400s
   ```

   **Update `spring.data.redis.*` properties** to no longer reference `${redis.*}` placeholders (since the legacy properties are deleted):
   ```properties
   spring.data.redis.host=127.0.0.1
   spring.data.redis.port=6379
   spring.data.redis.password=
   ```

3. **`devops/dev/config/application-dev.properties`** — Replace:
   ```properties
   # Replace: redis.password=redis
   spring.data.redis.password=redis
   ```

4. **Verify no remaining references to `redis.*` legacy properties:**
   ```bash
   grep -rn 'redis\.host\|redis\.port\|redis\.password\|redis\.cache\.\|redis\.sentinel\.\|redis\.cluster\.\|redis\.read\.timeout' \
     --include="*.java" --include="*.properties" --include="*.xml" --include="*.yml" \
     . | grep -v '/build/' | grep -v 'spring.data.redis'
   ```

**Verify:**

```bash
./gradlew compileJava   # Clean compile with no stale property references
./gradlew test           # All tests pass
```

---

## Step 14.10 — Final Verification

**What:** Full end-to-end verification that Redis caching works with Spring Boot auto-configuration.

**Verify:**

```bash
# 1. Clean build + all tests
./gradlew clean test

# 2. Boot the application
./gradlew bootRun

# 3. Check that the app starts without errors (look for Redis connection in logs)
# Expected: LettuceConnectionFactory initialized, RedisCacheManager created

# 4. Verify caching works:
#    - Call an HSA endpoint twice — second call should be faster (cached)
#    - Check Redis (redis-cli) that cache keys are created

# 5. Docker build + run (if applicable)
docker build -t intygstjanst .
docker run --rm -p 8080:8080 intygstjanst
```

---

## Summary: Files Modified/Deleted

| File                                                        | Action                                                        | Step       |
|-------------------------------------------------------------|---------------------------------------------------------------|------------|
| `integration-intyg-proxy-service/build.gradle`              | ✏️ Modify — replace explicit redis deps with starter          | 14.1       |
| `web/src/main/resources/application.properties`             | ✏️ Modify — add `spring.data.redis.*`, `spring.cache.*`       | 14.2       |
| `web/src/main/resources/application.properties`             | ✏️ Modify — remove `spring.autoconfigure.exclude`             | 14.3       |
| `IntygstjanstApplication.java`                              | ✏️ Modify — remove `exclude = {RedisAutoConfiguration.class}` | 14.3       |
| `BasicCacheConfiguration.java`                              | ✏️ Modify (14.4) → 🗑️ Delete (14.7)                          | 14.4, 14.7 |
| `RedisCacheConfig.java`                                     | ✨ Create — `RedisCacheManagerBuilderCustomizer` bean          | 14.5       |
| `IntygProxyServiceHsaCacheConfiguration.java` (main + test) | 🗑️ Delete                                                    | 14.6       |
| `CacheFactory.java`                                         | 🗑️ Delete                                                    | 14.7       |
| `RedisCacheOptionsSetter.java`                              | 🗑️ Delete                                                    | 14.7       |
| `ConnectionStringUtil.java`                                 | 🗑️ Delete                                                    | 14.7       |
| `CacheConfig.java`                                          | 🗑️ Delete                                                    | 14.7       |
| `ApplicationConfig.java`                                    | ✏️ Modify — remove `@EnableCaching`                           | 14.8       |
| `application.properties`                                    | ✏️ Modify — remove legacy `redis.*` props                     | 14.9       |
| `application-dev.properties`                                | ✏️ Modify — `redis.password` → `spring.data.redis.password`   | 14.9       |

## Property Mapping Reference

| Legacy Property                                             | Spring Boot Property                                       | Default     |
|-------------------------------------------------------------|------------------------------------------------------------|-------------|
| `redis.host`                                                | `spring.data.redis.host`                                   | `127.0.0.1` |
| `redis.port`                                                | `spring.data.redis.port`                                   | `6379`      |
| `redis.password`                                            | `spring.data.redis.password`                               | (empty)     |
| `redis.read.timeout`                                        | `spring.data.redis.timeout`                                | `PT1M`      |
| `redis.cache.default_entry_expiry_time_in_seconds`          | `spring.cache.redis.time-to-live`                          | `86400s`    |
| `redis.sentinel.master.name`                                | `spring.data.redis.sentinel.master`                        | `master`    |
| `redis.host` (semicolon-sep) + `redis.port` (semicolon-sep) | `spring.data.redis.sentinel.nodes` (comma-sep `host:port`) | —           |
| `redis.cluster.nodes` (semicolon-sep)                       | `spring.data.redis.cluster.nodes` (comma-sep)              | —           |
| `redis.cluster.max.redirects`                               | `spring.data.redis.cluster.max-redirects`                  | `3`         |
| `redis.cluster.password`                                    | `spring.data.redis.password`                               | —           |
| `redis.cluster.read.timeout`                                | `spring.data.redis.timeout`                                | `PT1M`      |

## Risks and Mitigations

| Risk                                                         | Impact                                                             | Mitigation                                                                                     |
|--------------------------------------------------------------|--------------------------------------------------------------------|------------------------------------------------------------------------------------------------|
| Jedis → Lettuce client switch                                | Low — all code programs against `RedisConnectionFactory` interface | Test with `bootRun` + Redis instance; Lettuce is Spring Boot's default and actively maintained |
| Sentinel/Cluster connection string format change (`;` → `,`) | Medium — deployment config files must update                       | Document in deployment runbook; validate in staging                                            |
| `employeeNameCache` has no explicit TTL config               | Low — it already uses the default TTL (86400s)                     | `spring.cache.redis.time-to-live=86400s` preserves the same default                            |
| Test config may need Redis auto-config exclusion             | Low — integration-intyg-proxy-service tests may not have Redis     | Check test configs; add `@AutoConfigureCache` or exclude in test if needed                     |
