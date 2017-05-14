/* eslint-disable no-console */

/**
 * Combines svgs in the Icons component to an svg spritesheet.
 */

const c = require('cli-color')
const fs = require('fs-extra')
const path = require('path')
const SVGSpriter = require('svg-sprite')

// TODO run this as part of icon icon creation scripts

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
console.log(c.magenta('Reading svgs from ' + svgsPath))
const svgFiles = fs.readdirSync(svgsPath)
const colWidth = svgFiles.reduce((l, name) => Math.max(l, name.length), 0)
var count = 0
svgFiles.map((fileName) => {
  count++
  process.stdout.write(c.blue(' ' +
    (fileName + '                            ').slice(0, colWidth)))
  if (count % 4 === 0) process.stdout.write('\n')
  const fullPath = path.join(svgsPath, fileName)
  spriter.add(fullPath, fileName, fs.readFileSync(fullPath))
})
spriter.compile((err, result) => {
  if (err) {
    console.error(err)
    process.exit(1)
  }
  for (var type in result.symbol) {
    console.log(c.cyan('Write ' + type + ': ' + result.symbol[type].path))
    fs.ensureDirSync(path.dirname(result.symbol[type].path))
    fs.writeFileSync(result.symbol[type].path, result.symbol[type].contents)
  }
})
console.log(c.green('Done'))
