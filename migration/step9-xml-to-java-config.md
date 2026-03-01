# Step 9 — Convert XML Bean Configuration to Java (Detailed Incremental Plan)

## Progress Tracker

> Update the Status column as each step is completed.
> Statuses: `⬜ TODO` | `🔄 IN PROGRESS` | `✅ DONE` | `⏭️ SKIPPED`

| Step     | Description                                                               | Status | Commit/PR | Verified | Notes                                                                          |
|----------|---------------------------------------------------------------------------|--------|-----------|----------|--------------------------------------------------------------------------------|
| **9.1**  | Remove empty `jaxrs-context.xml` import from `application-context.xml`    | ✅ DONE |           | ✅        | File deleted; no import existed in application-context.xml                     |
| **9.2**  | Convert `basic-cache-config.xml` → `CacheConfig.java`                     | ✅ DONE |           | ✅        |                                                                                |
| **9.3**  | Convert integration proxy service XML configs → Java config               | ✅ DONE |           | ✅        | Packages added to component-scan in application-context.xml; XML files deleted |
| **9.4**  | Convert `moduleRegistry` bean → Java config                               | ⬜ TODO |           |          |                                                                                |
| **9.5**  | Convert `IntygBootstrapBean` (profile bean) → `@Component`                | ⬜ TODO |           |          |                                                                                |
| **9.6**  | Convert classpath XML imports to `@ImportResource`                        | ⬜ TODO |           |          |                                                                                |
| **9.7**  | Convert CXF WS clients → `CxfClientConfig.java`                           | ⬜ TODO |           |          |                                                                                |
| **9.8**  | Convert CXF WS endpoints → `CxfEndpointConfig.java`                       | ⬜ TODO |           |          |                                                                                |
| **9.9**  | Convert CXF conduit (TLS) → Java config                                   | ⬜ TODO |           |          |                                                                                |
| **9.10** | Convert stub WS endpoints → `CxfStubConfig.java`                          | ⬜ TODO |           |          |                                                                                |
| **9.11** | Remove `application-context-ws.xml` and `application-context-ws-stub.xml` | ⬜ TODO |           |          |                                                                                |
| **9.12** | Remove `application-context.xml` + update `web.xml`                       | ⬜ TODO |           |          |                                                                                |
| **9.13** | Convert `test-application-context.xml` → Java `@Configuration`            | ⬜ TODO |           |          |                                                                                |

**Deployment batches:**

- 🚀 **Batch 1:** Steps 9.1–9.5 (safe, isolated changes — deploy together)
- 🚀 **Batch 2:** Step 9.6 (migrate classpath XML imports — deploy independently)
- 🚀 **Batch 3:** Steps 9.7–9.11 (CXF endpoint migration — deploy together)
- 🚀 **Batch 4:** Step 9.12 (the big switch — deploy independently)
- 🚀 **Batch 5:** Step 9.13 (test-only change — no deployment needed)

---

## Pre-conditions

Steps 1–8 must be completed:

- ✅ All tests on JUnit 5
- ✅ All infra dependencies inlined
- ✅ All JAX-RS controllers converted to Spring MVC
- ✅ `jaxrs-context.xml` content already removed (Step 8.15), but the `<import>` may still linger in `application-context.xml`

---

## Current State: XML Files to Convert

| XML File                                         | Location                                              | What It Does                                                                             | Target                                                                              |
|--------------------------------------------------|-------------------------------------------------------|------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| `application-context.xml`                        | `web/src/main/resources/`                             | Root context: component-scan, bean declarations, imports                                 | **Remove** — all config moves to `ApplicationConfig.java` + new Java config classes |
| `application-context-ws.xml`                     | `web/src/main/resources/`                             | 20+ `jaxws:endpoint` declarations, 6 `jaxws:client` declarations, CXF TLS conduit config | **Remove** → `CxfEndpointConfig.java`, `CxfClientConfig.java`                       |
| `application-context-ws-stub.xml`                | `web/src/main/resources/`                             | 5 stub `jaxws:endpoint` declarations, profile `it-fk-stub`                               | **Remove** → `CxfStubConfig.java`                                                   |
| `jaxrs-context.xml`                              | `web/src/main/resources/`                             | **Already empty** (Step 8.15 removed content)                                            | **Remove**                                                                          |
| `basic-cache-config.xml`                         | `integration-intyg-proxy-service/src/main/resources/` | `@EnableCaching`, profile-based cache manager setup                                      | **Remove** → `CacheConfig.java`                                                     |
| `hsa-integration-intyg-proxy-service-config.xml` | `integration-intyg-proxy-service/src/main/resources/` | Component-scan for HSA integration                                                       | **Remove** → Java config                                                            |
| `pu-integration-intyg-proxy-service-config.xml`  | `integration-intyg-proxy-service/src/main/resources/` | Component-scan for PU integration                                                        | **Remove** → Java config                                                            |
| `test-application-context.xml`                   | `web/src/test/resources/`                             | Test context: property-placeholder + 2 beans                                             | **Remove** → `@Configuration` inner class                                           |
| `persistence.xml`                                | `persistence/src/main/resources/META-INF/`            | JPA persistence unit with entity classes listed                                          | **Keep for now** — removed in Step 11 (JPA auto-config)                             |

