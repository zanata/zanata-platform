1. Install 0Install from [0Install for Windows](http://0install.net/install-windows.html).

### Run Zanata-CLI

```
0launch http://zanata.org/files/0install/zanata-cli-jre.xml {command}
```

If you already have Java 1.7 onwards installed,
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

### Install Java manually

Download and install Java (JRE) package from [Oracle Java SE](http://www.oracle.com/technetwork/java/javase/downloads/index.html).