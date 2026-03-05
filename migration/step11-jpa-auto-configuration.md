# Step 11 — Replace JPA Manual Config with Spring Boot Auto-Configuration (Detailed Incremental Plan)

## Progress Tracker

> Update the Status column as each step is completed.
> Statuses: `⬜ TODO` | `🔄 IN PROGRESS` | `✅ DONE` | `⏭️ SKIPPED`

| Step      | Description                                                                | Status  | Commit/PR | Verified | Notes |
|-----------|----------------------------------------------------------------------------|---------|-----------|----------|-------|
| **11.1**  | Add `spring-boot-starter-data-jpa` dependency                             | ✅ DONE |           | ✅        |       |
| **11.2**  | Map `db.*` / `hibernate.*` properties to Spring Boot conventions           | ✅ DONE |           | ✅        |       |
| **11.3**  | Map Liquibase properties to Spring Boot conventions                        | ✅ DONE |           | ✅        |       |
| **11.4**  | Remove auto-config exclusions for JPA/DataSource/Liquibase                | ⬜ TODO |           |          |       |
| **11.5**  | Add `@EntityScan` and `@EnableJpaRepositories` on main app class          | ⬜ TODO |           |          |       |
| **11.6**  | Remove `JpaConfigBase`, `JpaConfig`, and `JpaConstants` (keep constants)  | ⬜ TODO |           |          |       |
| **11.7**  | Remove `persistence.xml`                                                   | ⬜ TODO |           |          |       |
| **11.8**  | Clean up `@PersistenceContext(unitName=...)` annotations                   | ⬜ TODO |           |          |       |
| **11.9**  | Refactor `TransactionTemplate` → `@Transactional` in test/dev classes     | ⬜ TODO |           |          |       |
| **11.10** | Remove redundant explicit dependencies from `persistence/build.gradle`    | ⬜ TODO |           |          |       |
| **11.11** | Update persistence test infrastructure (`TestConfig`, `TestSupport`, `test.properties`) | ⬜ TODO |           |          |       |
| **11.12** | Update `ApplicationConfig` — remove `@DependsOn("dbUpdate")`             | ⬜ TODO |           |          |       |
| **11.13** | Final verification — `./gradlew bootRun` + `./gradlew test`               | ⬜ TODO |           |          |       |

**Deployment batches:**

- 🚀 **Batch 1:** Steps 11.1–11.3 (additive property mapping — manual config still active, Spring Boot auto-config still excluded)
- 🚀 **Batch 2:** Steps 11.4–11.5 (the actual switch — enable auto-config, disable manual config)
- 🚀 **Batch 3:** Steps 11.6–11.9 (remove dead code — `JpaConfigBase`, `JpaConfig`, `JpaConstants`, `persistence.xml`, redundant deps, refactor transactions)
- 🚀 **Batch 4:** Steps 11.10–11.13 (test infrastructure cleanup + final verification)

---

## Pre-conditions — Verified Current State

Before planning, the following assumptions from the incremental migration plan were verified against the actual codebase:

| Assumption                                                                  | Verified? | Actual State                                                                                                                                                                                               |
|-----------------------------------------------------------------------------|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Step 10 is complete — app runs via `./gradlew bootRun`                     | ✅         | `IntygstjanstApplication.java` exists with `@SpringBootApplication`. `web/build.gradle` has `org.springframework.boot` plugin. `bootRun` task configured.                                                 |
| `JpaConfigBase` manually configures DataSource, EntityManagerFactory, Tx    | ✅         | `JpaConfigBase.java` creates `HikariDataSource`, `LocalContainerEntityManagerFactoryBean`, `JpaTransactionManager`, and `SpringLiquibase` beans using `@Value`-injected `db.*`/`hibernate.*` properties.  |
| `JpaConfig` extends `JpaConfigBase` with `@Profile("!h2")`                | ✅         | `JpaConfig.java` is `@Configuration @EnableJpaRepositories @Profile("!h2")`. Extends `JpaConfigBase`.                                                                                                     |
| `JpaConstants` defines persistence unit name and entity scan packages       | ✅         | `PERSISTANCE_UNIT_NAME = "IneraCertificate"`, `BASE_PACKAGE_TO_SCAN = "se.inera.intyg.intygstjanst.persistence.model"`, `REPOSITORY_PACKAGE_TO_SCAN = "...model.dao"`.                                   |
| `persistence.xml` lists 9 entity classes in the `IneraCertificate` PU       | ✅         | `META-INF/persistence.xml` declares: `ApprovedReceiver`, `Arende`, `Certificate`, `CertificateMetaData`, `Relation`, `OriginalCertificate`, `SjukfallCertificate`, `SjukfallCertificateWorkCapacity`, `Reko`. |
| There is a 10th entity `CertificateType` NOT in `persistence.xml`           | ✅         | `CertificateType.java` has `@Entity @Table(name = "REF_CERTIFICATE_TYPE")` but is NOT listed in `persistence.xml`. It is discovered via `packagesToScan` on the `EntityManagerFactory`.                   |
| `@PersistenceContext(unitName = JpaConstants.PERSISTANCE_UNIT_NAME)` used   | ✅         | 7 production classes + 2 test classes use `@PersistenceContext(unitName = "IneraCertificate")`. 3 others use bare `@PersistenceContext`.                                                                  |
| Auto-config exclusions are set for JPA/DataSource/Liquibase                 | ✅         | Both `@SpringBootApplication(exclude=...)` and `application.properties` `spring.autoconfigure.exclude` list `DataSourceAutoConfiguration`, `HibernateJpaAutoConfiguration`, `LiquibaseAutoConfiguration`.  |
| DB properties use `db.*` prefix (not Spring Boot `spring.datasource.*`)     | ✅         | `application.properties`: `db.driver`, `db.url`, `db.username`, `db.password`, `db.pool.maxSize`. Dev overrides in `application-dev.properties`.                                                          |
| Hibernate properties use `hibernate.*` prefix directly                      | ✅         | `application.properties`: `hibernate.dialect`, `hibernate.hbm2ddl.auto`, `hibernate.show_sql`, `hibernate.format_sql`.                                                                                    |
| Liquibase configured manually in `JpaConfigBase.initDb()`                   | ✅         | `SpringLiquibase` bean named `"dbUpdate"` reads `changelog/changelog.xml`. `ApplicationConfig.moduleRegistry()` has `@DependsOn("dbUpdate")`.                                                             |
| Persistence module has explicit HikariCP + Hibernate deps                   | ✅         | `persistence/build.gradle`: `com.zaxxer:HikariCP`, `org.hibernate.orm:hibernate-core`, `org.hibernate.orm:hibernate-hikaricp`, `org.liquibase:liquibase-core`, `org.springframework.data:spring-data-jpa`. |
| Persistence tests use `@ContextConfiguration(classes = TestConfig.class)`   | ✅         | `TestSupport.java` base class uses `@ExtendWith(SpringExtension.class) @ContextConfiguration(classes = TestConfig.class) @ActiveProfiles("dev")`. `TestConfig` scans `persistence` and `logging` packages. |
| Persistence test properties use `db.*` prefix for H2                        | ✅         | `test.properties`: `db.driver=org.h2.Driver`, `db.url=jdbc:h2:mem:...`, `db.pool.maxSize=3`.                                                                                                              |
| No H2 `@Profile` config class exists (despite `@Profile("!h2")` on `JpaConfig`) | ✅    | No class with `@Profile("h2")` found. The `!h2` profile on `JpaConfig` appears to be a leftover from a removed H2-specific config variant. Tests activate profile `"dev"`, not `"h2"`.                  |
| `spring-boot-starter-data-jpa` not yet in dependencies                      | ✅         | Not present in `persistence/build.gradle` or `web/build.gradle`. Only `spring-data-jpa` and manual Hibernate deps.                                                                                        |

### Key Architectural Insight

The current JPA setup manually replicates what Spring Boot's `DataSourceAutoConfiguration` + `HibernateJpaAutoConfiguration` + `LiquibaseAutoConfiguration` provide out of the box:

```
Current (manual — JpaConfigBase)                Spring Boot auto-config equivalent
─────────────────────────────────                ─────────────────────────────────────
HikariDataSource bean (db.*)             →       spring.datasource.* properties
LocalContainerEntityManagerFactoryBean   →       HibernateJpaAutoConfiguration + @EntityScan
  + HibernateJpaVendorAdapter
  + setPersistenceUnitName(...)
  + setPackagesToScan(...)
  + additionalProperties()
JpaTransactionManager bean               →       JpaTransactionAutoConfiguration
SpringLiquibase bean (dbUpdate)          →       LiquibaseAutoConfiguration (spring.liquibase.*)
```

**The migration strategy is:**

1. **First**, add the Spring Boot `spring.datasource.*` / `spring.jpa.*` / `spring.liquibase.*` properties alongside the existing `db.*` / `hibernate.*` properties (the manual config still reads the old ones).
2. **Then**, remove the auto-config exclusions and add `@EntityScan` + `@EnableJpaRepositories` so Spring Boot creates the beans.
3. **Finally**, delete the now-dead manual config classes, `persistence.xml`, and redundant dependencies.