---

## Step 9.1 — Remove Empty `jaxrs-context.xml` Import

**What:** Step 8.15 already emptied `jaxrs-context.xml`, but `application-context.xml` may still have an `<import>` for it (or the file
itself may still exist). Remove both.

**Changes:**

1. If `jaxrs-context.xml` still exists → delete the file
2. If `application-context.xml` still has `<import resource="jaxrs-context.xml"/>` → remove that line

**Note:** Looking at the current `application-context.xml`, there is no import for `jaxrs-context.xml` — it was already removed in Step 8.
However, the file `jaxrs-context.xml` itself still exists on disk. Delete it.

**Files changed:**

- Delete `web/src/main/resources/jaxrs-context.xml`

**Verify:**

- `./gradlew test` — all tests pass
- Start app with Gretty — all endpoints respond

---

## Step 9.2 — Convert `basic-cache-config.xml` → `CacheConfig.java`

**What:** The `basic-cache-config.xml` in `integration-intyg-proxy-service` does three things:

1. `<cache:annotation-driven cache-manager="cacheManager"/>` — enables `@Cacheable` etc.
2. Profile `caching-enabled` or `prod` → loads `BasicCacheConfiguration` (Redis-backed `CacheManager`)
3. Profile `!caching-enabled` AND `!prod` → creates a `NoOpCacheManager` bean

The `BasicCacheConfiguration` Java class **already exists** and is already a `@Configuration` with `@EnableCaching`. It just needs to be
loaded via component-scan or `@Import` instead of XML.

**Changes:**

1. Create `CacheConfig.java` in `integration-intyg-proxy-service/.../configuration/` (or add to existing config):
   ```java
   @Configuration
   @EnableCaching
   public class CacheConfig {
       @Bean
       @Profile("!caching-enabled & !prod")
       public CacheManager noOpCacheManager() {
           return new NoOpCacheManager();
       }
   }
   ```
    - `BasicCacheConfiguration` is already `@Configuration` and `@EnableCaching` with beans conditional on its own loading. It is already
      component-scanned via the HSA/PU XML configs. It will continue to work.
    - The `NoOpCacheManager` fallback just needs to move from XML to Java.

2. Remove `<import resource="classpath:basic-cache-config.xml"/>` from `application-context.xml`
3. Delete `integration-intyg-proxy-service/src/main/resources/basic-cache-config.xml`

**Decision:** `@EnableCaching` is always active (added to `ApplicationConfig.java`), with `NoOpCacheManager` as fallback for non-caching
profiles. This matches the XML behavior where `<cache:annotation-driven>` was at the top level (all profiles).

**Files changed:**

- New: `integration-intyg-proxy-service/.../configuration/CacheConfig.java` (or add `NoOpCacheManager` bean to existing config)
- Modify: `web/src/main/resources/application-context.xml` (remove import line)
- Delete: `integration-intyg-proxy-service/src/main/resources/basic-cache-config.xml`
- Modify: `ApplicationConfig.java` (add `@EnableCaching`)

**Verify:**

- `./gradlew test` — all tests pass
- Start app with `caching-enabled` profile → verify Redis-backed caching still works
- Start app without `caching-enabled` → verify `NoOpCacheManager` is used (no errors)

---

## Step 9.3 — Convert Integration Proxy Service XML Configs → Java Config

**What:** Two XML files each contain only a `<context:component-scan>`:

- `hsa-integration-intyg-proxy-service-config.xml` →
  `<context:component-scan base-package="se.inera.intyg.infra.integration.intygproxyservice"/>`
- `pu-integration-intyg-proxy-service-config.xml` →
  `<context:component-scan base-package="se.inera.intyg.infra.pu.integration.intygproxyservice"/>`

