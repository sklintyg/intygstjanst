# Step 15 — Kubernetes Configuration Migration Guide

> **Audience:** Platform engineers responsible for updating K8s ConfigMaps and sealed secrets.
> **Context:** The `intygstjanst` application has been refactored from scattered `@Value` annotations
> + flat `application.properties` to structured YAML + immutable `@ConfigurationProperties` records.
> This document describes all configuration changes that require updates to Kubernetes resources.

---

## Background

Prior to this refactor, the application expected all environment-specific config as flat Spring
property keys injected via ConfigMap/Secret (e.g. `ntjp.ws.certificate.file`). After the refactor,
application-specific properties are namespaced under `app.*` and secrets are preferably injected as
environment variables (e.g. `HASH_SALT`). Framework properties (`spring.*`, `server.*`, etc.) are
unchanged.

> **IMPORTANT — Transition state:**
> The bridge properties `db.*`, `activemq.broker.*`, and `redis.*` are **still referenced** by
> `application.yml` to compose `spring.datasource.url`, `spring.activemq.*`, and
> `spring.data.redis.*`. These bridge properties must continue to be supplied until the datasource
> URL is refactored to use environment variables directly. All other legacy flat properties have
> been removed.

---

## 1. Property Rename Table

### 1.1 Custom application properties (all migrated to `app.*`)

