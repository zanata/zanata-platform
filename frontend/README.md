## This is a module to build frontend javascript projects

At the moment it only contains "user profile page" bundle.

To build it, just run

```mvn install```

Following mvn arguments is overrideable from command line:

```
<node.version>v0.12.2</node.version>
<npm.version>2.7.6</npm.version>
<node.install.directory>${download.dir}/zanata-frontend/node-${node.version}-npm-${npm.version}</node.install.directory>
<npm.cli.script>${node.install.directory}/node/npm/bin/npm-cli.js</npm.cli.script>
```

By default it will try to install npm modules from npm registry (default cache TTL is 10 seconds).
If you activate profile ```-DnpmOffline``` the cache-min option will become 9999999 which means it will try to install npm modules from cache first.
