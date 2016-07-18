var webpack = require('webpack')
var path = require('path')
var ExtractTextPlugin = require('extract-text-webpack-plugin')
var _ = require('lodash')
var defaultConfig = require('./webpack.config.js')
var bundleDest = __dirname

module.exports = _.merge({}, defaultConfig, {
  cache: false,
  output: {
    path: bundleDest,
    filename: 'frontend.bundle.min.js'
  },
  module: {
    loaders: [
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        include: path.join(__dirname, 'main'),
        loader: 'babel',
        query: {
          presets: ['react', 'stage-0', 'es2015']
        },
        babelrc: false
      },
      {
        test: /\.css$/,
        loader: ExtractTextPlugin.extract(
          'style',
          'css',
          'autoprefixer?browsers=last 2 versions'
        )
      }
    ]
  },
  plugins: defaultConfig.plugins.concat([
    new ExtractTextPlugin('bundle.css'),
    new webpack.optimize.DedupePlugin(),
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
  ])
})
