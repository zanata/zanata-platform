/**
 * Dev build config.
 *
 * This build is meant to use with webpack-dev-server for hot redeployment. It
 * should be optimised primarily for in-browser debugging, then for fast
 * incremental builds.
 */
var webpack = require('webpack')
var merge = require('webpack-merge')
var defaultConfig = require('./webpack.config.js')

module.exports = merge.smart(defaultConfig, {
  // TODO change to an option that will show original files in the debugger
  //      and will allow setting breakpoints
  //      See: https://webpack.github.io/docs/configuration.html#devtool
  // devtool: 'eval',
  // output: {
  //   // includes comments in the generated code about where the code came from
  //   pathinfo: true,
  //   // required for hot module replacement
  //   publicPath: 'http://localhost:8000/'
  // } // ,
  // plugins: [
  //   new webpack.DefinePlugin({
  //     'process.env': {
  //       'NODE_ENV': JSON.stringify('development')
  //     }
  //   })
  // ] // ,

  // eslint: {
  //   failOnError: false
  // }
})
