# Vault CDC

![Build](https://github.com/jizhang/vault-cdc/actions/workflows/build.yml/badge.svg)

```bash
# Checkstyle & spotbugs
mvn verify

# Build vault-cdc JAR file
mvn -am -pl cdc verify
```

## Setup CheckStyle in IDEA

* Install checkstyle plugin version >= 5.66.0
* Go to Settings / Tools / Checkstyle
    * Config the plugin to use checkstyle version 10.2
    * Activate the Google Checks configuration.
* Go to Settings / Editor / Code Style, create a new code style scheme, and import `google_checks_10.2.xml`
