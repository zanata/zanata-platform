# Scripts

The [scripts](../scripts) folder is for files that need to do something at
(build)[./build.md] time usually as part of npm scripts in
[package.json](../package.json) (more on that later).

They are node scripts (bash is possible too, but will not work on windows) and
usually use node modules to do something like creating an SVG sprite or creating
a json file from a javascript file. Basically something too complex to put
directly in npm scripts.
