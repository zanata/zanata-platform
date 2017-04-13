/**
 * Production build config.
 *
 * This should be optimised for production performance and a small download.
 * Builds with this config should fail on any error, including linting errors.
 */

var webpack = require('webpack')
var path = require('path')
var merge = require('webpack-merge')
var defaultConfig = require('./webpack.config.js')

module.exports = merge.smart(defaultConfig, {
  entry: {
    'frontend.legacy': './app/legacy'
  },
  cache: false,
  output: {
    filename: '[name].min.js'
  },
  module: {
    loaders: [
      {
        test: /\.jsx?$/,
        babelrc: false
      }
    ]
  },
  plugins: [
    new webpack.optimize.UglifyJsPlugin({
      compress: {
        warnings: false
      }
    }),
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('production')
      }
    })
  ],

  eslint: {
    failOnWarning: true
  }
})
