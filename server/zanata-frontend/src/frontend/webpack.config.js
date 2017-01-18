/**
 * This is the base config for all builds.
 *
 * Cleanup needed:
 *  - This config is used for a first-pass build that is required for atomic css
 *    to work. This should be fixed so that atomic works without the extra build.
 *  - This outputs to a different file than the production build, but we could
 *    just use the same filename for all builds.
 */

var webpack = require('webpack')
var path = require('path')
var ExtractTextPlugin = require('extract-text-webpack-plugin')

module.exports = {
  entry: {
    'bundle': './app/index'
  },
  output: {
    path: path.join(__dirname, 'dist'),
    filename: '[name].js'
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
        loader: 'atomic-loader?configPath=' + __dirname +
          '/atomicCssConfig.js' +
          '!babel?presets[]=react,presets[]=es2015,presets[]=stage-0'
      },
      {
        test: /\.css$/,
        include: [
          path.join(__dirname, 'app/styles'),
          path.join(__dirname, 'node_modules/zanata-ui/dist'),
        ],
        loader: ExtractTextPlugin.extract(
          'style',
          'css',
          'autoprefixer?browsers=last 2 versions'
        )
      },
      {
        test: /\.less$/,
        exclude: /node_modules/,
        include: path.join(__dirname, 'app/styles'),
        loader: ExtractTextPlugin.extract(
          'style',
          'css!less',
          'autoprefixer?browsers=last 2 versions'
        )
      },
    ]
  },
  plugins: [
    new ExtractTextPlugin('frontend.css'),
    new webpack.optimize.OccurenceOrderPlugin(),
    new webpack.optimize.DedupePlugin(),
    new webpack.NoErrorsPlugin(),
  ],
  resolve: {
    extensions: ['', '.js', '.jsx', '.json', '.css', '.less']
  },
  node: {
    __dirname: true
  },
  eslint: {
    failOnWarning: false,
    failOnError: true
  }
}