These packages are **already under component-scan** from `application-context.xml`'s broader scan if the class is in the classpath. But
since they are in a separate module (`integration-intyg-proxy-service`), the main `application-context.xml` component-scan does not cover
them — the XML imports are how they get loaded.

**Changes:**

1. Create `IntygProxyServiceConfig.java` in `integration-intyg-proxy-service`:
   ```java
   @Configuration
   @ComponentScan(basePackages = {
       "se.inera.intyg.infra.integration.intygproxyservice",
       "se.inera.intyg.infra.pu.integration.intygproxyservice"
   })
   public class IntygProxyServiceConfig {
   }
   ```
2. Add the two component-scan packages to `ApplicationConfig.java`'s existing `@ComponentScan`, **or** use
   `@Import(IntygProxyServiceConfig.class)` on `ApplicationConfig`.

   **Recommendation:** Since these are in a runtime-only dependency, the cleanest approach is to add
   `@Import(IntygProxyServiceConfig.class)` on `ApplicationConfig`, or simply widen the component-scan in `application-context.xml` to
   include these packages (then later in 9.12 move that scan to Java). For now, replace the two `<import resource="classpath:..."/>` lines
   in `application-context.xml` with adding these packages to the existing `<context:component-scan>`.

   **Simplest approach:** Add these packages to the component-scan in `application-context.xml` temporarily:
   ```xml
   <context:component-scan base-package="
       se.inera.intyg.intygstjanst.config,
       se.inera.intyg.intygstjanst.logging,
       se.inera.intyg.intygstjanst.persistence,
       se.inera.intyg.intygstjanst.web,
       se.inera.intyg.infra.integration.intygproxyservice,
       se.inera.intyg.infra.pu.integration.intygproxyservice,
       se.inera.intyg.infra.rediscache.core"/>
   ```

3. Remove the two `<import>` lines from `application-context.xml`
4. Delete the two XML files

**Files changed:**

- Modify: `web/src/main/resources/application-context.xml` (replace imports with component-scan entries)
- Delete: `integration-intyg-proxy-service/src/main/resources/hsa-integration-intyg-proxy-service-config.xml`
- Delete: `integration-intyg-proxy-service/src/main/resources/pu-integration-intyg-proxy-service-config.xml`

**Verify:**

- `./gradlew test` — all tests pass
- Start app → HSA and PU integrations still work

---

## Step 9.4 — Convert `moduleRegistry` Bean → Java Config

**What:** In `application-context.xml`:

```xml

<bean id="moduleRegistry" class="se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl"
		depends-on="dbUpdate">
	<property name="origin" value="INTYGSTJANST"/>
</bean>
```

This creates an `IntygModuleRegistryImpl` bean with the `origin` property set, and declares it depends on `dbUpdate` (the Liquibase bean).

**Changes:**

1. Add a `@Bean` method in `ApplicationConfig.java`:
   ```java
   @Bean
   @DependsOn("dbUpdate")
   public IntygModuleRegistryImpl moduleRegistry() {
       IntygModuleRegistryImpl registry = new IntygModuleRegistryImpl();
       registry.setOrigin("INTYGSTJANST");
       return registry;
   }
   ```
2. Remove the `<bean id="moduleRegistry" ...>` element from `application-context.xml`

**Note:** Several classes autowire `IntygModuleRegistryImpl` (not the interface). The `@Bean` return type should be
`IntygModuleRegistryImpl` to ensure type-compatibility with those injection points.

**Files changed:**

- Modify: `ApplicationConfig.java` (add bean method)
- Modify: `application-context.xml` (remove bean element)

**Verify:**

- `./gradlew test` — all tests pass
- Start app → verify the module registry loads (certificate registration still works)

---

## Step 9.5 — Convert `IntygBootstrapBean` (Profile Bean) → `@Component` + `@Profile`

**What:** In `application-context.xml`:

```xml

<beans profile="bootstrap">
	<bean id="IntygBootstrapBean" class="se.inera.intyg.intygstjanst.web.service.bean.IntygBootstrapBean">
		<property name="txManager" ref="transactionManager"/>
	</bean>
</beans>
```

The bean is only created when `bootstrap` profile is active. It already has `@Autowired` on `setTxManager` and `@PostConstruct`. The XML
`<property name="txManager" ref="transactionManager"/>` is redundant since the setter is already annotated with `@Autowired`.

**Changes:**

