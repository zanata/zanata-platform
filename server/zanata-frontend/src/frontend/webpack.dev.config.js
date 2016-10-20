var webpack = require('webpack')
var _ = require('lodash')
var defaultConfig = require('./webpack.config.js')

module.exports = _.merge({}, defaultConfig, {
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
