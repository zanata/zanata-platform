# Build

The build primarily uses 2 tools:

- Webpack
- NPM Scripts

## Webpack

This is used to process all Javascript, including converting ES6 code to ES5
(using Babel) and then bundles all javascript into 1 file. It is also where then
atomic css loader lives which scan the Javascript for atomic class names. It
then creates atomic.css from this.

## NPM Scripts

NPM Scripts are used to manage build tasks. Anything that can be run on the
command line or in node can be run from here. It should be fairly easy to
follow. Have a look at [package.json](../package.json) under the
`scripts` property.
