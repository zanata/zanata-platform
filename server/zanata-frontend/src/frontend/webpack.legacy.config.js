/**
 * Production legacy build config (build for the nav menu).
 *
 * This compiles the main navigation menu to be used separately from the
 * single-page frontend app, i.e. on JSF pages which have not been completely
 * converted to React yet.
 *
 * Uses the same settings as webpack.prod.config.js
 */

var webpack = require('webpack')
var path = require('path')
var _ = require('lodash')
var defaultConfig = require('./webpack.prod.config.js')

module.exports = _.merge({}, defaultConfig, {
  entry: './app/legacy',
  output: {
    path: path.join(__dirname, 'dist'),
    filename: 'frontend.bundle.legacy.min.js'
  }
})
