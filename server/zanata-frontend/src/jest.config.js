// FIXME the entry under 'moduleNameMapper' seems not to be applied properly
var config = require('./jest.config.json')
// we ave to use a separate json file because IntelliJ JEST plugin only accepts
// json file as JEST config
// See https://youtrack.jetbrains.com/issue/WEB-30009 - perhaps it works with 2017.3+?
module.exports = config
