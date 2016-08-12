# Zanata Alpha Editor

This is a single-page React+Redux application that uses REST APIs on the Zanata
server for all its data. It is meant to be packaged with zanata-war as a jar
dependency.

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

1. Make sure [node and npm](http://nodejs.org/) are installed. Version v5.4.1
2. Setup dependencies: `make setup`.
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

## License

Zanata is Free software, licensed under the [LGPL](http://www.gnu.org/licenses/lgpl-2.1.html).
