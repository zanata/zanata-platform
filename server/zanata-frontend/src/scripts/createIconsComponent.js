/* eslint-disable no-console */
var fs = require('fs')
// build spritesheet used in this script (could change to return sheet)
require('./build-icon-spritesheet')

var svgFile = './app/components/Icons/icons.svg'
var componentFileSrc = './app/components/Icons/index.jsx.src'
var componentFile = './app/components/Icons/index.jsx'

function getSVG () {
  const data = fs.readFileSync(svgFile, 'utf8')
  return data.replace(/ style="position:absolute"/g, '')
}

function generateComponent () {
  const data = fs.readFileSync(componentFileSrc, 'utf8')
  const svg = getSVG()
  return data.replace(/{{svgFile: 'icons.svg'}}/, '\'' + svg + '\'')
}

process.stdout.write('Generating Icons component with embedded SVG')
fs.writeFileSync(componentFile, generateComponent(), 'utf8')
console.log(' ... Done')
