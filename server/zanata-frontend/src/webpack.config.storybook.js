/**
 * This is the base config for Storybook builds. It does not utilize the latest
 * webpack features for backwards compatibility with storybook/react.
 */

var join = require('path').join
var _ = require('lodash')
// var CopyWebpackPlugin = require('copy-webpack-plugin')
var ManifestPlugin = require('webpack-manifest-plugin')

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

module.exports = function () {
  require.extensions['.css'] = () => {
    return
  }

  return {
    entry: {
      'frontend': './app/entrypoint/index',
      'editor': './app/editor/entrypoint/index.js'
    },

    output: {
      path: join(__dirname, 'dist'),
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
  }
}
