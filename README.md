Zanata
=====

Zanata is a web-based system for translators to translate
documentation and software online using a web-browser. It is
written in Java and uses modern web technologies like JBoss,
Seam, GWT, Hibernate, and a REST API. It currently supports
translation of DocBook/Publican documentation through PO
files. Projects can be uploaded to and downloaded from a Zanata
server using a Maven plugin or a Python client.

For *developers and writers*: By using Zanata for
your document translations, you can open up your project for
translations without opening your entire project in version
control.

For *translators*: No need to deal with PO files,
gettext or a version control system - just log in to the website, join
a language team and start translating, with translation memory (history
of similar translations) and the ability to see updates from other
translators in seconds.


Zanata is Free software, licensed under the [LGPL][].

[LGPL]: http://www.gnu.org/licenses/lgpl-2.1.html

Developers
----------

### Prerequisites

You will need:
- Java SDK 8 (OpenJDK recommended)
- npm
- Mysql or MariaDB
- JBoss EAP 6 or Wildfly

### Building

#### Quickly build a .war file

[`etc/scripts/quickbuild.sh`](etc/scripts/quickbuild.sh) - Builds the project
as quickly as possible, targeting both Firefox and Chrome when building GWT
components, and skipping all checks and verifications (i.e. tests, checkstyle, etc)

If you wish to build GWT components for chrome or firefox only, you can specify the
`-c` and `-f` arguments respectively.

The `-h` argument prints the script's help.

#### Build and run a server for testing

[`etc/scripts/cargowait.sh`](etc/scripts/cargowait.sh) - Builds the Zanata artifact
and starts a JBoss server using the cargo plugin. This script is particularly
useful for starting a Zanata instance with the aim of running functional tests
from an IDE.

The `-h` argument prints the script's help.
