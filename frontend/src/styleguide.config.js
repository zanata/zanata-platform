var defaultWebpackConfig = require('./webpack.config.js')
var path = require('path')

module.exports = {
  title: 'Zanata Style Guide',
  components: './src/components/**/index.jsx',
  template: './styleguide.html',
  updateWebpackConfig: function (webpackConfig, env) {
    webpackConfig.entry.push(path.join(__dirname, 'src/styles/base.css'))
    webpackConfig.entry.push(path.join(__dirname, 'src/styles/atomic.css'))
    webpackConfig.entry.push(path.join(__dirname, 'src/styles/animations.css'))
    webpackConfig.entry.push(path.join(__dirname, 'src/styles/extras.css'))
    webpackConfig.entry.push(path.join(__dirname, 'src/styles/styleguide.css'))
    webpackConfig.module.loaders =
      webpackConfig.module.loaders
        .concat(defaultWebpackConfig.module.loaders)
    return webpackConfig
  }
}
