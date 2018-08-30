import { Locale, ProcessStatus, ProjectSearchResult, LocaleId } from '../utils/prop-types-util'
import { DeepReadonly } from '../utils/DeepReadonly'
import { RouterState} from 'react-router-redux'
import { tuple } from '../utils/tuple'
import { TMX_ALL, TMX_PROJECT, TMX_VERSION } from '../actions/tmx-actions'

/* tslint:disable interface-over-type-literal */

// TODO eliminate all anys

export type RootState = DeepReadonly<{
  routing: RouterState
  explore: ExploreState
  glossary: GlossaryState
  common: CommonState
  profile: ProfileState
  languages: LanguagesState
  projectVersion: ProjectVersionState
  tmx: TmxState
  admin: AdminState
}>

// TODO verify types
export type ProjectVersionState = {
  MTMerge: {
    showMTMerge: boolean
    triggered: boolean
    processStatus?: ProcessStatus
    queryStatus?: string
  }
  TMMerge: {
    show: boolean
    triggered: boolean
    processStatus?: ProcessStatus
    queryStatus?: string
    projectsWithVersions: ProjectSearchResult[]
  }
  projectResultsTimestamp: Date
  locales: Locale[]
  fetchingProject: boolean
  fetchingLocale: boolean
  notification?: any
}

// see admin-reducer.js. Something is wrong with key and settings.
// TODO remove key?
export type AdminState = DeepReadonly<{
  notification?: any
  review: {
    // key?: any
    criteria: any[]
  },
  serverSettings: {
    // key?: any
    loading: boolean,
    saving: boolean,
    settings: any
  }
}>

export type CommonState = DeepReadonly<{
  locales: any[]
  loading: boolean
  selectedLocale: LocaleId
}>

// TODO use [Deep]Readonly. explore-reducer mutates local copies of ExploreState
export type ExploreState = {
  error: boolean
  loading: {
    Project: boolean
    LanguageTeam: boolean
    Person: boolean
    Group: boolean
  }
  results: any
}

export const GlossaryFileTypes = tuple('csv', 'po')
export type GlossaryFileType = typeof GlossaryFileTypes[number]

// Note that the reducer does not type-check with this shape
// TODO DeepReadonly
export type GlossaryState = DeepReadonly<{
  src: LocaleId
  locale: string
  filter: string
  sort: {
    src_content: boolean
  }
  selectedTerm: any
  permission: {
    canAddNewEntry: boolean
    canUpdateEntry: boolean
    canDeleteEntry: boolean
    canDownload: boolean
  }
  terms: any
  termIds: any[]
  termCount: number
  termsError: boolean
  termsLoading: boolean
  termsDidInvalidate: boolean
  stats: {
    srcLocale: any
    transLocales: any[]
  }
  saving: any
  deleting: any
  importFile: {
    show: boolean
    status: number
    file: null
    transLocale: null
  }
  exportFile: {
    show: boolean
    type: {value: GlossaryFileType, label: GlossaryFileType},
    status: number
    types: [
      {value: GlossaryFileType, label: GlossaryFileType},
      {value: GlossaryFileType, label: GlossaryFileType}
    ]
  }
  newEntry: {
    show: boolean
    isSaving: boolean
    entry: any // GlossaryHelper.generateEmptyEntry(DEFAULT_LOCALE.localeId)
  }
  deleteAll: {
    show: boolean
    isDeleting: boolean
  }
  statsError: boolean
  statsLoading: boolean
  notification?: any
  result?: any
  entities?: any
  warnings?: any
  id?: any
  srcLocale?: any
  transLocale?: any
  value?: any
  field?: any
  deleteGlossary?: any
  downloadGlossary?: any
  insertGlossary?: any
  projectSlug?: any
  query?: any
  updateGlossary?: any
  glossaryEntries?: any
  response?: any
}>

// Note that the reducer does not type-check with this shape
export type ProfileState = DeepReadonly<{
  matrix: any[]
  matrixForAllDays: any[]
  wordCountsForEachDayFilteredByContentState: any[]
  wordCountsForSelectedDayFilteredByContentState: any[]
  selectedDay: null
  contentStateOption: any // ContentStates[0]
  loading: boolean
  dateRange: any // utilsDate.getDateRange('This week')
  // utilsDate.getDateRangeFromOption(utilsDate.getDateRange('This week')) // eslint-disable-line max-len
  dailyDateRange: any
  user: {
    username: string
    loading: boolean
    languageTeams?: any
  }
  localeId?: any
  languageTeams?: any
}>

// Note that the reducer does not type-check with this shape
export type LanguagesState = DeepReadonly<{
  user: any // contains languageTeams
  loading: boolean
  locales: {
    results: any[]
    totalCount: number
  }
  newLanguage: {
    saving: boolean
    show: boolean
    searchResults: any[]
  }
  permission: {
    canDeleteLocale: boolean
    canAddLocale: boolean
  }
  deleting: boolean
  notification?: any
}>

const TmxExportTypes = tuple(TMX_ALL, TMX_PROJECT, TMX_VERSION)
export type TmxExportType = typeof TmxExportTypes[number]

export type TmxState = DeepReadonly<{
  tmxExport: {
    sourceLocale: any
    targetLocale: any
    showModal: boolean
    downloading: any
    sourceLanguages: any
    type: TmxExportType
  }
}>
