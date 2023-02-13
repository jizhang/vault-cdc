# Morph

![Build](https://github.com/ezalori/Morph/actions/workflows/build.yml/badge.svg)

```bash
# install dependencies
mvn install

# checkstyle & spotbugs
mvn verify

# build morph-web WAR file
mvn clean package -am -pl web
```

Import `morph-web` into IDE, run `org.ezalori.morph.WebApplication`, and it will start an API server listening on port `8081`.

## Setup CheckStyle in IDEA

* Install checkstyle plugin version >= 5.66.0
* Go to Settings / Tools / Checkstyle
    * Config the plugin to use checkstyle version 10.2
    * Activate the Google Checks configuration.
* Go to Settings / Editor / Code Style, create a new code style scheme, and import `google_checks_10.2.xml`
