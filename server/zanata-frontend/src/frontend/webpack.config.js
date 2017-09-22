/**
 * This is the base config for all builds. It uses env.buildtype to change
 * the output configuration for different build types.
 */

var webpack = require('webpack')
var autoprefixer = require('autoprefixer')
var join = require('path').join
var _ = require('lodash')
var ExtractTextPlugin = require('extract-text-webpack-plugin')
var postcssImport = require('postcss-import')
var postcssCustomProperties = require('postcss-custom-properties')
var postcssCalc = require('postcss-calc')
var postcssColorFunction = require('postcss-color-function')
var postcssCustomMedia = require('postcss-custom-media')
var postcssEsplit = require('postcss-esplit')
// var postcssBemLinter = require('postcss-bem-linter')
var ReactIntlAggregatePlugin = require('react-intl-aggregate-webpack-plugin')
var ReactIntlFlattenPlugin = require('react-intl-flatten-webpack-plugin')

/* Helper so we can use ternary with undefined to not specify a key */
function dropUndef (obj) {
  return _(obj).omitBy(_.isNil).value()
}

var postCssLoader = {
  loader: 'postcss-loader',
  options: {
    plugins: [
      postcssImport(),
      postcssCustomProperties,
      postcssCalc,
      postcssColorFunction,
      postcssCustomMedia,
      postcssEsplit({
        quiet: true
      }),

      /*
       * This is not called with each imported file, but only with top-level
       * files. Some work is needed before this will give useful output.
       */
      // postcssBemLinter({
      //   preset: 'suit',
      //   implicitComponents: [
      //     '**/components/**/*.css',
      //     '**/containers/**/*.css'
      //   ],
      //   implicitUtilities: [
      //     '**/editor/css/**/*.css'
      //   ]
      // }),

      autoprefixer({
        browsers: [
          'Explorer >= 9',
          'last 2 Chrome versions',
          'last 2 Firefox versions',
          'last 2 Safari versions',
          'last 2 iOS versions',
          'Android 4'
        ]
      })
    ]
  }
}

/*
 * To set env on command line:
 *   webpack --env.buildtype=draft
 *
 * To set env in build scripts, just pass it as a normal function argument:
 *   import createConfig from '../webpack.config'
 *   const config = createConfig({ buildtype: 'draft' })
 *
 * Valid buildtype settings: prod (default), draft, dev, storybook.
 *
 * More info:
 *   https://blog.flennik.com/the-fine-art-of-the-webpack-2-config-dc4d19d7f172
 */
