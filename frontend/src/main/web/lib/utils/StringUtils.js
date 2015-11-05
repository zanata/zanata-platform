import _ from 'lodash';

var StringUtils = {
  isEmptyOrNull: function (str) {
    return _.isEmpty(str);
  },

  trimLeadingSpace: function (str) {
    if(this.isEmptyOrNull(str)) {
      return str;
    }
    return str.replace(/^\s+/g, '');
  },

  trim: function (str) {
    return this.isEmptyOrNull(str) ? str : str.trim();
  }
};
export default StringUtils;
