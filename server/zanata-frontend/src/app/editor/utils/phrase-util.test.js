/* global jest describe expect it */
jest.disableAutomock()

import {
  getSaveButtonStatus
} from './phrase-util'
import {
    STATUS_UNTRANSLATED,
    STATUS_NEEDS_WORK,
    STATUS_TRANSLATED
} from './status-util'

describe('getSaveButtonStatusTest', () => {
  it('Returns UNTRANSLATED when nothing is translated', () => {
    const phrase1 = {
      newTranslations: []
    }

    const phrase2 = {
      newTranslations: [
        '',
        ''
      ]
    }

    expect(getSaveButtonStatus(phrase1)).toEqual(STATUS_UNTRANSLATED)
    expect(getSaveButtonStatus(phrase2)).toEqual(STATUS_UNTRANSLATED)
  })

  it('Returns NEEDS_WORK when some but not all translations are empty', () => {
    const phrase1 = {
      newTranslations: [
        '',
        'Hello'
      ]
    }
    const phrase2 = {
      newTranslations: [
        'Hi',
        '',
        'Hello'
      ]
    }

    expect(getSaveButtonStatus(phrase1)).toEqual(STATUS_NEEDS_WORK)
    expect(getSaveButtonStatus(phrase2)).toEqual(STATUS_NEEDS_WORK)
  })

  it('Returns TRANSLATED when all translated and something changed', () => {
    const phrase1 = {
      translations: [
        'Hi',
        ''
      ],
      newTranslations: [
        'Hi',
        'Hello'
      ]
    }
    const phrase2 = {
      translations: [
        'Hi',
        'Hello'
      ],
      newTranslations: [
        'Hi',
        'Haldo'
      ]
    }

    expect(getSaveButtonStatus(phrase1)).toEqual(STATUS_TRANSLATED)
    expect(getSaveButtonStatus(phrase2)).toEqual(STATUS_TRANSLATED)
  })

  it('Returns the current status when nothing has changed', () => {
    const phrase1 = {
      status: STATUS_NEEDS_WORK,
      translations: [
        'Hi',
        'Hello'
      ],
      newTranslations: [
        'Hi',
        'Hello'
      ]
    }
    const phrase2 = {
      status: STATUS_TRANSLATED,
      translations: [
        'Hi',
        'Hello'
      ],
      newTranslations: [
        'Hi',
        'Hello'
      ]
    }

    expect(getSaveButtonStatus(phrase1)).toEqual(STATUS_NEEDS_WORK)
    expect(getSaveButtonStatus(phrase2)).toEqual(STATUS_TRANSLATED)
  })
})
