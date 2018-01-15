/* eslint-disable no-console */
var fs = require('fs')
var iconsSrc = './app/components/Icons/svgs'
var iconsFileName = './app/components/Icon/list.js'
var svgFileRegex = /Icon-(.*).svg/

process.stdout.write('Generating list of icon names in Icon/list.js')
const files = fs.readdirSync(iconsSrc)
const fileNames = files.map(file => {
  const fileName = file.match(svgFileRegex)
  return fileName ? fileName[1] : undefined
}).filter(file => file)
const iconsFile =
  `module.exports = [${fileNames.map(file => `'${file}'`)}]\r\n`
fs.writeFileSync(iconsFileName, iconsFile)
console.log(' ... Done')
