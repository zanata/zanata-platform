The Ivy distribution of the client is a small script that will download the client the first time it is run. This distribution requires Apache Ivy to run.

1. See [Setup Ivy](/ivy/setup) for Apache Ivy installation.
2. Save [this script](https://raw.github.com/zanata/zanata-client-ivy/master/zanata-cli) somewhere on your path, and make sure it is executable. For example, assuming you have `~/bin in $PATH`,

```
cd ~/bin
wget https://raw.github.com/zanata/zanata-client-ivy/master/zanata-cli
chmod 755 zanata-cli
```

*Note:* It's a good idea to check for a new version of zanata-cli once in a while (especially when a new version of Zanata server is released).