| Old Property Name | New Property Path | ConfigMap or Secret | Notes |
|---|---|---|---|
| `internal.api.port` | `app.server.internal-port` | ConfigMap | Default `8081`; unlikely overridden in K8s |
| `ntjp.base.url` | `app.ntjp.base-url` | ConfigMap | Base URL used to compose all NTjP endpoint URLs |
| `ntjp.ws.certificate.file` | `app.ntjp.tls.certificate-file` | ConfigMap | File path to PKCS12 cert |
| `ntjp.ws.certificate.password` | `app.ntjp.tls.certificate-password` | **Secret** | Certificate password |
| `ntjp.ws.certificate.type` | `app.ntjp.tls.certificate-type` | ConfigMap | Default `PKCS12` |
| `ntjp.ws.key.manager.password` | `app.ntjp.tls.key-manager-password` | **Secret** | Key manager password |
| `ntjp.ws.truststore.file` | `app.ntjp.tls.truststore-file` | ConfigMap | File path to JKS truststore |
| `ntjp.ws.truststore.password` | `app.ntjp.tls.truststore-password` | **Secret** | Truststore password |
| `ntjp.ws.truststore.type` | `app.ntjp.tls.truststore-type` | ConfigMap | Default `JKS` |
| `registercertificatev1.endpoint.url` | `app.ntjp.endpoints.register-certificate-v1` | ConfigMap | Composed from `app.ntjp.base-url` by default |
| `registercertificatev3.endpoint.url` | `app.ntjp.endpoints.register-certificate-v3` | ConfigMap | Composed from `app.ntjp.base-url` by default |
| `registermedicalcertificatev3.endpoint.url` | `app.ntjp.endpoints.register-medical-certificate-v3` | ConfigMap | Composed from `app.ntjp.base-url` by default |
| `revokecertificatev2.endpoint.url` | `app.ntjp.endpoints.revoke-certificate-v2` | ConfigMap | Composed from `app.ntjp.base-url` by default |
| `revokemedicalcertificatev1.endpoint.url` | `app.ntjp.endpoints.revoke-medical-certificate-v1` | ConfigMap | Composed from `app.ntjp.base-url` by default |
| `sendmedicalcertificatequestionv1.endpoint.url` | `app.ntjp.endpoints.send-medical-certificate-question-v1` | ConfigMap | Composed from `app.ntjp.base-url` by default |
| `sendmessagetocarev2.endpoint.url` | `app.ntjp.endpoints.send-message-to-care-v2` | ConfigMap | Composed from `app.ntjp.base-url` by default |
| `sendmessagetorecipientv2.endpoint.url` | `app.ntjp.endpoints.send-message-to-recipient-v2` | ConfigMap | Composed from `app.ntjp.base-url` by default |
| `tsbas.send.certificate.to.recipient.registercertificate.version` | `app.ntjp.ts-bas-register-certificate-version` | ConfigMap | Default `v3` |
| `activemq.destination.queue.name` | `app.jms.statistics-queue` | ConfigMap | Default `certificate.queue` |
| `activemq.internal.notification.queue.name` | `app.jms.internal-notification-queue` | ConfigMap | Default `internal.notification.queue` |
| `certificate.event.queue.name` | `app.jms.certificate-event-queue` | ConfigMap | Default `intygstjanst.certificate.event.queue` |
| `statistics.enabled` | `app.jms.statistics-enabled` | ConfigMap | Default `true` |
| `certificateservice.base.url` | `app.integration.certificate-service.base-url` | ConfigMap | Base URL for certificate service |
| `integration.intygproxyservice.baseurl` | `app.integration.intyg-proxy-service.base-url` | ConfigMap | Base URL for intyg-proxy-service |
| `integration.intygproxyservice.employee.endpoint` | `app.integration.intyg-proxy-service.hsa.employee-endpoint` | ConfigMap | Default `/api/v2/employee` |
| `integration.intygproxyservice.credentialinformationforperson.endpoint` | `app.integration.intyg-proxy-service.hsa.credential-information-endpoint` | ConfigMap | Default `/api/v1/credentialinformation` |
| `integration.intygproxyservice.healthcareunit.endpoint` | `app.integration.intyg-proxy-service.hsa.healthcare-unit-endpoint` | ConfigMap | Default `/api/v2/healthcareunit` |
| `integration.intygproxyservice.healthcareunitmembers.endpoint` | `app.integration.intyg-proxy-service.hsa.healthcare-unit-members-endpoint` | ConfigMap | Default `/api/v2/healthcareunitmembers` |
| `integration.intygproxyservice.unit.endpoint` | `app.integration.intyg-proxy-service.hsa.unit-endpoint` | ConfigMap | Default `/api/v1/unit` |
| `integration.intygproxyservice.credentialsforperson.endpoint` | `app.integration.intyg-proxy-service.hsa.credentials-for-person-endpoint` | ConfigMap | Default `/api/v1/credentialsForPerson` |
| `integration.intygproxyservice.certificationperson.endpoint` | `app.integration.intyg-proxy-service.hsa.certification-person-endpoint` | ConfigMap | Default `/api/v1/certificationPerson` |
| `integration.intygproxyservice.lastupdate.endpoint` | `app.integration.intyg-proxy-service.hsa.last-update-endpoint` | ConfigMap | Default `/api/v1/lastUpdate` |
| `integration.intygproxyservice.provider.endpoint` | `app.integration.intyg-proxy-service.hsa.healthcare-provider-endpoint` | ConfigMap | Default `/api/v1/healthcareprovider` |
| `integration.intygproxyservice.person.endpoint` | `app.integration.intyg-proxy-service.pu.person-endpoint` | ConfigMap | Default `/api/v1/person` |
| `integration.intygproxyservice.persons.endpoint` | `app.integration.intyg-proxy-service.pu.persons-endpoint` | ConfigMap | Default `/api/v1/persons` |
| `hsa.intygproxyservice.getemployee.cache.expiry` | `app.integration.intyg-proxy-service.cache.employee-ttl-seconds` | ConfigMap | Default `60` |
| `hsa.intygproxyservice.gethealthcareunit.cache.expiry` | `app.integration.intyg-proxy-service.cache.healthcare-unit-ttl-seconds` | ConfigMap | Default `60` |
| `hsa.intygproxyservice.gethealthcareunitmembers.cache.expiry` | `app.integration.intyg-proxy-service.cache.healthcare-unit-members-ttl-seconds` | ConfigMap | Default `60` |
| `hsa.intygproxyservice.getunit.cache.expiry` | `app.integration.intyg-proxy-service.cache.unit-ttl-seconds` | ConfigMap | Default `60` |
| `hsa.intygproxyservice.gethealthcareprovider.cache.expiry` | `app.integration.intyg-proxy-service.cache.healthcare-provider-ttl-seconds` | ConfigMap | Default `60` |
| `it.diagnosis.chapters.file` | `app.diagnosis.chapters-file` | ConfigMap | Default `classpath:/diagnoskoder/diagnoskapitel.txt` |
| `it.diagnosisCodes.icd10se.file` | `app.diagnosis.icd10se-file` | ConfigMap | Default `classpath:/diagnoskoder/icd10se/icd-10-se.tsv` |
| `it.diagnosisCodes.ksh97p_kod.file` | `app.diagnosis.ksh97p-file` | ConfigMap | Default `classpath:/diagnoskoder/KSH97P_KOD.ANS` |
| `texts.file.directory` | `app.texts.file-directory` | ConfigMap | Default `classpath:/texts/` |
| `texts.update.cron` | `app.texts.update-cron` | ConfigMap | Default `0 0 0 * * *` |
| `recipients.update.cron` | `app.recipients.update-cron` | ConfigMap | Default `0 0 0 * * *` |
| `recipient.file` | `app.recipients.file` | ConfigMap | Path to recipients JSON file |
| `hash.salt` | `app.security.hash-salt` (or env var `HASH_SALT`) | **Secret** | Hash salt for certificate ID hashing |
| `erase.certificates.page.size` | `app.erase.page-size` | ConfigMap | Default `1000` |

