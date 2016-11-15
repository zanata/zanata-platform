/**
 * Draft build config (the fast build).
 *
 * This build is intended to build as fast as possible, while being semantically
 * the same as the production build.
 *
 * The output is not optimized for artifact size, human-readability or to work
 * well with development tools or debuggers. Use the prod or dev config if you
 * want those properties in the output.
 */

var webpack = require('webpack')
var ExtractTextPlugin = require('extract-text-webpack-plugin')
var _ = require('lodash')
var defaultConfig = require('./webpack.prod.config.js')

module.exports = _.merge({}, defaultConfig, {
  devtool: 'eval',

  module: {
    // FRAGILE: index must line up to override values correctly
    loaders: [
      {},
      {
        // prevent css optimization and minification
        loader: ExtractTextPlugin.extract('style', 'css?-minimize!postcss!rework')
      }
    ]
  }
})

// Prevent eslint running (it is the only defined preLoader)
module.exports.module.preLoaders = []

// do not minify/uglify the output
module.exports.plugins = _.filter(module.exports.plugins,
  (plugin) => plugin.constructor.name !== 'UglifyJsPlugin')
