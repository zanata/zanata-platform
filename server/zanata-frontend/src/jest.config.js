// FIXME the entry under 'moduleNameMapper' seems not to be applied properly
var config = require('./jest.config.json')
// we ave to use a separate json file because IntelliJ JEST plugin only accepts
// json file as JEST config
module.exports = config
