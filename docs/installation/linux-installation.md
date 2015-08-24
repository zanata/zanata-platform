## RHEL 7

1. Download binary package from [0Install v2.9](https://downloads.sourceforge.net/project/zero-install/0install/2.9/0install-linux-x86_64-2.9.tar.bz2)
2. Unpack it, and run the `install.sh` script inside. You'll need libcurl installed (most systems have it by default).

## RHEL 6/Fedora

Install EPEL repository for RHEL 6
```
## RHEL/CentOS 6 32-Bit ##
wget http://download.fedoraproject.org/pub/epel/6/i386/epel-release-6-8.noarch.rpm
rpm -ivh epel-release-6-8.noarch.rpm

## RHEL/CentOS 6 64-Bit ##
wget http://download.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm
rpm -ivh epel-release-6-8.noarch.rpm
```

1. To install 0Install,
    - RHEL: `yum install zeroinstall-injector`
    - Fedora: `yum install 0install`
2. Install Java runtime: `yum install java-1.7.0-openjdk`.
3. Setup alias: `0install -c add zanata-cli http://zanata.org/files/0install/zanata-cli.xml`.
4. Now you can run `zanata-cli --help` for more options.

*Note: If you have installed `zanata-cli` previously through another method, you need to uninstall that for this to work.*


## Debian based distro

1. Install 0Install: `apt-get install zeroinstall-injector`.
2. Install Java runtime: `apt-get install openjdk-7-jre`.
3. Run `0install -c add zanata-cli http://zanata.org/files/0install/zanata-cli.xml` to setup alias.
4. Now you can run `zanata-cli --help` for more options.

*Note: If you have installed `zanata-cli` previously through another method, you need to uninstall that for this to work.*


## Others

1. Follow 0Install in [0Install for Linux](http://0install.net/install-linux.html).
2. Install Java JRE (1.7 onwards) from [OpenJDK installation](http://openjdk.java.net/install/index.html)
3. Run `0install -c add zanata-cli http://zanata.org/files/0install/zanata-cli.xml` to setup alias.
4. Now you can run `zanata-cli --help` for more options.

*Note: If you have installed `zanata-cli` previously through another method, you need to uninstall that for this to work.*


### Useful command

**Run Zanata-CLI without alias**
```
0launch http://zanata.org/files/0install/zanata-cli.xml {command}
```

**Update Zanata-CLI manually**
```
0install update http://zanata.org/files/0install/zanata-cli.xml
```
