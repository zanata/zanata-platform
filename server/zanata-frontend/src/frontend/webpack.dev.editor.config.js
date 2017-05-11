/**
 * Editor dev build config.
 *
 * This separate config is needed for editor development to avoid having all
 * entry points rebuilt. Also because webpack-dev-server does not work properly
 * on rebuilds with multiple modules.
 *
 * This build is meant to use with webpack-dev-server for hot redeployment. It
 * should be optimised primarily for in-browser debugging, then for fast
 * incremental builds.
 */

var webpack = require('webpack')
var merge = require('webpack-merge')
var _ = require('lodash')
var devConfig = require('./webpack.dev.config.js')

// Overwrite multiple entry points. It is safe to modify in-place because the
// config is only used once per invocation (no risk of devConfig being used
// elsewhere in the same invocation)
devConfig.entry = { 'editor': './app/editor/index.js' }

module.exports = devConfig