This ensures the app is never in a broken state — the manual beans exist until auto-config takes over.

---

## Step 11.1 — Add `spring-boot-starter-data-jpa` Dependency

**What:** Add the Spring Boot JPA starter to `persistence/build.gradle`. This brings in Hibernate, HikariCP, Spring Data JPA, and the JPA auto-configuration classes — but they remain inactive because the auto-config exclusions are still in place.

**Why safe:** The auto-configuration classes are excluded via `@SpringBootApplication(exclude=...)` and `spring.autoconfigure.exclude` in `application.properties`. Adding the starter just puts them on the classpath without activating them. The existing manual `JpaConfigBase` beans continue to drive JPA.

**Changes:**

1. **`persistence/build.gradle`** — Add the starter dependency:
   ```groovy
   dependencies {
       implementation "org.springframework.boot:spring-boot-starter-data-jpa"
       // ...existing deps...
   }
   ```

   **Note:** The `persistence` module currently doesn't apply the `org.springframework.boot` plugin (only `web` does), so Spring Boot dependency management may not resolve the starter version automatically. Two options:

   a. **Preferred:** The root `build.gradle` already applies the Spring Boot plugin and `allprojects` provides the platform BOM. Verify that `spring-boot-starter-data-jpa` resolves transitively via the `intygBomVersion` platform. If not, add the Spring Boot BOM explicitly:
      ```groovy
      implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
      implementation "org.springframework.boot:spring-boot-starter-data-jpa"
      ```

   b. **Alternative:** If the Spring Boot BOM is already available to subprojects (check `./gradlew :intygstjanst-persistence:dependencies`), the version-less dependency will resolve fine.

**Verify:**

```bash
./gradlew clean build                                    # Compiles, all tests pass
./gradlew :intygstjanst-persistence:dependencies | grep spring-boot-starter-data-jpa  # Starter on classpath
```

**Risks:**

- Version conflict between explicitly declared `hibernate-core`/`HikariCP` and versions from the starter. Run `./gradlew :intygstjanst-persistence:dependencies` to check. Conflicts resolved in Step 11.9 when explicit deps are removed.

---

## Step 11.2 — Map `db.*` / `hibernate.*` Properties to Spring Boot Conventions

**What:** Add Spring Boot–conventional `spring.datasource.*` and `spring.jpa.*` properties to `application.properties`, mapped from the existing `db.*` / `hibernate.*` values. Keep the old properties too — they're still read by `JpaConfigBase` (which is still active).

**Why safe:** Adding new properties that nobody reads yet has zero effect. The auto-config that would read them is still excluded.

**Changes:**

1. **`web/src/main/resources/application.properties`** — Add Spring Boot datasource and JPA properties below the existing DB properties section:

   ```properties
   # ============================================================
   # Spring Boot DataSource configuration (Step 11)
   # Maps from the legacy db.* properties to Spring Boot conventions.
   # ============================================================
   spring.datasource.driver-class-name=${db.driver}
   spring.datasource.url=${db.url}
   spring.datasource.username=${db.username}
   spring.datasource.password=${db.password}

   # HikariCP pool settings
   spring.datasource.hikari.maximum-pool-size=${db.pool.maxSize}
   spring.datasource.hikari.minimum-idle=3
   spring.datasource.hikari.idle-timeout=15000
   spring.datasource.hikari.connection-timeout=3000
   spring.datasource.hikari.auto-commit=false

   # ============================================================
   # Spring Boot JPA/Hibernate configuration (Step 11)
   # ============================================================
   spring.jpa.database-platform=${hibernate.dialect}
   spring.jpa.hibernate.ddl-auto=${hibernate.hbm2ddl.auto:none}
   spring.jpa.show-sql=${hibernate.show_sql:false}
   spring.jpa.properties.hibernate.format_sql=${hibernate.format_sql:false}
   spring.jpa.properties.hibernate.id.new_generator_mappings=false
   spring.jpa.properties.hibernate.enable_lazy_load_no_trans=false
   spring.jpa.open-in-view=false
   ```

   **Key decisions:**

   - `spring.datasource.*` properties reference `${db.*}` so all existing environment/profile overrides continue to work without duplication.
   - `spring.jpa.hibernate.ddl-auto` defaults to `none` if `hibernate.hbm2ddl.auto` is empty (current prod default is empty string).
   - `spring.jpa.open-in-view=false` — explicitly disable the OpenEntityManagerInViewFilter. Spring Boot defaults to `true`, which would change behavior. The manual config never had OEIV enabled.
   - `spring.jpa.properties.hibernate.id.new_generator_mappings=false` — preserves the existing Hibernate setting from `JpaConfigBase.additionalProperties()`.

2. **`devops/dev/config/application-dev.properties`** — No changes needed. The `db.*` properties defined there will be picked up by the `${db.*}` references in the new `spring.datasource.*` properties.

3. **`persistence/src/test/resources/test.properties`** — No changes yet (handled in Step 11.10).

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew bootRun         # Starts — still using manual JpaConfigBase
```

The new properties exist but are inert — the auto-configurations that read them are still excluded.

---

## Step 11.3 — Map Liquibase Properties to Spring Boot Conventions

**What:** Add `spring.liquibase.*` properties to `application.properties`, mapping the Liquibase changelog location from the hardcoded value in `JpaConfigBase`.

**Why safe:** Same as 11.2 — `LiquibaseAutoConfiguration` is still excluded.

**Changes:**

1. **`web/src/main/resources/application.properties`** — Add Liquibase properties:

   ```properties
   # ============================================================
   # Spring Boot Liquibase configuration (Step 11)
   # ============================================================
   spring.liquibase.change-log=classpath:changelog/changelog.xml
   ```

   **Key decisions:**

   - The changelog path matches the hardcoded `LIQUIBASE_SCRIPT` constant in `JpaConfigBase`: `"changelog/changelog.xml"`.
   - Spring Boot's `LiquibaseAutoConfiguration` will use the auto-configured `DataSource` — no explicit datasource property needed.
   - No `spring.liquibase.enabled` property needed — Spring Boot enables Liquibase when it detects the `SpringLiquibase` class on the classpath (which it already is via the existing `liquibase-core` dependency).

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
```

---

## Step 11.4 — Remove Auto-Config Exclusions for JPA/DataSource/Liquibase

**What:** Remove `DataSourceAutoConfiguration`, `HibernateJpaAutoConfiguration`, and `LiquibaseAutoConfiguration` from the auto-configuration exclusion lists. This is **the moment Spring Boot takes over JPA**.

**Why this works:** Spring Boot auto-configuration checks for existing beans. When it finds an existing `DataSource`, `EntityManagerFactory`, or `SpringLiquibase` bean, it **backs off**. However, we're about to delete those manual beans (in Step 11.6). To avoid a chicken-and-egg problem, we do this step **together with** Step 11.5 (which tells Spring Boot where to scan for entities/repositories) and **before** Step 11.6 (which deletes the manual config).

**The transition moment:** After this step, **both** the manual beans (from `JpaConfigBase`) and the auto-configured beans compete. Spring Boot's auto-config backs off when it finds existing beans of the same type. So:

- `DataSourceAutoConfiguration` backs off because `JpaConfigBase.dataSource()` provides a `DataSource` bean.
- `HibernateJpaAutoConfiguration` backs off because `JpaConfigBase.entityManagerFactory()` provides an `EntityManagerFactory` bean.
- `LiquibaseAutoConfiguration` backs off because `JpaConfigBase.initDb()` provides a `SpringLiquibase` bean.

This means the app still uses the manual beans — but we can now safely remove them in Step 11.6 and the auto-config will activate.

**Changes:**

1. **`IntygstjanstApplication.java`** — Remove the three JPA-related exclusions:

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
           // REMOVED: DataSourceAutoConfiguration.class,
           // REMOVED: HibernateJpaAutoConfiguration.class,
           // REMOVED: LiquibaseAutoConfiguration.class,
           ActiveMQAutoConfiguration.class,
           RedisAutoConfiguration.class
       }
   )
   ```

   Also remove the now-unused imports:
   ```java
   // REMOVED: import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
   // REMOVED: import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
   // REMOVED: import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
   ```

2. **`web/src/main/resources/application.properties`** — Remove the three entries from `spring.autoconfigure.exclude`:

   ```properties
   spring.autoconfigure.exclude=\
     org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration,\
     org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
   ```

   (Remove `DataSourceAutoConfiguration`, `HibernateJpaAutoConfiguration`, `LiquibaseAutoConfiguration` from the list.)

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew bootRun         # Starts — auto-config backs off, manual beans still active
```

Check the logs for:
- **No** `DataSourceAutoConfiguration matched` or `HibernateJpaAutoConfiguration matched` messages (because manual beans cause back-off).
- **Yes** `HikariDataSource` initialized (from `JpaConfigBase.dataSource()`), same as before.
- **Yes** Liquibase changelog executed (from `JpaConfigBase.initDb()`), same as before.

