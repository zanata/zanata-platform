var webpack = require('webpack')
var _ = require('lodash')
var defaultConfig = require('./webpack.config.js')

module.exports = _.merge({}, defaultConfig, {
  plugins: defaultConfig.plugins.concat([
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('production')
      }
    }),
    new webpack.optimize.UglifyJsPlugin({
      compress: {
        // TODO should look at whether the warnings can be fixed instead of suppressed
        warnings: false
      }
    })
  ]),

  eslint: {
    failOnWarning: true,
  }
})
