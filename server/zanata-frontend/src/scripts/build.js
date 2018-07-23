/* eslint-disable no-console */
const c = require('cli-color')
const config = require('../webpack.prod.js')
const webpackBuild = require('./common-build.js')

console.log(c.white.bgCyan(' PRODUCTION BUILD '))

webpackBuild(config)
