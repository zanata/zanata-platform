1. Install 0Install from [0Install for Linux](http://0install.net/install-linux.html)
2. Install Java JRE (1.7 onwards)
    - for OpenJDK Java, see [OpenJDK installation](http://openjdk.java.net/install/index.html)
    - for Oracle Java, see [Oracle Java installation](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

### Run Zanata-CLI

```
0launch http://zanata.org/files/0install/zanata-cli.xml {command}
```

### Setup alias

To setup `zanata-cli` as an alias in your machine run:
```
0install -c add zanata-cli http://zanata.org/files/0install/zanata-cli.xml
```
Now you can run `zanata-cli --help` for more options. If you have installed `zanata-cli` previously through another method, you need to uninstall that for this to work.

### Update Zanata-CLI

```
0install update http://zanata.org/files/0install/zanata-cli.xml
```