**Risks:**

- If Spring Boot auto-config does NOT back off correctly (e.g., different bean names or types), you'll get duplicate `DataSource` or `EntityManagerFactory` beans → startup failure. If this happens, re-add the exclusions and investigate bean names/types. The fix is usually adding `@ConditionalOnMissingBean`-compatible bean names to the manual config or moving directly to Step 11.6 (removing manual config) in the same commit.

---

## Step 11.5 — Add `@EntityScan` and `@EnableJpaRepositories` on Main App Class

**What:** Add `@EntityScan` and `@EnableJpaRepositories` to `IntygstjanstApplication` so that when the manual config is removed (Step 11.6), Spring Boot knows where to find entities and repositories.

**Why safe:** These annotations are idempotent with the existing `JpaConfig.@EnableJpaRepositories` and `JpaConfigBase.entityManagerFactory().setPackagesToScan()`. When both the manual and auto-configured `EntityManagerFactory` exist, Spring deduplicates. When the manual config is removed, these annotations ensure continuity.

**Changes:**

1. **`IntygstjanstApplication.java`** — Add annotations:

   ```java
   import org.springframework.boot.autoconfigure.domain.EntityScan;
   import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

   @SpringBootApplication(
       scanBasePackages = { ... },
       exclude = {
           ActiveMQAutoConfiguration.class,
           RedisAutoConfiguration.class
       }
   )
   @EntityScan(basePackages = "se.inera.intyg.intygstjanst.persistence.model")
   @EnableJpaRepositories(basePackages = "se.inera.intyg.intygstjanst.persistence.model.dao")
   public class IntygstjanstApplication {
       // ...
   }
   ```

   **Key decisions:**

   - `@EntityScan("se.inera.intyg.intygstjanst.persistence.model")` matches `JpaConstants.BASE_PACKAGE_TO_SCAN`. This scans all `@Entity` classes in the `model` package and its sub-packages (including the `dao` sub-package where all 10 entities live).
   - `@EnableJpaRepositories("se.inera.intyg.intygstjanst.persistence.model.dao")` matches `JpaConstants.REPOSITORY_PACKAGE_TO_SCAN`. This discovers `ArendeRepository`, `RekoRepository`, `CertificateRepository` (all `extends JpaRepository`).

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew bootRun         # Starts — no duplicate bean errors
```

---

## Step 11.6 — Remove `JpaConfigBase`, `JpaConfig`, and `JpaConstants`

**What:** Delete the three manual JPA configuration classes. After this, Spring Boot auto-configuration takes over completely.

**Why now:** Steps 11.4–11.5 ensured that Spring Boot auto-config is enabled and knows where to scan for entities/repositories. The `spring.datasource.*`, `spring.jpa.*`, and `spring.liquibase.*` properties from Steps 11.2–11.3 provide the same configuration that `JpaConfigBase` was injecting via `@Value`.

**Changes:**

1. **Delete** `persistence/src/main/java/se/inera/intyg/intygstjanst/persistence/config/JpaConfigBase.java`
2. **Delete** `persistence/src/main/java/se/inera/intyg/intygstjanst/persistence/config/JpaConfig.java`
3. **Delete** `persistence/src/main/java/se/inera/intyg/intygstjanst/persistence/config/JpaConstants.java`

**What changes in behavior:**

| Aspect                        | Before (JpaConfigBase)                                                   | After (Spring Boot auto-config)                                                |
|-------------------------------|--------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| DataSource                    | `HikariDataSource` via `JpaConfigBase.dataSource()` reading `db.*`       | `HikariDataSource` via `DataSourceAutoConfiguration` reading `spring.datasource.*` (which references `db.*`) |
| EntityManagerFactory          | `LocalContainerEntityManagerFactoryBean` with `HibernateJpaVendorAdapter`, persistence unit name `IneraCertificate`, package scan `se.inera.intyg.intygstjanst.persistence.model` | `LocalContainerEntityManagerFactoryBean` via `HibernateJpaAutoConfiguration` + `@EntityScan("se.inera.intyg.intygstjanst.persistence.model")`. **Default persistence unit name** (no custom name). |
| TransactionManager            | `JpaTransactionManager` via `JpaConfigBase.transactionManager()`         | `JpaTransactionManager` via `JpaTransactionAutoConfiguration`                   |
| Liquibase                     | `SpringLiquibase` bean named `"dbUpdate"` reading `changelog/changelog.xml` | `SpringLiquibase` via `LiquibaseAutoConfiguration` reading `spring.liquibase.change-log=classpath:changelog/changelog.xml` |
| Hibernate properties          | Set via `additionalProperties()` method                                   | Set via `spring.jpa.properties.*` in `application.properties`                   |
| Persistence unit name         | `"IneraCertificate"` (explicit)                                           | `"default"` (Spring Boot default)                                               |

**Critical change — Persistence unit name:**

The persistence unit name changes from `"IneraCertificate"` to `"default"`. This affects all `@PersistenceContext(unitName = "IneraCertificate")` annotations in DAO classes. These are cleaned up in Step 11.8. However, since there is only **one** persistence unit, bare `@PersistenceContext` (without `unitName`) already works — Spring injects the only available `EntityManager`. The `unitName`-qualified annotations simply become unnecessary, not broken, because Spring Boot's auto-configured `EntityManagerFactory` is the only one and Spring falls back to it.

**⚠️ Important:** Verify this assumption. If Spring does NOT fall back when `unitName` doesn't match any registered persistence unit, you'll get a `NoSuchBeanDefinitionException` at startup. In that case, do Step 11.8 **before** or **together with** this step.

**Safest approach:** Do Steps 11.6 + 11.8 as one atomic commit.

**Critical change — Liquibase bean name:**

The manual Liquibase bean was named `"dbUpdate"`. `ApplicationConfig.moduleRegistry()` has `@DependsOn("dbUpdate")`. After this step, Spring Boot's auto-configured `SpringLiquibase` bean is named `"liquibase"` (the default). The `@DependsOn("dbUpdate")` will fail with a `NoSuchBeanDefinitionException`. This is fixed in Step 11.11, but **must be done together with this step** to avoid a broken startup.

**⚠️ Safest approach:** Do Steps 11.6 + 11.8 + 11.11 as one atomic commit.

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew bootRun         # Starts — Spring Boot auto-config now creates DataSource, EMF, TxManager, Liquibase
```

Check logs for:
- `HikariPool-1 - Starting...` — HikariCP starting (now from auto-config, same pool settings via `spring.datasource.hikari.*`)
- `Liquibase: changelog/changelog.xml` — Liquibase running (now from `LiquibaseAutoConfiguration`)
- No `JpaConfigBase` log message (`"Initialize data-source with url:"`) — manual config is gone

---

## Step 11.7 — Remove `persistence.xml`

**What:** Delete `persistence/src/main/resources/META-INF/persistence.xml`. Spring Boot's `@EntityScan` replaces the explicit `<class>` listings.

**Why safe:** `JpaConfigBase` used `setPackagesToScan()` which **overrides** `persistence.xml` entity discovery anyway. And now `@EntityScan` does the same. The `persistence.xml` file has been redundant since `JpaConfigBase` was introduced — it's kept as documentation/fallback only.

**Changes:**

1. **Delete** `persistence/src/main/resources/META-INF/persistence.xml`

2. **`persistence/build.gradle`** — The `sourceSets.main.output.resourcesDir` hack was needed because "JPA expects classes and configuration files to be in the same directory" (the comment in the file). Without `persistence.xml`, this hack is no longer needed:

   ```groovy
   // REMOVE these two lines:
   // sourceSets.main.output.resourcesDir = sourceSets.main.output.getClassesDirs().getSingleFile()
   // jar.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
   ```

   **⚠️ Caution:** Verify that Liquibase's `changelog/changelog.xml` (also in `src/main/resources`) is still found correctly after removing this hack. The changelog should be on the classpath as `classpath:changelog/changelog.xml` regardless of the output directory layout, because Gradle's default behavior places resources in the JAR root.

**Verify:**

```bash
./gradlew clean build                    # Compiles, all tests pass
./gradlew :intygstjanst-persistence:jar  # Verify JAR contents
jar tf persistence/build/libs/intygstjanst-persistence-*.jar | grep -E "persistence.xml|changelog"
# Should show: changelog/changelog.xml
# Should NOT show: META-INF/persistence.xml
```

---

## Step 11.8 — Clean Up `@PersistenceContext(unitName=...)` Annotations

**What:** Remove `unitName = JpaConstants.PERSISTANCE_UNIT_NAME` from all `@PersistenceContext` annotations. With only one `EntityManagerFactory` in the context, bare `@PersistenceContext` is sufficient.

**Why:** The manual config used persistence unit name `"IneraCertificate"`. Spring Boot's auto-configured `EntityManagerFactory` uses the default name. The `unitName` qualifier is no longer valid and may cause injection failures.

**Changes:**

