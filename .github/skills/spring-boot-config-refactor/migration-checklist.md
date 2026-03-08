# Migration checklist

Use this checklist when applying the `spring-boot-config-refactor` skill.

## Inventory
- Find all `application*.properties` and `application*.yml` files.
- Find dev/local configuration under `devops/dev/config/`.
- Find all `@Value` annotations.
- Find existing `@ConfigurationProperties` classes.
- Inspect the application bootstrap class for `@ConfigurationPropertiesScan`.
- Inspect Gradle tasks that start the app locally, especially custom tasks such as `appRunDebug`.

## Classification
- Mark each property as one of:
  - framework/library property
  - custom application property
  - environment-specific override
  - secret/sensitive value
- Mark properties that are duplicated across files.
- Mark properties with contradictory defaults.

## Refactor
- Convert base config to `application.yml`.
- Convert local dev config to `application-dev.yml` when appropriate.
- Add `application-test.yml` for test-only needs.
- Move custom keys under `app.*`.
- Create immutable record-based `@ConfigurationProperties` classes.
- Add validation annotations.
- Replace `@Value` injection sites.
- Remove stale keys and dead code.

## Verify
- Confirm Spring can bind the new properties structure.
- Confirm nested validation is enabled.
- Confirm local startup path still resolves additional config correctly.
- Confirm test configuration still works.
- Confirm no secrets were committed into repo defaults.
