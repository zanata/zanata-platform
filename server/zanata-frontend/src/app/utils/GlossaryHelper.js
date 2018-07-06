import { isUndefined, filter, forOwn, isEmpty, toString } from 'lodash'
import { trim } from './StringUtils'
import DateHelpers from './DateHelper'
import defined from 'defined'

var GlossaryHelper = {
  /**
   * Generate org.zanata.rest.dto.GlossaryTerm object
   * returns undefined if data is undefined or locale is empty
   *
   * @param data
   */
  // @ts-ignore any
  generateTermDTO: function (data, trimContent) {
    if (isUndefined(data) || isEmpty(data.locale)) {
      return
    }
    const comment = isEmpty(data.content) ? undefined : trim(data.comment)
    return {
      content: trimContent ? trim(data.content) : data.content,
      locale: data.locale,
      comment: comment
    }
  },

  /**
   * Generate org.zanata.rest.dto.GlossaryEntry object
   * @param data
   */
  // @ts-ignore any
  convertToDTO: function (data, qualifiedName) {
    var entry = {}

    entry.id = data.id
    entry.pos = trim(data.pos)
    entry.description = trim(data.description)
    entry.srcLang = data.srcTerm.locale
    entry.sourceReference = data.srcTerm.reference
    /** @type {any[]} */
    entry.glossaryTerms = []
    entry.qualifiedName = {name: qualifiedName}

    var srcTerm = this.generateTermDTO(data.srcTerm, false)
    if (!isUndefined(srcTerm)) {
      entry.glossaryTerms.push(srcTerm)
    }

    var transTerm = this.generateTermDTO(data.transTerm, true)
    if (!isUndefined(transTerm)) {
      entry.glossaryTerms.push(transTerm)
    }
    return [entry]
  },

  // @ts-ignore any
  generateEmptyTerm: function (localeId) {
    return {
      content: '',
      locale: localeId,
      comment: '',
      lastModifiedDate: '',
      lastModifiedBy: ''
    }
  },

  // @ts-ignore any
  generateEmptySrcTerm: function (localeId) {
    let term = this.generateEmptyTerm(localeId)
    term.reference = ''
    return term
  },

  // @ts-ignore any
  getTermByLocale: function (terms, localeId) {
    let term = filter(terms, ['locale', localeId])
    return term.length ? term[0] : undefined
  },

  // @ts-ignore any
  generateEmptyEntry: function (srcLocaleId) {
    return {
      description: undefined,
      pos: undefined,
      srcTerm: this.generateEmptyTerm(srcLocaleId)
    }
  },

  // @ts-ignore any
  generateEntry: function (entry, transLocaleId) {
    let srcTerm =
      this.getTermByLocale(entry.glossaryTerms, entry.srcLang)
    srcTerm.reference = entry.sourceReference
    const srcDate = toString(srcTerm.lastModifiedDate)
    if (!isEmpty(srcDate)) {
      srcTerm.lastModifiedDate =
        // @ts-ignore
        DateHelpers.shortDateTime(DateHelpers.getDate(srcDate))
    }

    let transTerm
    if (transLocaleId) {
      transTerm = this.getTermByLocale(entry.glossaryTerms, transLocaleId)
      if (transTerm) {
        const transDate = toString(transTerm.lastModifiedDate)
        if (!isEmpty(transDate)) {
          transTerm.lastModifiedDate =
            // @ts-ignore
            DateHelpers.shortDateTime(DateHelpers.getDate(transDate))
        }
        if (isEmpty(transTerm.comment)) {
          transTerm.comment = ''
        }
      } else {
        transTerm = this.generateEmptyTerm(transLocaleId)
      }
    }

    return {
      id: entry.id,
      pos: defined(entry.pos, ''),
      description: defined(entry.description, ''),
      termsCount: entry.termsCount > 0 ? entry.termsCount - 1 : 0,
      srcTerm: srcTerm,
      transTerm: transTerm,
      status: this.getDefaultEntryStatus()
    }
  },

  // @ts-ignore any
  toEmptyString: (val) => {
    return isEmpty(val) ? '' : val
  },

  // @ts-ignore any
  getEntryStatus: function (entry, originalEntry) {
    if (entry && originalEntry) {
      const source = this.toEmptyString(entry.srcTerm.content)
      const trans = entry.transTerm
        ? this.toEmptyString(entry.transTerm.content) : ''
      const comment = entry.transTerm
        ? this.toEmptyString(entry.transTerm.comment) : ''
      const desc = this.toEmptyString(entry.description)
      const pos = this.toEmptyString(entry.pos)

      const oriSource = this.toEmptyString(
        originalEntry.srcTerm.content)
      const oriTrans = originalEntry.transTerm
        ? this.toEmptyString(originalEntry.transTerm.content) : ''
      const oriComment = originalEntry.transTerm
        ? this.toEmptyString(originalEntry.transTerm.comment) : ''
      const oriDesc = this.toEmptyString(originalEntry.description)
      const oriPos = this.toEmptyString(originalEntry.pos)

      let isSrcModified = (desc !== oriDesc) ||
        (pos !== oriPos) ||
        (source !== oriSource)
      let isTransModified = (trans !== oriTrans) || (comment !== oriComment)

      let isSrcValid = !isEmpty(trim(source))

      return {
        isSrcModified: isSrcModified,
        isTransModified: isTransModified,
        isSrcValid: isSrcValid // source content is mandatory
      }
    }
    return this.getDefaultEntryStatus()
  },

  getDefaultEntryStatus: function () {
    return {
      isSrcModified: false,
      isTransModified: false,
      isSrcValid: true
    }
  },

  // @ts-ignore any
  convertSortToObject: function (sortString) {
    if (!sortString) {
      return {
        src_content: true
      }
    } else {
      let sort = {}
      if (sortString.startsWith('-')) {
        sort[sortString.substring(1, sortString.length)] = false
      } else {
        sort[sortString] = true
      }
      return sort
    }
  },

  // @ts-ignore any
  convertSortToParam: function (sort) {
    // @ts-ignore any
    let params = []
    forOwn(sort, function (value, field) {
      let param = (value ? '' : '-') + field
      // @ts-ignore any
      params.push(param)
    })
    // @ts-ignore any
    return params.length ? params.join() : ''
  }
}

export default GlossaryHelper
