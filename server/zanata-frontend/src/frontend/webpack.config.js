/**
 * This is the base config for all builds.
 *
 * Cleanup needed:
 *  - This config is used for a first-pass build that is required for atomic css
 *   to work. This should be fixed so that atomic works without the extra build.
 *  - This outputs to a different file than the production build, but we could
 *    just use the same filename for all builds.
 */

var webpack = require('webpack')
var autoprefixer = require('autoprefixer')
var join = require('path').join
var ExtractTextPlugin = require('extract-text-webpack-plugin')
var reworkCalc = require('rework-calc')
var reworkColorFunction = require('rework-color-function')
var reworkCustomMedia = require('rework-custom-media')
var reworkIeLimits = require('rework-ie-limits')
var reworkNpm = require('rework-npm')
var reworkVars = require('rework-vars')
var reworkSuitConformance = require('rework-suit-conformance')

module.exports = {
  entry: {
    'frontend': './app/index',
    'editor': './app/editor/index.js'
  },
  output: {
    path: join(__dirname, 'dist'),
    filename: '[name].js',
    chunkFilename: '[name].js'
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
        loader: 'eslint-loader'
      },

      /* Allows use of newer javascript syntax.
       *  - mainly ES6/ES2015 syntax, and a few ES2016 things
       *  - configured in .babelrc
       */
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        include: join(__dirname, 'app'),
        loader: 'babel-loader?presets[]=react,presets[]=es2015,presets[]=stage-0'
      },

      /* Bundles all the css and allows use of various niceties, including
       * imports, variables, calculations, and non-prefixed codes.
       */
      {
        test: /\.css$/,
        use: ExtractTextPlugin.extract({
          fallback: 'style-loader',
          use: [
            'css-loader',
            'csso-loader',
            'postcss-loader',
            'rework-loader'
          ]
        })
      },

      /* Bundles bootstrap css into the same bundle as the other css.
       * TODO look at running through csso and rework, same as other css
       */
      {
        test: /\.less$/,
        exclude: /node_modules/,
        loader: ExtractTextPlugin.extract({
          fallback: 'style-loader',
          use: [
            'css-loader',
            'postcss-loader',
            'less-loader'
          ]
        })
      }
    ]
  },

  plugins: [
    /* Outputs css to a separate file per entry-point.
       Note the call to .extract above */
    new ExtractTextPlugin({
      filename: '[name].css'
    }),
    new webpack.NoErrorsPlugin()
  ],

  resolve: {
    /* Subdirectories to check while searching up tree for module
     * Default is ['', '.js'] */
    extensions: ['.js', '.jsx', '.json', '.css', '.less']
  },

  node: {
    __dirname: true
  },

  eslint: {
    failOnWarning: false,
    failOnError: true
  },

  devtool: 'source-map',

  /* Used just to run autoprefix */
  postcss: [
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
  ],

  /* Enables a range of syntax improvements and checks for css files */
  rework: {
    use: [
      reworkNpm({ root: join(__dirname, 'app') }),
      reworkVars(),
      reworkCalc,
      reworkColorFunction,
      reworkCustomMedia,
      reworkIeLimits,
      reworkSuitConformance
    ]
  }
}
