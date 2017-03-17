/**
 * Dev build config.
 *
 * This build is meant to use with webpack-dev-server for hot redeployment. It
 * should be optimised primarily for in-browser debugging, then for fast
 * incremental builds.
 */

var webpack = require('webpack')
var merge = require('webpack-merge')
var _ = require('lodash')
var defaultConfig = require('./webpack.config.js')

module.exports = merge.smart(defaultConfig, {
  // TODO change to an option that will show original files in the debugger
  //      and will allow setting breakpoints
  //      See: https://webpack.github.io/docs/configuration.html#devtool
  devtool: 'eval',
  plugins: [
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('development')
      }
    })
  ],

  eslint: {
    failOnError: false
  },

  devServer: {
    // TODO include other devserver config that is on command-line now
    port: 8000,
    historyApiFallback: {
      // serve the index file instead of 404, needed to load the app using
      // paths other than /
      index: 'index.html',
      rewrites: [
        // Anything other than bundle.js and bundle.css should get the app.
        // This is needed in addition to specifying the index file - I would not
        // expect it to be, but paths ending with a document name still get 404
        // when this is not included.
        //   regex notes:
        //     - negative lookahead (?!(frontend|editor)(\.min)?\.(js|css))
        //       checks that the current character is not the start of something
        //       like "frontend.min.js" or "editor.css"
        //     - the "." after the lookahead will match any single character
        //       (when the negative lookahead did not match)
        //     - the outer non-capturing group repeats the above any number of times
        //     - wrapped in ^ and $ so it must match the whole string
        {
          from: /^(?:(?!(frontend|editor)(\.min)?\.(js|css)).)*$/,
          to: '/index.html'
        }
      ]
    },
    stats: {
      colors: true
    }
  }
})