### 1.2 Bridge properties (still required — not yet migrated)

> These properties are still referenced by `application.yml` to compose the datasource URL and
> connection strings. They must continue to be injected until the bridge is removed in a future step.

| Property Name | ConfigMap or Secret | Used By |
|---|---|---|
| `db.server` | ConfigMap | `spring.datasource.url` composition |
| `db.port` | ConfigMap | `spring.datasource.url` (default: `3306`) |
| `db.name` | ConfigMap | `spring.datasource.url` composition |
| `db.username` | **Secret** | `spring.datasource.username` |
| `db.password` | **Secret** | `spring.datasource.password` |
| `activemq.broker.url` | ConfigMap | `spring.activemq.broker-url` |
| `activemq.broker.username` | **Secret** | `spring.activemq.user` |
| `activemq.broker.password` | **Secret** | `spring.activemq.password` |
| `redis.host` | ConfigMap | `spring.data.redis.host` |
| `redis.port` | ConfigMap | `spring.data.redis.port` (default: `6379`) |
| `redis.password` | **Secret** | `spring.data.redis.password` |

---

## 2. ConfigMap — Before / After Example

### Before

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: intygstjanst-config
data:
  ntjp.base.url: "https://ntjp.example.se"
  ntjp.ws.certificate.file: "/certs/ntjp.p12"
  ntjp.ws.certificate.type: "PKCS12"
  ntjp.ws.truststore.file: "/certs/truststore.jks"
  ntjp.ws.truststore.type: "JKS"
  integration.intygproxyservice.baseurl: "https://intygproxyservice.example.se"
  certificateservice.base.url: "https://certificateservice.example.se"
  activemq.destination.queue.name: "certificate.queue"
  activemq.internal.notification.queue.name: "internal.notification.queue"
  certificate.event.queue.name: "intygstjanst.certificate.event.queue"
  hsa.intygproxyservice.getemployee.cache.expiry: "300"
  hsa.intygproxyservice.gethealthcareunit.cache.expiry: "300"
  recipient.file: "/config/recipients.json"
  erase.certificates.page.size: "1000"
```

### After

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: intygstjanst-config
data:
  app.ntjp.base-url: "https://ntjp.example.se"
  app.ntjp.tls.certificate-file: "/certs/ntjp.p12"
  app.ntjp.tls.certificate-type: "PKCS12"
  app.ntjp.tls.truststore-file: "/certs/truststore.jks"
  app.ntjp.tls.truststore-type: "JKS"
  app.integration.intyg-proxy-service.base-url: "https://intygproxyservice.example.se"
  app.integration.certificate-service.base-url: "https://certificateservice.example.se"
  app.jms.statistics-queue: "certificate.queue"
  app.jms.internal-notification-queue: "internal.notification.queue"
  app.jms.certificate-event-queue: "intygstjanst.certificate.event.queue"
  app.integration.intyg-proxy-service.cache.employee-ttl-seconds: "300"
  app.integration.intyg-proxy-service.cache.healthcare-unit-ttl-seconds: "300"
  app.recipients.file: "/config/recipients.json"
  app.erase.page-size: "1000"
  # Bridge properties — keep until datasource URL is refactored:
  db.server: "mysql.example.se"
  db.name: "intyg"
  activemq.broker.url: "tcp://activemq.example.se:61616"
  redis.host: "redis.example.se"
  redis.port: "6379"
```

