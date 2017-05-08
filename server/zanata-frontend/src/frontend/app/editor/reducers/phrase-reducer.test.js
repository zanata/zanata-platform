jest.disableAutomock()

import phraseReducer from './phrase-reducer'
import uiReducer from './ui-reducer'
import {
  CLAMP_PAGE,
  UPDATE_PAGE
} from '../actions/controls-header-actions'
import { COPY_GLOSSARY_TERM } from '../actions/glossary-action-types'
import {
  CANCEL_EDIT,
  COPY_FROM_ALIGNED_SOURCE,
  COPY_FROM_SOURCE,
  FETCHING_PHRASE_DETAIL,
  FETCHING_PHRASE_LIST,
  PENDING_SAVE_INITIATED,
  PHRASE_LIST_FETCHED,
  PHRASE_DETAIL_FETCHED,
  PHRASE_TEXT_SELECTION_RANGE,
  QUEUE_SAVE,
  SAVE_FINISHED,
  SAVE_INITIATED,
  SELECT_PHRASE,
  SELECT_PHRASE_SPECIFIC_PLURAL,
  TRANSLATION_TEXT_INPUT_CHANGED,
  UNDO_EDIT
} from '../actions/phrases-action-types'
import { COPY_SUGGESTION } from '../actions/suggestions-action-types'
import { SET_SAVE_AS_MODE } from '../actions/key-shortcuts-actions'
import { MOVE_NEXT, MOVE_PREVIOUS } from '../actions/phrase-navigation-actions'

