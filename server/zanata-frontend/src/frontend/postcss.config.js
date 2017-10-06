/* empty file, works around a bug in postcss-loader with webpack 2+ */

// FIXME just put the postcss config in here
module.exports = {
  parser: 'sugarss',
  plugins: {
    'postcss-import': {},
    'postcss-cssnext': {},
    'autoprefixer': {},
    'stylelint': {},
  }
}
