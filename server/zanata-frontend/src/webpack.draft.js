/**
 * Webpack Draft build configuration.
 * Merged with webpack.prod.js
 */
const merge = require('webpack-merge')
const prod = require('./webpack.prod.js')

/** @typedef
    {import('webpack').Configuration} WebpackConfig
 */

/**
 * @returns {WebpackConfig}
 */
module.exports = merge(prod, {
  /**
   * Webpack 4 optimization options.
   * Disable minimizers and optimization (UglyfyJS, OptimizeCSSAssetsPlugin) for draft build.
   */
  optimization: {
    minimize: false
  }
})
