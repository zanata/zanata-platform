** Prerequisite **
- Install Java runtime 8 (JRE 8) from http://www.oracle.com/technetwork/java/javase/downloads/index.html

### With 0install (recommended way)
1. Click [here](http://0install.de/files/zero-install.exe) to download and install [0Install for Windows](http://0install.net/install-windows.html).
2. Run `0install https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli-jre.xml` to setup alias.
3. Now you can run `zanata-cli.bat --help` for more options.

*Note: If you have installed `zanata-cli` previously through another method, you need to uninstall that for this to work.*

#### Useful command

**Run Zanata-cli without alias**
```
0launch https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli-jre.xml {command}
```

**Update Zanata-cli manually**
```
0install update https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli-jre.xml
```

**Zanata-cli feed without JRE dependency**

`https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml`

### Manual standalone installation

1. Download zanata-cli dist.zip from [maven central](http://search.maven.org/remotecontent?filepath=org/zanata/zanata-cli/3.8.1/zanata-cli-3.8.1-dist.zip)
2. Extract the downloaded file into a folder.
3. Search for `cmd` in Windows Start button and open up `Command prompt window`
4. Navigate to the extracted folder by using `cd` command. e.g. `cd C:\temp`
4. Run the following command `java -cp lib/* org.zanata.client.ZanataClient` to start zanata-cli.
