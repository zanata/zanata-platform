var webpack = require('webpack')
var path = require('path')
var _ = require('lodash')
var defaultConfig = require('./webpack.prod.config.js')
/**
 * This js file is used for all jsf pages that is not 100% reactjs yet.
 * jsf page which only display side menu
 */
module.exports = _.merge({}, defaultConfig, {
  entry: './app/legacy',
  output: {
    path: path.join(__dirname, 'dist'),
    filename: 'frontend.bundle.legacy.min.js'
  }
})
