/**
 * Webpack Development / Watch tasks build configuration.
 * Merged with webpack.common.js
 */
const webpack = require('webpack')
const merge = require('webpack-merge')
const common = require('./webpack.common.js')
const join = require('path').join
// `CheckerPlugin` is optional. Use it if you want async error reporting.
// We need this plugin to detect a `--watch` mode. It may be removed later
// after https://github.com/webpack/webpack/issues/3460 will be resolved.
const CheckerPlugin = require('awesome-typescript-loader').CheckerPlugin

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
    {import('webpack').Configuration & {
      devServer?: import('webpack-dev-server').Configuration
    }} WebpackConfig
 */

/**
 * @param {boolean=} isEditor
 * @param {number=} devServerPort
 * @returns {WebpackConfig}
 */
module.exports = function (isEditor, devServerPort) {
  const distDir = isEditor ? 'dist.editor' : 'dist'
  return merge(common, {
    // Built in Webpack mode definition
    mode: 'development',

    entry: {
      'frontend': './app/entrypoint/index',
      'editor': './app/editor/entrypoint/index.js'
    },

    output: {
      path: join(__dirname, distDir),
      filename: '[name].js',
      chunkFilename: '[name].js',
      // includes comments in the generated code about where the code came from
      pathinfo: true,
      // required for hot module replacement
      // or is it https://github.com/webpack-contrib/style-loader/issues/55 ?
      publicPath: `http://localhost:${devServerPort}/`
    },
    module: {
      rules: [
        /* Bundles all the css and allows use of various niceties, including
          * imports, variables, calculations, and non-prefixed codes.
          */
        {
          test: /\.css$/,
          use: [
            'style-loader',
            'css-loader',
            postCssLoader
          ]
        }
      ]
    },

    plugins: [
      new webpack.DefinePlugin({
        'process.env.NODE_ENV': JSON.stringify('development')
      }),
      // This makes it easier to see if watch has picked up changes yet.
      // https://github.com/webpack/webpack/issues/1499#issuecomment-155064216
      // There's probably a config option for this (stats?) but I can't find it.
      function () {
        // @ts-ignore any
        this.plugin('watch-run',
          /**
           * @param {any} _watching
           * @param {any} callback
           */
          function (_watching, callback) {
            // eslint-disable-next-line no-console
            console.log('Begin compile at ' + new Date())
            callback()
          })
      },

      new CheckerPlugin()
    ],

    /* Caching for incremental builds, only needed for dev mode */
    cache: true,

    /* fail on first error */
    bail: false,

    devtool: 'eval-source-map'
  })
}
