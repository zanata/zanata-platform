/* global test */

// This test ensures that all source files can be parsed and can resolve their
// imports even if they don't have unit tests yet.
// It should also be a useful test when changing our build toolchain, eg
// reconfiguring or replacing Babel.
test('can require all local js files', () => {
  const sourceFiles = /^.*\.(js|jsx|ts|tsx)$/
  // We skip test and story files because they don't just define functions
  // and constants, they have side effects:
  const testFiles = /\.(story|test)\.(js|ts)x?$/
  const dirsToSkip = /^\.|node_modules$|entrypoint$/
  const modules = require('require.all')({
    dir: '.',
    ignore: dirsToSkip,
    match: (name: string) =>
      name.match(sourceFiles) &&
      !name.endsWith('.d.ts'),
    not: testFiles,
    recursive: true,
    tree: false,
    // add path to avoid collisions in the flattened result
    map: (name: string, path: string, _isFile: boolean) => {
      const newName = path + '/' + name
      // uncomment this if you can't work out which file is failing to load:
      // console.log(newName)
      return newName
    },
    // parse all matching source files
    // @ts-ignore any
    require: _name => true
  })
  const count = Object.keys(modules).length
  if (count < 250) {
    throw new Error(`Some modules may be missing: only found ${count} modules`)
  }

  let countStrings = 0
  for (const key in modules) {
    if (modules.hasOwnProperty(key)) {
      const element = modules[key]
      if (typeof element === 'string') {
        ++countStrings
      }
    }
  }
  if (count === countStrings) {
    throw new Error('Modules were loaded as strings, not required/parsed')
  }

  // tslint:disable-next-line no-console
  console.log('Loaded ' + count + ' modules.')
})
