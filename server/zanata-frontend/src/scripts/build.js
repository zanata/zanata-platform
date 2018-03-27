/* eslint-disable no-console */
const c = require('cli-color')
const webpack = require('webpack')
const createConfig = require('../webpack.config.js')

// execute icon scripts to generate required files
require('./createIconsComponent')
require('./generateIconList')
require('./extract-messages')

const isDraft = process.argv.indexOf('--draft') !== -1

console.log(isDraft
  ? c.black.bgYellow(' DRAFT BUILD - do not deploy! ')
  : c.white.bgCyan(' PRODUCTION BUILD '))

/* equivalent of command-line `webpack --env.buildtype=draft` or
 * `webpack --env.buildtype=prod`
 */
const config = createConfig({ buildtype: isDraft ? 'draft' : 'prod' })
webpack(config, (err, stats) => {
  if (err) {
    console.error(err.stack || err)
    if (err.details) {
      console.error(err.details)
    }
    // ensure abnormal termination of the build
    // https://github.com/webpack/webpack/issues/708
    process.exitCode = 1
    return
  }

  if (stats.hasErrors()) {
    console.error(stats.toJson().errors)
  }
  if (stats.hasWarnings()) {
    console.warn(stats.toJson().warnings)
  }

  // stats options: https://webpack.js.org/configuration/stats/
  // This set of options appears to generate the same output as the CLI with
  // only the --display-error-details flag set.
  const statsOptions = {
    colors: true,
    cached: false,
    chunks: false,
    // prevents some extract-text-webpack-plugin output
    children: false,
    modules: false,
    cachedAssets: false,
    exclude: ['node_modules', 'bower_components', 'components'],
    errorDetails: true
  }
  console.log(stats.toString(statsOptions))
  if (stats.hasErrors() || stats.hasWarnings()) {
    // ensure abnormal termination of the build
    // https://github.com/webpack/webpack/issues/708
    process.exitCode = 1
  }
})
