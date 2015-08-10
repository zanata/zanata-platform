1. Click [here](http://downloads.sourceforge.net/project/zero-install/0install/2.8/ZeroInstall.pkg) to download binary package of [0Install for OS X](http://0install.net/install-osx.html).
2. Install 0Install: `sudo installer -pkg ZeroInstall.pkg -target`.
3. Install homebrew: `ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"`
4. Install cask: `brew install caskroom/cask/brew-cask`.
5. Install Java runtime: `brew cask install java`.
6. Setup alias: `0install -c add zanata-cli http://zanata.org/files/0install/zanata-cli.xml`.
7. Now you can run `zanata-cli --help` for more options.

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