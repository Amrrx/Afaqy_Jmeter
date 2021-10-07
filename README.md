<h1 align="center"><img src="https://jmeter.apache.org/images/logo.svg" alt="Apache JMeter logo" /></h1>
<h2 align="center"><img style="height:40px; opacity: 0.9;" src="https://afaqy.com/wp-content/uploads/2018/05/en.svg" alt="Afaqy logo" /></h2>

A modified clone from Apache Jmeter, modified to support the testing of vehicle tracking devices tcp protocols.

By Afaqy Software quality team.

[![Build Status](https://api.travis-ci.org/apache/jmeter.svg?branch=master)](https://travis-ci.org/apache/jmeter/)
[![codecov](https://codecov.io/gh/apache/jmeter/branch/master/graph/badge.svg)](https://codecov.io/gh/apache/jmeter)
[![License](https://img.shields.io/:license-apache-brightgreen.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Stack Overflow](https://img.shields.io/:stack%20overflow-jmeter-brightgreen.svg)](https://stackoverflow.com/questions/tagged/jmeter)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.jmeter/ApacheJMeter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.apache.jmeter/ApacheJMeter)
[![Javadocs](https://www.javadoc.io/badge/org.apache.jmeter/ApacheJMeter_core.svg)](https://www.javadoc.io/doc/org.apache.jmeter/ApacheJMeter_core)
[![Twitter](https://img.shields.io/twitter/url/https/github.com/apache/jmeter.svg?style=social)](https://twitter.com/intent/tweet?text=Powerful%20load%20testing%20with%20Apache%20JMeter:&url=https://jmeter.apache.org)

### Version
Afaqy_MOD: 1.1.0

Jmeter: 5.4.1

# Protocols
Supported device protocols:

- BCE Standard (Not SDK/IOT)


## Build instructions

### Release builds

JMeter is built using Gradle.

The following command builds and tests JMeter:

```sh
./gradlew build
```

If the system does not have a GUI display then:

```sh
./gradlew build -Djava.awt.headless=true
```

The output artifacts (jars, reports) are placed in the `build` folder.
For instance, binary artifacts can be found under `src/dist/build/distributions`.

The following command would compile the application and enable you to run `jmeter`
from the `bin` directory.

> **Note** that it completely refreshes `lib/` contents,
so it would remove custom plugins should you have them installed.

```sh
./gradlew createDist
```

Alternatively, you could get Gradle to start the GUI:

```sh
./gradlew runGui
```

## Licensing and Legal Information

For legal and licensing information, please see the following files:

- [LICENSE](LICENSE)
- [NOTICE](NOTICE)


## Thanks

**Thank you for using Apache JMeter - Afaqy_MOD.**

### Copyright
**Amr Aly @ 2021**
