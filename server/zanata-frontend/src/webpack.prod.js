const webpack = require('webpack')
const join = require('path').join
const CopyWebpackPlugin = require('copy-webpack-plugin')
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')
const ManifestPlugin = require('webpack-manifest-plugin')
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

/**
 * @param {boolean=} isEditor
 */
module.exports = function (isEditor) {
  const distDir = isEditor ? 'dist.editor' : 'dist'
  return {
    // Built in Webpack mode definition
    mode: 'production',

    entry: {
      'frontend': './app/entrypoint/index',
      'editor': './app/editor/entrypoint/index.js',
      'frontend.legacy': './app/entrypoint/legacy'
    },

    output: {
      path: join(__dirname, distDir),
      filename: '[name].[chunkhash:8].cache.js',
      chunkFilename: '[name].[chunkhash:8].cache.js'
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
            MiniCssExtractPlugin.loader,
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
      new webpack.DefinePlugin({
        'process.env.NODE_ENV': JSON.stringify('production')
      }),
      new MiniCssExtractPlugin({
        // Options similar to the same options in webpackOptions.output
        // both options are optional
        filename: '[name].[hash].css',
        chunkFilename: '[id].[hash].css'
      }),
      new webpack.HashedModuleIdsPlugin(),
      new CopyWebpackPlugin([
        {
          from: join(__dirname, 'messages/*.json'),
          to: 'messages',
          toType: 'dir',
          flatten: true,
          // @ts-ignore any
          transform(content, path) {
            // Minimize the JSON files
            return JSON.stringify(JSON.parse(content))
          }
        }
      ]),
      new OptimizeCSSAssetsPlugin({
        cssProcessor: require('cssnano'),
        cssProcessorOptions: { safe: true, discardComments: { removeAll: true } },
        canPrint: true
      }),
      new ManifestPlugin()
    ],
    // Suppress warnings about assets and entrypoint size
    performance: { hints: false },

    optimization: {
      minimize: true,
      // namedModules: true, // NamedModulesPlugin()
      splitChunks: { // CommonsChunkPlugin()
        name: 'runtime',
        chunks: 'all'
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

    /* fail on first error */
    bail: false,

    devtool: 'eval-source-map'
  }
}
