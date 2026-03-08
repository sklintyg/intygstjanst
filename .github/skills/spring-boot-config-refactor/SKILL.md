---
name: spring-boot-config-refactor
description: Refactor a Spring Boot application's configuration from legacy application.properties and scattered @Value usage to structured YAML and immutable @ConfigurationProperties records. Use this when migrating messy configuration, standardizing property naming, introducing app-prefixed custom properties, or separating sensible defaults from local development overrides.
license: MIT
---

# Spring Boot configuration refactor skill

Use this skill when the task is to **analyze, restructure, and migrate application configuration** in a Spring Boot codebase.

This skill is specifically optimized for repositories where:
- `application.properties` has grown organically over time and is hard to understand.
- Local development uses a `dev` profile and an additional config location such as `devops/dev/config/`.
- Test and production deployments are configured externally through Kubernetes `ConfigMap`/`Secret`/sealed secrets rather than profile-specific packaged files.
- Many settings are injected via `@Value` annotations.
- The goal is to move to **type-safe**, **immutable**, **record-based** configuration using `@ConfigurationProperties`.

## Desired target state

Follow these rules unless the user explicitly instructs otherwise:

1. Use YAML instead of `.properties` for Spring configuration.
2. Put **application-specific properties** under the `app` prefix.
3. Keep **standard Spring Boot / framework / library properties** under their normal prefixes (for example `spring.*`, `management.*`, `server.*`, `logging.*`).
4. Keep the same conceptual split between:
   - `application.yml` for minimal defaults and shared base configuration.
   - `application-dev.yml` for local development overrides.
   - `application-test.yml` for Spring Boot test scenarios when relevant.
5. Migrate from scattered `@Value` usage to `@ConfigurationProperties` with immutable Java records.
6. Inject configuration records as Spring beans via constructor injection.
7. Add validation so invalid configuration fails fast during startup.
8. Preserve the existing deployment model where production/test environment overrides come from external configuration, Kubernetes config maps, and secrets.
9. Do **not** introduce environment-specific packaged config for production unless the user explicitly asks for it.

## Key implementation principles

### Configuration layout
- `src/main/resources/application.yml`
  - Keep it minimal.
  - Include sensible defaults only.
  - Do not hardcode environment-specific secrets or infrastructure endpoints.
- `src/main/resources/application-dev.yml`
  - Use for local dev defaults and local overrides **only if that matches the repo’s current practice**.
  - If the project already loads external files from `devops/dev/config/` using `spring.config.additional-location`, preserve that approach unless the user asks to change it.
- `src/test/resources/application-test.yml`
  - Use when test-specific configuration is needed.

### Property modeling
- Group custom application properties into one or more records under a dedicated package such as:
  - `...config`
  - `...config.properties`
  - or the package already used by the project for application config
- Prefer a small number of well-structured top-level property records over many tiny fragmented ones.
- Use nested records to mirror the YAML hierarchy.
- Keep names domain-oriented and self-explanatory.

Example pattern:

```java
package com.example.app.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
    @Valid Feature feature,
    @Valid Integration integration
) {
    public record Feature(
        boolean enabled,
        @Min(1) int batchSize
    ) {}

    public record Integration(
        @NotBlank String baseUrl,
        @NotNull Timeout timeout
    ) {
        public record Timeout(
            @Min(1) int connectSeconds,
            @Min(1) int readSeconds
        ) {}
    }
}
```

### Spring Boot wiring
- Prefer `@ConfigurationPropertiesScan` on the main application class if it is not already in use.
- Alternatively use `@EnableConfigurationProperties(...)` only when there is a strong repository-specific reason.
- Use constructor injection exclusively.
- Access values using record accessors, for example `appProperties.integration().baseUrl()`.

### Validation
- Use `@Validated` on the configuration record.
- Use Jakarta Bean Validation annotations such as:
  - `@NotNull`
  - `@NotBlank`
  - `@Min`
  - `@Max`
  - `@Positive`
  - `@Valid` for nested records
- Ensure configuration errors fail fast at application startup.

### Handling existing `@Value` usage
- Search the codebase for all `@Value` annotations.
- Classify each occurrence:
  1. custom application property -> migrate under `app.*`
  2. framework/library property -> usually leave as framework config and inject a richer Spring abstraction where possible
  3. literal default with occasional override need -> model as a typed property with a sensible default in YAML, or keep optional via constructor defaulting only if clearly justified
