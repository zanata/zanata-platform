var webpack = require('webpack')
var path = require('path')
var _ = require('lodash')
var defaultConfig = require('./webpack.prod.config.js')
var bundleDest = __dirname
/**
 * This js file is used for all jsf pages that is not 100% reactjs yet.
 * jsf page which only display side menu
 */
module.exports = _.merge({}, defaultConfig, {
  entry: './main/legacy',
  output: {
    path: bundleDest,
    filename: 'frontend.legacy.min.js'
  }
})
