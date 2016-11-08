## Developers

### Prerequisites

You will need:
- Java SDK 8 (OpenJDK recommended)
- npm
- Mysql or MariaDB
- JBoss EAP 7 or WildFly 10

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

#### Development using docker (experimental)

For a quick Zanata development environment with Docker, please visit the [docker README](docker/README.md).
