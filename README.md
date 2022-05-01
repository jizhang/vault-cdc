# Morph

[![Build Status](https://travis-ci.org/ezalori/Morph.svg?branch=master)](https://travis-ci.org/ezalori/Morph)

```bash
# install dependencies
mvn install

# checkstyle & spotbugs
mvn verify

# build morph-web WAR file
mvn clean package -am -pl web
```

Import `morph-web` into IDE, run `org.ezalori.morph.WebApplication`, and it will start an API server listening on port `8081`.
