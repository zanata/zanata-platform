var defaultWebpackConfig = require('./webpack.config.js')
var path = require('path')

module.exports = {
  title: 'Zanata Style Guide',
  sections: [
    {
      name:'Components folder',
      components: './app/components/**/index.jsx',
    }
  ],
  template: './styleguide.html',
  updateWebpackConfig: function (webpackConfig, env) {
    webpackConfig.entry.push(path.join(__dirname, 'app/styles/style.less'))
    webpackConfig.entry.push(path.join(__dirname, 'app/styles/variables.less'))
    webpackConfig.entry.push(path.join(__dirname, 'app/styles/styleguide.css'))
    webpackConfig.module.loaders =
      webpackConfig.module.loaders
        .concat(defaultWebpackConfig.module.loaders)
    return webpackConfig
  }
}
