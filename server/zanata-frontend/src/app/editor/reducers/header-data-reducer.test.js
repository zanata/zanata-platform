/* global jest describe it expect */

import {
  DOCUMENT_SELECTED,
  HEADER_DATA_FETCHED,
  LOCALE_SELECTED,
  STATS_FETCHED
} from '../actions/header-action-types'
import headerDataReducer from './header-data-reducer'

const EXAMPLE_HEADER_DATA = {
  documents: [
    { name: 'file01.txt' },
    { name: 'file02.txt' },
    { name: 'file03.txt' }
  ],
  locales: [
    {
      localeId: 'en-US',
      displayName: 'English (United States)'
    },
    {
      localeId: 'de',
      displayName: 'German',
      pluralForms: 'nplurals=2; plural=(n != 1)'
    },
    {
      localeId: 'ja',
      displayName: 'Japanese',
      pluralForms: 'nplurals=1; plural=0'
    }
  ],
  versionSlug: 'myversion',
  projectInfo: {
    id: 'myproject',
    name: 'My Project'
  },
  myInfo: {
    name: 'rick',
    gravatarHash: '12345'
  }
}

describe('header-data-reducer test', () => {
  it('generates initial state', () => {
    const initialState = headerDataReducer(undefined, {})
    expect(initialState).toEqual({
      user: {
        name: '',
        gravatarUrl: '',
        dashboardUrl: ''
      },
      context: {
        projectVersion: {
          project: {
            slug: '',
            name: ''
          },
          version: '',
          url: '',
          docs: [],
          locales: {}
        },
        selectedDoc: {
          counts: {
            total: 0,
            approved: 0,
            rejected: 0,
            translated: 0,
            needswork: 0,
            untranslated: 0
          },
          id: ''
        },
        selectedLocale: ''
      }
    })
  })

  it('can select document', () => {
    const withData = headerDataReducer(undefined, {
      type: HEADER_DATA_FETCHED,
      payload: EXAMPLE_HEADER_DATA
    })
    const selected = headerDataReducer(withData, {
      type: DOCUMENT_SELECTED,
      payload: 'something.doc'
    })
    expect(selected.context.selectedDoc.id).toEqual('something.doc')
  })

  // FIXME these data are artificially combined together. Make them separate
  //       actions for easier testing and maintenance
  it('can incorporate fetched header data', () => {
    const withData = headerDataReducer(undefined, {
      type: HEADER_DATA_FETCHED,
      payload: EXAMPLE_HEADER_DATA
    })
    expect(withData).toEqual({
      user: {
        name: 'rick',
        gravatarUrl: '//www.gravatar.com/avatar/12345?d=mm&ampr=g&amps=72',
        dashboardUrl: '/dashboard'
      },
      context: {
        projectVersion: {
          project: {
            slug: 'myproject',
            name: 'My Project'
          },
          version: 'myversion',
          url: '/iteration/view/myproject/myversion',
          docs: [ 'file01.txt', 'file02.txt', 'file03.txt' ],
          locales: {
            'en-US': {
              id: 'en-US',
              name: 'English (United States)'
            },
            de: {
              id: 'de',
              name: 'German',
              nplurals: 2
            },
            ja: {
              id: 'ja',
              name: 'Japanese',
              nplurals: 1
            }
          }
        },
        selectedDoc: {
          counts: {
            total: 0,
            approved: 0,
            rejected: 0,
            translated: 0,
            needswork: 0,
            untranslated: 0
          },
          id: ''
        },
        selectedLocale: ''
      }
    })
  })

  it('can select locale', () => {
    const withData = headerDataReducer(undefined, {
      type: HEADER_DATA_FETCHED,
      payload: EXAMPLE_HEADER_DATA
    })
    const selected = headerDataReducer(withData, {
      type: LOCALE_SELECTED,
      payload: 'de'
    })
    expect(selected.context.selectedLocale).toEqual('de')
  })

  it('can receive stats', () => {
    const withStats = headerDataReducer(undefined, {
      type: STATS_FETCHED,
      // stats pre-preparation
      payload: [
        {
          approved: 0,
          fuzzy: 44,
          lastTranslated: null,
          locale: 'de',
          needReview: 44,
          rejected: 0,
          total: 4592,
          translated: 80,
          translatedOnly: 80,
          unit: 'WORD',
          untranslated: 4468
        },
        {
          approved: 0,
          fuzzy: 7,
          lastTranslated: null,
          locale: 'de',
          needReview: 7,
          rejected: 0,
          total: 495,
          translated: 8,
          translatedOnly: 8,
          unit: 'MESSAGE',
          untranslated: 480
        }
      ]
    })
    expect(withStats.context.selectedDoc.counts).toEqual({
      approved: 0,
      needswork: 7,
      rejected: 0,
      total: 495,
      translated: 8,
      untranslated: 480
    })
  })
})
