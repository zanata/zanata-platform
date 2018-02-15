/* global jest describe expect it */

import {
  STATUS_NEW,
  STATUS_UNTRANSLATED,
  STATUS_NEEDS_WORK,
  STATUS_NEEDS_WORK_SERVER,
  STATUS_TRANSLATED,
  STATUS_APPROVED,
  STATUS_REJECTED,

  defaultSaveStatus,
  nonDefaultValidSaveStatuses,
  transUnitStatusToPhraseStatus
} from './status-util'

describe('status-util', () => {
  it('defaultSaveStatus with no translations', () => {
    expect(defaultSaveStatus({
      translations: ['', ''],
      newTranslations: ['', '']
    })).toEqual(STATUS_UNTRANSLATED)
  })

  it('defaultSaveStatus with some empty', () => {
    expect(defaultSaveStatus({
      translations: ['', ''],
      newTranslations: ['foo', '']
    })).toEqual(STATUS_NEEDS_WORK)
  })

  it('defaultSaveStatus with changed translation', () => {
    expect(defaultSaveStatus({
      translations: ['foo', 'bar'],
      newTranslations: ['food', 'bart']
    })).toEqual(STATUS_TRANSLATED)
  })

  it('defaultSaveStatus with no changes uses same status', () => {
    expect(defaultSaveStatus({
      status: STATUS_APPROVED,
      translations: ['foo', 'bar'],
      newTranslations: ['foo', 'bar']
    })).toEqual(STATUS_APPROVED)
  })

  it('nonDefaultValidSaveStatuses with no translations', () => {
    expect(nonDefaultValidSaveStatuses({
      translations: ['', ''],
      newTranslations: ['', '']
    }, {
      permissions: { reviewer: true, translator: true }
    })).toEqual([])
  })

  it('nonDefaultValidSaveStatuses with some empty', () => {
    expect(nonDefaultValidSaveStatuses({
      translations: ['', ''],
      newTranslations: ['foo', '']
    })).toEqual([])
  })

  it('nonDefaultValidSaveStatuses with changed translation', () => {
    expect(nonDefaultValidSaveStatuses({
      translations: ['foo', 'bar'],
      newTranslations: ['food', 'bart']
    }, {
      permissions: { reviewer: true, translator: true }
    })).toEqual([STATUS_NEEDS_WORK])
  })

  it('nonDefaultValidSaveStatuses rejected with no changes', () => {
    expect(nonDefaultValidSaveStatuses({
      status: STATUS_REJECTED,
      translations: ['foo', 'bar'],
      newTranslations: ['foo', 'bar']
    }, {
      permissions: { reviewer: true, translator: true }
    })).toEqual([STATUS_TRANSLATED, STATUS_NEEDS_WORK])
  })

  it('nonDefaultValidSaveStatuses with no changes', () => {
    expect(nonDefaultValidSaveStatuses({
      status: STATUS_APPROVED,
      translations: ['foo', 'bar'],
      newTranslations: ['foo', 'bar']
    }, {
      permissions: { reviewer: true, translator: true }
    })).toEqual([STATUS_TRANSLATED, STATUS_NEEDS_WORK])
  })

  it('transUnitStatusToPhraseStatus', () => {
    expect(transUnitStatusToPhraseStatus(undefined))
      .toEqual(STATUS_UNTRANSLATED)
    expect(transUnitStatusToPhraseStatus(STATUS_NEW))
      .toEqual(STATUS_UNTRANSLATED)
    expect(transUnitStatusToPhraseStatus(STATUS_NEEDS_WORK_SERVER))
      .toEqual(STATUS_NEEDS_WORK)
    expect(transUnitStatusToPhraseStatus(STATUS_TRANSLATED.toUpperCase()))
      .toEqual(STATUS_TRANSLATED)
    expect(transUnitStatusToPhraseStatus(STATUS_APPROVED))
      .toEqual(STATUS_APPROVED)
    expect(transUnitStatusToPhraseStatus(STATUS_REJECTED))
      .toEqual(STATUS_REJECTED)
  })
})
