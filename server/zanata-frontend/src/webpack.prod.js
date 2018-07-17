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

// Default options for splitChunks cacheGroups
const groupsOptions = {
  chunks: 'all',
  minSize: 0,
  minChunks: 1,
  reuseExistingChunk: true,
  enforce: true
}

function recursiveIssuer (m) {
  if (m.issuer) {
    return recursiveIssuer(m.issuer)
  } else if (m.name) {
    return m.name
  } else {
    return false
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
      filename: '[name].[hash].cache.css',
      chunkFilename: '[name].[hash].css'
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
        parallel: true,
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
    // Split CSS chunks by entrypoint
    // See: https://github.com/webpack-contrib/mini-css-extract-plugin#extracting-css-based-on-entry
    splitChunks: { // CommonsChunkPlugin()
      name: 'runtime',
      cacheGroups: {
        frontendStyles: {
          name: 'frontend',
          test: (m, c, entry = 'frontend') => m.constructor.name === 'CssModule' && recursiveIssuer(m) === entry,
          ...groupsOptions
        },
        legacyStyles: {
          name: 'frontend.legacy',
          test: (m, c, entry = 'frontend.legacy') => m.constructor.name === 'CssModule' && recursiveIssuer(m) === entry,
          ...groupsOptions
        },
        editorStyles: {
          name: 'editor',
          test: (m, c, entry = 'editor') => m.constructor.name === 'CssModule' && recursiveIssuer(m) === entry,
          ...groupsOptions
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