Replace `@PersistenceContext(unitName = JpaConstants.PERSISTANCE_UNIT_NAME)` with `@PersistenceContext` in:

**Production code (5 files):**

1. `persistence/src/main/java/.../dao/impl/CertificateDaoImpl.java` (line 67)
2. `persistence/src/main/java/.../dao/impl/SjukfallCertificateDaoImpl.java` (line 74)
3. `persistence/src/main/java/.../dao/impl/RelationDaoImpl.java` (line 47)
4. `persistence/src/main/java/.../dao/impl/HealthCheckDaoImpl.java` (line 41)
5. `persistence/src/main/java/.../dao/impl/ApprovedReceiverDaoImpl.java` (line 42)

**Web module (2 files):**

6. `web/src/main/java/.../web/service/bean/IntygBootstrapBean.java` (line 69)
7. `web/src/main/java/.../web/integration/test/CertificateResource.java` (line 76)

**Test code (2 files):**

8. `persistence/src/test/java/.../dao/impl/ArendeRepositoryTest.java` (line 41)
9. `persistence/src/test/java/.../dao/impl/RekoRepositoryTest.java` (line 44)

Also remove the `JpaConstants` import from each of these files:
```java
// REMOVE: import se.inera.intyg.intygstjanst.persistence.config.JpaConstants;
```

**Files with bare `@PersistenceContext` (no changes needed):**

