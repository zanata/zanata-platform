# Zanata

Zanata is a web-based system for translators to translate
documentation and software online using a web-browser. It is
written in Java and uses modern web technologies like JBoss EAP,
CDI, GWT, Hibernate, and a REST API. It currently supports
translation of DocBook/Publican documentation through PO
files, and a number of other formats. Projects can be uploaded
to and downloaded from a Zanata server using a Maven plugin or
a command line client.

For *developers and writers*: By using Zanata for
your document translations, you can open up your project for
translations without opening your entire project in version
control.

For *translators*: No need to deal with PO files,
gettext or a version control system - just log in to the website, join
a language team and start translating, with translation memory (history
of similar translations) and the ability to see updates from other
translators in seconds.

Find out about Zanata here: http://zanata.org/


Zanata is Free software, licensed under the [LGPL][].

[LGPL]: http://www.gnu.org/licenses/lgpl-2.1.html


## Developers: Building Zanata from source

### Prerequisites

You will need:
- Java SDK 8 (OpenJDK recommended)
- zsh (for the build script)
- npm (optional)
- MySQL or MariaDB (optional)
- JBoss EAP 7 or WildFly 10 (optional)
- Linux or Mac OSX. Windows works too, sometimes.

A full build needs to download and install node, npm, mysql and WildFly/EAP,
some of which are platform-dependent.

### Building

The build script you need to know about is [./build](`./build`). It covers
all your Zanata-building needs.
Disclaimer: may not cover all your Zanata-building needs.

The `-h` argument prints the build script's help.

#### Building on Mac OS X and macOS Sierra

Currently, there is an extra step needed to build Zanata on a Mac.
For `./build` to work, you will need to point to the correct Java directory using
the following command (using the correct JDK version on your Mac):

`export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.X_XX.jdk/Contents/Home"`


This JAVA_HOME workaround will not be needed in the next release of the
Maven wrapper: io.takari:maven-wrapper:0.1.7.

See [https://github.com/takari/maven-wrapper/pull/14](https://github.com/takari/maven-wrapper/pull/14) for details.

#### Build everything with unit tests

`./build --all` - Builds the entire project (client and server ) fairly
quickly, skipping integration tests and static analysis (checkstyle, etc),
but including unit tests.

NB: If you need to run functional tests later without rebuilding, you should
add `-i` to install the war file to your Maven repo after packaging.

#### Quickly build a .war file for later tests or docker deployment

`./build --server -iQ` - Builds and installs the project as quickly as possible,
skipping all checks and verifications (i.e. tests, checkstyle, etc).

The binaries will be installed to your Maven repo for usage in later
(partial) builds and tests.

#### Quickly build and run a server for testing

`./build -w` - Builds zanata-war and starts a JBoss/WildFly server using the
cargo plugin. This is intended for starting a Zanata instance with the aim of
running functional tests from an IDE.

#### Development using docker (experimental)

For a quick Zanata development environment with Docker, please visit the [docker README](docker/README.md).

### Source code note
Please note that any references to pull request numbers in commit
messages (eg merge nodes) prior to 20 October 2016 are referring to the
old repositories (before they were merged into the zanata-platform
repository):

* https://github.com/zanata/zanata-api/pulls/
* https://github.com/zanata/zanata-client/pulls/
* https://github.com/zanata/zanata-common/pulls/
* https://github.com/zanata/zanata-parent/pulls/
* https://github.com/zanata/zanata-server/pulls/

GitHub tries to auto-link numbers to pull requests, but such links will
generally be incorrect for old commit messages.
