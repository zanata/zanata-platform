import Request from 'superagent';
import _ from 'lodash';
import Configs from '../constants/Configs';
import StringUtils from '../utils/StringUtils'

var GlossaryAPIStore = ({
  loadLocalesStats: function () {
    var url = Configs.baseUrl + "/glossary/info" + Configs.urlPostfix;

    return new Promise(function (resolve, reject) {
      Request.get(url)
        .set("Cache-Control", "no-cache, no-store, must-revalidate")
        .set('Accept', 'application/json')
        .set("Pragma", "no-cache")
        .set("Expires", 0)
        .end(function (err, res) {
          err ? reject(err) : resolve(res);
        })
    });
  },

  loadGlossaryByLocale: function (srcLocale, selectedTransLocaleId, filter, sort, page, pageSize) {
    if (!_.isUndefined(srcLocale) && !_.isNull(srcLocale)) {
      var url = this.glossaryAPIUrl(srcLocale.locale.localeId,
        selectedTransLocaleId, filter, sort, page, pageSize);

      return new Promise(function (resolve, reject) {
        Request.get(url)
          .set("Cache-Control", "no-cache, no-store, must-revalidate")
          .set('Accept', 'application/json')
          .set("Pragma", "no-cache")
          .set("Expires", 0)
          .end(function (err, res) {
            err ? reject(err) : resolve(res);
          });
      });
    }
  },

  glossaryAPIUrl: function (srcLocaleId, transLocale, filter, sort, page, pageSize) {
    var url = Configs.baseUrl + "/glossary/entries" + Configs.urlPostfix + "?srcLocale=" + srcLocaleId;

    if (!StringUtils.isEmptyOrNull(transLocale)) {
      url = url + "&transLocale=" + transLocale;
    }
    url =
      url + "&page=" + page + "&sizePerPage=" + pageSize;

    if (!StringUtils.isEmptyOrNull(filter)) {
      url = url + "&filter=" + filter;
    }
    return url + this.generateSortOrderParam(sort);
  },

  generateSortOrderParam: function (sort) {
    var params = [];
    _.forOwn(sort, function (value, field) {
      var param = (value ? '' : "-") + field;
      params.push(param);
    });
    return params.length ? '&sort=' + params.join() : '';
  },

  saveOrUpdateGlossary: function(entry) {
    var url = Configs.baseUrl + "/glossary/entries" + Configs.urlPostfix;
    return new Promise(function(resolve, reject) {
      Request.post(url)
        .set('Content-Type', 'application/json')
        .set('Accept', 'application/json')
        .send(entry)
        .end(function (err, res) {
          err ? reject(err) : resolve(res);
        })
    });
  },

  /**
   * data: {
 *  resId: term resId
 * }
   */
  deleteGlossary: function(data) {
    var url = Configs.baseUrl + "/glossary/entries/" + data.id + Configs.urlPostfix;

    return new Promise(function(resolve, reject) {
      Request.del(url)
        .set('Content-Type', 'application/json')
        .set('Accept', 'application/json')
        .end(function (err, res) {
          err ? reject(err) : resolve(res);
        })
    });
  },

  uploadFile: function(data, onProgressCallback) {
    var url = Configs.baseUrl + "/glossary",
      uploadFile = data.uploadFile;

    return new Promise(function(resolve, reject) {
      Request.post(url)
        .attach('file', uploadFile.file, uploadFile.file.name)
        .field('fileName', uploadFile.file.name)
        .field('srcLocale', data.srcLocale)
        .field('transLocale', data.uploadFile.transLocale)
        .set('Accept', 'application/json')
        .end(function (err, res) {
          err ? reject(err) : resolve(res);
        })
        .on('progress', function(e) {
          onProgressCallback(e.percent);
        });
    });
  }
});

export default GlossaryAPIStore;
