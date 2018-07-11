/**
 * Common webpack configuration.
 * Merged with webpack.dev|prod|story.js using webpack-merge
 * See: https://webpack.js.org/guides/production/
 */
const join = require('path').join
const ManifestPlugin = require('webpack-manifest-plugin')

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
        loader: 'awesome-typescript-loader'
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
  }
}
