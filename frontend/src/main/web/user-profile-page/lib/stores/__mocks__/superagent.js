var RequestMock = {};

var _mockResponse = {};
var _set = {};
var _get;

function get(getUrl) {
  _get = getUrl;
  return this;
}

function set(name, value) {
  _set[name] = value;
  return this;
}

function end(func) {
  func.call(this, _mockResponse);
}

RequestMock.get = get;
RequestMock.set = set;
RequestMock.end = end;

RequestMock.__setResponse = function(res) {
  _mockResponse = res;
};

RequestMock.getUrl = function() {
  return _get;
};

module.exports = RequestMock;
