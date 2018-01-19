import { isString } from 'util';

// This test ensures that all source files can be parsed and can resolve their
// imports even if they don't have unit tests yet.
// It should also be a useful test when changing our build toolchain, eg
// reconfiguring or replacing Babel.
test('can require all local js files', () => {
    const sourceFiles = /^.*\.(js|jsx|ts|tsx)$/
    const testFiles = /\.test\.(js|ts)x?$/
    const dirsToSkip = /^\.|node_modules$|entrypoint$/
    const modules = require('require.all')({
        dir: '.',
        ignore: dirsToSkip,
        match: sourceFiles,
        not: testFiles,
        recursive: true,
        tree: false,
        // add path to avoid collisions in the flattened result
        map: (name, path, isFile) => path + '/' + name,
        // parse all matching source files
        require: name => true,
    })
    const count = Object.keys(modules).length
    if (count < 286) throw new Error("Missing modules")

    let countStrings = 0
    for (const key in modules) {
        if (modules.hasOwnProperty(key)) {
            const element = modules[key];
            if (typeof element === 'string') {
                ++countStrings
            }
        }
    }
    if (count === countStrings) {
        throw new Error("Modules were loaded as strings, not required/parsed")
    }

    // console.log('Loaded ' + count + ' modules.')
});
