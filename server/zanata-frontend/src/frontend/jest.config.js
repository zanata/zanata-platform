// FIXME the entry under 'moduleNameMapper' seems not to be applied properly
import config from './jest.config.json'
// we have to use a separate json file because IntelliJ JEST plugin only accepts
// json file as JEST config
module.exports = config
