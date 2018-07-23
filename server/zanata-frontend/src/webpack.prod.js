/**
 * Webpack Production build configuration.
 * Merged with webpack.common.js
 * See: https://webpack.js.org/guides/production/ for production guidelines
 */
/* eslint-disable max-len */
const webpack = require('webpack')
const merge = require('webpack-merge')
const common = require('./webpack.common.js')
const join = require('path').join
const UglifyJsPlugin = require('uglifyjs-webpack-plugin')
const CopyWebpackPlugin = require('copy-webpack-plugin')
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')

const postCssLoader = {
  loader: 'postcss-loader',
  options: {
    plugins: [
      require('postcss-discard-duplicates'),
      require('postcss-import')(),
      require('postcss-url')(),
      require('postcss-cssnext')(),
      require('postcss-reporter')()
    ]
  }
}

/** @typedef
    {import('webpack').Configuration} WebpackConfig
 */

/**
 * @returns {WebpackConfig}
 */
module.exports = merge(common, {
  // Built in Webpack mode definition
  mode: 'production',

  entry: {
    'frontend': './app/entrypoint/index',
    'editor': './app/editor/entrypoint/index',
    'frontend.legacy': './app/entrypoint/legacy'
  },

  output: {
    path: join(__dirname, 'dist'),
    filename: '[name].[chunkhash:8].cache.js',
    chunkFilename: '[name].[chunkhash:8].cache.js'
  },

  module: {
    rules: [
      /* Bundles all the css and allows use of various niceties, including
        * imports, variables, calculations, and non-prefixed codes.
        */
      {
        test: /\.css$/,
        use: [
          MiniCssExtractPlugin.loader,
          'css-loader',
          postCssLoader
        ]
      }
    ]
  },
  plugins: [
    new webpack.DefinePlugin({
      'process.env.NODE_ENV': JSON.stringify('production')
    }),
    new MiniCssExtractPlugin({
      // Options similar to the same options in webpackOptions.output
      // both options are optional
      filename: '[name].[chunkhash:8].cache.css',
      chunkFilename: '[name].[chunkhash:8].cache.css'
    }),
    new webpack.HashedModuleIdsPlugin(),
    new CopyWebpackPlugin([
      {
        from: join(__dirname, 'messages/*.json'),
        to: 'messages',
        toType: 'dir',
        flatten: true,
        // @ts-ignore any
        transform (content, path) {
          // Minimize the JSON files
          return JSON.stringify(JSON.parse(content))
        }
      }
    ])
  ],
  /**
   * Webpack 4 optimization options.
   * Overwrite the default plugins/options here.
   * See: https://webpack.js.org/configuration/optimization/
   */
  optimization: {
    minimizer: [
      new UglifyJsPlugin({
        cache: true,
        sourceMap: true
      }),
      new OptimizeCSSAssetsPlugin({
        cssProcessor: require('cssnano'),
        cssProcessorOptions: {
          safe: true, discardComments: { removeAll: true }
        },
        canPrint: true
      })
    ],
    splitChunks: { // CommonsChunkPlugin()
      chunks: 'all',
      name: 'runtime',
      cacheGroups: {
        default: false,
        frontend: {
          name: 'frontend',
          chunks: chunk => chunk.name === 'frontend',
          test: /\.css$/
        },
        editor: {
          name: 'editor',
          chunks: chunk => chunk.name === 'editor',
          test: /\.css$/
        },
        legacy: {
          name: 'frontend.legacy',
          chunks: chunk => chunk.name === 'frontend.legacy',
          test: /\.css$/
        },
      }
    },
    noEmitOnErrors: true // NoEmitOnErrorsPlugin
    // namedModules: true, // NamedModulesPlugin()
    // concatenateModules: true // ModuleConcatenationPlugin
  },

  /* fail on first error */
  bail: true,

  devtool: 'source-map'
})
