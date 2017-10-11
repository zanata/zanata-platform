/* eslint-disable no-console */

/**
 * Combines svgs in the Icons component to an svg spritesheet.
 */

const c = require('cli-color')
const fs = require('fs-extra')
const path = require('path')
const SVGSpriter = require('svg-sprite')

console.log(c.bgCyan(c.black(' Generating icons spritesheet ')))

const spriter = new SVGSpriter({
  dest: 'app/components/Icons',
  svg: {},
  mode: {
    symbol: {
      dest: '',
      sprite: 'icons.svg',
      inline: true
    }
  }
})
const svgsPath = path.resolve('app/components/Icons/svgs/')
console.log(c.magenta('Reading svgs from ' + path.relative('', svgsPath)))
const svgFiles = fs.readdirSync(svgsPath)
printIconFileNames(svgFiles)
svgFiles.map((fileName) => {
  const fullPath = path.join(svgsPath, fileName)
  spriter.add(fullPath, fileName, fs.readFileSync(fullPath))
})
spriter.compile((err, result) => {
  if (err) {
    console.error(err)
    process.exit(1)
  }
  for (var type in result.symbol) {
    const p = result.symbol[type].path
    console.log(c.cyan('Write ' + type + ': ' + path.relative('', p)))
    fs.ensureDirSync(path.dirname(p))
    fs.writeFileSync(p, result.symbol[type].contents)
  }
})
console.log(c.green('Done'))

function printIconFileNames (fileNames) {
  const columnWidth = fileNames.reduce((l, name) => Math.max(l, name.length), 0)
  fileNames.map((fileName, index) => {
    process.stdout.write(c.blue(' ' +
      (fileName + '                            ').slice(0, columnWidth)))
    if (index % 4 === 3) process.stdout.write('\n')
  })
  if (fileNames.length % 4 !== 0) process.stdout.write('\n')
}
