/**
 * Production build config.
 *
 * This should be optimised for production performance and a small download.
 * Builds with this config should fail on any error, including linting errors.
 */

var webpack = require('webpack')
var merge = require('webpack-merge')
var defaultConfig = require('./webpack.config.js')

module.exports = merge.smart(defaultConfig, {
  // entry: {
  //   'frontend.legacy': './app/legacy'
  // },
  // cache: false,
  // output: {
  //   filename: '[name].min.js',
  //   chunkFilename: '[name].min.js'
  // },
  // module: {
  //   rules: [
  //     {
  //       test: /\.jsx?$/,
  //       babelrc: false
  //     }
  //   ]
  // },
  // plugins: [
  //   new webpack.optimize.UglifyJsPlugin({
  //     sourceMap: true
  //   }),
  //   new webpack.DefinePlugin({
  //     'process.env': {
  //       'NODE_ENV': JSON.stringify('production')
  //     }
  //   }),
  //   // Workaround to switch old loaders to minimize mode
  //   // FIXME update loaders and configure them directly instead
  //   new webpack.LoaderOptionsPlugin({
  //     minimize: true
  //   })
  // ],

  // fail on first error
  // bail: true,

  // eslint: {
  //   failOnWarning: true
  // }
})
