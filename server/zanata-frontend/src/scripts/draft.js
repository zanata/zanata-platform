/* eslint-disable no-console */
const c = require('cli-color')
const config = require('../webpack.draft.js')
const webpackBuild = require('./common-build.js')

console.log(c.black.bgYellow(' DRAFT BUILD - do not deploy! '))

webpackBuild(config)
