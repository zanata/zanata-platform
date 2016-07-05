var autoprefixer = require('autoprefixer')
var ExtractTextPlugin = require('extract-text-webpack-plugin')
var join = require('path').join
var reworkCalc = require('rework-calc')
var reworkColorFunction = require('rework-color-function')
var reworkCustomMedia = require('rework-custom-media')
var reworkIeLimits = require('rework-ie-limits')
var reworkNpm = require('rework-npm')
var reworkVars = require('rework-vars')
var reworkSuitConformance = require('rework-suit-conformance')

module.exports = {
  entry: './index.js',
  output: {
    path: join(__dirname, 'build'),
    filename: 'bundle.js'
  },
  module: {
    /* Checks for errors in syntax, and for problematic and inconsistent
     * code in all JavaScript files.
     * Configured in .eslintrc
     */
    preLoaders: [
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        loader: 'eslint'
      }
    ],
    loaders: [
      /* Allows use of newer javascript syntax.
       *  - mainly ES6/ES2015 syntax, and a few ES2016 things
       *  - configured in .babelrc
       */
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        loader: 'babel'
      },

      /* Bundles all the css and allows use of various niceities, including
       * imports, variables, calculations, and non-prefixed codes.
       */
      {
        test: /\.css$/,
        exclude: /node_modules/,
        loader: ExtractTextPlugin.extract('style', 'css!csso!postcss!rework')
      }
    ]
  },

  plugins: [
    /* Outputs css to a separate file. Note the call to .extract above */
    new ExtractTextPlugin('bundle.css')
  ],

  resolve: {
    /* Subdirectories to check while searching up tree for module */
    // TODO remove when components migrated to use .js (default is ['', '.js'])
    extensions: ['', '.js', '.jsx']
  },
  devtool: 'source-map',
  devServer: {
    historyApiFallback: true
  },

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
      reworkNpm(),
      reworkVars(),
      reworkCalc,
      reworkColorFunction,
      reworkCustomMedia,
      reworkIeLimits,
      reworkSuitConformance
    ]
  }
}
