var autoprefixer = require('autoprefixer')
var reworkCalc = require('rework-calc')
var reworkColorFunction = require('rework-color-function')
var ExtractTextPlugin = require('extract-text-webpack-plugin')
var reworkCustomMedia = require('rework-custom-media')
var reworkIeLimits = require('rework-ie-limits')
var reworkNpm = require('rework-npm')
var reworkVars = require('rework-vars')
var reworkSuitConformance = require('rework-suit-conformance')

/*
 * This is a slightly modified version of the webpack config for the main app.
 * It is used primarily to build the css in the same way as the main app, but
 * inserts it onto the page rather than building it to a separate file.
 *
 * The css file is imported in .storybook-frontend/config.js to trigger the css
 * build with this configuration.
 */
module.exports = {
  module: {
    loaders: [
      /* Allows use of newer javascript syntax.
       *  - mainly ES6/ES2015 syntax, and a few ES2016 things
       *  - configured in .babelrc
       */
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        loader: 'babel?presets[]=react,presets[]=es2015,presets[]=stage-0'
      },

      /* Bundles all the css and allows use of various niceities, including
       * imports, variables, calculations, and non-prefixed codes.
       */
      {
        test: /\.css$/,
        loader: 'style!css!csso!postcss!rework'
      },

      /* Bundles bootstrap css into the same bundle as the other css.
       * TODO look at running through csso and rework, same as other css
       */
      {
        test: /\.less$/,
        exclude: /node_modules/,
        loader: 'style!css!postcss!less'
      }
    ]
  },

  resolve: {
    /* Subdirectories to check while searching up tree for module */
    // TODO remove when components migrated to use .js (default is ['', '.js'])
    extensions: ['', '.js', '.jsx']
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
