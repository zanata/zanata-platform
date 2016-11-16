/**
 * Draft legacy build config (fast build for the nav menu).
 *
 * This compiles the main navigation menu to be used separately frm the
 * single-page frontend app, i.e. on JSF pages which have not been completely
 * converted to React yet.
 *
 * Uses the same settings as webpack.draft.config.js
 * Uses entry-point and output copied from webpack.legacy.config.js
 */

var webpack = require('webpack')
var path = require('path')
var _ = require('lodash')
var defaultConfig = require('./webpack.draft.config.js')

module.exports = _.merge({}, defaultConfig, {
  entry: './app/legacy',
  output: {
    path: path.join(__dirname, 'dist'),
    filename: 'frontend.bundle.legacy.min.js'
  }
})