describe('phrase-reducer test', () => {
  it('generates initial state', () => {
    const initialState = phraseReducer(undefined, { type: 'any' })
    expect(initialState).toEqual({
      fetchingList: false,
      fetchingDetail: false,
      saveAsMode: false,
      inDoc: {},
      detail: {},
      selectedPhraseId: undefined,
      selectedTextRange: {
        start: 0,
        end: 0
      },
      paging: {
        countPerPage: 20,
        pageIndex: 0
      }
    })
  })

  it('can clamp page', () => {
    /* Note that we have a state structure problem here that is illustrated by
     * the need to mock getState() with some data from several places in state.
     * The solution is to rearrange the state so that the phrase filtering data
     * and current document are part of the phrases data.
     * FIXME move phrase filtering into phrase reducer
     * FIXME move selected doc state (consider overall docs/phrases structure)
     */

    // Use ui-reducer just to get the default unfiltered state rather than
    // manually build it up. This will be unnecessary when filter state is
    // nested within the phrase state.
    const { filter } = uiReducer(undefined, {}).textFlowDisplay
    const initialState = phraseReducer(undefined, {})
    // Simplified model for test, since code happens to only check id of the
    // first element. Could be a little fragile but we can make a proper list
    // of phrases if this ever stops working.
    const phraseList = new Array(50)
    phraseList[0] = { id: 'p01' }

    const withPhrases = phraseReducer(initialState, {
      type: PHRASE_LIST_FETCHED,
      docId: 'doc1',
      phraseList: phraseList,
      statusList: new Array(50)
    })
    const pageTooHigh = phraseReducer(withPhrases, {
      type: UPDATE_PAGE,
      page: 7
    })
    const clamped = phraseReducer(pageTooHigh, {
      type: CLAMP_PAGE,
      getState: () => {
        return {
          context: {
            docId: 'doc1'
          },
          phrases: {
            inDoc: {
              doc1: phraseList
            },
            paging: {
              countPerPage: 20,
              pageIndex: 7
            }
          },
          ui: {
            textFlowDisplay: {
              filter: filter
            }
          }
        }
      }
    })
    expect(clamped.paging.pageIndex).toEqual(2)
  })

  it('can cancel editing', () => {
    const withPhraseList = phraseReducer(undefined, {
      type: PHRASE_LIST_FETCHED,
      docId: 'mydoc',
      phraseList: [
        { id: 'p01' },
        { id: 'p02' }
      ],
      statusList: ['translated', 'translated']
    })
    const withPhraseDetail = phraseReducer(withPhraseList, {
      type: PHRASE_DETAIL_FETCHED,
      phrases: {
        'p01': {
          selectedPluralIndex: 2,
          sources: [ 'singular source', 'plural source' ],
          translations: [ 'translation', '', '' ],
          newTranslations:
            [ 'new translation', 'new translations', 'third plural' ]
        }
      }
    })
    const canceled = phraseReducer(withPhraseDetail, {
      type: CANCEL_EDIT
    })
    expect(canceled.selectedPhraseId).toBeUndefined()
    expect(canceled.detail['p01'].newTranslations)
      .toEqual([ 'translation', '', '' ])
  })

  it('can copy glossary term', () => {
    const withPhraseList = phraseReducer(undefined, {
      type: PHRASE_LIST_FETCHED,
      docId: 'mydoc',
      phraseList: [
        { id: 'p01' },
        { id: 'p02' }
      ],
      statusList: ['translated', 'translated']
    })
    const withPhraseDetail = phraseReducer(withPhraseList, {
      type: PHRASE_DETAIL_FETCHED,
      phrases: {
        'p01': {
          newTranslations: [ 'copy something here', 'translations' ]
        },
        'p02': {
          newTranslations: [ 'another translation' ]
        }
      }
    })
    const withTextSelection = phraseReducer(withPhraseDetail, {
      type: PHRASE_TEXT_SELECTION_RANGE,
      payload: {
        start: 5,
        end: 14
      }
    })
    const copied = phraseReducer(withTextSelection, {
      type: COPY_GLOSSARY_TERM,
      payload: {
        termTranslation: 'TERM'
      }
    })
    expect(copied.detail).toEqual({
      'p01': {
        newTranslations: [ 'copy TERM here', 'translations' ],
        shouldGainFocus: 1
      },
      'p02': {
        newTranslations: [ 'another translation' ]
      }
    })
  })

  it('can copy from aligned source', () => {
    const withPhraseList = phraseReducer(undefined, {
      type: PHRASE_LIST_FETCHED,
      docId: 'mydoc',
      phraseList: [
        { id: 'p01' },
        { id: 'p02' }
      ],
      statusList: ['translated', 'translated']
    })
    const withPhraseDetail = phraseReducer(withPhraseList, {
      type: PHRASE_DETAIL_FETCHED,
      phrases: {
        'p01': {
          selectedPluralIndex: 2,
          sources: [ 'singular source', 'plural source' ],
          newTranslations: [ 'translation', 'translations', 'third plural' ]
        }
      }
    })
    const copied = phraseReducer(withPhraseDetail, {
      type: COPY_FROM_ALIGNED_SOURCE
    })
    expect(copied.detail['p01'].newTranslations).toEqual(
      [ 'translation', 'translations', 'plural source' ]
    )
  })

  it('can copy from source', () => {
    const withPhraseList = phraseReducer(undefined, {
      type: PHRASE_LIST_FETCHED,
      docId: 'mydoc',
      phraseList: [
        { id: 'p01' },
        { id: 'p02' }
      ],
      statusList: ['translated', 'translated']
    })
    const withPhraseDetail = phraseReducer(withPhraseList, {
      type: PHRASE_DETAIL_FETCHED,
      phrases: {
        'p01': {
          selectedPluralIndex: 2,
          sources: [ 'singular source', 'plural source' ],
          newTranslations: [ 'translation', 'translations', 'third plural' ]
        }
      }
    })
    const copied = phraseReducer(withPhraseDetail, {
      type: COPY_FROM_SOURCE,
      phraseId: 'p01',
      sourceIndex: 1
    })
    expect(copied.detail).toEqual({
      'p01': {
        selectedPluralIndex: 2,
        shouldGainFocus: 1,
        sources: [ 'singular source', 'plural source' ],
        newTranslations: [ 'translation', 'translations', 'plural source' ]
      }
    })
  })

  it('can record fetching phrase detail', () => {
    const fetching = phraseReducer(undefined, {
      type: FETCHING_PHRASE_DETAIL
    })
    expect(fetching.fetchingDetail).toEqual(true)
  })

  it('can update selected page number', () => {
    const initialState = phraseReducer(undefined, { type: 'any' })
    const pageChanged = phraseReducer(initialState, {
      type: UPDATE_PAGE,
      page: 7
    })
    const samePage = phraseReducer(pageChanged, {
      type: UPDATE_PAGE,
      page: 7
    })
    expect(pageChanged.paging.pageIndex).toEqual(7)
    expect(samePage.paging.pageIndex).toEqual(7)
  })

  it('can record fetched phrases', () => {
    const initialState = phraseReducer(undefined, { type: 'any' })
    const fetching = phraseReducer(initialState, {
      type: FETCHING_PHRASE_LIST
    })
    const withPhrases = phraseReducer(fetching, {
      type: PHRASE_LIST_FETCHED,
      docId: 'mydoc',
      phraseList: [
        { id: 'p01' },
        { id: 'p02' },
        { id: 'p03' }
      ],
      statusList: ['translated', 'untranslated']
    })
    expect(fetching.fetchingList).toEqual(true)
    expect(withPhrases.fetchingList).toEqual(false)
    expect(withPhrases.inDoc['mydoc']).toEqual([
      { id: 'p01' },
      { id: 'p02' },
      { id: 'p03' }
    ])
    expect(withPhrases.selectedPhraseId).toEqual('p01')
    expect(withPhrases.docStatus).toEqual(['translated', 'untranslated'])
  })

  it('can queue and clear a save', () => {
    const withPhraseList = phraseReducer(undefined, {
      type: PHRASE_LIST_FETCHED,
      docId: 'mydoc',
      phraseList: [
        { id: 'p01' },
        { id: 'p02' }
      ],
      statusList: ['translated', 'translated']
    })
    const withPhraseDetail = phraseReducer(withPhraseList, {
      type: PHRASE_DETAIL_FETCHED,
      phrases: {
        'p01': {
          sources: [ 'translation', 'translations' ],
          translations: ['', ''],
          newTranslations: ['翻訳', '翻訳']
        }
      }
    })
    const withQueuedSave = phraseReducer(withPhraseDetail, {
      type: QUEUE_SAVE,
      phraseId: 'p01',
      saveInfo: {
        localeId: 'ja',
        status: 'translated',
        translations: ['翻訳', '翻訳']
      }
    })
    const withSaveInitiated = phraseReducer(withQueuedSave, {
      type: PENDING_SAVE_INITIATED,
      phraseId: 'p01'
    })
    expect(withQueuedSave.detail['p01'].pendingSave).toEqual({
      localeId: 'ja',
      status: 'translated',
      translations: ['翻訳', '翻訳']
    })
    expect(withSaveInitiated.detail['p01'].pendingSave).toBeUndefined()
  })

  it('can record finished save', () => {
    const withPhraseList = phraseReducer(undefined, {
      type: PHRASE_LIST_FETCHED,
      docId: 'mydoc',
      phraseList: [
        { id: 'p01' },
        { id: 'p02' }
      ],
      statusList: ['translated', 'translated']
    })
    const withPhraseDetail = phraseReducer(withPhraseList, {
      type: PHRASE_DETAIL_FETCHED,
      phrases: {
        'p01': {
          sources: [ 'translation', 'translations' ],
          translations: ['', ''],
          newTranslations: ['翻訳', '翻訳']
        }
      }
    })
    const saving = phraseReducer(withPhraseDetail, {
      type: SAVE_INITIATED,
      phraseId: 'p01',
      saveInfo: {
        localeId: 'ja',
        status: 'translated',
        translations: ['翻訳', '翻訳']
      }
    })
    const saved = phraseReducer(saving, {
      type: SAVE_FINISHED,
      phraseId: 'p01',
      status: 'translated',
      revision: 3
    })
    expect(saved.detail['p01'].inProgressSave).toBeUndefined()
    expect(saved.detail['p01'].translations).toEqual(['翻訳', '翻訳'])
    expect(saved.detail['p01'].status).toEqual('translated')
    expect(saved.detail['p01'].revision).toEqual(3)
  })

  it('can record save info', () => {
    const withPhraseList = phraseReducer(undefined, {
      type: PHRASE_LIST_FETCHED,
      docId: 'mydoc',
      phraseList: [
        { id: 'p01' },
        { id: 'p02' }
      ],
      statusList: ['translated', 'translated']
    })
    const withPhraseDetail = phraseReducer(withPhraseList, {
      type: PHRASE_DETAIL_FETCHED,
      phrases: {
        'p01': {
          sources: [ 'translation', 'translations' ],
          translations: ['', '']
        }
      }
    })
    const saving = phraseReducer(withPhraseDetail, {
      type: SAVE_INITIATED,
      phraseId: 'p01',
      saveInfo: {
        localeId: 'ja',
        status: 'translated',
        translations: ['翻訳', '翻訳']
      }
    })
    expect(saving.detail['p01'].inProgressSave).toEqual({
      localeId: 'ja',
      status: 'translated',
      translations: ['翻訳', '翻訳']
    })
  })

  it('can select phrase', () => {
    const { filter } = uiReducer(undefined, {}).textFlowDisplay
    const phraseList = [ { id: 'p01' }, { id: 'p02' } ]
    const initialState = phraseReducer(undefined, {
      type: PHRASE_LIST_FETCHED,
      docId: 'mydoc',
      phraseList: phraseList,
      statusList: ['translated', 'untranslated']
    })
    const selected = phraseReducer(initialState, {
      type: SELECT_PHRASE,
      phraseId: 'p02',
      getState: () => {
        return {
          context: {
            docId: 'doc1'
          },
          phrases: {
            inDoc: {
              doc1: phraseList
            },
            paging: {
              countPerPage: 20,
              pageIndex: 7
            }
          },
          ui: {
            textFlowDisplay: {
              filter: filter
            }
          }
        }
      }
    })
    // First phrase is selected by default when phrases are loaded.
    expect(initialState.selectedPhraseId).toEqual('p01')
    expect(selected.selectedPhraseId).toEqual('p02')
  })

  it('can select a specific plural', () => {
    const { filter } = uiReducer(undefined, {}).textFlowDisplay
    const phraseList = [ { id: 'p01' }, { id: 'p02' } ]
    const withPhraseList = phraseReducer(undefined, {
      type: PHRASE_LIST_FETCHED,
      docId: 'mydoc',
      phraseList: phraseList,
      statusList: ['translated', 'untranslated']
    })
    const withPhraseDetail = phraseReducer(withPhraseList, {
      type: PHRASE_DETAIL_FETCHED,
      phrases: {
        'p01': {
          newTranslations: [ 'translation', 'translations' ]
        },
        'p02': {
          newTranslations: [ 'another translation' ]
        }
      }
    })
    const selected = phraseReducer(withPhraseDetail, {
      type: SELECT_PHRASE_SPECIFIC_PLURAL,
      phraseId: 'p02',
      index: 1,
      getState: () => {
        return {
          context: {
            docId: 'doc1'
          },
          phrases: {
            inDoc: {
              doc1: phraseList
            },
            paging: {
              countPerPage: 20,
              pageIndex: 7
            }
          },
          ui: {
            textFlowDisplay: {
              filter: filter
            }
          }
        }
      }
    })
    expect(selected.selectedPhraseId).toEqual('p02')
    expect(selected.detail['p02'].selectedPluralIndex).toEqual(1)
  })

  it('can input translation text', () => {
    const initialState = phraseReducer(undefined, {})
    const withPhrases = phraseReducer(initialState, {
      type: PHRASE_DETAIL_FETCHED,
      phrases: {
        '123': {
          newTranslations: [ '', '' ]
        },
        '124': {
          newTranslations: [ '' ]
        }
      }
    })
    const textEntered = phraseReducer(withPhrases, {
      type: TRANSLATION_TEXT_INPUT_CHANGED,
      id: '123',
      index: 0,
      text: 'translation'
    })
    expect(textEntered.detail).toEqual({
      '123': {
        newTranslations: [ 'translation', '' ]
      },
      '124': {
        newTranslations: [ '' ]
      }
    })

    const textUpdated = phraseReducer(textEntered, {
      type: TRANSLATION_TEXT_INPUT_CHANGED,
      id: '123',
      index: 0,
      text: 'NEW IMPROVED translation'
    })
    expect(textUpdated.detail).toEqual({
      '123': {
        newTranslations: [ 'NEW IMPROVED translation', '' ]
      },
      '124': {
        newTranslations: [ '' ]
      }
    })

    const pluralEntered = phraseReducer(textUpdated, {
      type: TRANSLATION_TEXT_INPUT_CHANGED,
      id: '123',
      index: 1,
      text: 'translations'
    })
    expect(pluralEntered.detail).toEqual({
      '123': {
        newTranslations: [ 'NEW IMPROVED translation', 'translations' ]
      },
      '124': {
        newTranslations: [ '' ]
      }
    })

    const otherPhraseTextEntered = phraseReducer(pluralEntered, {
      type: TRANSLATION_TEXT_INPUT_CHANGED,
      id: '124',
      index: 0,
      text: 'another translation'
    })
    expect(otherPhraseTextEntered.detail).toEqual({
      '123': {
        newTranslations: [ 'NEW IMPROVED translation', 'translations' ]
      },
      '124': {
        newTranslations: [ 'another translation' ]
      }
    })

    const textBackspaced = phraseReducer(otherPhraseTextEntered, {
      type: TRANSLATION_TEXT_INPUT_CHANGED,
      id: '123',
      index: 0,
      text: ''
    })
    expect(textBackspaced.detail).toEqual({
      '123': {
        newTranslations: [ '', 'translations' ]
      },
      '124': {
        newTranslations: [ 'another translation' ]
      }
    })
  })

  it('can undo edit', () => {
    // fetch phrase list so it sets the selected index
    // should select phrase '123'
    const withPhrases = phraseReducer(undefined, {
      type: PHRASE_LIST_FETCHED,
      docId: 'mydoc',
      phraseList: [
        { id: '1' },
        { id: '2' }
      ],
      statusList: ['fuzzy', 'untranslated']
    })
    const withDetail = phraseReducer(withPhrases, {
      type: PHRASE_DETAIL_FETCHED,
      phrases: {
        '1': {
          translations: [ 'original', 'originals' ],
          newTranslations: [ 'original', 'originals' ]
        },
        '2': {
          translations: [ '' ],
          newTranslations: [ '' ]
        }
      }
    })
    const textEntered = phraseReducer(withDetail, {
      type: TRANSLATION_TEXT_INPUT_CHANGED,
      id: '1',
      index: 0,
      text: 'originally'
    })
    const undone = phraseReducer(textEntered, {
      type: UNDO_EDIT
    })
    expect(undone.detail).toEqual({
      '1': {
        translations: [ 'original', 'originals' ],
        newTranslations: [ 'original', 'originals' ]
      },
      '2': {
        translations: [ '' ],
        newTranslations: [ '' ]
      }
    })
  })

  it('can copy suggestions', () => {
    const withPhraseList = phraseReducer(undefined, {
      type: PHRASE_LIST_FETCHED,
      docId: 'mydoc',
      phraseList: [
        { id: 'p01' },
        { id: 'p02' }
      ],
      statusList: ['translated', 'translated']
    })
    const withPhraseDetail = phraseReducer(withPhraseList, {
      type: PHRASE_DETAIL_FETCHED,
      phrases: {
        'p01': {
          newTranslations: [ 'translation', 'SUGGESTIONS' ],
          selectedPluralIndex: 1
        },
        'p02': {
          newTranslations: [ 'another translation' ]
        }
      }
    })
    const copied = phraseReducer(withPhraseDetail, {
      type: COPY_SUGGESTION,
      suggestion: {
        targetContents: [ 'SUGGESTION', 'SUGGESTIONS' ]
      }
    })
    expect(copied.detail).toEqual({
      'p01': {
        newTranslations: [ 'translation', 'SUGGESTIONS' ],
        selectedPluralIndex: 1,
        shouldGainFocus: 1
      },
      'p02': {
        newTranslations: [ 'another translation' ]
      }
    })
  })

  it('can set save as mode on and off', () => {
    const saveAs = phraseReducer(undefined, {
      type: SET_SAVE_AS_MODE,
      active: true
    })
    const notSaveAs = phraseReducer(saveAs, {
      type: SET_SAVE_AS_MODE,
      active: false
    })
    expect(saveAs.saveAsMode).toEqual(true)
    expect(notSaveAs.saveAsMode).toEqual(false)
  })

  it('can move to next and previous phrase', () => {
    const phraseList = [ { id: 'p01' }, { id: 'p02' }, { id: 'p03' } ]
    const mockState = {
      context: { docId: 'mydoc' },
      phrases: { inDoc: { mydoc: phraseList } },
      ui: {
        textFlowDisplay: {
          filter: uiReducer(undefined, {}).textFlowDisplay.filter
        }
      }
    }
    const getState = () => mockState
    const withPhrases = phraseReducer(undefined, {
      type: PHRASE_LIST_FETCHED,
      docId: 'mydoc',
      phraseList: phraseList,
      statusList: ['translated', 'translated', 'translated']
    })
    const second = phraseReducer(withPhrases, { type: MOVE_NEXT, getState })
    const third = phraseReducer(second, { type: MOVE_NEXT, getState })
    const topBound = phraseReducer(third, { type: MOVE_NEXT, getState })
    const secondAgain =
      phraseReducer(topBound, { type: MOVE_PREVIOUS, getState })
    const first = phraseReducer(secondAgain, { type: MOVE_PREVIOUS, getState })
    const bottomBound = phraseReducer(first, { type: MOVE_PREVIOUS, getState })

    expect(withPhrases.selectedPhraseId).toEqual('p01')
    expect(second.selectedPhraseId).toEqual('p02')
    expect(third.selectedPhraseId).toEqual('p03')
    expect(topBound.selectedPhraseId).toEqual('p03')
    expect(secondAgain.selectedPhraseId).toEqual('p02')
    expect(first.selectedPhraseId).toEqual('p01')
    expect(bottomBound.selectedPhraseId).toEqual('p01')
  })
})
