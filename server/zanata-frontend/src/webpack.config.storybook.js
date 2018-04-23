/**
 * This is the base config for Storybook builds. It does not utilize the latest
 * webpack features for backwards compatibility with storybook/react.
 */

var join = require('path').join
var _ = require('lodash')
// var CopyWebpackPlugin = require('copy-webpack-plugin')
var ManifestPlugin = require('webpack-manifest-plugin')

/**
 * Helper so we can use ternary with undefined to not specify a key
 * @param {any} obj
 */
function dropUndef (obj) {
  return _(obj).omitBy(_.isNil).value()
}

var postCssLoader = {
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

/**
 * To set env on command line:
 *   webpack --env.buildtype=storybook
 *
 * To set env in build scripts, just pass it as a normal function argument:
 *   import createConfig from '../webpack.config'
 *   const config = createConfig({ buildtype: 'storybook' })
 *
 * Valid buildtype settings: storybook.
 *
 * More info:
 *   https://blog.flennik.com/the-fine-art-of-the-webpack-2-config-dc4d19d7f172
 * @param {any} env
 * @param {boolean=} isEditor
 * @param {number=} devServerPort
 */
module.exports = function (env, isEditor, devServerPort) {
  const distDir = isEditor ? 'dist.editor' : 'dist'

  require.extensions['.css'] = () => {
    return
  }

  return dropUndef({
    entry: dropUndef({
      'frontend': './app/entrypoint/index',
      'editor': './app/editor/entrypoint/index.js'
    }),

    output: {
      path: join(__dirname, distDir),
      filename: '[name].js',
      chunkFilename: '[name].js',
      publicPath: `http://localhost:9001`
    },
    module: {
      rules: _.compact([
        /* Transpiles JS/JSX/TS/TSX files through TypeScript (tsc)
         */
        {
          test: /\.(j|t)sx?$/,
          exclude: /node_modules/,
          include: join(__dirname, 'app'),
          loader: 'awesome-typescript-loader'
        },

        /* Bundles all the css and allows use of various niceties, including
         * imports, variables, calculations, and non-prefixed codes.
         * The draft and prod options were removed as they were causing
         * errors with css-loader. In both cases, the css is minified.
         */
        {
          test: /\.css$/,
          use: [
            {
              loader: 'style-loader',
              options: {
                importLoaders: 1
              }
            },
            postCssLoader
          ]
        },

        /* Bundles bootstrap css into the same bundle as the other css.
         */
        {
          test: /\.less$/,
          use: [
            {
              loader: 'style-loader',
              options: {
                importLoaders: 1
              }
            },
            {
              loader: 'postcss-loader',
              options: {
                plugins: [
                  require('postcss-discard-duplicates')
                ]
              }
            },
            postCssLoader,
            {
              loader: 'less-loader',
              options: {javascriptEnabled: true}
            }
          ]
        }
      ])
    },

    plugins: _.compact([
      // This makes it easier to see if watch has picked up changes yet.
      // https://github.com/webpack/webpack/issues/1499#issuecomment-155064216
      // There's probably a config option for this (stats?) but I can't find it.
      function () {
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

      // TODO: Include react-itnl I18n messages in Storybook
      // Convert source (en) and translated strings to the format the app
      // can consume, in the dist directory.
      // new CopyWebpackPlugin([
      //   {
      //     from: join(__dirname, 'messages/*.json'),
      //     to: 'messages',
      //     toType: 'dir',
      //     flatten: true,
      //     transform (content, path) {
      //       // Minimize the JSON files
      //       return JSON.stringify(JSON.parse(content))
      //     }
      //   }
      // ]),

      new ManifestPlugin()
    ]),

    resolve: {
      /* Subdirectories to check while searching up tree for module
       * Default is ['', '.js'] */
      extensions: ['.js', '.jsx', '.json', '.css', '.less', '.ts', '.tsx']
    },

    node: {
      __dirname: true
    },

    /* Caching for incremental builds, only needed for dev mode */
    cache: true,

    /* fail on first error */
    bail: false,

    devtool: 'eval-source-map'
  })
}