module.exports = function (env) {
  var buildtype = env && env.buildtype || 'prod'

  /**
   * Development build.
   *
   * This build is meant to use with webpack-dev-server for hot redeployment. It
   * should be optimised primarily for in-browser debugging, then for fast
   * incremental builds.
   */
  var dev = buildtype === 'dev'

  /**
   * Storybook config.
   *
   * Used by storybook to build individual components. Should inline the css and
   * optimize for fast incremental builds.
   */
  var storybook = buildtype === 'storybook'

  /**
   * Draft build (the fast build).
   *
   * This build is intended to build as fast as possible, while being
   * semantically the same as the production build.
   *
   * The output is not optimised for artifact size, human-readability or to work
   * well with development tools or debuggers. Use the prod or dev config if you
   * want those properties in the output.
   */
  var draft = buildtype === 'draft'

  /**
   * Production build config.
   *
   * This should be optimised for production performance and a small download.
   * Builds with this config should fail on any error, including linting errors.
   */
  var prod = buildtype === 'prod'

  // several options have the same values for both draft and prod
  var fullBuild = draft || prod

  return dropUndef({
    entry: storybook ? undefined : dropUndef({
      'frontend': './app/index',
      'editor': './app/editor/index.js',
      'frontend.legacy': fullBuild ? './app/legacy' : undefined
    }),
    output: storybook ? undefined : dropUndef({
      path: join(__dirname, 'dist'),
      filename: fullBuild ? '[name].min.js' : '[name].js',
      chunkFilename: fullBuild ? '[name].min.js' : '[name].js',
      // includes comments in the generated code about where the code came from
      pathinfo: dev,
      // required for hot module replacement
      publicPath: dev ? 'http://localhost:8000/' : undefined
    }),
    module: {
      rules: _.compact([
        /* Checks for errors in syntax, and for problematic and inconsistent
        * code in all JavaScript files.
        * Configured in .eslintrc
        */
        // TODO consider turning on for storybook
        storybook ? undefined : {
          test: /\.jsx?$/,
          exclude: /node_modules/,
          enforce: 'pre',
          loader: 'eslint-loader',
          options: {
            failOnWarning: !dev,
            failOnError: !dev
          }
        },

        /* Allows use of newer javascript syntax.
         *  - mainly ES6/ES2015 syntax, and a few ES2016 things
         *  - configured in .babelrc
         */
        {
          test: /\.jsx?$/,
          exclude: /node_modules/,
          include: join(__dirname, 'app'),
          loader: 'babel-loader',
          options: {
            babelrc: true
          }
        },

        /* Bundles all the css and allows use of various niceties, including
         * imports, variables, calculations, and non-prefixed codes.
         */
        {
          test: /\.css$/,
          use: ExtractTextPlugin.extract({
            fallback: 'style-loader',
            use: _.compact([
              {
                loader: 'css-loader',
                options: {
                  minimize: prod
                }
              },
              draft ? undefined : 'csso-loader',
              postCssLoader
            ])
          })
        },

        /* Bundles bootstrap css into the same bundle as the other css.
         * TODO look at running through csso, same as other css
         */
        {
          test: /\.less$/,
          exclude: /node_modules/,
          use: ExtractTextPlugin.extract({
            fallback: 'style-loader',
            use: [
              {
                loader: 'css-loader',
                options: {
                  minimize: prod
                }
              },
              postCssLoader,
              'less-loader'
            ]
          })
        }
      ])
    },

    plugins: _.compact([
      /* Outputs css to a separate file per entry-point.
         Note the call to .extract above */
      new ExtractTextPlugin({
        filename: '[name].css',
        // storybook should use the fallback: style-loader
        disable: storybook
      }),
      new webpack.NoEmitOnErrorsPlugin(),

      prod
        ? new webpack.optimize.UglifyJsPlugin({ sourceMap: true })
        : undefined,
      prod
        // Workaround to switch old loaders to minimize mode
        ? new webpack.LoaderOptionsPlugin({ minimize: true })
        : undefined,

      new webpack.DefinePlugin({
        'process.env': {
          'NODE_ENV': JSON.stringify(
            dev || storybook ? 'development' : 'production')
        }
      }),

      // Prepare source strings for translation.
      // This puts the strings generated by react-intl into a single file.
      fullBuild
        ? new ReactIntlAggregatePlugin({
          messagesPattern: join(__dirname, 'build/messages/**/*.json'),
          aggregateOutputDir: join(__dirname, 'messages'),
          aggregateFilename: 'en-us'
        })
        : undefined,

      // Convert source (en-US) and translated strings to the format the app
      // can consume, in the dist directory.
      fullBuild
        ? new ReactIntlFlattenPlugin({
          aggregatePattern: join(__dirname, 'messages/*.json'),
          // dist/messages/[locale-id].json
          langOutputDir: 'messages'
        })
        : undefined
    ]),

    resolve: {
      /* Subdirectories to check while searching up tree for module
       * Default is ['', '.js'] */
      extensions: ['.js', '.jsx', '.json', '.css', '.less']
    },

    node: {
      __dirname: true
    },

    /* Caching for incremental builds, only needed for dev mode */
    cache: !fullBuild,

    /* fail on first error */
    bail: fullBuild,

    devtool: prod ? 'source-map' : 'eval'
  })
}