---

## 3. Sealed Secrets — Before / After Example

> Values are redacted. Replace `<sealed>` with the actual re-sealed ciphertext.

### Before

```yaml
apiVersion: bitnami.com/v1alpha1
kind: SealedSecret
metadata:
  name: intygstjanst-secrets
spec:
  encryptedData:
    ntjp.ws.certificate.password: <sealed>
    ntjp.ws.key.manager.password: <sealed>
    ntjp.ws.truststore.password: <sealed>
    hash.salt: <sealed>
    db.username: <sealed>
    db.password: <sealed>
    activemq.broker.username: <sealed>
    activemq.broker.password: <sealed>
    redis.password: <sealed>
```

### After

```yaml
apiVersion: bitnami.com/v1alpha1
kind: SealedSecret
metadata:
  name: intygstjanst-secrets
spec:
  encryptedData:
    app.ntjp.tls.certificate-password: <sealed>
    app.ntjp.tls.key-manager-password: <sealed>
    app.ntjp.tls.truststore-password: <sealed>
    app.security.hash-salt: <sealed>
    # Bridge secrets — keep until datasource bridge is removed:
    db.username: <sealed>
    db.password: <sealed>
    activemq.broker.username: <sealed>
    activemq.broker.password: <sealed>
    redis.password: <sealed>
```

> **Alternative for secrets:** Instead of injecting as Spring property keys, secrets may be injected
> as environment variables. The application reads `${HASH_SALT:}` for the hash salt. If you prefer
> environment variables for all secrets, map them as follows:

| Secret Key | Env Var |
|---|---|
| `app.security.hash-salt` | `HASH_SALT` |
| `app.ntjp.tls.certificate-password` | `NTJP_CERTIFICATE_PASSWORD` (requires updating `application.yml` placeholder) |
| `app.ntjp.tls.key-manager-password` | `NTJP_KEY_MANAGER_PASSWORD` (requires updating `application.yml` placeholder) |
| `app.ntjp.tls.truststore-password` | `NTJP_TRUSTSTORE_PASSWORD` (requires updating `application.yml` placeholder) |

---

## 4. Environment Variable Reference

Properties that use env var placeholders in `application.yml`:

| Env Var | Default | Required | Description |
|---|---|---|---|
| `HASH_SALT` | `""` | **Yes (production)** | Salt used for certificate ID hashing — must be non-empty in production |

All other properties are injected as Spring property keys via ConfigMap/Secret (see sections 1–3).

---

## 5. Properties with Defaults (no K8s injection required)

These properties have sensible defaults in `application.yml` and **do not need** to be present in
ConfigMaps unless the default needs to be overridden:

