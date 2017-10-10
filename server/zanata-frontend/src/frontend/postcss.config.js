module.exports = {
  parser: 'postcss-less',
  exec: true,
  plugins: {
    'postcss-cssnext': {},
    'cssnano': {},
    'stylelint': {},
  }
}