- Replace field injection and `@Value` injection with constructor-injected configuration beans.
- Remove dead properties after migration.

### Property naming rules
- Application-specific keys must move under `app`.
- Avoid flat names when the domain has hierarchy.
- Prefer this:
  - `app.integration.certificate-service.base-url`
  - `app.jobs.cleanup.batch-size`
- Avoid this:
  - `certificateServiceUrl`
  - `cleanupBatch`

### Secrets and external config
- Never hardcode secrets in repository defaults.
- Assume sensitive values should come from environment variables, sealed secrets, or external config in the devops repository.
- Preserve placeholder-based configuration where appropriate, for example `${DB_PASSWORD}`.
- Preserve compatibility with `spring.config.additional-location` if the project already relies on it.

## Required workflow

When using this skill, follow this workflow.

### Step 1: Audit the current state
Inspect at least:
- `application.properties`
- any existing `application-*.properties` or `.yml` files
- local dev config under `devops/dev/config/` if present
- the custom Gradle run task (for example `appRunDebug`)
- main application class
- all usages of `@Value`
- any existing `@ConfigurationProperties` classes
- test configuration files under `src/test/resources`

Produce a short migration assessment that identifies:
- what should remain standard framework configuration
- what should move under `app.*`
- which properties appear environment-specific
- which values look like secrets
- where defaults are currently duplicated or contradictory

### Step 2: Design the target structure
Create a proposed target model before editing:
- YAML hierarchy
- Java record structure
- file placement
- migration of local dev overrides
- how tests should load configuration

Prefer a design that is easy to navigate and reflects the functional domains in the application.

### Step 3: Perform the refactor
Make the refactor incrementally and safely:
- create the new YAML files
- create the configuration records
- enable scanning
- migrate injection sites from `@Value`
- remove obsolete properties
- update test configuration if needed
- keep runtime behavior equivalent unless the user asked for behavioral changes

### Step 4: Verify
After changes, verify as much as possible:
- the application still starts
- the local dev run path still works
- tests still load the expected configuration
- no unresolved placeholders remain
- the new record-based config is actually used by dependent beans

If you cannot run the application, still perform static verification by checking references and configuration consistency.

## Output expectations

When working on a refactor, structure the response like this:

1. **Assessment**
   - brief summary of current state
   - key problems found
2. **Target structure**
   - proposed YAML layout
   - proposed Java configuration records
3. **Changes made**
   - files created/updated
   - `@Value` usages migrated
4. **Notes / follow-up**
   - remaining risks
   - recommended cleanup not yet done

## Strong preferences
- Prefer **clarity over cleverness**.
- Prefer **few well-structured config records** over many fragmented ones.
- Prefer **minimal defaults** in `application.yml`.
- Prefer **externalized environment-specific config** for deployed environments.
- Prefer **keeping deployment semantics stable** over introducing a new profile strategy for production.

## Avoid
- Do not convert standard Spring Boot properties to `app.*`.
- Do not keep spreading configuration logic across `@Value` annotations.
- Do not place secrets in `application.yml` or `application-dev.yml` unless the user explicitly wants local-only placeholders and understands the tradeoff.
- Do not introduce mutable configuration POJOs when records are sufficient.
- Do not silently change how the application is started in local dev.
- Do not assume that profile-specific files should drive Kubernetes deployment.

## Repository-specific guidance for this style of project
This skill is especially suitable for projects that follow these conventions:
- Spring Boot application
- Gradle build
- local development starts with a custom task such as `appRunDebug`
- local overrides may come from `devops/dev/config/`
- deployed environments use external config and secrets rather than packaged profile files

If the repository matches this pattern, align the refactor to that model rather than forcing a generic Spring Boot setup.

## Example request prompts
Use prompts like:
- `Use /spring-boot-config-refactor to migrate this repo from @Value and application.properties to YAML + ConfigurationProperties records.`
- `Use /spring-boot-config-refactor to audit our current config structure and propose a target app.* hierarchy.`
- `Use /spring-boot-config-refactor to refactor the local dev config flow without changing our Kubernetes deployment strategy.`
