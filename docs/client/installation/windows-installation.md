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

1. Download the latest zanata-cli `dist.zip` from [Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3Aorg.zanata%20a%3Azanata-cli)
2. Extract the downloaded zip file into a folder.
3. Search for `cmd` in Windows Start button and open up `Command prompt window`
4. Navigate to the extracted folder by using `cd` command. e.g. `cd C:\temp\zanata-cli-x.y.z`
5. Run the command `bin\zanata-cli` to start zanata-cli.
6. You may also which to add the bin directory to your system PATH, to ensure zanata-cli is always available.
