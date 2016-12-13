/**
 * Production build config.
 *
 * This should be optimised for production performance and a small download.
 * Builds with this config should fail on any error, including linting errors.
 */

var webpack = require('webpack')
var path = require('path')
var ExtractTextPlugin = require('extract-text-webpack-plugin')
var _ = require('lodash')
var defaultConfig = require('./webpack.config.js')

module.exports = _.merge({}, defaultConfig, {
  entry: {
    'bundle': './app/index',
    'bundle.legacy': './app/legacy'
  },
  cache: false,
  output: {
    path: path.join(__dirname, 'dist'),
    filename: 'frontend.[name].min.js'
  },
  module: {
    preLoaders: [
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        loader: 'eslint'
      }
    ],
    loaders: [
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        include: path.join(__dirname, 'app'),
        loader: 'babel',
        query: {
          presets: ['react', 'es2015', 'stage-0']
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
      },
      {
        test: /\.less$/,
        loader: ExtractTextPlugin.extract(
          'style',
          'css!less',
          'autoprefixer?browsers=last 2 versions'
        )
      }
    ]
  },
  plugins: defaultConfig.plugins.concat([
    new ExtractTextPlugin('frontend.css'),
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
