# Morph

[![Build Status](https://travis-ci.org/ezalori/Morph.svg?branch=master)](https://travis-ci.org/ezalori/Morph)

## Setup

```bash
# install dependencies
mvn install

# checkstyle & spotbugs
mvn verify

# build morph-web WAR file
mvn clean package -am -pl web
```

Import `morph-web` into IDE, run `org.ezalori.morph.web.WebApplication`, and it will start an API server listening on port `8081`.

### H2 Database

H2 database file is located in `/morph.mv.db`, and you can access the h2 web console at `http://127.0.0.1:8081/h2-console`.
