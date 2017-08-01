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
// var reworkCalc = require('rework-calc')
// var reworkColorFunction = require('rework-color-function')
// var reworkCustomMedia = require('rework-custom-media')
// var reworkIeLimits = require('rework-ie-limits')
// var reworkNpm = require('rework-npm')
// var reworkVars = require('rework-vars')
// var reworkSuitConformance = require('rework-suit-conformance')

// var postcssNpm = require('postcss-npm')
var postcssImport = require('postcss-import')
var postcssCssVariables = require('postcss-css-variables')
// var postcssVars = require('postcss-vars')
var postcssCalc = require('postcss-calc')
var postcssColorFunction = require('postcss-color-function')
var postcssCustomMedia = require('postcss-custom-media')
var postcssEsplit = require('postcss-esplit')
var postcssBemLinter = require('postcss-bem-linter')

/* Used just to run autoprefix */
var postCssLoader = {
  loader: 'postcss-loader',
  options: {
    plugins: [
      // looks outdated, trying a different one
      // postcssNpm(/*{ root: join(__dirname, 'app') }*/),
      postcssImport(),

      // seeing error "processor is not a function" with this in the stack
      // postcssVars,
      // This one is failing to recognize variables from theme.css that are
      // used in other places.
      postcssCssVariables,

      postcssCalc,
      postcssColorFunction,
      postcssCustomMedia,
      postcssEsplit,

      // emitting too many warnings, can't see what is happening
      // postcssBemLinter,
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

// Postcss equivalents of these rework things
//
// Suitcss can be generated from a nested structure, but so far we are just
// linting for it.
//  SUIT convention is the default
// eslint-disable-next-line max-len
// https://webdesign.tutsplus.com/tutorials/using-postcss-with-bem-and-suit-methodologies--cms-24592

/* Enables a range of syntax improvements and checks for css files */
// var reworkLoader = {
//   loader: 'rework-loader',
//   options: {
//     use: [
//       reworkNpm({ root: join(__dirname, 'app') }),
//       reworkVars(),
//       reworkCalc,
//       reworkColorFunction,
//       reworkCustomMedia,
//       reworkIeLimits,
//       reworkSuitConformance
//     ]
//   }
// }

module.exports = function (env) {
  var buildtype = env && env.buildtype || 'prod'
  var dev = buildtype === 'dev'
  var draft = buildtype === 'draft'
  var prod = buildtype === 'prod'

  // several options have the same values for both draft and prod
  var fullBuild = draft || prod

  var config = {
    // prod adds frontend.legacy
    entry: {
      'frontend': './app/index',
      'editor': './app/editor/index.js',
      // TODO check this works as desired, don't want a failure in dev
      'frontend.legacy': fullBuild ? './app/legacy' : undefined
    },
    cache: !fullBuild,
    output: {
      path: join(__dirname, 'dist'),
      filename: fullBuild ? '[name].min.js' : '[name].js',
      chunkFilename: fullBuild ? '[name].min.js' : '[name].js'
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
            failOnWarning: !dev,
            failOnError: !dev
          }
        },

        /* Allows use of newer javascript syntax.
         *  - mainly ES6/ES2015 syntax, and a few ES2016 things
         *  - configured in .babelrc
         */
        {
          test: /\.jsx?$/,
          exclude: /node_modules/,
          include: join(__dirname, 'app'),
          loader: 'babel-loader',
          options: {
            // do not use babelrc for full build. Not sure why it would need to
            // be used for incremental build.
            // FIXME babelrc configures translation file output
            babelrc: !fullBuild,
            presets: [ 'react', 'es2015', 'stage-0' ]
          }
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
              postCssLoader // ,
              // reworkLoader
            ]
          })
        },

        /* Bundles bootstrap css into the same bundle as the other css.
         * TODO look at running through csso and rework, same as other css
         */
        {
          test: /\.less$/,
          exclude: /node_modules/,
          use: ExtractTextPlugin.extract({
            fallback: 'style-loader',
            use: [
              'css-loader',
              postCssLoader,
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
      new webpack.NoEmitOnErrorsPlugin()
    ],

    resolve: {
      /* Subdirectories to check while searching up tree for module
       * Default is ['', '.js'] */
      extensions: ['.js', '.jsx', '.json', '.css', '.less']
    },

    node: {
      __dirname: true
    },

    devtool: prod ? 'source-map' : 'eval'
  }

  return config
}