- `web/src/main/java/.../web/service/monitoring/HealthMonitor.java` — already bare
- `web/src/main/java/.../web/integration/test/SjukfallCertResource.java` — already bare
- `persistence/src/test/java/.../dao/impl/ApprovedReceiverDaoImplTest.java` — already bare
- `persistence/src/test/java/.../dao/impl/RelationDaoImplTest.java` — already bare
- `persistence/src/test/java/.../dao/impl/CertificateDaoImplTest.java` — already bare
- `persistence/src/test/java/.../dao/impl/SjukfallCertificateDaoImplTest.java` — already bare

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
# Confirm no references remain:
grep -rn "JpaConstants" persistence/src/ web/src/  # Should return nothing
grep -rn "PERSISTANCE_UNIT_NAME" persistence/src/ web/src/  # Should return nothing
grep -rn "IneraCertificate" persistence/src/ web/src/  # Should return nothing
```

---

## Step 11.9 — Refactor `TransactionTemplate` → `@Transactional` in Test/Dev Classes

**What:** Replace verbose programmatic `TransactionTemplate.execute(...)` calls with declarative `@Transactional` annotations in three classes: `CertificateResource`, `SjukfallCertResource`, and `IntygBootstrapBean`. This modernizes and simplifies the transaction management, removing ~200 lines of boilerplate.

**Why now:** After Steps 11.6–11.8, the manual `JpaConfigBase` is gone, the `@Qualifier("transactionManager")` pattern is no longer needed (Spring Boot auto-configures a single `PlatformTransactionManager`), and `JpaConstants` has been deleted. These three classes still hold references to the old transaction infrastructure (`TransactionTemplate`, `@Qualifier("transactionManager")`, `PlatformTransactionManager` setter injection). Cleaning them up completes the JPA migration.

**Current state of each class:**

| Class                    | `TransactionTemplate` usage                                  | `@PersistenceContext` | Error handling pattern                                              |
|--------------------------|--------------------------------------------------------------|-----------------------|---------------------------------------------------------------------|
| `CertificateResource`   | 7 methods with `transactionTemplate.execute(status -> ...)` + 1 method already using `@Transactional` | `unitName` (cleaned in 11.8) | `try/catch` → `status.setRollbackOnly()` → return 500 response     |
| `SjukfallCertResource`  | 2 methods with anonymous `TransactionCallback` inner classes  | bare (no change)      | `try/catch` → `status.setRollbackOnly()` → return 500 response     |
| `IntygBootstrapBean`     | 3 methods: `bootstrapCertificate` (lambda), `addIntyg` + `addSjukfall` (anonymous inner classes) | `unitName` (cleaned in 11.8) | `try/catch` → `status.setRollbackOnly()` → log error               |

---

### 11.9a — Refactor `CertificateResource`

**Strategy:** Add `@Transactional` on each mutating method. Remove `TransactionTemplate`, `PlatformTransactionManager`, and the setter injection. Simplify error handling.

**Key design decision — error handling:**

The current pattern is:
```java
return transactionTemplate.execute(status -> {
    try {
        // ... do work ...
        return ResponseEntity.ok().build();
    } catch (Exception e) {
        status.setRollbackOnly();
        LOGGER.warn("...", e);
        return ResponseEntity.internalServerError().build();
    }
});
```

With `@Transactional`, unchecked exceptions automatically trigger rollback. The simplest replacement is:
```java
@Transactional
public ResponseEntity<?> someMethod(...) {
    try {
        // ... do work ...
        return ResponseEntity.ok().build();
    } catch (Exception e) {
        LOGGER.warn("...", e);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "...", e);
    }
}
```

Throwing `ResponseStatusException` (a `RuntimeException`) ensures the transaction is rolled back AND Spring returns a proper 500 response. The `status.setRollbackOnly()` call is no longer needed.

**Note on self-calling:** `deleteCertificatesForCitizen()` and `deleteCertificatesForUnit()` both call `deleteCertificate()` internally. Since these are intra-class calls, they bypass the Spring proxy — so `deleteCertificate()`'s `@Transactional` is NOT honored for internal calls. This is actually **correct behavior**: the outer method's `@Transactional` creates the transaction boundary, and the inner calls run within it. When `deleteCertificate()` is called directly via HTTP (as a standalone endpoint), the proxy IS involved and `@Transactional` works normally.

However, the internal `deleteCertificate(String id)` method currently returns `ResponseEntity`, which is awkward for internal use. Extract the core logic into a private `void` method:

**Changes:**

```java
@RestController
@ApiBasePath("/resources")
@RequestMapping("/certificate")
@Profile({"dev", "testability-api"})
public class CertificateResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateResource.class);

    @PersistenceContext
    private EntityManager entityManager;

    // REMOVED: private TransactionTemplate transactionTemplate;
    // REMOVED: @Autowired setTxManager(...)

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @GetMapping("/{id}")
    public Certificate getCertificate(@PathVariable("id") String id) {
        return entityManager.find(Certificate.class, id);
    }

    @DeleteMapping("/citizen/{id}")
    @Transactional
    public ResponseEntity<?> deleteCertificatesForCitizen(@PathVariable("id") String id) {
        LOGGER.info("Deleting certificates for citizen {}", id);
        @SuppressWarnings("unchecked")
        List<String> certificates = entityManager
            .createQuery("SELECT c.id FROM Certificate c WHERE c.civicRegistrationNumber=:personId")
            .setParameter("personId", id).getResultList();
        for (String certificate : certificates) {
            removeCertificateById(certificate);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unit/{id}")
    @Transactional
    public ResponseEntity<?> deleteCertificatesForUnit(@PathVariable("id") String id) {
        LOGGER.info("Deleting certificates for unit {}", id);
        @SuppressWarnings("unchecked")
        List<String> certificates = entityManager
            .createQuery("SELECT c.id FROM Certificate c WHERE c.careUnitId=:careUnitHsaId")
            .setParameter("careUnitHsaId", id).getResultList();
        for (String certificate : certificates) {
            removeCertificateById(certificate);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteCertificate(@PathVariable("id") final String id) {
        LOGGER.info("Deleting certificate {}", id);
        removeCertificateById(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping()
    @Transactional
    public ResponseEntity<?> deleteAllCertificates() {
        @SuppressWarnings("unchecked")
        List<Certificate> certificates = entityManager.createQuery("SELECT c FROM Certificate c").getResultList();
        for (Certificate certificate : certificates) {
            if (certificate.getOriginalCertificate() != null) {
                entityManager.remove(certificate.getOriginalCertificate());
            }
            entityManager.remove(certificate);
        }

        List<SjukfallCertificate> sjukfallCertificates = entityManager
            .createQuery("SELECT c FROM SjukfallCertificate c", SjukfallCertificate.class).getResultList();
        for (SjukfallCertificate sjukfallCert : sjukfallCertificates) {
            entityManager.remove(sjukfallCert);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping()
    @Transactional
    public ResponseEntity<?> insertCertificate(@RequestBody final CertificateHolder certificateHolder) throws Exception {
        Certificate certificate = ConverterUtil.toCertificate(certificateHolder);
        LOGGER.info("insert certificate {} ({})", certificate.getId(), certificate.getType());
        OriginalCertificate originalCertificate = new OriginalCertificate(LocalDateTime.now(),
            getXmlBody(certificateHolder), certificate);
        ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion());
        final Utlatande utlatande = moduleApi.getUtlatandeFromXml(originalCertificate.getDocument());
        certificate.setAdditionalInfo(moduleApi.getAdditionalInfo(moduleApi.getIntygFromUtlatande(utlatande)));
        CertificateMetaData metaData = new CertificateMetaData(certificate,
            utlatande.getGrundData().getSkapadAv().getPersonId(),
            utlatande.getGrundData().getSkapadAv().getFullstandigtNamn(), certificate.isRevoked(),
            ConverterUtil.getDiagnoses(certificateHolder.getAdditionalMetaData()));
        certificate.setCertificateMetaData(metaData);
        entityManager.persist(certificate);
        entityManager.persist(metaData);
        entityManager.persist(originalCertificate);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/approvedreceivers")
    @Transactional
    public ResponseEntity<?> deleteApprovedReceivers(@PathVariable("id") final String id) {
        LOGGER.info("removing approved receivers for certificate {}", id);
        @SuppressWarnings("unchecked")
        List<ApprovedReceiver> approvedReceivers = entityManager
            .createQuery("SELECT ar FROM ApprovedReceiver ar WHERE ar.certificateId=:certificateId")
            .setParameter("certificateId", id)
            .getResultList();
        for (ApprovedReceiver ar : approvedReceivers) {
            entityManager.remove(ar);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/approvedreceivers")
    @Transactional
    public ResponseEntity<?> registerApprovedReceivers(@PathVariable("id") final String id,
        @RequestBody final RegisterApprovedReceiversType registerApprovedReceiversType) {
        for (ReceiverApprovalStatus ras : registerApprovedReceiversType.getApprovedReceivers()) {
            ApprovedReceiver approvedReceiver = new ApprovedReceiver();
            approvedReceiver.setCertificateId(id);
            approvedReceiver.setReceiverId(ras.getReceiverId());
            approvedReceiver.setApproved(parseApprovalStatus(ras.getApprovalStatus().value()));
            LOGGER.info("register approved receiver {} for certificate {}", id, ras.getReceiverId());
            entityManager.persist(approvedReceiver);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/deleteCertificates")
    @Transactional
    public ResponseEntity<?> deleteCertificates(@RequestBody List<String> certificateIds) {
        // ... unchanged — already uses @Transactional ...
    }

    // Private helper — no @Transactional needed (runs within caller's transaction)
    private void removeCertificateById(String id) {
        Certificate certificate = entityManager.find(Certificate.class, id);
        if (certificate != null) {
            entityManager.remove(certificate.getOriginalCertificate());
            entityManager.remove(certificate);
        }
        SjukfallCertificate sjukfallCertificate = entityManager.find(SjukfallCertificate.class, id);
        if (sjukfallCertificate != null) {
            entityManager.remove(sjukfallCertificate);
        }
    }

    // ... existing private helper methods unchanged ...
}
```

**Summary of changes:**

| What                                | Before                                                  | After                                        |
|--------------------------------------|---------------------------------------------------------|----------------------------------------------|
| Transaction demarcation              | `transactionTemplate.execute(status -> { ... })`        | `@Transactional` on method                   |
| Error handling                       | `try/catch` + `status.setRollbackOnly()` + return 500   | Let exceptions propagate (auto-rollback)     |
| `TransactionTemplate` field          | Present, injected via setter                            | **Removed**                                  |
| `PlatformTransactionManager` setter  | `@Autowired @Qualifier("transactionManager")`           | **Removed**                                  |
| `deleteCertificate` internal calls   | Called `deleteCertificate()` → nested `transactionTemplate.execute` | Call `removeCertificateById()` (private, no transaction nesting) |
| `@Transactional` import              | `jakarta.transaction.Transactional` (on `deleteCertificates`) | `org.springframework.transaction.annotation.Transactional` (Spring's — supports `readOnly`, `rollbackFor`, etc.) |

**Important — `@Transactional` import:**

The existing `deleteCertificates()` method uses `jakarta.transaction.Transactional`. Switch all methods to use `org.springframework.transaction.annotation.Transactional` instead. Spring's annotation is richer (supports `readOnly`, `rollbackFor`, `propagation`, etc.) and is the Spring Boot convention.

**Removed imports:**
```java
// REMOVED:
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

// ADDED:
import org.springframework.transaction.annotation.Transactional;
```

---

### 11.9b — Refactor `SjukfallCertResource`

**Strategy:** Same as `CertificateResource`. Replace `TransactionTemplate` + anonymous inner classes with `@Transactional`.

**Changes:**

```java
@RestController
@ApiBasePath("/resources")
@RequestMapping("/sjukfallcert")
@Profile({"dev", "testability-api"})
public class SjukfallCertResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SjukfallCertResource.class);

    @PersistenceContext
    private EntityManager entityManager;

    // REMOVED: private TransactionTemplate transactionTemplate;
    // REMOVED: @Autowired setTxManager(...)

    @GetMapping("/{id}")
    public SjukfallCertificate getSjukfallCertificate(@PathVariable("id") String id) {
        return entityManager.find(SjukfallCertificate.class, id);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteSjukfallCertificate(@PathVariable("id") final String id) {
        SjukfallCertificate cert = entityManager.find(SjukfallCertificate.class, id);
        if (cert != null) {
            entityManager.remove(cert);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping()
    @Transactional
    public ResponseEntity<?> deleteAllSjukfallCertificates() {
        List<SjukfallCertificate> certificates = entityManager
            .createQuery("SELECT sc FROM SjukfallCertificate sc", SjukfallCertificate.class)
            .getResultList();
        for (SjukfallCertificate sjukfallCert : certificates) {
            entityManager.remove(sjukfallCert);
        }
        return ResponseEntity.ok().build();
    }
}
```

**Removed imports:**
```java
// REMOVED:
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

// ADDED (if not already present):
import org.springframework.transaction.annotation.Transactional;
```

**Lines saved:** ~40 lines of anonymous inner classes and boilerplate removed.

---

### 11.9c — Refactor `IntygBootstrapBean`

**Strategy:** This class is more complex because `@Transactional` on intra-class calls bypasses the Spring proxy. The current code intentionally uses per-certificate transactions (so one bad certificate doesn't roll back others). To preserve this behavior with `@Transactional`, extract the per-certificate persistence logic into a separate `@Service` class.

**Why not just add `@Transactional` on the existing methods?**

`IntygBootstrapBean.bootstrapModuleCertificates()` is called from `@PostConstruct initData()`, which iterates over certificates and calls `bootstrapCertificate()` for each one. If `bootstrapCertificate()` has `@Transactional`, it won't be honored for intra-class calls (proxy bypass). The `TransactionTemplate` works because it's programmatic.

**Solution:** Extract a `IntygBootstrapPersister` service:

1. **Create** `web/src/main/java/se/inera/intyg/intygstjanst/web/service/bean/IntygBootstrapPersister.java`:

```java
package se.inera.intyg.intygstjanst.web.service.bean;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateMetaData;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.web.service.converter.CertificateToSjukfallCertificateConverter;

/**
 * Handles transactional persistence for bootstrap data.
 * Extracted from IntygBootstrapBean so that @Transactional works
 * via Spring's proxy mechanism (intra-class calls bypass the proxy).
 */
@Service
public class IntygBootstrapPersister {

    private static final Logger LOG = LoggerFactory.getLogger(IntygBootstrapPersister.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final CertificateToSjukfallCertificateConverter certificateToSjukfallCertificateConverter;

    public IntygBootstrapPersister(CertificateToSjukfallCertificateConverter converter) {
        this.certificateToSjukfallCertificateConverter = converter;
    }

    @Transactional
    public void persistCertificate(Certificate certificate, OriginalCertificate originalCertificate,
                                    CertificateMetaData metaData, Utlatande utlatande) {
        if (entityManager.find(Certificate.class, certificate.getId()) != null) {
            LOG.info("Bootstrapping of certificate '{}' skipped. Already in database.", certificate.getId());
            return;
        }

        entityManager.persist(metaData);
        entityManager.persist(originalCertificate);
        entityManager.persist(certificate);

        if (isSjukfallsGrundandeIntyg(certificate.getType())) {
            persistSjukfallIfConvertable(certificate, utlatande);
        }
    }

    @Transactional
    public void persistLocalCertificate(Certificate certificate, OriginalCertificate originalCertificate,
                                         CertificateMetaData metaData) {
        if (entityManager.find(Certificate.class, certificate.getId()) != null) {
            LOG.info("Bootstrapping of certificate '{}' skipped. Already in database.", certificate.getId());
            return;
        }
        entityManager.persist(metaData);
        entityManager.persist(originalCertificate);
        entityManager.persist(certificate);
    }

    @Transactional
    public void persistSjukfall(Certificate certificate, Utlatande utlatande) {
        persistSjukfallIfConvertable(certificate, utlatande);
    }

    private void persistSjukfallIfConvertable(Certificate certificate, Utlatande utlatande) {
        if (certificateToSjukfallCertificateConverter.isConvertableFk7263(utlatande)) {
            SjukfallCertificate sjukfallCert = certificateToSjukfallCertificateConverter.convertFk7263(certificate, utlatande);
            if (entityManager.find(SjukfallCertificate.class, sjukfallCert.getId()) == null) {
                entityManager.persist(sjukfallCert);
            }
        }
        if (certificateToSjukfallCertificateConverter.isConvertableLisjp(utlatande)) {
            SjukfallCertificate sjukfallCert = certificateToSjukfallCertificateConverter.convertLisjp(certificate, utlatande);
            if (entityManager.find(SjukfallCertificate.class, sjukfallCert.getId()) == null) {
                entityManager.persist(sjukfallCert);
            }
        }
    }

    private boolean isSjukfallsGrundandeIntyg(String type) {
        return Fk7263EntryPoint.MODULE_ID.equalsIgnoreCase(type)
            || LisjpEntryPoint.MODULE_ID.equalsIgnoreCase(type);
    }
}
```

2. **Update `IntygBootstrapBean`** — Inject `IntygBootstrapPersister` and delegate:

```java
@Component
@Profile("bootstrap")
@DependsOn({"transportConverterUtil", "internalConverterUtil", "transportToInternal", "befattningService"})
public class IntygBootstrapBean {

    private static final Logger LOG = LoggerFactory.getLogger(IntygBootstrapBean.class);
    private static final String DEFAULT_TYPE_VERSION_FALLBACK = "1.0";

    // REMOVED: @PersistenceContext private EntityManager entityManager;
    // REMOVED: private TransactionTemplate transactionTemplate;
    // REMOVED: @Autowired setTxManager(...)

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private IntygBootstrapPersister persister;

    @PostConstruct
    public void initData() {
        bootstrapModuleCertificates();
        bootstrapLocalCertificates();
    }

    private void bootstrapModuleCertificates() {
        for (Resource resource : getResourceListing("classpath*:module-bootstrap-certificate/*.xml")) {
            // ... existing resource validation ...
            try {
                // ... existing parsing logic ...
                ModuleApi moduleApi = moduleRegistry.getModuleApi(moduleName, intygMajorTypeVersion);
                bootstrapCertificate(xmlString, moduleApi,
                    moduleRegistry.getModuleEntryPoint(moduleName).getDefaultRecipient());
            } catch (Exception e) {
                LOG.error("Could not bootstrap certificate in file '{}'", resourceFilename, e);
            }
        }
    }

    private void bootstrapCertificate(String xmlString, ModuleApi moduleApi, String defaultRecipient)
            throws ModuleException {
        final Utlatande utlatande = moduleApi.getUtlatandeFromXml(xmlString);
        final String additionalInfo = moduleApi.getAdditionalInfo(moduleApi.getIntygFromUtlatande(utlatande));

        Certificate certificate = new Certificate(utlatande.getId());
        certificate.setAdditionalInfo(additionalInfo);
        certificate.setCareGiverId(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid());
        certificate.setCareUnitId(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsid());
        certificate.setCareUnitName(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsnamn());
        certificate.setCivicRegistrationNumber(utlatande.getGrundData().getPatient().getPersonId());
        certificate.setDeletedByCareGiver(false);
        OriginalCertificate originalCertificate = new OriginalCertificate(
            utlatande.getGrundData().getSigneringsdatum(), xmlString, certificate);
        certificate.setOriginalCertificate(originalCertificate);
        certificate.setSignedDate(utlatande.getGrundData().getSigneringsdatum());
        certificate.setSigningDoctorName(utlatande.getGrundData().getSkapadAv().getFullstandigtNamn());
        certificate.setStates(Arrays.asList(
            new CertificateStateHistoryEntry("HSVARD", CertificateState.RECEIVED,
                utlatande.getGrundData().getSigneringsdatum().plusMinutes(1)),
            new CertificateStateHistoryEntry(defaultRecipient, CertificateState.SENT,
                utlatande.getGrundData().getSigneringsdatum().plusMinutes(2))));
        certificate.setType(utlatande.getTyp());
        certificate.setTypeVersion(utlatande.getTextVersion() != null
            ? utlatande.getTextVersion() : DEFAULT_TYPE_VERSION_FALLBACK);
        certificate.setValidFromDate(null);
        certificate.setValidToDate(null);
        certificate.setWireTapped(false);

        CertificateMetaData metaData = new CertificateMetaData(certificate,
            utlatande.getGrundData().getSkapadAv().getPersonId(),
            utlatande.getGrundData().getSkapadAv().getFullstandigtNamn(), false, null);
        certificate.setCertificateMetaData(metaData);

        // Delegate to @Transactional service (proxy-based, works correctly)
        persister.persistCertificate(certificate, originalCertificate, metaData, utlatande);
    }

    private void addIntyg(final Resource metadata, final Resource content) {
        try {
            Certificate certificate = new CustomObjectMapper().readValue(metadata.getInputStream(), Certificate.class);
            String contentString = Resources.toString(content.getURL(), StandardCharsets.UTF_8);
            OriginalCertificate originalCertificate = new OriginalCertificate(
                certificate.getSignedDate(), contentString, certificate);

            ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion());
            final Utlatande utlatande = moduleApi.getUtlatandeFromXml(contentString);
            certificate.setAdditionalInfo(moduleApi.getAdditionalInfo(moduleApi.getIntygFromUtlatande(utlatande)));

            CertificateMetaData metaData = new CertificateMetaData(certificate,
                utlatande.getGrundData().getSkapadAv().getPersonId(),
                utlatande.getGrundData().getSkapadAv().getFullstandigtNamn(), false, null);

            persister.persistLocalCertificate(certificate, originalCertificate, metaData);
        } catch (Exception e) {
            LOG.error("Loading failed of {}: {}", metadata.getFilename(), e);
        }
    }

    private void addSjukfall(final Resource metadata, final Resource content) {
        try {
            Certificate certificate = new CustomObjectMapper().readValue(metadata.getInputStream(), Certificate.class);
            if (!isSjukfallsGrundandeIntyg(certificate.getType())) {
                return;
            }
            String contentString = Resources.toString(content.getURL(), StandardCharsets.UTF_8);
            ModuleApi moduleApi = moduleRegistry.getModuleApi(certificate.getType(), certificate.getTypeVersion());
            Utlatande utlatande = moduleApi.getUtlatandeFromXml(contentString);

            persister.persistSjukfall(certificate, utlatande);
        } catch (Exception e) {
            LOG.error("Loading of Sjukfall intyg failed for {}: {}", metadata.getFilename(), e);
        }
    }

    // ... remaining helper methods unchanged ...
}
```

**Summary of changes for `IntygBootstrapBean`:**

| What                                | Before                                                  | After                                        |
|--------------------------------------|---------------------------------------------------------|----------------------------------------------|
| Transaction demarcation              | `transactionTemplate.execute(...)` with lambdas/inner classes | `@Transactional` on `IntygBootstrapPersister` methods |
| `@PersistenceContext`                | In `IntygBootstrapBean`                                 | Moved to `IntygBootstrapPersister`            |
| `TransactionTemplate`               | Injected via setter                                     | **Removed**                                  |
| `PlatformTransactionManager` setter  | `@Autowired @Qualifier("transactionManager")`           | **Removed**                                  |
| Per-certificate transaction boundary | Each `transactionTemplate.execute()` = one transaction  | Each `persister.persistCertificate()` call = one transaction (**preserved**) |
| Object construction vs persistence   | Mixed together inside transaction callback              | Separated: build objects outside tx, persist inside `@Transactional` method |
| Entity manager usage                 | Direct `entityManager.persist()` in bean                | Delegated to `IntygBootstrapPersister`        |

**Key behavioral preservation:** Each certificate is still persisted in its own transaction. If one fails, only that certificate is rolled back — other certificates are unaffected.

---

### What gets removed across all three classes

| Removed element                                      | Occurrences |
|------------------------------------------------------|-------------|
| `private TransactionTemplate transactionTemplate`    | 3           |
| `@Autowired setTxManager(@Qualifier(...) ...)`       | 3           |
| `transactionTemplate.execute(status -> { ... })`     | 10          |
| `new TransactionCallbackWithoutResult() { ... }`     | 2           |
| `new TransactionCallback<...>() { ... }`             | 2           |
| `status.setRollbackOnly()`                           | 9           |
| `import ...TransactionTemplate`                      | 3           |
| `import ...PlatformTransactionManager`               | 3           |
| `import ...TransactionCallback`                      | 1           |
| `import ...TransactionCallbackWithoutResult`         | 1           |
| `import ...TransactionStatus`                        | 2           |

**Total: ~200 lines of boilerplate removed, replaced with `@Transactional` annotations.**

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew bootRun --args='--spring.profiles.active=dev,bootstrap,testability-api'
# Verify: bootstrap data loaded (check logs for "Bootstrapping certificate" messages)
# Verify: test endpoints respond (DELETE /resources/certificate/..., etc.)
```

---

## Step 11.10 — Remove Redundant Explicit Dependencies from `persistence/build.gradle`

**What:** Remove explicit dependency declarations for libraries now provided transitively by `spring-boot-starter-data-jpa`.

**Why safe:** The starter brings these dependencies with Spring Boot–managed versions. Removing explicit declarations avoids version conflicts and lets Spring Boot manage the dependency graph.

**Changes:**

1. **`persistence/build.gradle`** — Remove the following explicit dependencies (now provided by `spring-boot-starter-data-jpa`):

   ```groovy
   // REMOVE — provided by spring-boot-starter-data-jpa:
   // implementation "com.zaxxer:HikariCP"
   // implementation "org.hibernate.orm:hibernate-core"
   // implementation "org.hibernate.orm:hibernate-hikaricp"
   // implementation "org.springframework.data:spring-data-jpa"

   // KEEP:
   implementation "org.springframework.boot:spring-boot-starter-data-jpa"
   implementation "jakarta.persistence:jakarta.persistence-api"  // KEEP if used for compile-only annotations
   implementation "org.liquibase:liquibase-core"                  // KEEP — or verify if starter-data-jpa includes it
   ```

   **Decision on `liquibase-core`:** `spring-boot-starter-data-jpa` does NOT include Liquibase. Spring Boot provides `spring-boot-starter-liquibase` which is just a convenience starter that includes `liquibase-core`. Since we don't want to add another starter, keep the explicit `liquibase-core` dependency. Alternatively, add `spring-boot-starter-liquibase` to `web/build.gradle` and remove `liquibase-core` from `persistence/build.gradle`.

   **Decision on `jakarta.persistence-api`:** `spring-boot-starter-data-jpa` → `hibernate-core` → `jakarta.persistence-api` transitively. Can be removed. However, if `persistence` module classes only need the JPA API (not Hibernate), keeping it as a `compileOnly` dependency makes the API boundary clearer. Either approach works.

2. **`web/build.gradle`** — Check if `spring-data-jpa` is explicitly listed:

   ```groovy
   // REMOVE — now transitive via persistence module's spring-boot-starter-data-jpa:
   // implementation "org.springframework.data:spring-data-jpa"
   ```

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew :intygstjanst-persistence:dependencies --configuration runtimeClasspath | grep -E "hikari|hibernate|spring-data"
# Should show these from spring-boot-starter-data-jpa transitively
```

---

## Step 11.11 — Update Persistence Test Infrastructure

**What:** Update `TestConfig`, `TestSupport`, and `test.properties` to work with Spring Boot auto-configuration instead of the now-deleted `JpaConfigBase`/`JpaConfig`.

**Why:** The test `@ContextConfiguration(classes = TestConfig.class)` scans the `persistence` package, which previously found `JpaConfig extends JpaConfigBase`. Now `JpaConfig` is deleted, so tests need a new way to configure JPA.

**Changes:**

1. **`persistence/src/test/resources/test.properties`** — Replace `db.*` properties with Spring Boot conventions:

   ```properties
   # Spring Boot DataSource (H2 in-memory for tests)
   spring.datasource.driver-class-name=org.h2.Driver
   spring.datasource.url=jdbc:h2:mem:dataSource;MODE=MySQL;IGNORECASE=TRUE;DB_CLOSE_DELAY=-1
   spring.datasource.username=sa
   spring.datasource.password=

   # HikariCP
   spring.datasource.hikari.maximum-pool-size=3

   # JPA
   spring.jpa.hibernate.ddl-auto=none
   spring.jpa.open-in-view=false

   # Liquibase
   spring.liquibase.change-log=classpath:changelog/changelog.xml

   # Application-specific
   hash.salt=salt
   ```

2. **`persistence/src/test/java/.../TestConfig.java`** — Update to enable Spring Boot auto-configuration:

   **Option A — Use `@DataJpaTest` (preferred for JPA tests):**

   Convert `TestSupport` to use `@DataJpaTest` instead of `@ContextConfiguration`. This is cleaner and aligns with Spring Boot testing conventions:

   ```java
   // TestSupport.java
   @DataJpaTest
   @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // Use our H2 config, not Spring Boot's default
   @Import(TestConfig.class)  // If TestConfig has extra beans
   @Transactional
   public abstract class TestSupport {
       // ...existing helper methods...
   }
   ```

   **Option B — Keep `@ContextConfiguration` with `@EnableAutoConfiguration`:**

   If Option A requires too many test changes, modify `TestConfig` to enable auto-configuration:

   ```java
   @Configuration
   @EnableAutoConfiguration(exclude = {
       ActiveMQAutoConfiguration.class,
       RedisAutoConfiguration.class
   })
   @EntityScan("se.inera.intyg.intygstjanst.persistence.model")
   @EnableJpaRepositories("se.inera.intyg.intygstjanst.persistence.model.dao")
   @ComponentScan(basePackages = {"se.inera.intyg.intygstjanst.persistence", "se.inera.intyg.intygstjanst.logging"})
   @PropertySource("classpath:test.properties")
   public class TestConfig {
   }
   ```

   `TestSupport` stays the same (`@ContextConfiguration(classes = TestConfig.class)`).

   **Key decisions:**

   - Option B is the **minimal-change** approach. It keeps the existing test structure and just enables JPA auto-configuration within the test context.
   - `@EntityScan` and `@EnableJpaRepositories` are needed because the test context is isolated — it doesn't see `IntygstjanstApplication`'s annotations.
   - Exclude `ActiveMQAutoConfiguration` and `RedisAutoConfiguration` to avoid unrelated auto-config in persistence tests.

**Verify:**

```bash
./gradlew :intygstjanst-persistence:test   # All persistence tests pass
./gradlew test                              # All tests pass across all modules
```

---

## Step 11.12 — Update `ApplicationConfig` — Remove `@DependsOn("dbUpdate")`

**What:** Remove `@DependsOn("dbUpdate")` from `ApplicationConfig.moduleRegistry()`. The manual Liquibase bean was named `"dbUpdate"` — Spring Boot's auto-configured `SpringLiquibase` bean is named `"liquibase"`.

**Why:** After `JpaConfigBase` is deleted, there is no bean named `"dbUpdate"`. The `@DependsOn` would cause a `NoSuchBeanDefinitionException`.

**Changes:**

1. **`web/src/main/java/.../config/ApplicationConfig.java`** — Update `moduleRegistry()`:

   ```java
   @Bean
   @DependsOn("liquibase")  // Was: @DependsOn("dbUpdate") — Spring Boot's auto-configured Liquibase bean is named "liquibase"
   public IntygModuleRegistryImpl moduleRegistry() {
       final var registry = new IntygModuleRegistryImpl();
       registry.setOrigin(ApplicationOrigin.INTYGSTJANST);
       return registry;
   }
   ```

   **Alternative:** If the `@DependsOn` is no longer needed (e.g., the module registry doesn't actually require Liquibase to have run first), remove it entirely:

   ```java
   @Bean
   public IntygModuleRegistryImpl moduleRegistry() {
       final var registry = new IntygModuleRegistryImpl();
       registry.setOrigin(ApplicationOrigin.INTYGSTJANST);
       return registry;
   }
   ```

   **Analysis:** The `@DependsOn("dbUpdate")` ensures that Liquibase migrations have run before the module registry is created. This is important if `IntygModuleRegistryImpl` queries the database during initialization. Check if `IntygModuleRegistryImpl` accesses the DB — if not, `@DependsOn` can be removed. If it does, use `@DependsOn("liquibase")`.

**Verify:**

```bash
./gradlew clean build     # Compiles, all tests pass
./gradlew bootRun         # Starts without NoSuchBeanDefinitionException
```

---

## Step 11.13 — Final Verification

**What:** Comprehensive verification that JPA auto-configuration works identically to the manual configuration.

**Start the application:**

```bash
./gradlew :intygstjanst-web:bootRun
```

**Verification checklist:**

| Check                                | How                                                           | Expected                                                   |
|--------------------------------------|---------------------------------------------------------------|------------------------------------------------------------|
| Application starts                   | `./gradlew bootRun` — no exceptions                           | Started successfully                                       |
| HikariCP pool initialized            | Check logs for `HikariPool-1 - Starting...`                   | Pool initialized with max size from `db.pool.maxSize`      |
| Database connected                   | Check logs for `HikariPool-1 - Start completed`               | Connection to MySQL (or H2 in dev) successful              |
| Liquibase ran                        | Check logs for `Liquibase: changelog/changelog.xml`            | Changelog executed or "already up to date"                 |
| No `JpaConfigBase` logs              | Grep logs for `"Initialize data-source with url"`              | Not present — manual config is gone                        |
| EntityManager injected               | Hit any endpoint that queries the DB                           | Data returned correctly                                    |
| JPA repositories work                | Hit endpoint using `CertificateRepository`                     | Data returned correctly                                    |
| Transactions work                    | Create/update/delete via any endpoint                          | Changes committed to DB                                    |
| SOAP endpoints work                  | `curl http://localhost:8080/inera-certificate/get-certificate-se/v2.0?wsdl` | WSDL response                       |
| REST endpoints work                  | `curl http://localhost:8081/inera-certificate/internalapi/v1/certificatetexts` | Valid response                     |
| All tests pass                       | `./gradlew test`                                               | BUILD SUCCESSFUL                                           |
| No `JpaConstants` references remain  | `grep -rn "JpaConstants" .`                                    | No matches                                                 |
| No `persistence.xml` remains         | `find . -name "persistence.xml"`                               | No matches                                                 |
| No `JpaConfigBase` remains           | `find . -name "JpaConfigBase.java"`                            | No matches                                                 |

**Verify property mapping:**

| Old property (still in `application.properties`)  | Spring Boot property                                | Value source         |
|----------------------------------------------------|----------------------------------------------------|----------------------|
| `db.driver=com.mysql.cj.jdbc.Driver`               | `spring.datasource.driver-class-name=${db.driver}` | Same driver          |
| `db.url=jdbc:mysql://...`                          | `spring.datasource.url=${db.url}`                  | Same URL             |
| `db.username` / `db.password`                      | `spring.datasource.username/password`              | Same credentials     |
| `db.pool.maxSize=20`                               | `spring.datasource.hikari.maximum-pool-size`       | Same pool size       |
| `hibernate.dialect`                                 | `spring.jpa.database-platform`                     | Same dialect         |
| `hibernate.hbm2ddl.auto`                           | `spring.jpa.hibernate.ddl-auto`                    | Same DDL strategy    |
| `hibernate.show_sql` / `hibernate.format_sql`       | `spring.jpa.show-sql` / `spring.jpa.properties.*`  | Same logging config  |

---

## Risk Register

| #  | Risk                                                                                 | Impact | Mitigation                                                                                                                                                                                                                                         |
|----|--------------------------------------------------------------------------------------|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1  | `@PersistenceContext(unitName="IneraCertificate")` fails with auto-configured EMF    | High   | Spring Boot's default persistence unit name is `"default"`. If `unitName` doesn't match, Spring may fail to inject. **Mitigation:** Do Steps 11.6 + 11.8 together. Alternatively, configure `spring.jpa.properties.hibernate.ejb.persistenceUnitName=IneraCertificate` to keep the name. |
| 2  | `@DependsOn("dbUpdate")` fails after Liquibase bean rename                          | High   | Spring Boot names the Liquibase bean `"liquibase"`. **Mitigation:** Do Steps 11.6 + 11.11 together. Update to `@DependsOn("liquibase")` or remove if not needed.                                                                                 |
| 3  | Duplicate DataSource/EMF beans during transition                                     | Medium | Auto-config backs off when manual beans exist. But if bean types or names differ slightly, duplicates may occur. **Mitigation:** Watch for `BeanDefinitionOverrideException` on startup. If needed, set `spring.main.allow-bean-definition-overriding=true` temporarily. |
| 4  | HikariCP pool settings differ between manual and auto-config                         | Medium | `JpaConfigBase` set `autoCommit=false`, `minIdle=3`, `idleTimeout=15000`, `connTimeout=3000`. Ensure all are mapped to `spring.datasource.hikari.*` properties. **Mitigation:** Verified in Step 11.2 — all settings mapped explicitly.             |
| 5  | `hibernate.id.new_generator_mappings=false` dropped                                  | Medium | If this setting is lost, Hibernate may use a different ID generation strategy → primary key conflicts on existing data. **Mitigation:** Explicitly set via `spring.jpa.properties.hibernate.id.new_generator_mappings=false` in Step 11.2.          |
| 6  | `spring.jpa.open-in-view` defaults to `true` in Spring Boot                         | Medium | The manual config never had OEIV. If Spring Boot enables it, lazy-loading behavior changes. **Mitigation:** Explicitly set `spring.jpa.open-in-view=false` in Step 11.2.                                                                          |
| 7  | Test `@ContextConfiguration` fails after `JpaConfig` removal                        | Medium | Tests rely on `TestConfig.@ComponentScan` finding `JpaConfig`. After deletion, the scan finds nothing. **Mitigation:** Update `TestConfig` in Step 11.10 to enable auto-configuration.                                                             |
| 8  | `persistence/build.gradle` `sourceSets` hack removal breaks classpath                | Low    | The `sourceSets.main.output.resourcesDir` hack ensured `persistence.xml` was in the same dir as classes. Without `persistence.xml`, the hack is unnecessary. Verify Liquibase `changelog.xml` is still on the classpath after removal.              |
| 9  | `@Profile("!h2")` on `JpaConfig` was protecting something                           | Low    | No H2-profiled config class exists. The `!h2` profile seems vestigial. Removing `JpaConfig` removes this guard, but since nothing activates `h2` profile, no impact.                                                                               |
| 10 | Version conflicts between explicit deps and starter-managed deps                     | Low    | The `intygBomVersion` platform may pin different versions than Spring Boot's BOM. **Mitigation:** Run `./gradlew dependencies` after Step 11.9 and check for unexpected version changes.                                                            |

---

## Rollback Plan

If Step 11 causes issues:

1. **Git revert** the commit(s) back to the state after Step 10. The manual `JpaConfigBase`/`JpaConfig` config will be restored.
2. Re-add the auto-config exclusions in `IntygstjanstApplication` and `application.properties`.
3. The application will use manual JPA config again while issues are investigated.

**Partial rollback:** If only the test infrastructure is broken (Step 11.11), revert just that step — the production code change (Steps 11.1–11.10) can be kept.

---

## Summary: What Changes at Each Sub-step

| Step   | Auto-config exclusions               | Manual JPA config          | `persistence.xml` | `@PersistenceContext` | Transaction style / Properties used     |
|--------|--------------------------------------|----------------------------|--------------------|----------------------|-----------------------------------------|
| Before | DS, Hibernate, Liquibase excluded    | `JpaConfigBase` + `JpaConfig` active | ✅ Exists          | `unitName="IneraCertificate"` | `TransactionTemplate` / `db.*`, `hibernate.*` |
| 11.1   | DS, Hibernate, Liquibase excluded    | Active (unchanged)          | ✅ Exists          | Unchanged             | Unchanged                               |
| 11.2   | DS, Hibernate, Liquibase excluded    | Active (unchanged)          | ✅ Exists          | Unchanged             | `db.*` + `spring.datasource.*` (unused) |
| 11.3   | DS, Hibernate, Liquibase excluded    | Active (unchanged)          | ✅ Exists          | Unchanged             | + `spring.liquibase.*` (unused)         |
| 11.4   | ❌ **Removed** (auto-config enabled) | Active (auto-config backs off) | ✅ Exists       | Unchanged             | `spring.datasource.*` ready but backed off |
| 11.5   | Removed                              | Active (auto-config backs off) | ✅ Exists       | Unchanged             | `@EntityScan` + `@EnableJpaRepositories` added |
| 11.6   | Removed                              | ❌ **Deleted**              | ✅ Exists          | Needs cleanup         | **`spring.datasource.*` active**        |
| 11.7   | Removed                              | Deleted                     | ❌ **Deleted**     | Needs cleanup         | `spring.datasource.*` active            |
| 11.8   | Removed                              | Deleted                     | Deleted            | ✅ **Cleaned up** (bare `@PersistenceContext`) | `spring.datasource.*` active |
| 11.9   | Removed                              | Deleted                     | Deleted            | Cleaned up            | ✅ **`@Transactional`** (TransactionTemplate removed) |
| 11.10  | Removed                              | Deleted                     | Deleted            | Cleaned up            | Redundant deps removed                  |
| 11.11  | Removed                              | Deleted                     | Deleted            | Cleaned up            | Tests use `spring.datasource.*`         |
| 11.12  | Removed                              | Deleted                     | Deleted            | Cleaned up            | `@DependsOn("liquibase")`               |
| 11.13  | Removed                              | Deleted                     | Deleted            | Cleaned up            | ✅ Fully auto-configured                |

**Recommended atomic commit grouping:**

- **Commit 1:** Steps 11.1–11.3 (additive, zero risk)
- **Commit 2:** Steps 11.4–11.6 + 11.8 + 11.12 (the switch — must be atomic to avoid broken `unitName`/`@DependsOn` references)
- **Commit 3:** Step 11.7 (remove `persistence.xml` + build hack)
- **Commit 4:** Step 11.9 (transaction refactoring — `TransactionTemplate` → `@Transactional`)
- **Commit 5:** Step 11.10 (dependency cleanup)
- **Commit 6:** Step 11.11 (test infrastructure)

---

## Design Note: Why Property Indirection (`${db.*}`)

The Spring Boot properties reference the legacy `db.*` properties via `${db.driver}`, `${db.url}`, etc. This is intentional:

1. **Zero changes to deployment configs.** All existing environment variables, Kubernetes ConfigMaps, Helm values, and dev config files that set `db.server`, `db.username`, etc. continue to work without modification.
2. **Gradual deprecation.** After Step 11 is stable in production, a follow-up task can rename `db.*` → `spring.datasource.*` in deployment configs and remove the indirection.
3. **Minimal blast radius.** If something goes wrong with auto-config, reverting the code changes restores the manual config — no deployment config rollback needed.

Once the migration is fully validated, the legacy `db.*` / `hibernate.*` properties can be removed from `application.properties` and replaced with direct `spring.datasource.*` / `spring.jpa.*` values in a separate, low-risk follow-up.
