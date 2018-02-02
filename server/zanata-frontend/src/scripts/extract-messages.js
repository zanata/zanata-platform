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
    messages[r.id] = {
      defaultMessage: r.defaultMessage,
      description: r.description
    }
  })
  fs.writeFileSync(`messages/en-us.json`, JSON.stringify(messages, undefined, 2))
})
