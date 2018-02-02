/**
 * This is the base config for all builds. It uses env.buildtype to change
 * the output configuration for different build types.
 */

var webpack = require('webpack')
var autoprefixer = require('autoprefixer')
var join = require('path').join
var _ = require('lodash')
var stylelint = require('stylelint')
var postcssDiscardDuplicates = require('postcss-discard-duplicates')
var ExtractTextPlugin = require('extract-text-webpack-plugin')
var postcssImport = require('postcss-import')
var postcssCustomProperties = require('postcss-custom-properties')
var postcssCalc = require('postcss-calc')
var postcssColorFunction = require('postcss-color-function')
var postcssCustomMedia = require('postcss-custom-media')
var postcssEsplit = require('postcss-esplit')
var ReactIntlAggregatePlugin = require('react-intl-aggregate-webpack-plugin')
var ReactIntlFlattenPlugin = require('react-intl-flatten-webpack-plugin')
var ManifestPlugin = require('webpack-manifest-plugin')
var cssNano = require('cssnano')
// `CheckerPlugin` is optional. Use it if you want async error reporting.
// We need this plugin to detect a `--watch` mode. It may be removed later
// after https://github.com/webpack/webpack/issues/3460 will be resolved.
const { CheckerPlugin } = require('awesome-typescript-loader')

/* Helper so we can use ternary with undefined to not specify a key */
function dropUndef (obj) {
  return _(obj).omitBy(_.isNil).value()
}

var postCssLoader = {
  loader: 'postcss-loader',
  options: {
    plugins: [
      postcssDiscardDuplicates(),
      postcssImport(),
      postcssCustomProperties,
      postcssCalc,
      cssNano(),
      postcssColorFunction,
      postcssCustomMedia,
      postcssEsplit({
        quiet: true
      }),

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

  require.extensions['.css'] = () => {
    return;
  };

  return dropUndef({
    entry: storybook ? undefined : dropUndef({
      'frontend': './app/entrypoint/index',
      'editor': './app/editor/entrypoint/index.js',
      'frontend.legacy': fullBuild ? './app/entrypoint/legacy' : undefined
    }),

    output: storybook ? undefined : dropUndef({
      path: join(__dirname, 'dist'),
      filename: fullBuild ? '[name].[chunkhash:8].cache.js' : '[name].js',
      chunkFilename: fullBuild ? '[name].[chunkhash:8].cache.js' : '[name].js',
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

        // TODO consider turning on for storybook
        storybook ? undefined : {
          test: /\.tsx?$/,
          exclude: /node_modules/,
          enforce: 'pre',
          loader: 'tslint-loader',
          options: {
            failOnHint: !dev,
            formatter: 'verbose',
          }
        },

        /* Transpiles JS/JSX/TS/TSX files through TypeScript (tsc)
         */
        {
          test: /\.(j|t)sx?$/,
          exclude: /node_modules/,
          include: join(__dirname, 'app'),
          loader: 'awesome-typescript-loader',
        },

        /* TODO:
        { enforce: "pre", test: /\.js$/, loader: "source-map-loader" },
        */

        /* Bundles all the css and allows use of various niceties, including
         * imports, variables, calculations, and non-prefixed codes.
         * The draft and prod options were removed as they were causing
         * errors with css-loader. In both cases, the css is minified.
         */
        {
          test: /\.css$/,
          use: ExtractTextPlugin.extract({
            fallback: 'style-loader',
            use: _.compact([
              {
                loader: 'css-loader',
                options: {
                  importLoaders: 1
                }
              },
              postCssLoader
            ])
          })
        },

        /* Bundles bootstrap css into the same bundle as the other css.
         */
        {
          test: /\.less$/,
          exclude: /node_modules/,
          use: ExtractTextPlugin.extract({
            fallback: 'style-loader',
            use: [
              {
                loader: 'postcss-loader',
                options: {
                  plugins: [
                    require('stylelint'),
                    require('postcss-discard-duplicates')
                  ]
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
      // This makes it easier to see if watch has picked up changes yet.
      // https://github.com/webpack/webpack/issues/1499#issuecomment-155064216
      // There's probably a config option for this (stats?) but I can't find it.
      function() {
        this.plugin('watch-run', function(watching, callback) {
            console.log('Begin compile at ' + new Date());
            callback();
        })
      },

      new CheckerPlugin(),

      /* Outputs css to a separate file per entry-point.
         Note the call to .extract above */
      new ExtractTextPlugin({
        filename: '[name].[chunkhash:8].cache.css',
        // storybook should use the fallback: style-loader
        disable: storybook || dev
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

      fullBuild ? new webpack.HashedModuleIdsPlugin() : undefined,

      storybook || dev ? undefined : new webpack.optimize.CommonsChunkPlugin({
        name: 'runtime'
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
        : undefined,

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
    cache: !fullBuild,

    /* fail on first error */
    bail: fullBuild,

    devtool: prod ? 'source-map' : 'eval-source-map'
  })
}
