const join = require('path').join
const ManifestPlugin = require('webpack-manifest-plugin')
// `CheckerPlugin` is optional. Use it if you want async error reporting.
// We need this plugin to detect a `--watch` mode. It may be removed later
// after https://github.com/webpack/webpack/issues/3460 will be resolved.
const CheckerPlugin = require('awesome-typescript-loader').CheckerPlugin
const tsImportPluginFactory = require('ts-import-plugin')

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
  return {
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
        /* Checks for errors in syntax, and for problematic and inconsistent
        * code in all JavaScript files.
        * Configured in .eslintrc
        */
        {
          test: /\.jsx?$/,
          exclude: /node_modules/,
          enforce: 'pre',
          loader: 'eslint-loader',
          options: {
            failOnWarning: false,
            failOnError: false
          }
        },

        /* Checks for errors in syntax, and for problematic and inconsistent
        * code in all TypeScript files.
        * Configured in tslint.json
        */
        {
          test: /\.tsx?$/,
          exclude: /node_modules/,
          enforce: 'pre',
          loader: 'tslint-loader',
          options: {
            failOnHint: false,
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
              before: [tsImportPluginFactory({
                libraryName: 'antd',
                libraryDirectory: 'es',
                style: 'css'
              })]
            }),
            compilerOptions: {
              module: 'es2015'
            }
          }
        },

        /* Bundles all the css and allows use of various niceties, including
          * imports, variables, calculations, and non-prefixed codes.
          * The draft and prod options were removed as they were causing
          * errors with css-loader. In both cases, the css is minified.
          */
        {
          test: /\.css$/,
          use: [
            'style-loader',
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
          ]
        }
      ]
    },

    plugins: [
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

      new ManifestPlugin()
    ],
    // Suppress warnings about assets and entrypoint size
    performance: { hints: false },

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
