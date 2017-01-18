## This is a module to build Zanata frontend javascript projects

This module contains: User profile page, Glossary page, and Zanata side menu bar.

## To run/setup in nodeJS

Navigate to `frontend`, run `make install`

### To run in dev mode http://localhost:8000 (a HTTP server to serve index.html with webpack produced bundle.js)

- need http://localhost:8080/zanata to run separately
`make start`

### Production Build

`make build`

### Run styleguide

`make styleguide-build` followed by `make styleguide-server`


## To generate a jar dependency

```mvn install```

It will build and deploy to local maven repository a jar file containing the javascript bundle.
The jar file can be used directly under any servlet 3 compatible container and the bundle is accessible as static resources.
See [Servlet 3 static resources](http://www.webjars.org/documentation#servlet3).

The following Maven properties can be overridden on the command line with ```-Dkey=value```:

```
<node.version>v5.6.0</node.version>
<yarn.version>v0.18.1</yarn.version>
<yarn.install.directory>${download.dir}/zanata-frontend/node-${node.version}-yarn-${yarn.version}</yarn.install.directory>
```

By default it will try to install npm modules from npm registry (default cache TTL is 10 seconds).

## Documentation

For extensive details on each part of the front-end see the
[documentation](./docs).


## ignore-scripts

This module is setup to disable lifecycle scripts for yarn command by default. See help using `yarn -h`
The configuration is in .yarnrc in this module.