| Property | Default Value |
|---|---|
| `app.server.internal-port` | `8081` |
| `app.ntjp.tls.certificate-type` | `PKCS12` |
| `app.ntjp.tls.truststore-type` | `JKS` |
| `app.ntjp.ts-bas-register-certificate-version` | `v3` |
| `app.jms.statistics-queue` | `certificate.queue` |
| `app.jms.internal-notification-queue` | `internal.notification.queue` |
| `app.jms.certificate-event-queue` | `intygstjanst.certificate.event.queue` |
| `app.jms.statistics-enabled` | `true` |
| `app.integration.intyg-proxy-service.hsa.employee-endpoint` | `/api/v2/employee` |
| `app.integration.intyg-proxy-service.hsa.credential-information-endpoint` | `/api/v1/credentialinformation` |
| `app.integration.intyg-proxy-service.hsa.healthcare-unit-endpoint` | `/api/v2/healthcareunit` |
| `app.integration.intyg-proxy-service.hsa.healthcare-unit-members-endpoint` | `/api/v2/healthcareunitmembers` |
| `app.integration.intyg-proxy-service.hsa.unit-endpoint` | `/api/v1/unit` |
| `app.integration.intyg-proxy-service.hsa.credentials-for-person-endpoint` | `/api/v1/credentialsForPerson` |
| `app.integration.intyg-proxy-service.hsa.certification-person-endpoint` | `/api/v1/certificationPerson` |
| `app.integration.intyg-proxy-service.hsa.last-update-endpoint` | `/api/v1/lastUpdate` |
| `app.integration.intyg-proxy-service.hsa.healthcare-provider-endpoint` | `/api/v1/healthcareprovider` |
| `app.integration.intyg-proxy-service.pu.person-endpoint` | `/api/v1/person` |
| `app.integration.intyg-proxy-service.pu.persons-endpoint` | `/api/v1/persons` |
| `app.integration.intyg-proxy-service.cache.employee-ttl-seconds` | `60` |
| `app.integration.intyg-proxy-service.cache.healthcare-unit-ttl-seconds` | `60` |
| `app.integration.intyg-proxy-service.cache.healthcare-unit-members-ttl-seconds` | `60` |
| `app.integration.intyg-proxy-service.cache.unit-ttl-seconds` | `60` |
| `app.integration.intyg-proxy-service.cache.healthcare-provider-ttl-seconds` | `60` |
| `app.diagnosis.chapters-file` | `classpath:/diagnoskoder/diagnoskapitel.txt` |
| `app.diagnosis.icd10se-file` | `classpath:/diagnoskoder/icd10se/icd-10-se.tsv` |
| `app.diagnosis.ksh97p-file` | `classpath:/diagnoskoder/KSH97P_KOD.ANS` |
| `app.texts.file-directory` | `classpath:/texts/` |
| `app.texts.update-cron` | `0 0 0 * * *` |
| `app.recipients.update-cron` | `0 0 0 * * *` |
| `app.erase.page-size` | `1000` |
| `db.port` | `3306` |
| `redis.port` | `6379` |

---

## 6. Migration Checklist

Follow this order to update K8s configuration safely:

- [ ] **1. Update ConfigMap** — rename all old property keys to new `app.*` names (see section 1.1). Keep bridge properties (`db.*`, `activemq.broker.*`, `redis.*`) until further notice.
- [ ] **2. Reseal secrets** — re-encrypt all secrets using the new `app.*` key names (see section 3). The old sealed secrets for `hash.salt`, `ntjp.ws.certificate.password`, `ntjp.ws.key.manager.password`, `ntjp.ws.truststore.password` must be replaced. Bridge secrets (`db.username`, `db.password`, `activemq.broker.*`, `redis.password`) remain unchanged for now.
- [ ] **3. Deploy new application version** — deploy the refactored application version alongside the updated ConfigMap and sealed secrets.
- [ ] **4. Verify health probes** — confirm `/actuator/health/liveness` and `/actuator/health/readiness` return `UP` on the new pod.
- [ ] **5. Verify key behaviours** — confirm certificate registration, statistics queue, recipient loading, and Redis caching work as expected.
- [ ] **6. Remove old ConfigMap keys** — after verifying the new version is stable, clean up any legacy flat keys if they were kept as a safety net.

---

## 7. Rollback Procedure

If the new application version fails to start or behaves incorrectly after the ConfigMap/secret update:

1. **Roll back the deployment** to the previous application version (which reads the old flat property names).
2. **Restore the previous ConfigMap** and sealed secrets (which use the old flat property names). Since the old application version does not understand `app.*` keys, you must restore the entire ConfigMap, not just rename keys.
3. **Do not mix** old and new application versions with new ConfigMap keys — the old version has no aliases and cannot read `app.*` properties.

---

## 8. Future Cleanup (out of scope for this step)

Once deployed and stable, consider:
- **Remove the datasource bridge**: Replace `${db.server}/${db.port}/${db.name}` in `spring.datasource.url` with environment variable placeholders (e.g. `${DB_HOST}`, `${DB_PORT}`, `${DB_NAME}`), then remove `db.*` from ConfigMap and `db.username`/`db.password` from secrets.
- **Remove the ActiveMQ bridge**: Replace `${activemq.broker.*}` with `${ACTIVEMQ_BROKER_URL}`, `${ACTIVEMQ_USERNAME}`, `${ACTIVEMQ_PASSWORD}` env vars.
- **Remove the Redis bridge**: Replace `${redis.host}/${redis.port}/${redis.password}` with `${REDIS_HOST}`, `${REDIS_PORT}`, `${REDIS_PASSWORD}` env vars.
