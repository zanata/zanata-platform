# Installation

The Zanata client (zanata-cli) can be installed on most systems. Installation is easiest under Fedora, other systems can use Apache Ivy for installation. Manual installation is also possible.


## Installation on Fedora

If you are using Fedora, zanata-cli is available through the `yum` package manager.

```bash
sudo yum install zanata-client
```

## Installation with Ivy

The Ivy distribution of the client is a small script that will download the client the first time it is run. This distribution requires Apache Ivy to run.

Instructions for installing Apache Ivy and the Ivy version of zanata-cli can be found at [Zanata Ivy Client on github](https://github.com/zanata/zanata-client-ivy).

*Note:* The Ivy client will download required files the first time it is run, which may take several minutes. It will show `resolving dependencies...` while this is happening.

## Manual Installation

To manually install zanata-cli:

 1. Navigate to [zanata-cli on Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.zanata%22%20AND%20a%3A%22zanata-cli%22).
 1. Download either `dist.zip` or `dist.tar.gz`.

 1. Extract the contents of the archive to your location of choice.
 1. Create a symbolic link to the `zanata-cli` script in the bin directory of the extracted archive. e.g. from the archive directory, run `sudo ln -s bin/zanata-cli  /usr/local/bin/zanata-cli`.

 1. (optional) you can also enable tab-autocomplete for the client if you use bash as your terminal shell. This can be done by copying or linking the `zanata-cli-completion` script from the bin directory to `/etc/bash_completion.d/`. e.g. `ln -s bin/zanata-cli-completion /etc/bash_completion.d/zanata-cli-completion`.

## Nightly Builds

If you like to live dangerously, the client nightly relase is available. This may have newer features, but is not guaranteed to be stable.

The latest nightly build is available as an archive that can be installed manually. To install the latest nightly build:

 1. Open [Client nightly builds](http://repository-zanata.forge.cloudbees.com/snapshot/org/zanata/zanata-cli/).
 1. Open the directory showing the highest version number.
 1. Download either of the distributable archives (ending with `-dist.zip` or `-dist.tar.gz`).
 1. Install as per manual installation instructions above.