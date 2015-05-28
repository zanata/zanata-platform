var webpack = require('webpack');
var path = require('path');

// default destination
var bundleDest = __dirname;
process.argv.forEach(function(arg) {
  if (/^bundleDest=.+$/.test(arg)) {
    bundleDest = arg.split('=')[1];
  }
});

module.exports = {
  context: __dirname,
  entry: [
    './index.js',
  ],
  output: {
    path: bundleDest,
    filename: path.basename(__dirname) + '.bundle.min.js',
    pathinfo: true
  },
  module: {
    loaders: [
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        loader: 'babel-loader'
      }
    ]
  },
  plugins: [
    new webpack.DefinePlugin({ "global.GENTLY": false }),
    new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/),
    new webpack.optimize.UglifyJsPlugin(),
    new webpack.optimize.DedupePlugin(),
    new webpack.DefinePlugin({
      "process.env": {
        NODE_ENV: JSON.stringify("production")
      }
    }),
    new webpack.NoErrorsPlugin()
  ],
  resolve: {
    extensions: ['', '.js', '.jsx', '.json']
  },
  node: {
    __dirname: true
  }
};
