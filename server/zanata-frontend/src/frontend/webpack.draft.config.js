/**
 * Draft build config (the fast build).
 *
 * This build is intended to build as fast as possible, while being semantically
 * the same as the production build.
 *
 * The output is not optimised for artifact size, human-readability or to work
 * well with development tools or debuggers. Use the prod or dev config if you
 * want those properties in the output.
 */

var ExtractTextPlugin = require('extract-text-webpack-plugin')
var _ = require('lodash')
var merge = require('webpack-merge')
var defaultConfig = require('./webpack.prod.config.js')

module.exports = merge.smart(defaultConfig, {
  devtool: 'eval',

  module: {
    loaders: [
      {
        test: /\.css$/,
        loader: ExtractTextPlugin.extract(
          'style',
          'css?-minimize',
          'autoprefixer?browsers=last 2 versions'
        )
      },
      {
        test: /\.less$/,
        loader: ExtractTextPlugin.extract(
          'style',
          'css?-minimize!less',
          'autoprefixer?browsers=last 2 versions'
        )
      }
    ]
  },
  // do not minify/uglify the output
  plugins: _.filter(defaultConfig.plugins,
    (plugin) => plugin.constructor.name !== 'UglifyJsPlugin')
})
