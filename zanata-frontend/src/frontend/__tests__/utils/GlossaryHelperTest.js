jest.disableAutomock()

import GlossaryHelper from '../../app/utils/GlossaryHelper'

describe('GlossaryHelperTest', function () {
  // var GlossaryHelper
  // var _ = require('lodash')

  // beforeEach(function () {
  //   GlossaryHelper = require('../GlossaryHelper')
  // })

  // FIXME module has diverged (method has been renamed or removed)
  // it('can update translation comment', function () {
  //   var entry = {transTerm: {content: null}}
  //   expect(GlossaryHelper.canUpdateTransComment(entry)).toEqual(false)
  //
  //   entry = {transTerm: {content: undefined}}
  //   expect(GlossaryHelper.canUpdateTransComment(entry)).toEqual(false)
  //
  //   entry = {transTerm: {content: 'some content'}}
  //   expect(GlossaryHelper.canUpdateTransComment(entry)).toEqual(true)
  // })

  // FIXME module has diverged (method has been renamed or removed)
  // it('test is source term valid', function () {
  //   var entry = {srcTerm: {content: null}}
  //   expect(GlossaryHelper.isSourceValid(entry)).toEqual(false)
  //
  //   entry = {srcTerm: {content: undefined}}
  //   expect(GlossaryHelper.isSourceValid(entry)).toEqual(false)
  //
  //   entry = {srcTerm: {content: 'some content'}}
  //   expect(GlossaryHelper.isSourceValid(entry)).toEqual(true)
  // })

  it('can get term by locale', function () {
    var term1 = {
      'content': 'process',
      'locale': 'en-US',
      'lastModifiedDate': 1439435990000,
      'lastModifiedBy': ''
    }
    var term2 = {
      'content': 'process',
      'locale': 'de',
      'lastModifiedDate': 1439435990000,
      'lastModifiedBy': ''
    }
    var term3 = {
      'content': 'process',
      'locale': 'fr',
      'lastModifiedDate': 1439435990000,
      'lastModifiedBy': ''
    }

    var terms = [term1, term2, term3]
    var localeId = 'en-US'
    expect(GlossaryHelper.getTermByLocale(terms, localeId)).toEqual(term1)

    localeId = 'de'
    expect(GlossaryHelper.getTermByLocale(terms, localeId)).toEqual(term2)

    localeId = 'fr'
    expect(GlossaryHelper.getTermByLocale(terms, localeId)).toEqual(term3)

    localeId = 'non-exist'
    expect(GlossaryHelper.getTermByLocale(terms, localeId)).toEqual(undefined)
  })

  // FIXME module has diverged (method has been renamed or removed)
  // it('can generate term dto', function () {
  //   var data = {content: 'content', locale: 'de', comment: 'comment'}
  //   var term = GlossaryHelper.generateGlossaryTermDTO(data)
  //   expect(term.content).toEqual(data.content)
  //   expect(term.locale).toEqual(data.locale)
  //   expect(term.comments).toEqual(data.comment)
  //
  //   data = {content: '', locale: '', comment: 'comment'}
  //   expect(GlossaryHelper.generateGlossaryTermDTO(data)).toBeNull()
  // })

  // FIXME module has diverged (method has been renamed or removed)
  // it('can generate glossary dto', function () {
  //   var srcTerm = {
  //     content: 'src_content',
  //     locale: 'en-US',
  //     comment: 'comment',
  //     reference: 'ref'
  //   }
  //   var transTerm = {
  //     content: 'trans_content',
  //     locale: 'en-US',
  //     comment: 'comment'
  //   }
  //   var data = {
  //     id: 'resId',
  //     pos: 'noun',
  //     description: 'description',
  //     srcTerm: srcTerm,
  //     transTerm: transTerm
  //   }
  //
  //   var glossary = GlossaryHelper.generateGlossaryDTO(data)
  //   expect(_.size(glossary.glossaryEntries)).toEqual(1)
  //
  //   var entry = glossary.glossaryEntries[0]
  //   expect(entry.id).toEqual(data.id)
  //   expect(entry.pos).toEqual(data.pos)
  //   expect(entry.description).toEqual(data.description)
  //   expect(_.size(entry.glossaryTerms)).toEqual(2)
  // })

  it('can generate empty term', function () {
    var localeId = 'de'
    var term = GlossaryHelper.generateEmptyTerm(localeId)
    expect(term.locale).toEqual(localeId)
  })

  it('can generate empty src term', function () {
    var localeId = 'de'
    var term = GlossaryHelper.generateEmptySrcTerm(localeId)
    expect(term.locale).toEqual(localeId)
  })

  // FIXME module has diverged (some expected values are not defined)
  // it('can get entry status', function () {
  //   var entry1 = generateEntry('en-US', 'de')
  //   var entry2 = generateEntry('en-US', 'de')
  //
  //   var status = GlossaryHelper.getEntryStatus(entry1, entry2)
  //   expectStatus(status, false, false, false, false, false)
  //
  //   // modify source
  //   entry1.description = 'updated'
  //   status = GlossaryHelper.getEntryStatus(entry1, entry2)
  //   expectStatus(status, true, false, false, false, false)
  //
  //   // modify source, source is valid
  //   entry1.srcTerm.content = 'updated'
  //   status = GlossaryHelper.getEntryStatus(entry1, entry2)
  //   expectStatus(status, true, false, true, false, false)
  //
  //   // modify source, source is valid, trans modified
  //   entry1.transTerm.content = 'updated'
  //   status = GlossaryHelper.getEntryStatus(entry1, entry2)
  //   expectStatus(status, true, true, true, false, false)
  //
  //   // modify source, source is valid, trans modified, can update comment
  //   entry2.transTerm.content = 'original content'
  //   status = GlossaryHelper.getEntryStatus(entry1, entry2)
  //   expectStatus(status, true, true, true, true, false)
  // })

  function expectStatus (status, isSrcModified, isTransModified, isSrcValid,
    canUpdateTransComment, isSaving) {
    expect(status.isSrcModified).toEqual(isSrcModified)
    expect(status.isTransModified).toEqual(isTransModified)
    expect(status.isSrcValid).toEqual(isSrcValid)
    expect(status.canUpdateTransComment).toEqual(canUpdateTransComment)
    expect(status.isSaving).toEqual(isSaving)
  }

  function generateEntry (srcLocale, transLocale) {
    return {
      id: '', pos: '', description: '',
      srcTerm: GlossaryHelper.generateEmptySrcTerm(srcLocale),
      transTerm: GlossaryHelper.generateEmptyTerm(transLocale),
      status: GlossaryHelper.getDefaultEntryStatus()
    }
  }
})
