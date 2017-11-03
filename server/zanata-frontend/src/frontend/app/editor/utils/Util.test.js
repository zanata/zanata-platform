jest.disableAutomock()

import {
  parseNPlurals,
  prepareDocs,
  prepareLocales,
  prepareStats
} from './Util'


describe('parseNPluralsTest', () => {
  it('can parse valid Plural-Forms string', () => {
    // Valid plural forms from
    // https://www.gnu.org/software/gettext/manual/html_node/Plural-forms.html
    expect(parseNPlurals('nplurals=2; plural=n == 1 ? 0 : 1;')).toEqual(2)
    expect(parseNPlurals('nplurals=1; plural=0;')).toEqual(1)
    expect(parseNPlurals('nplurals=2; plural=n != 1;')).toEqual(2)
    expect(parseNPlurals('nplurals=2; plural=n>1;')).toEqual(2)
    expect(parseNPlurals(
      'nplurals=3; plural=n%10==1 && n%100!=11 ? 0 : n != 0 ? 1 : 2;'))
      .toEqual(3)
    expect(parseNPlurals(
      'nplurals=6; plural=n==0 ? 0 : n==1 ? 1 : n==2 ? 2 ' +
      ': n%100>=3 && n%100<=10 ? 3 : n%100>=11 ? 4 : 5;')).toEqual(6)
  })

  it('returns undefined when plural string is null or empty', () => {
    expect(parseNPlurals(undefined)).toBeUndefined()
    expect(parseNPlurals(null)).toBeUndefined()
    expect(parseNPlurals('')).toBeUndefined()
  })

  it('returns undefined for strings without valid nplurals', () => {
    expect(parseNPlurals('nplurals=x; plural=y;')).toBeUndefined()
    expect(parseNPlurals('not even a plural forms string')).toBeUndefined()
    expect(parseNPlurals('mplurals=7; the mumber of plurals')).toBeUndefined()
  })
})

describe('prepareLocalesTest', () => {
  it('Can transform locales to the expected form', () => {
    // Values taken from API response.
    const unpreparedLocales =
      [
        {
          'localeId': 'de',
          'displayName': 'German',
          'nativeName': 'Deutsch',
          'enabled': true,
          'enabledByDefault': true,
          'pluralForms': 'nplurals=2; plural=(n != 1)'
        }, {
          'localeId': 'ja',
          'displayName': 'Japanese',
          'nativeName': '日本語',
          'enabled': true,
          'enabledByDefault': true,
          'pluralForms': 'nplurals=1; plural=0'
        }
      ]

    const preparedLocales = {
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

    expect(prepareLocales(unpreparedLocales)).toEqual(preparedLocales)
  })
})


describe('prepareStatsTest', () => {
  it('Can can translate statistics to the expected form.', () => {
    // Values taken from API response.
    const unpreparedStats = [
      {
        'total': 4592,
        'untranslated': 4592,
        'needReview': 0,
        'translated': 0,
        'approved': 0,
        'rejected': 0,
        'fuzzy': 0,
        'unit': 'WORD',
        'locale': 'de',
        'lastTranslated': null,
        'translatedOnly': 0
      }, {
        'total': 495,
        'untranslated': 460,
        'needReview': 0,
        'translated': 15,
        'approved': 5,
        'rejected': 0,
        'fuzzy': 20,
        'unit': 'MESSAGE',
        'locale': 'de',
        'lastTranslated': null,
        'translatedOnly': 10
      }
    ]

    // just the MESSAGE stats
    const preparedStats = {
      'total': 495,
      'untranslated': 460,
      'approved': 5,
      'rejected': 0,
      'needswork': 20,
      'translated': 10
    }

    expect(prepareStats(unpreparedStats)).toEqual(preparedStats)
  })
})

describe('prepareDocsTest', () => {
  it('Can handle a null document list', () => {
    expect(prepareDocs([])).toEqual([])
  })

  it('Can transform a list of documents to the expected structure.', () => {
    // Values taken from API response.
    const unpreparedDocs = [
      {
        'name': 'template20161102.pot',
        'contentType': 'application/x-gettext',
        'lang': 'en-US',
        'type': 'FILE',
        'revision': 1
      }, {
        'name': 'flags/template20161102.pot',
        'contentType': 'application/x-gettext',
        'lang': 'en-US',
        'type': 'FILE',
        'revision': 1
      }, {
        'name': 'msgctxt/template20161102.pot',
        'contentType': 'application/x-gettext',
        'lang': 'en-US',
        'type': 'FILE',
        'revision': 1
      }
    ]
    const preparedDocs = [
      'template20161102.pot',
      'flags/template20161102.pot',
      'msgctxt/template20161102.pot'
    ]
    expect(prepareDocs(unpreparedDocs)).toEqual(preparedDocs)
  })
})
