/**
 * Webpack Storybook build configuration.
 * Merged with webpack.common.js
 */
const webpack = require('webpack')
const merge = require('webpack-merge')
const common = require('./webpack.common.js')

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
    {import('webpack').Configuration} WebpackConfig
 */

/**
 * @returns {WebpackConfig}
 */
module.exports = merge(common, {
  module: {
    rules: [
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
    }
  ],
  /* Caching for incremental builds, only needed for dev mode */
  cache: true,

  /* fail on first error */
  bail: false,

  devtool: 'eval-source-map'
})