1. Add `@Component` and `@Profile("bootstrap")` to `IntygBootstrapBean.java`:
   ```java
   @Component
   @Profile("bootstrap")
   public class IntygBootstrapBean {
   ```
    - The class is already in `se.inera.intyg.intygstjanst.web.service.bean` which is under the existing component-scan.
    - The `setTxManager` method is already `@Autowired`, so Spring will inject the `transactionManager` automatically.

2. Remove the `<beans profile="bootstrap">` block from `application-context.xml`

**Files changed:**

- Modify: `IntygBootstrapBean.java` (add `@Component` + `@Profile("bootstrap")`)
- Modify: `application-context.xml` (remove the profile beans block)

**Verify:**

- `./gradlew test` — all tests pass
- Start app with `bootstrap` profile → data is bootstrapped
- Start app without `bootstrap` profile → no bootstrap bean is created

---

## Step 9.6 — Convert Classpath XML Imports to `@ImportResource`

**What:** `application-context.xml` imports three external XML configs from JAR dependencies:

```xml

<import resource="classpath:common-config.xml"/>
<import resource="classpath*:module-config.xml"/>
<import resource="classpath*:it-module-cxf-servlet.xml"/>
```

These are XML files **shipped inside external JARs** (from `se.inera.intyg.common` modules like `fk7263`, `lisjp`, etc.). We **cannot
convert them to Java** — they belong to other projects. We must keep loading them via `@ImportResource`.

**Changes:**

1. Update the `@ImportResource` annotation on `ApplicationConfig.java`:
   ```java
   @ImportResource({
       "classpath:META-INF/cxf/cxf.xml",
       "classpath:common-config.xml",
       "classpath*:module-config.xml",
       "classpath*:it-module-cxf-servlet.xml"
   })
   ```
2. Remove the three `<import>` lines from `application-context.xml`
3. Also move the component-scan for external packages from `application-context.xml` to `ApplicationConfig.java`:
   ```java
   @ComponentScan(basePackages = {
       "se.inera.intyg.common.support.modules.support.api",
       "se.inera.intyg.common.services",
       "se.inera.intyg.common.support.services",
       "se.inera.intyg.common.util.integration.json"
   })
   ```

   **Note:** `ApplicationConfig.java` is already under the main component-scan. The `@ComponentScan` on `ApplicationConfig` will **add**
   these packages to the scan.

**Files changed:**

- Modify: `ApplicationConfig.java` (update `@ImportResource`, add `@ComponentScan`)
- Modify: `application-context.xml` (remove the three import lines + the component-scan for external packages)

**Verify:**

- `./gradlew test` — all tests pass
- Start app → all certificate modules load correctly (register/get/list operations work)

---

## Step 9.7 — Convert CXF WS Clients → `CxfClientConfig.java`

**What:** `application-context-ws.xml` declares 6 JAX-WS clients:

```xml

<jaxws:client id="revokeMedicalCertificateClient" serviceClass="...RevokeMedicalCertificateResponderInterface" address="${...}"/>
<jaxws:client id="sendMedicalCertificateQuestionClient" serviceClass="...SendMedicalCertificateQuestionResponderInterface" address="${...}"/>
<jaxws:client id="sendMessageToCareClient" serviceClass="...SendMessageToCareResponderInterface" address="${...}"/>
<jaxws:client id="sendMessageToRecipientClient" serviceClass="...SendMessageToRecipientResponderInterface" address="${...}"/>
<jaxws:client id="revokeCertificateClient" serviceClass="...RevokeCertificateResponderInterface" address="${...}"/>
<jaxws:client id="registerCertificateClient" serviceClass="...RegisterCertificateResponderInterface" address="${...}"/>
```

These are injected via `@Qualifier` in `SoapIntegrationServiceImpl` and `CertificateEventSendServiceImpl`.

**Usage analysis:**

- `revokeCertificateClient` → used in `SoapIntegrationServiceImpl`
- `sendMessageToRecipientClient` → used in `SoapIntegrationServiceImpl` and `CertificateEventSendMessageServiceImpl`
- `sendMessageToCareClient` → used in `SoapIntegrationServiceImpl`
- `revokeMedicalCertificateClient` → used in `SoapIntegrationServiceImpl` (injected by type, no `@Qualifier`)
- `sendMedicalCertificateQuestionClient` → used in `SoapIntegrationServiceImpl` (injected by type, no `@Qualifier`)
- `registerCertificateClient` → used in `CertificateEventSendServiceImpl`

**Changes:**

