var fs = require('fs')
var svgFile = './app/components/Icons/icons.svg'
var componentFileSrc = './app/components/Icons/index.jsx.src'
var componentFile = './app/components/Icons/index.jsx'

function getSVG (cb) {
  fs.readFile(svgFile, 'utf8', function (err, data) {
    var result
    if (err) return console.log(err)
    result = data.replace(/ style="position:absolute"/g, '')
    cb(result)
  })
}

function generateComponent (cb) {
  fs.readFile(componentFileSrc, 'utf8', function (err, data) {
    if (err) return console.log(err)
    getSVG(function (svg) {
      var component = data.replace(/{{svgFile: 'icons.svg'}}/, '\'' + svg + '\'')
      cb(component)
    })
  })
}

generateComponent(function (component) {
  fs.writeFile(componentFile, component, 'utf8', function (err) {
    if (err) return console.log(err)
  })
})
