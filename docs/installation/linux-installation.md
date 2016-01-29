## RHEL 7
There are two ways to install zanata-client, via `0install` or `yum`.

### With 0install

**Note: If you have previously installed `zanata-cli` through 0install, please run this command to update your `zanata-cli`**

    0install destroy zanata-cli | yes | 0install -c add zanata-cli https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml

**Note: If you have previously installed `zanata-cli` with Ivy or yum, you need to uninstall it first**

---

1. Install EPEL repository for RHEL 7

        sudo rpm -ivh https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm

2. Install `0install`

        sudo yum -y install 0install java-1.8.0-openjdk unzip

3. Use `zanata-cli` as alias: 

        mkdir -p ~/bin | yes | 0install -c add zanata-cli https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml

4. It should be done now. Run `zanata-cli --help` for the usage of the client.
 
### With yum

**Note: If you have previously installed `zanata-cli` with Ivy or 0install, you need to uninstall it first**

---

1. Install `epel-zanata` repo

        cd /etc/yum.repos.d ; sudo wget https://repos.fedorapeople.org/dchen/zanata/epel-zanata.repo

2. Install package `zanata-cli-bin`

        sudo yum -y install zanata-cli-bin java-1.8.0-openjdk

3. It should be done now. Run `zanata-cli --help` for the usage of the client.

## RHEL 6
There are two ways to install zanata-client, via `0install` or `yum`.

### With 0install

**Note: If you have previously installed `zanata-cli` through 0install, please run this command to update your `zanata-cli`**

    0install destroy zanata-cli | yes | 0install -c add zanata-cli https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml

**Note: If you have previously installed `zanata-cli` with Ivy or yum, you need to uninstall it first**

---

1. Download and extract `0install` binary package
   a. for 64-bit machine (such as x86_64)

        cd /tmp; wget https://downloads.sourceforge.net/project/zero-install/0install/2.10/0install-linux-x86_64-2.10.tar.bz2
        tar xjvf 0install-linux-x86_64-2.10.tar.bz2
        cd 0install-linux-x86_64-2.10
  
   b. for 32-bit machine (such as i486)
   
        cd /tmp; wget https://downloads.sourceforge.net/project/zero-install/0install/2.10/0install-linux-i486-2.10.tar.bz2
        tar xjvf 0install-linux-i486-2.10.tar.bz2
        cd 0install-linux-i486-2.10

2. Install `libcurl` if you have not

        sudo yum -y install libcurl java-1.8.0-openjdk unzip


3. Install `0install`

        ./install.sh 

4. Use `zanata-cli` as alias: 

        mkdir -p ~/bin | yes | 0install -c add zanata-cli https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml

5. It should be done now. Run `zanata-cli --help` for the usage of the client.

### With yum

**Note: If you have previously installed `zanata-cli` with Ivy or 0install, you need to uninstall it first**

---

1. Install `epel-zanata` repo

        cd /etc/yum.repos.d ; sudo wget https://repos.fedorapeople.org/dchen/zanata/epel-zanata.repo

2. Install package `zanata-cli-bin`

        sudo yum -y install zanata-cli-bin java-1.8.0-openjdk

3. It should be done now. Run `zanata-cli --help` for the usage of the client.

## Fedora
There are two ways to install zanata-client, via `0install` or `dnf`.

### With 0install

**Note: If you have previously installed `zanata-cli` through 0install, please run this command to update your `zanata-cli`**

    0install destroy zanata-cli | yes | 0install -c add zanata-cli https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml

**Note: If you have previously installed `zanata-cli` with Ivy or dnf, you need to uninstall it first**

---

1. Install 0install

        sudo yum -y install 0install java-1.8.0-openjdk unzip

2. Use `zanata-cli` as alias: 

        mkdir -p ~/bin | yes | 0install -c add zanata-cli https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml

3. It should be done now. Run `zanata-cli --help` for the usage of the client.

### With dnf

**Note: If you have previously installed `zanata-cli` with Ivy or 0install, you need to uninstall it first**

---

1. Install `zanata-client`

        sudo dnf -y install zanata-client

## Debian based distro

**Note: If you have previously installed `zanata-cli` through 0install, please run this command to update your `zanata-cli`**

    0install destroy zanata-cli | yes | 0install -c add zanata-cli https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml

**Note: If you have previously installed `zanata-cli` with Ivy or dnf, you need to uninstall it first**

---

1. Install 0install

        sudo apt-get install zeroinstall-injector

2. Install Java runtime: 

        sudo apt-get install openjdk-8-jre

3. Use `zanata-cli` as alias: 

        mkdir -p ~/bin | yes | 0install -c add zanata-cli https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml

4. It should be done now. Run `zanata-cli --help` for the usage of the client.

## Others
*Note: If you have installed `zanata-cli` previously through another method, you need to uninstall that for this to work.*

1. Follow 0Install in [0Install for Linux](http://0install.net/install-linux.html).
2. Install Java JRE (1.8 onwards) from [OpenJDK installation](http://openjdk.java.net/install/index.html)
3. Use `zanata-cli` as alias: 

        mkdir -p ~/bin | yes | 0install -c add zanata-cli https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml

4. It should be done now. Run `zanata-cli --help` for the usage of the client.

## 0install Useful commands

 * Run Zanata-CLI without alias

        0launch https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml {command}

 * Update Zanata-CLI manually
 
        0install update https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml

