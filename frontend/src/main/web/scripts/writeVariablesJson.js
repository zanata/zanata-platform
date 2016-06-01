var jsonfile = require('jsonfile')
var variables = require('../src/constants/styles.js')
var file = './src/constants/styles.json'

jsonfile.writeFile(file, variables, {spaces: 2}, function (err) {
  err && console.error(err)
})