1. Create `CxfClientConfig.java` in `se.inera.intyg.intygstjanst.config`:
   ```java
   @Configuration
   public class CxfClientConfig {
   
       @Bean("revokeMedicalCertificateClient")
       public RevokeMedicalCertificateResponderInterface revokeMedicalCertificateClient(
               @Value("${revokemedicalcertificatev1.endpoint.url}") String address) {
           JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
           factory.setServiceClass(RevokeMedicalCertificateResponderInterface.class);
           factory.setAddress(address);
           return (RevokeMedicalCertificateResponderInterface) factory.create();
       }
       // ... repeat for all 6 clients
   }
   ```
2. Remove the 6 `<jaxws:client>` elements from `application-context-ws.xml`

**Files changed:**

- New: `web/src/main/java/se/inera/intyg/intygstjanst/config/CxfClientConfig.java`
- Modify: `application-context-ws.xml` (remove 6 `<jaxws:client>` elements)

**Verify:**

- `./gradlew test` — all tests pass
- Start app → test sending a certificate to recipient, revoking a certificate (SOAP client calls)
- Verify the correct `@Qualifier` beans are resolved

---

## Step 9.8 — Convert CXF WS Endpoints → `CxfEndpointConfig.java`

**What:** `application-context-ws.xml` declares ~20 `jaxws:endpoint` elements. Each one:

- Specifies an `address` (URL path)
- Specifies an `implementor` class
- Optionally has `outFaultInterceptors` (a `SoapFaultToSoapResponseTransformerInterceptor` with an XSLT path)
- Optionally has `schemaLocations` for validation

The implementor classes use `@Autowired` for their dependencies. Currently, CXF creates instances of these classes and Spring injects their
dependencies (because CXF integrates with the Spring context).

**Approach:** Use CXF's programmatic API (`EndpointImpl` from `org.apache.cxf.jaxws`) to register endpoints in Java.

**Changes:**

1. Create `CxfEndpointConfig.java` in `se.inera.intyg.intygstjanst.config`:
   ```java
   @Configuration
   public class CxfEndpointConfig {
   
       @Autowired private Bus bus;
   
       @Bean
       public Endpoint getRecipientsEndpoint(
               GetRecipientsForCertificateResponderImpl implementor) {
           EndpointImpl endpoint = new EndpointImpl(bus, implementor);
           endpoint.publish("/get-recipients-for-certificate/v1.1");
           return endpoint;
       }
       // ... repeat for each endpoint
   }
   ```

2. **Decision:** Option B — declare all implementor instances as `@Bean` methods in `CxfEndpointConfig` for explicit control. This keeps
   the implementor classes as plain POJOs (no annotation changes needed) and gives full visibility of what beans exist in a single config
   class. Spring will autowire the `@Autowired` fields inside each implementor since they are Spring-managed beans (created via `@Bean`).

   The full config structure looks like:
   ```java
   @Configuration
   public class CxfEndpointConfig {

       @Autowired private Bus bus;

       // -- Implementor beans (24 classes) --

       @Bean
       public GetRecipientsForCertificateResponderImpl getRecipientsForCertificateResponder() {
           return new GetRecipientsForCertificateResponderImpl();
       }
       // ... repeat for each implementor

       // -- Endpoint registration (24 endpoints) --

       @Bean
       public Endpoint getRecipientsEndpoint(
               GetRecipientsForCertificateResponderImpl implementor) {
           EndpointImpl endpoint = new EndpointImpl(bus, implementor);
           endpoint.publish("/get-recipients-for-certificate/v1.1");
           return endpoint;
       }
       // ... repeat for each endpoint
   }
   ```

3. For endpoints with `outFaultInterceptors`, add them programmatically:
   ```java
   endpoint.getOutFaultInterceptors().add(
       new SoapFaultToSoapResponseTransformerInterceptor("transform/...xslt"));
   ```

4. For endpoints with `schemaLocations`, set them programmatically:
   ```java
   endpoint.setSchemaLocations(List.of(
       "classpath:/core_components/...",
       ...
   ));
   ```

5. Remove all `<jaxws:endpoint>` elements from `application-context-ws.xml` (but **not** the stub endpoints — those are in
   `application-context-ws-stub.xml`)

**Implementor classes to declare as `@Bean` in `CxfEndpointConfig` (24 classes — no modifications to the classes themselves):**

