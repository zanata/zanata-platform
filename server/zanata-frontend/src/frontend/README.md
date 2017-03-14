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


## ignore-scripts

This module is setup to disable lifecycle scripts for yarn command by default. See help using `yarn -h`
The configuration is in .yarnrc in this module.


# Zanata Alpha Editor

The editor is a separate single-page React+Redux application that uses REST APIs
on the Zanata server for all its data. It is meant to be packaged with
zanata-war as a jar dependency.

## Build jar

To package the editor as a jar, use maven:

```
mvn package
```

## Development Builds

All the build instructions below are for use when developing this editor on its
own. They are not needed to make the final jar package. They just allow for
faster builds and immediate feedback on code changes.

Make sure you test changes against the current server before checking them in.

## Setup and Deployment
1. Make sure [node and yarn](http://nodejs.org/) are installed. Node: v5.6.0, Yarn: v0.18.1
2. Setup dependencies: `make install`.
3. Build compressed files: `make build`, files will be in /app/dist


### Run with live reload

Build and run a server: `make watch`.

 - Editor is available at [localhost:8080](http://localhost:8080)
   - the editor will be blank at the base URL, include the project-version to
     show content. The format is
     localhost:8000/#/{project-slug}/{version-slug}/translate
 - Assumes a server is already serving the Zanata REST API.


### Run with live reload and local API server

Build and run server and API server: `make watch-fakeserver`.

 - Editor is available at [localhost:8080](http://localhost:8080)
   - URL for a working document from the default API server [Tiny Project 1, hello.txt to French](http://localhost:8080/#/tiny-project/1/translate/hello.txt/fr)
 - REST API server is available at
   [localhost:7878/zanata/rest](http://localhost:7878/zanata/rest)


## Running tests

Run tests with `make test`.


## Code Guidelines

### Javascript

And [these](https://github.com/zanata/javascript) for Javascript.

Always add documentation.

### CSS

For CSS I am aiming to move to [these guidelines](https://github.com/suitcss/suit/blob/master/doc/README.md).

## ignore-scripts

This module is setup to disable lifecycle scripts for yarn command by default. See help using `yarn -h`
The configuration is in .yarnrc in this module.
