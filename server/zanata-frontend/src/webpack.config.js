/**
 * This is the base config for all builds. It uses env.buildtype to change
 * the output configuration for different build types.
 */

var webpack = require('webpack')
var join = require('path').join
var _ = require('lodash')
const UglifyJsPlugin = require('uglifyjs-webpack-plugin')
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin')
var MiniCssExtractPlugin = require('mini-css-extract-plugin')
var CopyWebpackPlugin = require('copy-webpack-plugin')
var ManifestPlugin = require('webpack-manifest-plugin')
// `CheckerPlugin` is optional. Use it if you want async error reporting.
// We need this plugin to detect a `--watch` mode. It may be removed later
// after https://github.com/webpack/webpack/issues/3460 will be resolved.
const CheckerPlugin = require('awesome-typescript-loader').CheckerPlugin
const tsImportPluginFactory = require('ts-import-plugin')
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
 * @param {any} env
 * @param {boolean=} isEditor
 * @param {number=} devServerPort
 */
module.exports = function (env, isEditor, devServerPort) {
  var buildtype = env && env.buildtype || 'prod'
  const distDir = isEditor ? 'dist.editor' : 'dist'

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
    return
  }

  return dropUndef({
    // Built in Webpack mode definition
    mode: dev ? 'development' : 'production',

    entry: storybook ? undefined : dropUndef({
      'frontend': './app/entrypoint/index',
      'editor': './app/editor/entrypoint/index.js',
      'frontend.legacy': fullBuild ? './app/entrypoint/legacy' : undefined
    }),

    output: storybook ? undefined : dropUndef({
      path: join(__dirname, distDir),
      filename: fullBuild ? '[name].[chunkhash:8].cache.js' : '[name].js',
      chunkFilename: fullBuild ? '[name].[chunkhash:8].cache.js' : '[name].js',
      // includes comments in the generated code about where the code came from
      pathinfo: dev,
      // required for hot module replacement
      // or is it https://github.com/webpack-contrib/style-loader/issues/55 ?
      publicPath: dev
        ? `http://localhost:${devServerPort}/`
        : undefined
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
            formatter: 'verbose'
          }
        },
        /* Transpiles JS/JSX/TS/TSX files through TypeScript (tsc)
         */
        {
          test: /\.(j|t)sx?$/,
          exclude: /node_modules/,
          include: join(__dirname, 'app'),
          loader: 'awesome-typescript-loader',
          // load antd through modular import plugin
          options: {
            transpileOnly: true,
            getCustomTransformers: () => ({
              before: [ tsImportPluginFactory({
                libraryName: 'antd',
                libraryDirectory: 'es',
                style: 'css'
              }) ]
            }),
            compilerOptions: {
              module: 'es2015'
            }
          }
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
          use: [
            dev ? 'style-loader' : MiniCssExtractPlugin.loader,
            'css-loader',
            postCssLoader
          ]
        },

        /* Bundles less files into the same bundle as the other css.
         */
        {
          test: /\.less$/,
          use: [
            {
              loader: 'style-loader'
            }, {
              loader: 'css-loader'
            }, {
              loader: 'less-loader', options: {
                javascriptEnabled: true
              }
            }
          ]}
      ])
    },

    plugins: _.compact([
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

      new CheckerPlugin(),

      new MiniCssExtractPlugin({
        // Options similar to the same options in webpackOptions.output
        // both options are optional
        filename: dev ? '[name].css' : '[name].[hash].css',
        chunkFilename: dev ? '[id].css' : '[id].[hash].css'
      }),

      new webpack.DefinePlugin({
        'process.env': {
          'NODE_ENV': JSON.stringify(
            dev || storybook ? 'development' : 'production')
        }
      }),

      fullBuild ? new webpack.HashedModuleIdsPlugin() : undefined,

      // Convert source (en) and translated strings to the format the app
      // can consume, in the dist directory.
      fullBuild
        ? new CopyWebpackPlugin([
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
        : undefined,

      new ManifestPlugin()
    ]),
    // Suppress warnings about assets and entrypoint size
    performance: { hints: false },
    optimization: {
      minimizer: [
        new UglifyJsPlugin({
          cache: true,
          parallel: true,
          sourceMap: true
        }),
        new OptimizeCSSAssetsPlugin({})
      ],
      // namedModules: true, // NamedModulesPlugin()
      splitChunks: { // CommonsChunkPlugin()
        name: 'runtime'
      },
      noEmitOnErrors: true // NoEmitOnErrorsPlugin
      // concatenateModules: true // ModuleConcatenationPlugin
    },

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