- `GetRecipientsForCertificateResponderImpl`
- `ListKnownRecipientsResponderImpl`
- `ListRelationsForCertificateResponderImpl`
- `GetCertificateTypeInfoResponderImpl`
- `ListCertificatesResponderImpl`
- `RevokeMedicalCertificateResponderImpl`
- `RevokeCertificateResponderImpl`
- `SendMedicalCertificateResponderImpl`
- `SendCertificateToRecipientResponderImpl`
- `SendMessageToCareResponderImpl`
- `SendMessageToRecipientResponderImpl`
- `SetCertificateStatusResponderImpl`
- `RegisterCertificateResponderImpl`
- `v2.GetCertificateResponderImpl`
- `v3.ListCertificatesForCitizenResponderImpl`
- `v4.ListCertificatesForCitizenResponderImpl`
- `v3.ListCertificatesForCareResponderImpl`
- `v2.SetCertificateStatusResponderImpl`
- `ListApprovedReceiversResponderImpl`
- `ListPossibleReceiversResponderImpl`
- `RegisterApprovedReceiversResponderImpl`
- `ListActiveSickLeavesForCareUnitResponderImpl`
- `ListSickLeavesForPersonResponderImpl`
- `ListSickLeavesForCareResponderImpl`

**Files changed:**

- New: `web/src/main/java/se/inera/intyg/intygstjanst/config/CxfEndpointConfig.java`
- Modify: `application-context-ws.xml` (remove all `<jaxws:endpoint>` elements, keep the `<import>` for stubs and the conduit config)
- No changes to implementor classes (they remain plain POJOs; Spring autowires their fields since they are `@Bean`-managed)

**Verify:**

- `./gradlew test` — all tests pass
- Start app → hit every SOAP endpoint with a SOAP request and verify correct response
- WSDL pages still accessible at each endpoint address (e.g., `/inera-certificate/get-recipients-for-certificate/v1.1?wsdl`)

---

## Step 9.9 — Convert CXF Conduit (TLS) → Java Config

**What:** `application-context-ws.xml` has a profile-conditional HTTP conduit for TLS:

```xml

<beans profile="!dev">
	<http:conduit name="...">
		<http:client AllowChunking="false"
		.../>
		<http:tlsClientParameters
		...>
		<sec:keyManagers
		...>
		<sec:keyStore file="${ntjp.ws.certificate.file}"
		.../>
	</sec:keyManagers>
	<sec:trustManagers>
		<sec:keyStore file="${ntjp.ws.truststore.file}"
		.../>
	</sec:trustManagers>
	<sec:cipherSuitesFilter>...</sec:cipherSuitesFilter>
</http:tlsClientParameters>
		</http:conduit>
		</beans>
```

This configures TLS for outbound SOAP calls to NTJP (the national service platform). Only active in non-dev profiles.

**Changes:**

1. **Decision:** Create a dedicated `CxfTlsConfig.java` (separate from `CxfClientConfig`) for cleaner separation of concerns:
   ```java
   @Configuration
   @Profile("!dev")
   public class CxfTlsConfig {
       @Value("${ntjp.ws.certificate.file}") String certFile;
       @Value("${ntjp.ws.certificate.password}") String certPassword;
       @Value("${ntjp.ws.certificate.type}") String certType;
       @Value("${ntjp.ws.key.manager.password}") String keyManagerPassword;
       @Value("${ntjp.ws.truststore.file}") String truststoreFile;
       @Value("${ntjp.ws.truststore.password}") String truststorePassword;
       @Value("${ntjp.ws.truststore.type}") String truststoreType;
       
       @Bean
       public void configureCxfConduit(Bus bus) {
           // Programmatic CXF conduit configuration
           // using HTTPConduitConfigurer or direct bus manipulation
       }
   }
   ```

2. Remove the `<beans profile="!dev">` conduit block from `application-context-ws.xml`

**Files changed:**

- New: `web/src/main/java/se/inera/intyg/intygstjanst/config/CxfTlsConfig.java`
- Modify: `application-context-ws.xml` (remove conduit block)

**Verify:**

- `./gradlew test` — all tests pass
- Start app in dev mode → no TLS configured (works as before)
- Deploy to test environment (non-dev profile) → verify outbound SOAP calls to NTJP still use TLS

---

## Step 9.10 — Convert Stub WS Endpoints → `CxfStubConfig.java`

**What:** `application-context-ws-stub.xml` declares 5 stub endpoints, activated by profile `it-fk-stub`:

