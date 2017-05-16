/* eslint-disable no-console */
const c = require('cli-color')
const webpack = require('webpack')
const prodConfig = require('../webpack.prod.config.js')
const draftConfig = require('../webpack.draft.config.js')

// execute icon scripts to generate required files
require('./createIconsComponent')
require('./generateIconList')

const isDraft = process.argv.indexOf('--draft') !== -1

console.log(isDraft
  ? c.bgYellow(' DRAFT BUILD - do not deploy! ')
  : c.bgCyan(' PRODUCTION BUILD '))

const config = isDraft ? draftConfig : prodConfig
webpack(config, (err, stats) => {
  if (err) {
    console.error(err.stack || err)
    if (err.details) {
      console.error(err.details)
    }
    return
  }

  if (stats.hasErrors()) {
    console.error(stats.toJson().errors)
  }
  if (stats.hasWarnings()) {
    console.warn(stats.toJson().warnings)
  }

  // stats options: https://webpack.github.io/docs/node.js-api.html#stats
  // This set of options appears to generate the same output as the CLI with
  // only the --display-error-details flag set.
  const statsOptions = {
    colors: true,
    cached: false,
    chunks: false,
    modules: true,
    cachedAssets: false,
    exclude: ['node_modules', 'bower_components', 'components'],
    errorDetails: true
  }
  console.log(stats.toString(statsOptions))
})
