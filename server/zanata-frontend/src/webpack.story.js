const join = require('path').join
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

/** @typedef
    {import('webpack').Configuration & {
      devServer?: import('webpack-dev-server').Configuration
    }} WebpackConfig
 */

/**
 * @returns {WebpackConfig}
 */
module.exports = {
  module: {
    rules: [
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
