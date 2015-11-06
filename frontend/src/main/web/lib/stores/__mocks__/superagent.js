var RequestMock = {
  data:  {},

  createRequest: function(res) {
    return {
      _url: null,
      _res: res,
      _set: {},

      get: function(_url) {
        this._url = _url;
        return this;
      },
      set: function(name, value) {
        this._set[name] = value;
        return this;
      },
      end: function (func) {
        func.call(this, null, this._res);
      }
    };
  },

  get: function(url) {
    if(this.data[url]) {
      return this.data[url].get(url);
    } else {
      console.warn('No request created for ', url, 'Returning empty request');
      return this.createRequest(null);
    }
  },

  __setResponse: function(url, res) {
    var request = this.createRequest(res);
    this.data[url] = request;
  }
};

module.exports = RequestMock;
