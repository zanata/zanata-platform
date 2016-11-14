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

var ExtractTextPlugin = require('extract-text-webpack-plugin')
var _ = require('lodash')
var defaultConfig = require('./webpack.prod.config.js')

module.exports = _.merge({}, defaultConfig, {
  devtool: 'eval',

  // FRAGILE: merging, must line up with the same index or it will break
  loaders: [
    {},
    {
      loader: ExtractTextPlugin.extract(
        'style',
        'css?-minimize',
        'autoprefixer?browsers=last 2 versions'
      )
    },
    {
      loader: ExtractTextPlugin.extract(
        'style',
        'css?-minimize!less',
        'autoprefixer?browsers=last 2 versions'
      )
    }
  ]
})

// do not minify/uglify the output
module.exports.plugins = _.filter(module.exports.plugins,
  (plugin) => plugin.constructor.name !== 'UglifyJsPlugin')


console.log('draft config plugins')
console.log(module.exports.plugins)
