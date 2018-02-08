import { CALL_API } from 'redux-api-middleware'
import {
  fetchVersionLocales,
  fetchProjectPage,
  mergeVersionFromTM,
  queryTMMergeProgress,
  cancelTMMergeRequest
} from './version-actions'

describe('version-action test', () => {
  it('can fetch version locales', () => {
    const apiAction = fetchVersionLocales('meikai', 'ver1')
    expect(apiAction[CALL_API].endpoint).toEqual(
      '/rest/project/meikai/version/ver1/locales'
    )
  })
  it('can fetch project pages', () => {
    const apiAction = fetchProjectPage('meikai')
    expect(apiAction[CALL_API].endpoint).toEqual(
        '/rest/search/projects?q=meikai&includeVersion=true'
    )
  })
  it('can build url endpoint from merge options', () => {
    const mergeOptions = {
      selectedLanguage: {
        displayName: "Japanese",
        enabled: true,
        enabledByDefault: true,
        localeId: "ja",
        nativeName: "日本語",
        pluralForms: "nplurals=1; plural=0"
      },
      matchPercentage: 100,
      differentDocId: false,
      differentContext: false,
      fromImportedTM: false,
      selectedVersions: [
        {
          projectSlug: "meikai1",
          version: {
            id: "ver1",
            status: "ACTIVE"
          }
        },
        {
          projectSlug: "meikai2",
          version: {
            id: "ver2",
            status: "ACTIVE"
          }
        }
      ]
    }
    const apiAction = mergeVersionFromTM('meikai', 'ver1', mergeOptions)
    expect(apiAction[CALL_API].endpoint).toEqual(
      '/rest/project/meikai/version/ver1/tm-merge'
    )
  })
  it('can query TM merge progress', () => {
    const url = '/rest/process/key/TMMergeForVerKey-1-ja'
    const apiAction = queryTMMergeProgress(url)
    expect(apiAction[CALL_API].endpoint).toEqual(
      '/rest/process/key/TMMergeForVerKey-1-ja'
    )
  })
  it('can cancel the TM merge process', () => {
    const url = '/rest/cancel/process/key/TMMergeForVerKey-1-ja'
    const apiAction = cancelTMMergeRequest(url)
    expect(apiAction[CALL_API].endpoint).toEqual(
      '/rest/cancel/process/key/TMMergeForVerKey-1-ja'
    )
  })
})
