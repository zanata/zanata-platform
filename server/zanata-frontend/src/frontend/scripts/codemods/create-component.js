/* eslint-disable no-console */

/* Script to create all the files for a new component.
 *
 * This aims to save devs a bit of boilerplate work and help
 * keep our component structure consistent.
 */

const _ = require('lodash')
const c = require('cli-color')
const fs = require('fs-extra')
const path = require('path')
const readline = require('readline')

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
})

// TODO use promises
askIfEditor(isEditor => {
  askName(name => {
    askIfStateless(isStateless => {
      writeln()
      write(c.blue('Making a '))
      write(c.blueBright(isStateless ? 'stateless ' : 'stateful '))
      write(c.blueBright(isEditor ? 'editor ' : 'frontend '))
      write(c.blue('component called '))
      writeln(c.blueBright(name))
      writeln()

      const absoluteComponentDir = path.resolve(
        __dirname, '..', '..', 'app',
        (isEditor ? 'editor' : ''),
        'components', name)
      const componentDir = path.relative(process.cwd(), absoluteComponentDir)

      createComponent({
        name: name,
        directory: absoluteComponentDir,
        stateless: isStateless
      })

      writeln()
      writeln('Next steps:')
      write(' 1. Run ')
      writeln(c.xterm(127)('make storybook-' +
        (isEditor ? 'editor' : 'frontend')))
      write(' 2. Open storybook in your browser and click ')
      writeln(c.xterm(128)(name))
      write(' 3. Start editing your files in ')
      writeln(c.xterm(129)(componentDir))
      writeln()

      rl.close()
    })
  })
})

function askIfEditor (andThen) {
  rl.question(c.cyan('Is it for the editor? '), (response) => {
    isYes(response, (err, isEditor) => {
      if (err) {
        console.error(c.red(
          'I wanted something like yes or no, not "' + response + '"'))
        askIfEditor(andThen)
      } else {
        andThen(isEditor)
      }
    })
  })
}

function isYes (string, cb) {
  if (/^\s*[yY]([eE][sS])?\s*$/.test(string)) {
    cb(undefined, true)
  } else if (/^\s*[nN][oO]?\s*$/.test(string)) {
    cb(undefined, false)
  } else {
    cb(true)
  }
}

function askName (andThen) {
  rl.question(c.cyan('What is the component name? '), (compName) => {
    const name = compName.trim()
    if (isValidName(name)) {
      // TODO check the component doesn't exist yet
      andThen(name)
    } else {
      console.error(c.red('"' + name + '" does not look valid.'))
      console.error(c.yellow(
        'It should be caps-case; like Bobbins, RickyRouse or MonaldMuck.'))
      askName(andThen)
    }
  })
}

function isValidName (name) {
  return !_.isEmpty(name) && isCapsCase(name)
}

function isCapsCase (name) {
  return /^([A-Z])\w+$/.test(name)
}

function askIfStateless (andThen) {
  rl.question(c.cyan('Is it a stateless component? [y/n/?] '),
    (response) => {
      isYes(response, (err, isStateless) => {
        if (err) {
          if (response === '?') {
            write('  Stateless components are just a render function.')
            write('  They are prefered, unless you need to:')
            write('   - hold some state')
            write('   - pass values to a callback')
          } else {
            console.error(c.red('What is "' + response + '"?'))
            console.error(
              c.yellow('I wanted "yes", "no" or "what does it mean"'))
          }
          askIfStateless(andThen)
        } else {
          andThen(isStateless)
        }
      })
    })
}

/**
 * opts:
 *  directory: component directory to create
 *  stateless: bool - use the stateless component template?
 */
function createComponent (opts) {
  const dir = opts.directory
  const name = opts.name
  fs.ensureDirSync(dir)

  const templateName = opts.stateless
  ? 'stateless.template.js'
  : 'stateful.template.js'
  copyWithName(name,
    path.resolve(__dirname, templateName),
    path.resolve(dir, 'index.js')
  )

  const storyFileName = name + '.story.js'
  copyWithName(name,
    path.resolve(__dirname, 'story.template.js'),
    path.resolve(dir, storyFileName)
  )

  // FIXME add in alphabetical order instead
  const storiesPath = path.resolve(dir, '..', 'components.story.js')
  write(c.green(' +add to: '))
  writeln(path.relative('', storiesPath))
  fs.appendFileSync(storiesPath,
    "require('./" + name + '/' + storyFileName + "')\n"
  )

  copyWithName(name,
    path.resolve(__dirname, 'test.template.js'),
    path.resolve(dir, name + '.test.js')
  )
}

function copyWithName (name, from, to) {
  write(c.green('  create: '))
  writeln(path.relative('', to))
  const template = fs.readFileSync(from, 'utf8')
  const withName = template.replace(/COMPONENT_NAME_HERE/g, name)
  fs.writeFileSync(to, withName, 'utf8')
}

function write (s) {
  process.stdout.write(s)
}

function writeln (s) {
  if (s !== undefined) {
    write(s)
  }
  write('\n')
}

// TODO (optional) read whether the component is connected?
//      Alternative is to add a script to connect a component.
