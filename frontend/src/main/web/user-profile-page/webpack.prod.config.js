var webpack = require('webpack');
var path = require('path');

// bundle destination (default is current directory)
var bundleDest = process.env.npm_config_env_bundleDest || __dirname;

module.exports = {
  context: __dirname,
  entry: [
    './index.js'
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
    new webpack.optimize.UglifyJsPlugin({
        compress: {
            warnings: false
        }
    }),
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
