var webpack = require('webpack');
var path = require('path');

// bundle destination (default is current directory)
var bundleDest = process.env.npm_config_env_bundleDest || __dirname;

module.exports = {
  context: __dirname,
  entry: [
    './index'
  ],
  output: {
    path: bundleDest,
    filename: 'frontend.bundle.min.js',
    pathinfo: true
  },
  module: {
    loaders: [
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        loaders: ['babel-loader']
      },
      {
        test: /\.css$/,
        loaders: ['style', 'css', 'cssnext']
      }
    ]
  },
  cssnext: {
    compress: true,
    features: {
      rem: false,
      pseudoElements: false,
      colorRgba: false
    }
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
    extensions: ['', '.js', '.jsx', '.json', '.css']
  },
  node: {
    __dirname: true
  }
};
