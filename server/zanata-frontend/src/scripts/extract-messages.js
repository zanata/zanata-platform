var fs = require('fs')
var glob = require('glob')
var parse = require('typescript-react-intl').default

function runner (pattern, cb) {
  var results = []
  pattern = pattern || 'app/**/*.@(js|jsx|ts|tsx)'
  glob(pattern, function (err, files) {
    if (err) {
      throw new Error(err)
    }
    files.forEach(f => {
      var contents = fs.readFileSync(f).toString()
      var res = parse(contents)
      results = results.concat(res)
    })

    cb && cb(results)
  })
}

runner(null, function (res) {
  var messages = {}
  res.forEach(r => {
    // TODO: Update Zanata to read react-intl JSON (with defaultMessage and
    // description) and output flattened JSON (id:translationText)
    messages[r.id] = r.defaultMessage
  })
  fs.writeFileSync(`messages/en.json`, JSON.stringify(messages, undefined, 2))
})
