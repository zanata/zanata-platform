var fs = require('fs')
var iconsSrc = './src/components/Icons/svgs'
var iconsFileName = './src/components/Icon/list.js'
var svgFileRegex = /Icon-(.*).svg/

fs.readdir(iconsSrc, function (err, files) {
  if (err) {
    console.error(err)
    return
  }
  const fileNames = files.map(file => {
    const fileName = file.match(svgFileRegex)
    return fileName ? fileName[1] : undefined
  }).filter(file => file)
  const iconsFile =
    `module.exports = [${fileNames.map(file => `'${file}'`)}]\r\n`
  fs.writeFile(iconsFileName, iconsFile, (err) => {
    if (err) throw err
    console.log('Icon file list saved')
  })
})
