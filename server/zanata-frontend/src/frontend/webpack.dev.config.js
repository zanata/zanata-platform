/**
 * Dev build config.
 *
 * This build is meant to use with webpack-dev-server for hot redeployment. It
 * should be optimised primarily for in-browser debugging, then for fast
 * incremental builds.
 */

var webpack = require('webpack')
var _ = require('lodash')
var defaultConfig = require('./webpack.config.js')

module.exports = _.merge({}, defaultConfig, {
  // TODO change to an option that will show original files in the debugger
  //      and will allow setting breakpoints
  //      See: https://webpack.github.io/docs/configuration.html#devtool
  devtool: 'eval',
  plugins: defaultConfig.plugins.concat([
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('development')
      }
    })
  ]),
  devServer: {
    historyApiFallback: true,
    stats: {
      colors: true
    }
  }
})