- `SendMedicalCertificateQuestionResponderStub`
- `RevokeMedicalCertificateResponderStub`
- `SendMessageToCareResponderStub`
- `SendMessageToRecipientResponderStub`
- `RevokeCertificateResponderStub` (also has a separate `<bean>` for the implementor since it's referenced by id)

**Changes:**

1. Create `CxfStubConfig.java`:
   ```java
   @Configuration
   @Profile("it-fk-stub")
   public class CxfStubConfig {
   
       @Autowired private Bus bus;
   
       @Bean
       public RevokeCertificateResponderStub revokeCertificateClientStub() {
           return new RevokeCertificateResponderStub();
       }
   
       @Bean
       public Endpoint sendMedicalCertificateQuestionStubEndpoint(
               SendMedicalCertificateQuestionResponderStub implementor) {
           EndpointImpl ep = new EndpointImpl(bus, implementor);
           ep.publish("/stubs/SendMedicalCertificateQuestion/1/rivtabp20");
           return ep;
       }
       // ... repeat for each stub endpoint
   }
   ```

2. Stub implementors that are not yet `@Component`-annotated:
    - `SendMedicalCertificateQuestionResponderStub` — check if it has `@Component`
    - `RevokeMedicalCertificateResponderStub` — check if it has `@Component`
    - `SendMessageToCareResponderStub` — already has `@Component`
    - `SendMessageToRecipientResponderStub` — check
    - `RevokeCertificateResponderStub` — explicitly declared as `<bean>` in XML, also used as endpoint implementor

   For stubs that aren't `@Component`, either add `@Component` + `@Profile("it-fk-stub")` or declare them as `@Bean` in `CxfStubConfig`.

3. Delete `application-context-ws-stub.xml`

**Files changed:**

- New: `web/src/main/java/se/inera/intyg/intygstjanst/config/CxfStubConfig.java`
- Possibly modify: stub implementor classes (add `@Component` + `@Profile`)
- Modify: `application-context-ws.xml` (remove `<import resource="application-context-ws-stub.xml"/>`)
- Delete: `web/src/main/resources/application-context-ws-stub.xml`

**Verify:**

- `./gradlew test` — all tests pass
- Start app with `it-fk-stub` profile → stub endpoints respond
- Start app without `it-fk-stub` profile → no stub endpoints are registered

---

## Step 9.11 — Remove `application-context-ws.xml`

**What:** After steps 9.7–9.10, `application-context-ws.xml` should be empty (all endpoints, clients, stubs, and conduit config have been
moved to Java). Remove the file and its `<import>` from `application-context.xml`.

**Changes:**

1. Remove `<import resource="application-context-ws.xml"/>` from `application-context.xml`
2. Delete `web/src/main/resources/application-context-ws.xml`

**Files changed:**

- Modify: `application-context.xml` (remove import)
- Delete: `web/src/main/resources/application-context-ws.xml`

**Verify:**

- `./gradlew test` — all tests pass
- Start app → all SOAP + REST endpoints still work

---

## Step 9.12 — Remove `application-context.xml` + Update `web.xml`

**What:** At this point, `application-context.xml` should only contain:

- `<context:annotation-config/>` (redundant — already implied by `@Configuration`)
- `<context:component-scan>` (already covered by `ApplicationConfig.java`)
- Maybe some leftover bits

Move any remaining configuration to `ApplicationConfig.java` and switch `web.xml` to load the Java config class instead of the XML file.

**Changes:**

1. Ensure `ApplicationConfig.java` has all the component-scan packages from the XML:
   ```java
   @ComponentScan(basePackages = {
       "se.inera.intyg.intygstjanst.config",
       "se.inera.intyg.intygstjanst.logging",
       "se.inera.intyg.intygstjanst.persistence",
       "se.inera.intyg.intygstjanst.web",
       "se.inera.intyg.common.support.modules.support.api",
       "se.inera.intyg.common.services",
       "se.inera.intyg.common.support.services",
       "se.inera.intyg.common.util.integration.json",
       "se.inera.intyg.infra.integration.intygproxyservice",
       "se.inera.intyg.infra.pu.integration.intygproxyservice",
       "se.inera.intyg.infra.rediscache.core"
   })
   ```

2. Update `web.xml` to use `AnnotationConfigWebApplicationContext`:
   ```xml
   <context-param>
       <param-name>contextClass</param-name>
       <param-value>org.springframework.web.context.support.AnnotationConfigWebApplicationContext</param-value>
   </context-param>
   <context-param>
       <param-name>contextConfigLocation</param-name>
       <param-value>se.inera.intyg.intygstjanst.config.ApplicationConfig</param-value>
   </context-param>
   ```

3. Delete `web/src/main/resources/application-context.xml`

**Files changed:**

- Modify: `ApplicationConfig.java` (ensure complete `@ComponentScan`)
- Modify: `web.xml` (switch to `AnnotationConfigWebApplicationContext`)
- Delete: `web/src/main/resources/application-context.xml`

**Verify:**

- `./gradlew test` — all tests pass
- `./gradlew appRun` → application starts successfully
- Test all SOAP endpoints (WSDL accessible, SOAP requests work)
- Test all REST endpoints (`/internalapi/*`, `/api/*`, `/resources/*`)
- Verify bootstrap profile still works
- Verify caching works in both caching-enabled and disabled profiles

---

## Step 9.13 — Convert `test-application-context.xml` → Java `@Configuration`

**What:** `web/src/test/resources/test-application-context.xml` contains:

```xml

<context:property-placeholder location="classpath:application.properties"/>
<context:annotation-config/>
<bean id="recipientService" class="...RecipientServiceImpl"/>
<bean id="recipientRepo" class="...RecipientRepoImpl"/>
```

This file is currently **not referenced by any test** (grep found no references in test Java files). It may be dead code.

**Changes:**

1. Verify that no test uses this file:
   ```bash
   grep -r "test-application-context" web/src/test/
   ```
2. If unused → simply delete it.
3. If used → create a test `@Configuration` inner class:
   ```java
   @Configuration
   @PropertySource("classpath:application.properties")
   static class TestAppConfig {
       @Bean
       public RecipientServiceImpl recipientService() {
           return new RecipientServiceImpl();
       }
       @Bean
       public RecipientRepoImpl recipientRepo() {
           return new RecipientRepoImpl();
       }
   }
   ```

**Files changed:**

- Delete: `web/src/test/resources/test-application-context.xml`

**Verify:**

- `./gradlew test` — all tests pass

---

## Risk Assessment

| Risk                                                                                      | Likelihood | Impact | Mitigation                                                          |
|-------------------------------------------------------------------------------------------|------------|--------|---------------------------------------------------------------------|
| CXF programmatic endpoint registration behaves differently than XML `jaxws:endpoint`      | Medium     | High   | Test each SOAP endpoint individually after Step 9.8                 |
| Schema validation (via `schemaLocations`) not working in programmatic API                 | Medium     | Medium | Verify with a request that triggers validation                      |
| TLS conduit config in Java doesn't match XML behavior                                     | Medium     | High   | Test in staging environment with real NTJP connectivity             |
| Component-scan ordering / bean creation order changes                                     | Low        | Medium | `@DependsOn` annotations where needed                               |
| External XML configs (`common-config.xml`, `module-config.xml`) conflict with Java config | Low        | Medium | These are loaded via `@ImportResource` — no change in how they work |

---

## Checklist Before Starting

- [ ] Step 8 is fully completed and verified
- [ ] All existing tests pass (`./gradlew test`)
- [ ] You have a way to test SOAP endpoints (SoapUI, curl, integration tests)
- [ ] You have access to a staging/test environment to verify TLS (Step 9.9)
- [ ] Git branch created for Step 9

---

## Summary: File Inventory

### Files to Create

| File                     | Step |
|--------------------------|------|
| `CxfClientConfig.java`   | 9.7  |
| `CxfEndpointConfig.java` | 9.8  |
| `CxfTlsConfig.java`      | 9.9  |
| `CxfStubConfig.java`     | 9.10 |

### Files to Modify

| File                         | Steps                                |
|------------------------------|--------------------------------------|
| `ApplicationConfig.java`     | 9.2, 9.4, 9.6, 9.12                  |
| `application-context.xml`    | 9.1–9.6, 9.11 (then deleted in 9.12) |
| `application-context-ws.xml` | 9.7–9.10 (then deleted in 9.11)      |
| `web.xml`                    | 9.12                                 |
| `IntygBootstrapBean.java`    | 9.5                                  |
| Stub implementor classes     | 9.10                                 |

### Files to Delete

| File                                             | Step |
|--------------------------------------------------|------|
| `jaxrs-context.xml`                              | 9.1  |
| `basic-cache-config.xml`                         | 9.2  |
| `hsa-integration-intyg-proxy-service-config.xml` | 9.3  |
| `pu-integration-intyg-proxy-service-config.xml`  | 9.3  |
| `application-context-ws-stub.xml`                | 9.10 |
| `application-context-ws.xml`                     | 9.11 |
| `application-context.xml`                        | 9.12 |
| `test-application-context.xml`                   | 9.13 |





