import _ from 'lodash';
import StringUtils from './StringUtils'
import DateHelpers from './DateHelper'

var GlossaryHelper = {
  /**
   * Generate org.zanata.rest.dto.GlossaryTerm object
   * returns null if data is undefined or content and locale is empty
   *
   * @param data
   */
  generateGlossaryTermDTO: function (data, trimContent) {
    if(_.isUndefined(data)) {
      return;
    }
    var content = trimContent ? StringUtils.trim(data.content) : data.content,
      locale = data.locale,
      comment = StringUtils.trim(data.comment);

    if(StringUtils.isEmptyOrNull(locale)) {
      return;
    } else {
      return  {
        content: content,
        locale: locale,
        comment: comment
      };
    }
  },

  /**
   * Generate org.zanata.rest.dto.GlossaryEntry object
   * @param data
   */
  generateGlossaryEntryDTO: function (data) {
    var entry = {};

    entry.id = data.id;
    entry.pos = StringUtils.trim(data.pos);
    entry.description = StringUtils.trim(data.description);
    entry.srcLang = data.srcTerm.locale;
    entry.sourceReference = data.srcTerm.reference;
    entry.glossaryTerms = [];

    var srcTerm = this.generateGlossaryTermDTO(data.srcTerm, false);
    if(!_.isUndefined(srcTerm)) {
      entry.glossaryTerms.push(srcTerm);
    }

    var transTerm = this.generateGlossaryTermDTO(data.transTerm, true);
    if(!_.isUndefined(transTerm)) {
      entry.glossaryTerms.push(transTerm);
    }
    return entry;
  },

  generateEmptyTerm: function(transLocaleId){
    return {
      content: '',
      locale: transLocaleId,
      comment: '',
      lastModifiedDate: '',
      lastModifiedBy: ''
    }
  },

  generateSrcTerm: function (localeId) {
    var term = this.generateEmptyTerm(localeId);
    term['reference'] = '';
    return term;
  },

  getTermByLocale: function (terms, localeId) {
    var term = _.filter(terms, 'locale', localeId);
    return term.length ? term[0] : null;
  },

  generateEntry: function (entry, transLocaleId) {
    var srcTerm =
      this.getTermByLocale(entry.glossaryTerms, entry.srcLang);

    srcTerm.reference = entry.sourceReference;
    if(!StringUtils.isEmptyOrNull(srcTerm.lastModifiedDate)) {
      srcTerm.lastModifiedDate = DateHelpers.shortDate(DateHelpers.getDate(srcTerm.lastModifiedDate));
    }
    var transTerm =
      this.getTermByLocale(entry.glossaryTerms, transLocaleId);

    if(transTerm) {
      transTerm.lastModifiedDate = DateHelpers.shortDate(DateHelpers.getDate(transTerm.lastModifiedDate));
      if(_.isUndefined(transTerm.comment)) {
        transTerm.comment = ''
      }
    } else {
      transTerm = this.generateEmptyTerm(transLocaleId);
    }

    return {
      id: entry.id,
      pos: _.isUndefined(entry.pos) ? '' : entry.pos,
      description: _.isUndefined(entry.description) ? '' : entry.description,
      termsCount: entry.termsCount > 0 ? entry.termsCount - 1 : 0 , //remove source term from count
      srcTerm: srcTerm,
      transTerm: transTerm,
      status: this.getDefaultEntryStatus()
    };
  },

  getEntryStatus: function (entry, originalEntry) {
    var isSrcModified = (entry.description !== originalEntry.description) ||
      (entry.pos !== originalEntry.pos) || (entry.srcTerm.content !== originalEntry.srcTerm.content);
    var isTransModified = entry.transTerm.content !== originalEntry.transTerm.content;

    var isSrcValid = this.isSourceValid(entry);
    var canUpdateTransComment = this.canUpdateTransComment(originalEntry);
    return {
      isSrcModified: isSrcModified,
      isTransModified: isTransModified,
      isSrcValid: isSrcValid, //source content is mandatory
      canUpdateTransComment: canUpdateTransComment,
      isSaving: entry.status.isSaving
    };
  },

  getDefaultEntryStatus: function () {
    return {
      isSrcModified: false,
      isTransModified: false,
      isSrcValid: true,
      canUpdateTransComment: true,
      isSaving: false
    }
  },

  isSourceValid: function (entry) {
    return !StringUtils.isEmptyOrNull(StringUtils.trim(entry.srcTerm.content));
  },

  canUpdateTransComment: function (entry) {
    return !StringUtils.isEmptyOrNull(entry.transTerm.content);
  }
};
export default GlossaryHelper;
