## RHEL 7

1. Install EPEL repository for RHEL 7
```
sudo rpm -ivh https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
```

2. Install 0install 
```
sudo yum -y install 0install
```

3. Setup alias: `0install -c add zanata-cli http://zanata.org/files/0install/zanata-cli.xml`.
4. Now you can run `zanata-cli --help` for more options.

## RHEL 6

1. Download binary package from [0Install v2.10 x86_64](https://downloads.sourceforge.net/project/zero-install/0install/2.10/0install-linux-x86_64-2.10.tar.bz2) or [0Install v2.10 i486](https://downloads.sourceforge.net/project/zero-install/0install/2.10/0install-linux-i486-2.10.tar.bz2)
2. Unpack it, and run the `install.sh` script inside. You'll need libcurl installed (most systems have it by default).
3. Setup alias: `0install -c add zanata-cli http://zanata.org/files/0install/zanata-cli.xml`.
4. Now you can run `zanata-cli --help` for more options.

## Fedora

The recommend way to install is though 0install:

1. Install 0install
```
sudo yum -y install 0install
```

2. Setup alias: `0install -c add zanata-cli http://zanata.org/files/0install/zanata-cli.xml`.
3. Now you can run `zanata-cli --help` for more options.

You can also install using yum/dnf:

```
sudo yum -y install zanata-client
```

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
