var ReactTools = require('react-tools');
var Babel = require('babel-core');
module.exports = {
  process: function(src) {
    return Babel.transform(src).code;
  }
};
