/* eslint-disable no-console */

/**
 * Run a development server that will refresh when code files are changed.
 *
 * Serves frontend by default.
 * Add flag --editor to serve editor instead.
 */

const fs = require('fs-extra')
const c = require('cli-color')
const WebpackDevServer = require('webpack-dev-server')
const webpack = require('webpack')
const createConfig = require('../webpack.dev.js')
const devServerConfig = require('./dev-server.config.js')

const isEditor = process.argv.indexOf('--editor') !== -1
const port = isEditor ? 8001 : 8000
const devRoot = `http://localhost:${port}`
const zanataRoot = 'http://localhost:8080'

const webpackConfig = createConfig(isEditor, port)

fs.ensureDir('dist')
  .then(() => {
    const path = isEditor ? 'app/editor/' : ''
    return fs.copy(path + 'index.html', 'dist/index.html')
  })
  .then(runDevServer)
  .catch(err => {
    console.error(c.red('Script failed with the following error'))
    console.error(err)
  })

function runDevServer () {
  if (isEditor) {
    webpackConfig.entry = {
      'editor': [
        // for inline mode
        `webpack-dev-server/client?${devRoot}/`,
        // for hot module replacement (not currently enabled)
        // 'webpack/hot/dev-server',
        // the entry-point
        webpackConfig.entry.editor
      ]
    }
  } else {
    webpackConfig.entry = {
      'frontend': [
        `webpack-dev-server/client?${devRoot}/`,
        webpackConfig.entry.frontend
      ]
    }
  }

  const compiler = webpack(webpackConfig)
  const server = new WebpackDevServer(compiler, devServerConfig(port))

  server.listen(port, 'localhost', () => {
    console.log('       Server: ' +
      c.green(`${devRoot}/{sub-path}`))
    if (isEditor) {
      console.log('URL structure: ' + c.cyan(
        `${devRoot}/project/translate/` +
        c.bold('{project}') +
        '/v/' + c.bold('{version}') +
        '/' + c.bold('{path/and/docId}') +
        '?lang=' + c.bold('{locale}')))
    } else {
      console.log('  Valid paths: ' + c.cyan([
        `${devRoot}/explore`,
        `${devRoot}/glossary`,
        `${devRoot}/glossary/project/` + c.bold('{projectSlug}'),
        `${devRoot}/languages`,
        `${devRoot}/profile/view/` + c.bold('{username}')]
        .join('\n               ')))
    }
    console.log(`See also:      ${devRoot}/webpack-dev-server`)
    console.log('REST requests: ' + c.blue(
      `${zanataRoot}/rest (you need to run zanata server)`))
    console.log(c.magenta('Watch browser console for actions and errors.'))
    console.log(c.red('If you see errors, please fix them before you commit.'))
    console.log(c.yellow(
      'Wait for "webpack: Compiled successfully." (can take several seconds).'))
  })
}
