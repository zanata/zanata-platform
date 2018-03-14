## RHEL 7
There are two ways to install zanata-cli, via `0install` or `yum`.

### With 0install
**Note: If you have previously installed `zanata-cli` with Ivy or yum, you need to uninstall it first**

 0. If you have not install 0install, or not sure, run following commands to install 0install
and other dependencies:

      sudo rpm -ivh https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
      sudo yum -y install 0install java-1.8.0-openjdk unzip

 1. To install zanata-cli, run:

      mkdir -p ~/bin
      0install destroy zanata-cli
      0install -c add zanata-cli https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml
      0install -c update zanata-cli

 2. It should be done now. Run `zanata-cli --help` for the usage of the client.

 3. To update zanata-cli, run:

      0install -c update zanata-cli

---
### With yum
**Note: If you have previously installed `zanata-cli` with Ivy or 0install, you need to uninstall it first**

This method uses a 3rd-party yum repository, `Zanata_Team el-zanata` (a.k.a. dchen's epel-zanata).

 0. To install `Zanata_Team el-zanata`, run:

       sudo curl -L -o /etc/yum.repos.d/el-zanata.repo https://repos.fedorapeople.org/Zanata_Team/zanata/el-zanata.repo

 1. To install zanata-cli, run:

       sudo yum -y install zanata-cli-bin

 2. It should be done now. Run `zanata-cli --help` for the usage of the client.

 3. To update zanata-cli, run:

       sudo yum -y update zanata-cli-bin

## RHEL 6
There are two ways to install zanata-client, via `0install` or `yum`.

### With 0install

**Note: If you have previously installed `zanata-cli` with Ivy or yum, you need to uninstall it first**


 0. If you have not install 0install, or not sure, run following commands to install 0install
and other dependencies:
   a. Install other dependencies:

        sudo yum -y install libcurl java-1.8.0-openjdk unzip

   b. Download and extract 0install for 64-bit machine (such as x86_64)

        cd /tmp; wget https://downloads.sourceforge.net/project/zero-install/0install/2.10/0install-linux-x86_64-2.10.tar.bz2
        tar xjvf 0install-linux-x86_64-2.10.tar.bz2
        cd 0install-linux-x86_64-2.10

   c. Download and extract 0install for 32-bit machine (such as i486)

        cd /tmp; wget https://downloads.sourceforge.net/project/zero-install/0install/2.10/0install-linux-i486-2.10.tar.bz2
        tar xjvf 0install-linux-i486-2.10.tar.bz2
        cd 0install-linux-i486-2.10

   d. Install `0install`

         ./install.sh


 1. To install zanata-cli, run:

        mkdir -p ~/bin
        0install destroy zanata-cli
        0install -c add zanata-cli https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml
        0install -c update zanata-cli

 2. It should be done now. Run `zanata-cli --help` for the usage of the client.

 3. To update zanata-cli, run:

        0install -c update zanata-cli

---
### With yum
**Note: If you have previously installed `zanata-cli` with Ivy or 0install, you need to uninstall it first**

 0. To install `Zanata_Team el-zanata`, run:

       sudo curl -L -o /etc/yum.repos.d/el-zanata.repo https://repos.fedorapeople.org/Zanata_Team/zanata/el-zanata.repo

 1. To install zanata-cli, run:

       sudo yum -y install zanata-cli-bin

 2. It should be done now. Run `zanata-cli --help` for the usage of the client.

 3. To update zanata-cli, run:

       sudo yum -y update zanata-cli-bin


## Fedora
There are two ways to install zanata-client, via `0install` or `dnf`.

### With 0install
**Note: If you have previously installed `zanata-cli` with Ivy or yum, you need to uninstall it first**

 0. If you have not install 0install, or not sure, run following commands to install 0install
and other dependencies:

      sudo yum -y install 0install java-1.8.0-openjdk unzip

 1. To install zanata-cli, run:

      mkdir -p ~/bin
      0install destroy zanata-cli
      0install -c add zanata-cli https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml
      0install -c update zanata-cli

 2. It should be done now. Run `zanata-cli --help` for the usage of the client.

 3. To update zanata-cli, run:

      0install -c update zanata-cli

---
### With dnf

`zanata-client`, the package that contains `zanata-cli`, is already in official Fedora repository.

**Note: If you have previously installed `zanata-cli` with Ivy or 0install, you need to uninstall it first**

 1. To install `zanata-cli`, run:

        sudo dnf -y install zanata-client

 2. It should be done now. Run `zanata-cli --help` for the usage of the client.

 3. To update zanata-cli, run:

        sudo dnf -y update zanata-client


## Debian Based Distributions

 0. To install 0install and other dependencies, run:

      sudo apt-get install zeroinstall-injector openjdk-8-jre

 1. To install zanata-cli, run:

      mkdir -p ~/bin
      0install destroy zanata-cli
      0install -c add zanata-cli https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml
      0install -c update zanata-cli

 2. It should be done now. Run `zanata-cli --help` for the usage of the client.

 3. To update zanata-cli, run:

      0install -c update zanata-cli


## Others
*Note: If you have installed `zanata-cli` previously through another method, you need to uninstall that for this to work.*

 0. Follow 0Install in [0Install for Linux](http://0install.net/install-linux.html).
 1. Install Java JRE (1.8 onwards) from [OpenJDK installation](http://openjdk.java.net/install/index.html)
 2. To install zanata-cli, run:

      mkdir -p ~/bin
      0install destroy zanata-cli
      0install -c add zanata-cli https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml
      0install -c update zanata-cli

 3. It should be done now. Run `zanata-cli --help` for the usage of the client.

 4. To update zanata-cli, run:

      0install -c update zanata-cli

## 0install Useful commands

 * Run Zanata-CLI without alias

        0launch https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml {command}

 * Update Zanata-CLI manually

        0install update zanata-cli

 * Set version of zanata-cli to 4.4.0

        0install -c update zanata-cli --version=4.4.0

## Troubleshooting: Fedora dnf

#### The latest package is not in repository
It may still in updates-testing repository. Run:

       sudo dnf -y --enablerepo=updates-testing update zanata-client

## Troubleshooting: RHEL yum

#### The latest package is not in repository
You local yum cache might not be updated. To refresh local yum cache,
run following command before you try again

        sudo yum makecache

##  Troubleshooting: 0install
#### 0launch URL works, but `zanata-cli` does not
Your `~/bin` might not be in path. Run:

     grep $HOME/bin<<<$PATH  || echo "$HOME/bin is not in PATH"

If `~/bin` is not in the path, then add following in both `~/.bashrc` and `~/.bash_profile`

     PATH+=":$HOME/bin"


#### Downloaded package does not work
Your feed may be broken, to fix it, run:

      mkdir -p ~/bin
      0install destroy zanata-cli
      0install -c add zanata-cli https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml
      0install -c update zanata-cli

